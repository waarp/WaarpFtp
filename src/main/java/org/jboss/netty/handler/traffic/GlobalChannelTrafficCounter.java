/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.handler.traffic;

import java.util.concurrent.TimeUnit;

import org.jboss.netty.handler.traffic.GlobalChannelTrafficShapingHandler.MixtePerChannel;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;

/**
 * Version for {@link GlobalChannelTrafficShapingHandler}
 *
 */
public class GlobalChannelTrafficCounter extends TrafficCounter {
    /**
     * @param trafficShapingHandler
     * @param executor
     * @param name
     * @param checkInterval
     */
    public GlobalChannelTrafficCounter(GlobalChannelTrafficShapingHandler trafficShapingHandler,
            Timer timer, String name, long checkInterval) {
        super(trafficShapingHandler, timer, name, checkInterval);
    }

    /**
     * Class to implement monitoring at fix delay
     *
     */
    private static class MixteTrafficMonitoringTask implements TimerTask {
        /**
         * The associated TrafficShapingHandler
         */
        private final GlobalChannelTrafficShapingHandler trafficShapingHandler1;

        /**
         * The associated TrafficCounter
         */
        private final TrafficCounter counter;

        /**
         * @param trafficShapingHandler The parent handler to which this task needs to callback to for accounting
         * @param counter The parent TrafficCounter that we need to reset the statistics for
         */
        MixteTrafficMonitoringTask(
                GlobalChannelTrafficShapingHandler trafficShapingHandler,
                TrafficCounter counter) {
            trafficShapingHandler1 = trafficShapingHandler;
            this.counter = counter;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            if (!counter.monitorActive) {
                return;
            }
            long newLastTime = milliSecondFromNano();
            counter.resetAccounting(newLastTime);
            for (MixtePerChannel mixtePerChannel : trafficShapingHandler1.channelQueues.values()) {
                mixtePerChannel.channelTrafficCounter.resetAccounting(newLastTime);
            }
            if (trafficShapingHandler1 != null) {
                trafficShapingHandler1.doAccounting(counter);
            }
            counter.timer.newTimeout(this, counter.checkInterval.get(), TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Start the monitoring process
     */
    public synchronized void start() {
        if (monitorActive) {
            return;
        }
        lastTime.set(milliSecondFromNano());
        long localCheckInterval = checkInterval.get();
        if (localCheckInterval > 0) {
            monitorActive = true;
            timerTask =
                    new MixteTrafficMonitoringTask(
                            (GlobalChannelTrafficShapingHandler) trafficShapingHandler, this);
            timeout = timer.newTimeout(timerTask, checkInterval.get(), TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Stop the monitoring process
     */
    public synchronized void stop() {
        if (!monitorActive) {
            return;
        }
        monitorActive = false;
        resetAccounting(milliSecondFromNano());
        if (trafficShapingHandler != null) {
            trafficShapingHandler.doAccounting(this);
        }
        if (timeout != null) {
            timeout.cancel();
        }
    }

    @Override
    public void resetCumulativeTime() {
        for (MixtePerChannel mixtePerChannel :
            ((GlobalChannelTrafficShapingHandler) trafficShapingHandler).channelQueues.values()) {
            mixtePerChannel.channelTrafficCounter.resetCumulativeTime();
        }
        super.resetCumulativeTime();
    }

}
