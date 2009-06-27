/**
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3.0 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package goldengate.ftp.core.config;

import goldengate.common.command.exception.Reply425Exception;
import goldengate.common.file.DataBlockSizeEstimator;
import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;
import goldengate.ftp.core.control.FtpPipelineFactory;
import goldengate.ftp.core.data.handler.FtpDataPipelineFactory;
import goldengate.ftp.core.session.FtpSession;
import goldengate.ftp.core.session.FtpSessionReference;
import goldengate.ftp.core.utils.FtpChannelUtils;
import goldengate.ftp.core.utils.FtpSignalHandler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.traffic.ChannelTrafficShapingHandler;
import org.jboss.netty.handler.traffic.GlobalTrafficShapingHandler;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.util.ObjectSizeEstimator;

/**
 * Internal configuration of the FTP server, related to Netty
 *
 * @author Frederic Bregier
 *
 */
public class FtpInternalConfiguration {
    // Static values
    /**
     * Internal Logger
     */
    private static final GgInternalLogger logger = GgInternalLoggerFactory
            .getLogger(FtpInternalConfiguration.class);

    // Network Internals
    /**
     * Time elapse for retry in ms
     */
    public static final long RETRYINMS = 10;

    /**
     * Number of retry before error
     */
    public static final int RETRYNB = 3;

    /**
     * Hack to say Windows or Unix (USR1 not OK on Windows)
     */
    public static final boolean ISUNIX = !System.getProperty("os.name")
            .toLowerCase().startsWith("windows");

    /**
     * Default size for buffers (NIO)
     */
    public static final int BUFFERSIZEDEFAULT = 0x10000; // 64K

    // Dynamic values
    /**
     * List of all Command Channels to enable the close call on them using Netty
     * ChannelGroup
     */
    private ChannelGroup commandChannelGroup = null;

    /**
     * ExecutorService Boss
     */
    private final ExecutorService execBoss = Executors.newCachedThreadPool();

    /**
     * ExecutorService Worker
     */
    private final ExecutorService execWorker = Executors.newCachedThreadPool();

    /**
     * ChannelFactory for Command part
     */
    private ChannelFactory commandChannelFactory = null;

    /**
     * ThreadPoolExecutor for command
     */
    private volatile OrderedMemoryAwareThreadPoolExecutor pipelineExecutor = null;

    /**
     * Bootstrap for Command server
     */
    private ServerBootstrap serverBootstrap = null;

    /**
     * List of all Data Channels to enable the close call on them using Netty
     * ChannelGroup
     */
    private ChannelGroup dataChannelGroup = null;

    /**
     * ExecutorService Data Passive Boss
     */
    private final ExecutorService execPassiveDataBoss = Executors
            .newCachedThreadPool();

    /**
     * ExecutorService Data Passive Worker
     */
    private final ExecutorService execPassiveDataWorker = Executors
            .newCachedThreadPool();

    /**
     * ChannelFactory for Data Passive part
     */
    private ChannelFactory dataPassiveChannelFactory = null;

    /**
     * ExecutorService Data Active Boss
     */
    private final ExecutorService execActiveDataBoss = Executors
            .newCachedThreadPool();

    /**
     * ExecutorService Data Active Worker
     */
    private final ExecutorService execActiveDataWorker = Executors
            .newCachedThreadPool();

    /**
     * ChannelFactory for Data Active part
     */
    private ChannelFactory dataActiveChannelFactory = null;

    /**
     * FtpSession references used by Data Connection process
     */
    private final FtpSessionReference ftpSessionReference = new FtpSessionReference();

    /**
     * ThreadPoolExecutor for data
     */
    private volatile OrderedMemoryAwareThreadPoolExecutor pipelineDataExecutor = null;

    /**
     * ServerBootStrap for Active connections
     */
    private ClientBootstrap activeBootstrap = null;

    /**
     * ClientBootStrap for Passive connections
     */
    private ServerBootstrap passiveBootstrap = null;

    /**
     * ExecutorService for TrafficCounter
     */
    private final ExecutorService execTrafficCounter = Executors
            .newCachedThreadPool();

    /**
     * Global TrafficCounter (set from global configuration)
     */
    private volatile GlobalTrafficShapingHandler globalTrafficShapingHandler = null;

    /**
     * ObjectSizeEstimator
     */
    private ObjectSizeEstimator objectSizeEstimator = null;

    /**
     *
     * @author Frederic Bregier goldengate.ftp.core.config BindAddress
     *
     */
    public class BindAddress {
        /**
         * Parent passive channel
         */
        public final Channel parent;

        /**
         * Number of binded Data connections
         */
        public final AtomicInteger nbBind;

        /**
         * Constructor
         *
         * @param channel
         */
        public BindAddress(Channel channel) {
            parent = channel;
            nbBind = new AtomicInteger(0);
        }
    }

    /**
     * List of already bind local addresses for Passive connections
     */
    private final ConcurrentHashMap<InetSocketAddress, BindAddress> hashBindPassiveDataConn = new ConcurrentHashMap<InetSocketAddress, BindAddress>();

    /**
     * Global Configuration
     */
    private final FtpConfiguration configuration;

    /**
     * Constructor
     *
     * @param configuration
     */
    public FtpInternalConfiguration(FtpConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Startup the server
     *
     */
    public void serverStartup() {
        InternalLoggerFactory.setDefaultFactory(InternalLoggerFactory
                .getDefaultFactory());
        // Command
        commandChannelGroup = new DefaultChannelGroup(configuration.fromClass
                .getName());
        // ChannelGroupFactory.getGroup(configuration.fromClass);
        commandChannelFactory = new NioServerSocketChannelFactory(execBoss,
                execWorker, configuration.SERVER_THREAD);
        // Data
        dataChannelGroup = new DefaultChannelGroup(configuration.fromClass
                .getName() +
                ".data");
        // ChannelGroupFactory.getGroup(configuration.fromClass.getName()+".data");
        dataPassiveChannelFactory = new NioServerSocketChannelFactory(
                execPassiveDataBoss, execPassiveDataWorker,
                configuration.SERVER_THREAD);
        dataActiveChannelFactory = new NioClientSocketChannelFactory(
                execActiveDataBoss, execActiveDataWorker, 4);

        // Passive Data Connections
        passiveBootstrap = new ServerBootstrap(dataPassiveChannelFactory);
        passiveBootstrap.setPipelineFactory(new FtpDataPipelineFactory(
                configuration.dataBusinessHandler, configuration, false));
        passiveBootstrap.setOption("connectTimeoutMillis",
                configuration.TIMEOUTCON);
        passiveBootstrap.setOption("reuseAddress", true);
        passiveBootstrap.setOption("tcpNoDelay", true);
        passiveBootstrap.setOption("child.connectTimeoutMillis",
                configuration.TIMEOUTCON);
        passiveBootstrap.setOption("child.tcpNoDelay", true);
        passiveBootstrap.setOption("child.keepAlive", true);
        passiveBootstrap.setOption("child.reuseAddress", true);
        // Active Data Connections
        activeBootstrap = new ClientBootstrap(dataActiveChannelFactory);
        activeBootstrap.setPipelineFactory(new FtpDataPipelineFactory(
                configuration.dataBusinessHandler, configuration, true));
        activeBootstrap.setOption("connectTimeoutMillis",
                configuration.TIMEOUTCON);
        activeBootstrap.setOption("reuseAddress", true);
        activeBootstrap.setOption("tcpNoDelay", true);
        activeBootstrap.setOption("child.connectTimeoutMillis",
                configuration.TIMEOUTCON);
        activeBootstrap.setOption("child.tcpNoDelay", true);
        activeBootstrap.setOption("child.keepAlive", true);
        activeBootstrap.setOption("child.reuseAddress", true);
        // Main Command server
        serverBootstrap = new ServerBootstrap(getCommandChannelFactory());
        serverBootstrap.setPipelineFactory(new FtpPipelineFactory(
                configuration.businessHandler, configuration));
        serverBootstrap.setOption("child.tcpNoDelay", true);
        serverBootstrap.setOption("child.keepAlive", true);
        serverBootstrap.setOption("child.reuseAddress", true);
        serverBootstrap.setOption("child.connectTimeoutMillis",
                configuration.TIMEOUTCON);
        serverBootstrap.setOption("tcpNoDelay", true);
        serverBootstrap.setOption("reuseAddress", true);
        serverBootstrap.setOption("connectTimeoutMillis",
                configuration.TIMEOUTCON);

        FtpChannelUtils.addCommandChannel(serverBootstrap
                .bind(new InetSocketAddress(configuration.getServerPort())),
                configuration);

        // Init signal handler
        FtpSignalHandler.initSignalHandler(configuration);
        // Factory for TrafficShapingHandler
        objectSizeEstimator = new DataBlockSizeEstimator();
        globalTrafficShapingHandler = new GlobalTrafficShapingHandler(
                objectSizeEstimator, execTrafficCounter, configuration
                        .getServerGlobalWriteLimit(), configuration
                        .getServerGlobalReadLimit(), configuration
                        .getDelayLimit());
    }

    /**
     * Add a session from a couple of addresses
     *
     * @param ipOnly
     * @param fullIp
     * @param session
     */
    public void setNewFtpSession(InetAddress ipOnly, InetSocketAddress fullIp,
            FtpSession session) {
        // logger.debug("SetNewSession");
        ftpSessionReference.setNewFtpSession(ipOnly, fullIp, session);
    }

    /**
     * Return and remove the FtpSession
     *
     * @param channel
     * @param active
     * @return the FtpSession if it exists associated to this channel
     */
    public FtpSession getFtpSession(Channel channel, boolean active) {
        // logger.debug("getSession");
        if (active) {
            return ftpSessionReference.getActiveFtpSession(channel);
        } else {
            return ftpSessionReference.getPassiveFtpSession(channel);
        }
    }

    /**
     * Remove the FtpSession
     *
     * @param ipOnly
     * @param fullIp
     */
    public void delFtpSession(InetAddress ipOnly, InetSocketAddress fullIp) {
        // logger.debug("delSession");
        ftpSessionReference.delFtpSession(ipOnly, fullIp);
    }

    /**
     * Test if the couple of addresses is already in the context
     *
     * @param ipOnly
     * @param fullIp
     * @return True if the couple is present
     */
    public boolean hasFtpSession(InetAddress ipOnly, InetSocketAddress fullIp) {
        return ftpSessionReference.contains(ipOnly, fullIp);
    }

    /**
     * Try to add a Passive Channel listening to the specified local address
     *
     * @param address
     * @throws Reply425Exception
     *             in case the channel cannot be opened
     */
    public void bindPassive(InetSocketAddress address) throws Reply425Exception {
        configuration.getLock().lock();
        try {
            BindAddress bindAddress = hashBindPassiveDataConn.get(address);
            if (bindAddress == null) {
                logger.info("Bind really to {}", address);
                Channel parentChannel = null;
                try {
                    parentChannel = passiveBootstrap.bind(address);
                } catch (ChannelException e) {
                    logger.warn("Cannot open passive connection {}", e
                            .getMessage());
                    throw new Reply425Exception(
                            "Cannot open a Passive Connection ");
                }
                bindAddress = new BindAddress(parentChannel);
                FtpChannelUtils.addDataChannel(parentChannel, configuration);
            }
            bindAddress.nbBind.incrementAndGet();
            logger.info("Bind number to {} is {}", address, bindAddress.nbBind);
            hashBindPassiveDataConn.put(address, bindAddress);
        } finally {
            configuration.getLock().unlock();
        }
    }

    /**
     * Try to unbind (closing the parent channel) the Passive Channel listening
     * to the specified local address if the last one. It returns only when the
     * underlying parent channel is closed if this was the last session that
     * wants to open on this local address.
     *
     * @param address
     */
    public void unbindPassive(InetSocketAddress address) {
        configuration.getLock().lock();
        try {
            BindAddress bindAddress = hashBindPassiveDataConn.get(address);
            if (bindAddress != null) {
                int nbBind = bindAddress.nbBind.decrementAndGet();
                logger.info("Bind number to {} left is {}", address, nbBind);
                if (nbBind == 0) {
                    Channels.close(bindAddress.parent);
                    hashBindPassiveDataConn.remove(address);
                }
            } else {
                logger.warn("No Bind to {}", address);
            }
        } finally {
            configuration.getLock().unlock();
        }
    }

    /**
     *
     * @return the number of Binded Passive Connections
     */
    public int getNbBindedPassive() {
        return hashBindPassiveDataConn.size();
    }

    /**
     * Return the associated PipelineExecutor for Command Pipeline
     *
     * @return the Command Pipeline Executor
     */
    public OrderedMemoryAwareThreadPoolExecutor getPipelineExecutor() {
        configuration.getLock().lock();
        try {
            if (pipelineExecutor == null) {
                // Memory limitation: no limit by channel, 1GB global, 100 ms of
                // timeout
                pipelineExecutor = new OrderedMemoryAwareThreadPoolExecutor(
                        configuration.SERVER_THREAD * 4,
                        configuration.maxGlobalMemory / 40,
                        configuration.maxGlobalMemory / 4, 200,
                        TimeUnit.MILLISECONDS, Executors.defaultThreadFactory());
            }
        } finally {
            configuration.getLock().unlock();
        }
        return pipelineExecutor;
    }

    /**
     * Return the associated PipelineExecutor for Data Pipeline
     *
     * @return the Data Pipeline Executor
     */
    public OrderedMemoryAwareThreadPoolExecutor getDataPipelineExecutor() {
        configuration.getLock().lock();
        try {
            if (pipelineDataExecutor == null) {
                // Memory limitation: no limit by channel, 1GB global, 100 ms of
                // timeout
                pipelineDataExecutor = new OrderedMemoryAwareThreadPoolExecutor(
                        configuration.SERVER_THREAD * 4,
                        configuration.maxGlobalMemory / 10,
                        configuration.maxGlobalMemory, 200,
                        TimeUnit.MILLISECONDS, Executors.defaultThreadFactory());
            }
        } finally {
            configuration.getLock().unlock();
        }
        return pipelineDataExecutor;
    }

    /**
     *
     * @return the ActiveBootstrap
     */
    public ClientBootstrap getActiveBootstrap() {
        return activeBootstrap;
    }

    /**
     * @return the commandChannelFactory
     */
    public ChannelFactory getCommandChannelFactory() {
        return commandChannelFactory;
    }

    /**
     * @return the commandChannelGroup
     */
    public ChannelGroup getCommandChannelGroup() {
        return commandChannelGroup;
    }

    /**
     * @return the dataPassiveChannelFactory
     */
    public ChannelFactory getDataPassiveChannelFactory() {
        return dataPassiveChannelFactory;
    }

    /**
     * @return the dataActiveChannelFactory
     */
    public ChannelFactory getDataActiveChannelFactory() {
        return dataActiveChannelFactory;
    }

    /**
     * @return the dataChannelGroup
     */
    public ChannelGroup getDataChannelGroup() {
        return dataChannelGroup;
    }

    /**
     * @return the objectSizeEstimator
     */
    public ObjectSizeEstimator getObjectSizeEstimator() {
        return objectSizeEstimator;
    }

    /**
     *
     * @return The TrafficCounterFactory
     */
    public GlobalTrafficShapingHandler getGlobalTrafficShapingHandler() {
        return globalTrafficShapingHandler;
    }

    /**
     *
     * @return a new ChannelTrafficShapingHandler
     */
    public ChannelTrafficShapingHandler newChannelTrafficShapingHandler() {
        return new ChannelTrafficShapingHandler(objectSizeEstimator,
                execTrafficCounter, configuration.getServerChannelWriteLimit(),
                configuration.getServerChannelReadLimit(), configuration
                        .getDelayLimit());
    }
}
