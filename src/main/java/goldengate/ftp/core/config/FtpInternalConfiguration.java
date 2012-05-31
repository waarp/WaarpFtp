/**
   This file is part of GoldenGate Project (named also GoldenGate or GG).

   Copyright 2009, Frederic Bregier, and individual contributors by the @author
   tags. See the COPYRIGHT.txt in the distribution for a full listing of
   individual contributors.

   All GoldenGate Project is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   GoldenGate is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with GoldenGate .  If not, see <http://www.gnu.org/licenses/>.
 */
package goldengate.ftp.core.config;

import goldengate.common.command.exception.Reply425Exception;
import goldengate.common.file.DataBlockSizeEstimator;
import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;
import goldengate.common.utility.GgThreadFactory;
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
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.ObjectSizeEstimator;
import org.jboss.netty.util.Timer;

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
     * Time elapse for WRITE OR CLOSE WAIT elaps in ms
     */
    public static final long WAITFORNETOP = 1000;
    /**
     * Hack to say Windows or Unix (USR1 not OK on Windows)
     */
    public static Boolean ISUNIX = null;

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
     * Timer for TrafficCounter
     */
    private Timer timerTrafficCounter = 
        new HashedWheelTimer(new GgThreadFactory("TimerTrafficFtp"), 10, TimeUnit.MILLISECONDS, 1024);
    
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
        public int nbBind = 0;

        /**
         * Constructor
         *
         * @param channel
         */
        public BindAddress(Channel channel) {
            parent = channel;
            nbBind = 0;
        }
    }

    /**
     * List of already bind local addresses for Passive connections
     */
    private final ConcurrentHashMap<InetSocketAddress, BindAddress> hashBindPassiveDataConn =
        new ConcurrentHashMap<InetSocketAddress, BindAddress>();

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
        ISUNIX = !System.getProperty("os.name").toLowerCase().startsWith("win");
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
        commandChannelFactory = new NioServerSocketChannelFactory(execBoss,
                execWorker, configuration.SERVER_THREAD);
        // Data
        dataChannelGroup = new DefaultChannelGroup(configuration.fromClass
                .getName() +
                ".data");
        dataPassiveChannelFactory = new NioServerSocketChannelFactory(
                execPassiveDataBoss, execPassiveDataWorker,
                configuration.SERVER_THREAD);
        dataActiveChannelFactory = new NioClientSocketChannelFactory(
                execActiveDataBoss, execActiveDataWorker, configuration.CLIENT_THREAD);

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
                objectSizeEstimator, timerTrafficCounter, configuration
                        .getServerGlobalWriteLimit(), configuration
                        .getServerGlobalReadLimit(), configuration
                        .getDelayLimit());
        pipelineExecutor = new OrderedMemoryAwareThreadPoolExecutor(
                configuration.CLIENT_THREAD,
                configuration.maxGlobalMemory / 40,
                configuration.maxGlobalMemory / 4, 1000,
                TimeUnit.MILLISECONDS, objectSizeEstimator,
                new GgThreadFactory("CommandExecutor"));
        pipelineDataExecutor = new OrderedMemoryAwareThreadPoolExecutor(
                configuration.CLIENT_THREAD,
                configuration.maxGlobalMemory / 10,
                configuration.maxGlobalMemory, 1000,
                TimeUnit.MILLISECONDS, objectSizeEstimator,
                new GgThreadFactory("DataExecutor"));
    }
    /**
     * 
     * @return an ExecutorService
     */
    public ExecutorService getWorker() {
        return execWorker;
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
     * 
     * @return the number of Active Sessions
     */
    public int getNumberSessions() {
        return ftpSessionReference.sessionsNumber();
    }
    /**
     * Try to add a Passive Channel listening to the specified local address
     *
     * @param address
     * @throws Reply425Exception
     *             in case the channel cannot be opened
     */
    public void bindPassive(InetSocketAddress address) throws Reply425Exception {
        configuration.bindLock();
        try {
            BindAddress bindAddress = hashBindPassiveDataConn.get(address);
            if (bindAddress == null) {
                logger.debug("Bind really to {}", address);
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
                hashBindPassiveDataConn.put(address, bindAddress);
            }
            bindAddress.nbBind++;
            logger.debug("Bind number to {} is {}", address, bindAddress.nbBind);
        } finally {
            configuration.bindUnlock();
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
        configuration.bindLock();
        try {
            BindAddress bindAddress = hashBindPassiveDataConn.get(address);
            if (bindAddress != null) {
                bindAddress.nbBind--;
                logger.debug("Bind number to {} left is {}", address, bindAddress.nbBind);
                if (bindAddress.nbBind == 0) {
                    Channels.close(bindAddress.parent);
                    hashBindPassiveDataConn.remove(address);
                }
            } else {
                logger.warn("No Bind to {}", address);
            }
        } finally {
            configuration.bindUnlock();
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
        return pipelineExecutor;
    }

    /**
     * Return the associated PipelineExecutor for Data Pipeline
     *
     * @return the Data Pipeline Executor
     */
    public OrderedMemoryAwareThreadPoolExecutor getDataPipelineExecutor() {
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
        if (configuration.getServerChannelWriteLimit() == 0 &&
                configuration.getServerChannelReadLimit() == 0) {
            return null;
        }
        return new ChannelTrafficShapingHandler(objectSizeEstimator,
                timerTrafficCounter, configuration.getServerChannelWriteLimit(),
                configuration.getServerChannelReadLimit(), configuration
                        .getDelayLimit());
    }
    
    public void releaseResources() {
        execBoss.shutdown();
        execWorker.shutdown();
        execPassiveDataBoss.shutdown();
        execPassiveDataWorker.shutdown();
        execActiveDataBoss.shutdown();
        execActiveDataWorker.shutdown();
        timerTrafficCounter.stop();
        activeBootstrap.releaseExternalResources();
        passiveBootstrap.releaseExternalResources();
        serverBootstrap.releaseExternalResources();
    }
}
