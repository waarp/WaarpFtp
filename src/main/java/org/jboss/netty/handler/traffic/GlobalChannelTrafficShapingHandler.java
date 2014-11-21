/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.handler.traffic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.util.ObjectSizeEstimator;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.jboss.netty.util.internal.ConcurrentHashMap;

/**
 * This implementation of the {@link AbstractTrafficShapingHandler} is for global
 * and per channel traffic shaping, that is to say a global limitation of the bandwidth, whatever
 * the number of opened channels and a per channel limitation of the bandwidth.<br><br>
 * This version shall not be in the same pipeline than other TrafficShapingHandler.<br><br>
 *
 * The general use should be as follow:<br>
 * <ul>
 * <li>Create your unique GlobalChannelTrafficShapingHandler like:<br><br>
 * <tt>GlobalChannelTrafficShapingHandler myHandler = new GlobalChannelTrafficShapingHandler(executor);</tt><br><br>
 * The executor could be the underlying IO worker pool<br>
 * <tt>pipeline.addLast(myHandler);</tt><br><br>
 *
 * <b>Note that this handler has a Pipeline Coverage of "all" which means only one such handler must be created
 * and shared among all channels as the counter must be shared among all channels.</b><br><br>
 *
 * Other arguments can be passed like write or read limitation (in bytes/s where 0 means no limitation)
 * or the check interval (in millisecond) that represents the delay between two computations of the
 * bandwidth and so the call back of the doAccounting method (0 means no accounting at all).<br>
 * Note that as this is a fusion of both Global and Channel Traffic Shaping, limits are in 2 sets,
 * respectively Global and Channel.<br><br>
 *
 * A value of 0 means no accounting for checkInterval. If you need traffic shaping but no such accounting,
 * it is recommended to set a positive value, even if it is high since the precision of the
 * Traffic Shaping depends on the period where the traffic is computed. The highest the interval,
 * the less precise the traffic shaping will be. It is suggested as higher value something close
 * to 5 or 10 minutes.<br><br>
 *
 * maxTimeToWait, by default set to 15s, allows to specify an upper bound of time shaping.<br><br>
 * </li>
 * <li>In your handler, you should consider to use the <code>channel.isWritable()</code> and
 * <code>channelWritabilityChanged(ctx)</code> to handle writability, or through
 * <code>future.addListener(new GenericFutureListener())</code> on the future returned by
 * <code>ctx.write()</code>.</li>
 * <li>You shall also consider to have object size in read or write operations relatively adapted to
 * the bandwidth you required: for instance having 10 MB objects for 10KB/s will lead to burst effect,
 * while having 100 KB objects for 1 MB/s should be smoothly handle by this TrafficShaping handler.<br><br></li>
 * <li>Some configuration methods will be taken as best effort, meaning
 * that all already scheduled traffics will not be
 * changed, but only applied to new traffics.<br>
 * So the expected usage of those methods are to be used not too often,
 * accordingly to the traffic shaping configuration.</li>
 * </ul><br>
 *
 * Be sure to call {@link #release()} once this handler is not needed anymore to release all internal resources.
 * This will not shutdown the {@link EventExecutor} as it may be shared, so you need to do this by your own.
 */
@Sharable
public class GlobalChannelTrafficShapingHandler extends AbstractTrafficShapingHandler {
    private static final InternalLogger logger =
            InternalLoggerFactory.getInstance(GlobalChannelTrafficShapingHandler.class);
    /**
     * All queues per channel
     */
    final ConcurrentMap<Integer, MixtePerChannel> channelQueues = new ConcurrentHashMap<Integer, MixtePerChannel>();

    /**
     * Global queues size
     */
    private final AtomicLong queuesSize = new AtomicLong();

    /**
     * Maximum cumulative writing bytes for one channel among all (as long as channels stay the same)
     */
    private final AtomicLong cumulativeWrittenBytes = new AtomicLong();

    /**
     * Maximum cumulative read bytes for one channel among all (as long as channels stay the same)
     */
    private final AtomicLong cumulativeReadBytes = new AtomicLong();

    /**
     * Max size in the list before proposing to stop writing new objects from next handlers
     * for all channel (global)
     */
    long maxGlobalWriteSize = DEFAULT_MAX_SIZE * 100; // default 400MB

    /**
     * Limit in B/s to apply to write
     */
    private long writeChannelLimit;

    /**
     * Limit in B/s to apply to read
     */
    private long readChannelLimit;

    private static final float DEFAULT_DEVIATION = 0.1F;
    private float maxDeviation;
    private float minValueDeviation;
    private float maxValueDeviation;
    private volatile boolean readDeviationActive;
    private volatile boolean writeDeviationActive;

    static class MixtePerChannel {
        List<ToSend> messagesQueue;
        TrafficCounter channelTrafficCounter;
        ReentrantLock lock;
        long queueSize;
        long lastWrite;
        long lastRead;
    }

    /**
     * Create the global TrafficCounter
     */
    void createGlobalTrafficCounter(Timer timer) {
        // Default
        setMaxDeviation(DEFAULT_DEVIATION);
        if (timer == null) {
            throw new NullPointerException("timer");
        }
        TrafficCounter tc = new GlobalChannelTrafficCounter(this, timer, "GlobalTC", checkInterval);
        setTrafficCounter(tc);
        tc.start();
    }

    @Override
    int userDefinedWritabilityIndex() {
        return AbstractTrafficShapingHandler.GLOBAL_DEFAULT_USER_DEFINED_WRITABILITY_INDEX;
    }

    /**
     * Create a new instance
     *
     * @param timer
     *            the {@link Timer} to use for the {@link TrafficCounter}
     * @param writeGlobalLimit
     *            0 or a limit in bytes/s
     * @param readGlobalLimit
     *            0 or a limit in bytes/s
     * @param writeChannelLimit
     *            0 or a limit in bytes/s
     * @param readChannelLimit
     *            0 or a limit in bytes/s
     * @param checkInterval
     *            The delay between two computations of performances for
     *            channels or 0 if no stats are to be computed
     * @param maxTime
     *            The maximum delay to wait in case of traffic excess
     */
    public GlobalChannelTrafficShapingHandler(Timer timer,
            long writeGlobalLimit, long readGlobalLimit,
            long writeChannelLimit, long readChannelLimit,
            long checkInterval, long maxTime) {
        super(timer, writeGlobalLimit, readGlobalLimit, checkInterval, maxTime);
        createGlobalTrafficCounter(timer);
        this.writeChannelLimit = writeChannelLimit;
        this.readChannelLimit = readChannelLimit;
    }

    /**
     * Create a new instance
     *
     * @param timer
     *          the {@link Timer} to use for the {@link TrafficCounter}
     * @param writeGlobalLimit
     *            0 or a limit in bytes/s
     * @param readGlobalLimit
     *            0 or a limit in bytes/s
     * @param writeChannelLimit
     *            0 or a limit in bytes/s
     * @param readChannelLimit
     *            0 or a limit in bytes/s
     * @param checkInterval
     *          The delay between two computations of performances for
     *            channels or 0 if no stats are to be computed
     */
    public GlobalChannelTrafficShapingHandler(Timer timer,
            long writeGlobalLimit, long readGlobalLimit,
            long writeChannelLimit, long readChannelLimit,
            long checkInterval) {
        super(timer, writeGlobalLimit, readGlobalLimit, checkInterval);
        this.writeChannelLimit = writeChannelLimit;
        this.readChannelLimit = readChannelLimit;
        createGlobalTrafficCounter(timer);
    }

    /**
     * Create a new instance
     *
     * @param timer
     *          the {@link Timer} to use for the {@link TrafficCounter}
     * @param writeGlobalLimit
     *            0 or a limit in bytes/s
     * @param readGlobalLimit
     *            0 or a limit in bytes/s
     * @param writeChannelLimit
     *            0 or a limit in bytes/s
     * @param readChannelLimit
     *            0 or a limit in bytes/s
     */
    public GlobalChannelTrafficShapingHandler(Timer timer,
            long writeGlobalLimit, long readGlobalLimit,
            long writeChannelLimit, long readChannelLimit) {
        super(timer, writeGlobalLimit, readGlobalLimit);
        this.writeChannelLimit = writeChannelLimit;
        this.readChannelLimit = readChannelLimit;
        createGlobalTrafficCounter(timer);
    }

    /**
     * Create a new instance
     *
     * @param timer
     *          the {@link Timer} to use for the {@link TrafficCounter}
     * @param checkInterval
     *          The delay between two computations of performances for
     *            channels or 0 if no stats are to be computed
     */
    public GlobalChannelTrafficShapingHandler(Timer timer, long checkInterval) {
        super(timer, checkInterval);
        createGlobalTrafficCounter(timer);
    }

    /**
     * Create a new instance
     *
     * @param timer
     *          the {@link Timer} to use for the {@link TrafficCounter}
     */
    public GlobalChannelTrafficShapingHandler(Timer timer) {
        super(timer);
        createGlobalTrafficCounter(timer);
    }

    /**
     * @param objectSizeEstimator ObjectSizeEstimator to use
     * @param timer
     *            the {@link Timer} to use for the {@link TrafficCounter}
     * @param writeLimit write Global Limit
     *            0 or a limit in bytes/s
     * @param readLimit read Global Limit
     *            0 or a limit in bytes/s
     * @param writeChannelLimit
     *            0 or a limit in bytes/s
     * @param readChannelLimit
     *            0 or a limit in bytes/s
     * @param checkInterval
     *            The delay between two computations of performances for
     *            channels or 0 if no stats are to be computed
     * @param maxTime
     *            The maximum delay to wait in case of traffic excess
     */
    public GlobalChannelTrafficShapingHandler(ObjectSizeEstimator objectSizeEstimator, Timer timer, long writeLimit,
            long readLimit, long writeChannelLimit, long readChannelLimit, long checkInterval, long maxTime) {
        super(objectSizeEstimator, timer, writeLimit, readLimit, checkInterval, maxTime);
        this.writeChannelLimit = writeChannelLimit;
        this.readChannelLimit = readChannelLimit;
        createGlobalTrafficCounter(timer);
    }

    /**
     * @param objectSizeEstimator ObjectSizeEstimator to use
     * @param timer
     *            the {@link Timer} to use for the {@link TrafficCounter}
     * @param writeLimit write Global Limit
     *            0 or a limit in bytes/s
     * @param readLimit read Global Limit
     *            0 or a limit in bytes/s
     * @param writeChannelLimit
     *            0 or a limit in bytes/s
     * @param readChannelLimit
     *            0 or a limit in bytes/s
     * @param checkInterval
     *            The delay between two computations of performances for
     *            channels or 0 if no stats are to be computed
     */
    public GlobalChannelTrafficShapingHandler(ObjectSizeEstimator objectSizeEstimator, Timer timer, long writeLimit,
            long readLimit, long writeChannelLimit, long readChannelLimit, long checkInterval) {
        super(objectSizeEstimator, timer, writeLimit, readLimit, checkInterval);
        this.writeChannelLimit = writeChannelLimit;
        this.readChannelLimit = readChannelLimit;
        createGlobalTrafficCounter(timer);
    }

    /**
     * @param objectSizeEstimator ObjectSizeEstimator to use
     * @param timer
     *            the {@link Timer} to use for the {@link TrafficCounter}
     * @param writeLimit write Global Limit
     *            0 or a limit in bytes/s
     * @param readLimit read Global Limit
     *            0 or a limit in bytes/s
     * @param writeChannelLimit
     *            0 or a limit in bytes/s
     * @param readChannelLimit
     *            0 or a limit in bytes/s
     */
    public GlobalChannelTrafficShapingHandler(ObjectSizeEstimator objectSizeEstimator, Timer timer, long writeLimit,
            long readLimit, long writeChannelLimit, long readChannelLimit) {
        super(objectSizeEstimator, timer, writeLimit, readLimit);
        this.writeChannelLimit = writeChannelLimit;
        this.readChannelLimit = readChannelLimit;
        createGlobalTrafficCounter(timer);
    }

    /**
     * @param objectSizeEstimator ObjectSizeEstimator to use
     * @param timer
     *            the {@link Timer} to use for the {@link TrafficCounter}
     * @param checkInterval
     *            The delay between two computations of performances for
     *            channels or 0 if no stats are to be computed
     */
    public GlobalChannelTrafficShapingHandler(ObjectSizeEstimator objectSizeEstimator, Timer timer,
            long checkInterval) {
        super(objectSizeEstimator, timer, checkInterval);
        createGlobalTrafficCounter(timer);
    }

    /**
     * @param objectSizeEstimator ObjectSizeEstimator to use
     * @param timer
     *            the {@link Timer} to use for the {@link TrafficCounter}
     */
    public GlobalChannelTrafficShapingHandler(ObjectSizeEstimator objectSizeEstimator, Timer timer) {
        super(objectSizeEstimator, timer);
        createGlobalTrafficCounter(timer);
    }

    /**
     * @return the current max deviation
     */
    public float maxDeviation() {
        return maxDeviation;
    }

    /**
     * @param maxDeviation the maximum deviation to allow during computation of average,
     *      default deviation being 0.1, so +/-10% of the desired bandwidth. Maximum being 0.4.
     */
    public void setMaxDeviation(float maxDeviation) {
        if (maxDeviation > 0.4F) {
            // Ignore
            return;
        }
        this.maxDeviation = maxDeviation;
        minValueDeviation = 1.0F - maxDeviation;
        // Penalize more too fast channels
        maxValueDeviation = 1.0F + maxDeviation * 4;
    }

    private void computeDeviationCumulativeBytes() {
        // compute the maximum cumulativeXxxxBytes among still connected Channels
        long maxWrittenBytes = 0;
        long maxReadBytes = 0;
        long minWrittenBytes = Long.MAX_VALUE;
        long minReadBytes = Long.MAX_VALUE;
        for (MixtePerChannel mixtePerChannel : channelQueues.values()) {
            long value = mixtePerChannel.channelTrafficCounter.getCumulativeWrittenBytes();
            if (maxWrittenBytes < value) {
                maxWrittenBytes = value;
            }
            if (minWrittenBytes > value) {
                minWrittenBytes = value;
            }
            value = mixtePerChannel.channelTrafficCounter.getCumulativeReadBytes();
            if (maxReadBytes < value) {
                maxReadBytes = value;
            }
            if (minReadBytes > value) {
                minReadBytes = value;
            }
        }
        boolean multiple = channelQueues.size() > 1;
        readDeviationActive = multiple && minReadBytes < maxReadBytes / 2;
        writeDeviationActive = multiple && minWrittenBytes < maxWrittenBytes / 2;
        cumulativeWrittenBytes.set(maxWrittenBytes);
        cumulativeReadBytes.set(maxReadBytes);
    }

    @Override
    protected void doAccounting(TrafficCounter counter) {
        computeDeviationCumulativeBytes();
        super.doAccounting(counter);
    }

    private long computeBalancedWait(float maxLocal, float maxGlobal, long wait) {
        float ratio = maxLocal / maxGlobal;
        // if in the boundaries, same value
        if (ratio > maxDeviation) {
            if (ratio < minValueDeviation) {
                return wait;
            } else {
                ratio = maxValueDeviation;
                if (wait < MINIMAL_WAIT) {
                    wait = MINIMAL_WAIT;
                }
            }
        } else {
            ratio = minValueDeviation;
        }
        return (long) (wait * ratio);
    }

    /**
     * @return the maxGlobalWriteSize
     */
    public long getMaxGlobalWriteSize() {
        return maxGlobalWriteSize;
    }

    /**
     * Note the change will be taken as best effort, meaning
     * that all already scheduled traffics will not be
     * changed, but only applied to new traffics.<br>
     * So the expected usage of this method is to be used not too often,
     * accordingly to the traffic shaping configuration.
     *
     * @param maxGlobalWriteSize the maximum Global Write Size allowed in the buffer
     *            globally for all channels before write suspended is set
     */
    public void setMaxGlobalWriteSize(long maxGlobalWriteSize) {
        this.maxGlobalWriteSize = maxGlobalWriteSize;
    }

    /**
     * @return the global size of the buffers for all queues
     */
    public long queuesSize() {
        return queuesSize.get();
    }

    /**
     * @param newWriteLimit Channel write limit
     * @param newReadLimit Channel read limit
     */
    public void configureChannel(long newWriteLimit, long newReadLimit) {
        writeChannelLimit = newWriteLimit;
        readChannelLimit = newReadLimit;
        long now = TrafficCounter.milliSecondFromNano() + 1;
        for (MixtePerChannel mixtePerChannel : channelQueues.values()) {
            mixtePerChannel.channelTrafficCounter.resetAccounting(now);
        }
    }

    /**
     * @return Channel write limit
     */
    public long getWriteChannelLimit() {
        return writeChannelLimit;
    }

    /**
     * @param Channel write limit
     */
    public void setWriteChannelLimit(long writeLimit) {
        writeChannelLimit = writeLimit;
        long now = TrafficCounter.milliSecondFromNano() + 1;
        for (MixtePerChannel mixtePerChannel : channelQueues.values()) {
            mixtePerChannel.channelTrafficCounter.resetAccounting(now);
        }
    }

    /**
     * @return Channel read limit
     */
    public long getReadChannelLimit() {
        return readChannelLimit;
    }

    /**
     * @return Channel read limit
     */
    public void setReadChannelLimit(long readLimit) {
        readChannelLimit = readLimit;
        long now = TrafficCounter.milliSecondFromNano() + 1;
        for (MixtePerChannel mixtePerChannel : channelQueues.values()) {
            mixtePerChannel.channelTrafficCounter.resetAccounting(now);
        }
    }

    /**
     * Release all internal resources of this instance
     */
    public final void release() {
        trafficCounter.stop();
    }

    private MixtePerChannel getOrSetPerChannel(ChannelHandlerContext ctx) {
        // ensure creation is limited to one thread per channel
        Channel channel = ctx.getChannel();
        Integer key = channel.hashCode();
        MixtePerChannel mixtePerChannel = channelQueues.get(key);
        if (mixtePerChannel == null) {
            mixtePerChannel = new MixtePerChannel();
            mixtePerChannel.messagesQueue = new LinkedList<ToSend>();
            // Don't start it since managed through the Global one
            mixtePerChannel.channelTrafficCounter = new TrafficCounter(this, null, "ChannelTC" +
                    ctx.getChannel().hashCode(), checkInterval);
            mixtePerChannel.lock = new ReentrantLock(true);
            mixtePerChannel.queueSize = 0L;
            mixtePerChannel.lastRead = TrafficCounter.milliSecondFromNano();
            mixtePerChannel.lastWrite = mixtePerChannel.lastRead;
            channelQueues.put(key, mixtePerChannel);
        }
        return mixtePerChannel;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        getOrSetPerChannel(ctx);
        trafficCounter.resetCumulativeTime();
        super.channelConnected(ctx, e);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        trafficCounter.resetCumulativeTime();
        Channel channel = ctx.getChannel();
        Integer key = channel.hashCode();
        MixtePerChannel mixtePerChannel = channelQueues.remove(key);
        if (mixtePerChannel != null) {
            // write operations need synchronization
            mixtePerChannel.lock.lock();
            try {
                queuesSize.addAndGet(-mixtePerChannel.queueSize);
                mixtePerChannel.messagesQueue.clear();
            } finally {
                mixtePerChannel.lock.unlock();
            }
        }
        releaseWriteSuspended(ctx);
        releaseReadSuspended(ctx);
        super.channelClosed(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent evt)
            throws Exception {
        long now = TrafficCounter.milliSecondFromNano();
        try {
            ReadWriteStatus rws = checkAttachment(ctx);
            long size = calculateSize(evt.getMessage());
            if (size > 0) {
                // compute the number of ms to wait before reopening the channel
                // compute the number of ms to wait before reopening the channel
                long waitGlobal = trafficCounter.readTimeToWait(size, getReadLimit(), maxTime, now);
                Integer key = ctx.getChannel().hashCode();
                MixtePerChannel mixtePerChannel = channelQueues.get(key);
                long wait = 0;
                if (mixtePerChannel != null) {
                    wait = mixtePerChannel.channelTrafficCounter.readTimeToWait(size, readChannelLimit, maxTime, now);
                    if (readDeviationActive) {
                        // now try to balance between the channels
                        long maxLocalRead = 0;
                        maxLocalRead = mixtePerChannel.channelTrafficCounter.getCumulativeReadBytes();
                        long maxGlobalRead = cumulativeReadBytes.get();
                        if (maxLocalRead <= 0) {
                            maxLocalRead = 1;
                        }
                        if (maxGlobalRead < maxLocalRead) {
                            maxGlobalRead = maxLocalRead;
                        }
                        wait = computeBalancedWait(maxLocalRead, maxGlobalRead, wait);
                    }
                }
                if (wait < waitGlobal) {
                    wait = waitGlobal;
                }
                wait = checkWaitReadTime(ctx, wait, now);
                if (wait >= MINIMAL_WAIT) { // At least 10ms seems a minimal
                    // time in order to try to limit the traffic
                    if (release.get()) {
                        return;
                    }
                    Channel channel = ctx.getChannel();
                    if (channel != null && channel.isConnected()) {
                        // Only AutoRead AND HandlerActive True means Context Active
                        if (logger.isDebugEnabled()) {
                            logger.debug("Read suspend: " + wait + ":" + channel.isReadable() + ":" +
                                    rws.readSuspend);
                        }
                        if (timer == null) {
                            // Sleep since no executor
                            // logger.warn("Read sleep since no timer for "+wait+" ms for "+this);
                            Thread.sleep(wait);
                            return;
                        }
                        if (channel.isReadable() && ! rws.readSuspend) {
                            rws.readSuspend = true;
                            channel.setReadable(false);
                            if (logger.isDebugEnabled()) {
                                logger.debug("Suspend final status => " + channel.isReadable() + ":" +
                                        rws.readSuspend);
                            }
                            // Create a Runnable to reactive the read if needed. If one was create before
                            // it will just be reused to limit object creation
                            if (rws.reopenReadTimerTask == null) {
                                rws.reopenReadTimerTask = new ReopenReadTimerTask(ctx);
                            }
                            timeout = timer.newTimeout(rws.reopenReadTimerTask, wait,
                                    TimeUnit.MILLISECONDS);
                        }
                    }
                }
            }
        } finally {
            informReadOperation(ctx, now);
            // The message is then forcedly passed to the next handler (not to super)
            ctx.sendUpstream(evt);
        }
    }

    @Override
    protected long checkWaitReadTime(final ChannelHandlerContext ctx, long wait, final long now) {
        Integer key = ctx.getChannel().hashCode();
        MixtePerChannel mixtePerChannel = channelQueues.get(key);
        if (mixtePerChannel != null) {
            if (wait > maxTime && now + wait - mixtePerChannel.lastRead > maxTime) {
                wait = maxTime;
            }
        }
        return wait;
    }

    @Override
    protected void informReadOperation(final ChannelHandlerContext ctx, final long now) {
        Integer key = ctx.getChannel().hashCode();
        MixtePerChannel mixtePerChannel = channelQueues.get(key);
        if (mixtePerChannel != null) {
            mixtePerChannel.lastRead = now;
        }
    }

    private static final class ToSend {
        final long relativeTimeAction;
        final MessageEvent toSend;
        final long size;

        private ToSend(final long delay, final MessageEvent toSend, final long size) {
            this.relativeTimeAction = delay;
            this.toSend = toSend;
            this.size = size;
        }
    }

    protected long maximumCumulativeWrittenBytes() {
        return cumulativeWrittenBytes.get();
    }

    protected long maximumCumulativeReadBytes() {
        return cumulativeReadBytes.get();
    }

    protected Collection<TrafficCounter> channelTrafficCounters() {
        List<TrafficCounter> list = new ArrayList<TrafficCounter>(channelQueues.size());
        for (MixtePerChannel mpc : channelQueues.values()) {
            list.add(mpc.channelTrafficCounter);
        }
        return list;
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent evt)
            throws Exception {
        long wait = 0;
        long size = calculateSize(evt.getMessage());
        long now = TrafficCounter.milliSecondFromNano();
        try {
            if (size > 0) {
                // compute the number of ms to wait before continue with the channel
                long waitGlobal = trafficCounter.writeTimeToWait(size, getWriteLimit(), maxTime, now);
                Integer key = ctx.getChannel().hashCode();
                MixtePerChannel mixtePerChannel = channelQueues.get(key);
                if (mixtePerChannel != null) {
                    wait = mixtePerChannel.channelTrafficCounter.writeTimeToWait(size, writeChannelLimit, maxTime, now);
                    if (writeDeviationActive) {
                        // now try to balance between the channels
                        long maxLocalWrite = 0;
                        maxLocalWrite = mixtePerChannel.channelTrafficCounter.getCumulativeWrittenBytes();
                        long maxGlobalWrite = cumulativeWrittenBytes.get();
                        if (maxLocalWrite <= 0) {
                            maxLocalWrite = 1;
                        }
                        if (maxGlobalWrite < maxLocalWrite) {
                            maxGlobalWrite = maxLocalWrite;
                        }
                        wait = computeBalancedWait(maxLocalWrite, maxGlobalWrite, wait);
                    }
                }
                if (wait < waitGlobal) {
                    wait = waitGlobal;
                }
                if (wait < MINIMAL_WAIT || release.get()) {
                    wait = 0;
                }
            }
        } finally {
            // The message is scheduled
            submitWrite(ctx, evt, size, wait, now);
        }
    }

    @Override
    protected void submitWrite(final ChannelHandlerContext ctx, final MessageEvent evt,
            final long size, final long writedelay, final long now) throws Exception {
        Channel channel = ctx.getChannel();
        Integer key = channel.hashCode();
        MixtePerChannel mixtePerChannel = channelQueues.get(key);
        if (mixtePerChannel == null) {
            // in case write occurs before handlerAdded is raized for this handler
            // imply a synchronized only if needed
            mixtePerChannel = getOrSetPerChannel(ctx);
        }
        ToSend newToSend;
        long delay = writedelay;
        boolean globalSizeExceeded = false;
        // write operations need synchronization
        mixtePerChannel.lock.lock();
        try {
            if (writedelay == 0 && mixtePerChannel.messagesQueue.isEmpty()) {
                if (!channel.isConnected()) {
                    // ignore
                    return;
                }
                trafficCounter.bytesRealWriteFlowControl(size);
                mixtePerChannel.channelTrafficCounter.bytesRealWriteFlowControl(size);
                ctx.sendDownstream(evt);
                mixtePerChannel.lastWrite = now;
                return;
            }
            if (delay > maxTime && now + delay - mixtePerChannel.lastWrite > maxTime) {
                delay = maxTime;
            }
            if (timer == null) {
                // Sleep since no executor
                Thread.sleep(delay);
                if (!ctx.getChannel().isConnected()) {
                    // ignore
                    return;
                }
                trafficCounter.bytesRealWriteFlowControl(size);
                mixtePerChannel.channelTrafficCounter.bytesRealWriteFlowControl(size);
                ctx.sendDownstream(evt);
                mixtePerChannel.lastWrite = now;
                return;
            }
            if (!ctx.getChannel().isConnected()) {
                // ignore
                return;
            }
            newToSend = new ToSend(delay + now, evt, size);
            mixtePerChannel.messagesQueue.add(newToSend);
            mixtePerChannel.queueSize += size;
            queuesSize.addAndGet(size);
            checkWriteSuspend(ctx, delay, mixtePerChannel.queueSize);
            if (queuesSize.get() > maxGlobalWriteSize) {
                globalSizeExceeded = true;
            }
        } finally {
            mixtePerChannel.lock.unlock();
        }
        if (globalSizeExceeded) {
            setWritable(ctx, false);
        }
        final long futureNow = newToSend.relativeTimeAction;
        final MixtePerChannel forSchedule = mixtePerChannel;
        timer.newTimeout(new TimerTask() {
            public void run(Timeout timeout) throws Exception {
                sendAllValid(ctx, forSchedule, futureNow);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void sendAllValid(final ChannelHandlerContext ctx, final MixtePerChannel mixtePerChannel, final long now)
            throws Exception {
        // write operations need synchronization
        mixtePerChannel.lock.lock();
        try {
            while (!mixtePerChannel.messagesQueue.isEmpty()) {
                ToSend newToSend = mixtePerChannel.messagesQueue.remove(0);
                if (newToSend.relativeTimeAction <= now) {
                    if (! ctx.getChannel().isConnected()) {
                        // ignore
                        break;
                    }
                    long size = newToSend.size;
                    trafficCounter.bytesRealWriteFlowControl(size);
                    mixtePerChannel.channelTrafficCounter.bytesRealWriteFlowControl(size);
                    mixtePerChannel.queueSize -= size;
                    queuesSize.addAndGet(-size);
                    ctx.sendDownstream(newToSend.toSend);
                    mixtePerChannel.lastWrite = now;
                } else {
                    mixtePerChannel.messagesQueue.add(0, newToSend);
                    break;
                }
            }
            if (mixtePerChannel.messagesQueue.isEmpty()) {
                releaseWriteSuspended(ctx);
            }
        } finally {
            mixtePerChannel.lock.unlock();
        }
    }

    @Override
    public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        if ((((Integer) e.getValue()) & Channel.OP_WRITE) == 0 && ! ctx.getChannel().getUserDefinedWritability(index)) {
            // drop it silently if too quick
            if (writeDeviationActive) {
                Integer key = ctx.getChannel().hashCode();
                MixtePerChannel mixtePerChannel = channelQueues.get(key);
                if (mixtePerChannel != null) {
                    double maxLocalWrite = 0.0;
                    maxLocalWrite = mixtePerChannel.channelTrafficCounter.getCumulativeWrittenBytes();
                    double maxGlobalWrite = cumulativeWrittenBytes.get();
                    if (maxLocalWrite / maxGlobalWrite < maxDeviation) {
                        // try to not drop event for late channels
                        ctx.sendUpstream(e);
                        return;
                    }
                }
            }
            // drop event
            e.getFuture().setSuccess();
            return;
        }
        ctx.sendUpstream(e);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        builder.append(" Write Channel Limit: ").append(writeChannelLimit);
        builder.append(" Read Channel Limit: ").append(readChannelLimit);
        return builder.toString();
    }
}
