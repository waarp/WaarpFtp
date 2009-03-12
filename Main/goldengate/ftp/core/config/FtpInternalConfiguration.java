/**
 * Frederic Bregier LGPL 24 janv. 09 
 * FtpInternalConfiguration.java goldengate.ftp.core.config GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.config;

import goldengate.ftp.core.command.exception.Reply425Exception;
import goldengate.ftp.core.control.FtpPipelineFactory;
import goldengate.ftp.core.data.handler.FtpDataPipelineFactory;
import goldengate.ftp.core.data.handler.FtpPerformanceCounterFactory;
import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;
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
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.trafficshaping.PerformanceCounterFactory;
import org.jboss.netty.logging.InternalLoggerFactory;

/**
 * Internal configuration of the FTP server, related to Netty
 * @author frederic
 * goldengate.ftp.core.config FtpInternalConfiguration
 * 
 */
public class FtpInternalConfiguration {
	// Static values
	/**
	 * Internal Logger
	 */
	private static final FtpInternalLogger logger =
        FtpInternalLoggerFactory.getLogger(FtpInternalConfiguration.class);
	// Network Internals
	/**
	 *  Time elapse for retry in ms
	 */
	public static final long RETRYINMS = 10;
	/**
	 * Number of retry before error
	 */
	public static final int RETRYNB = 3;
	/**
	 * Hack to say Windows or Unix (USR1 not OK on Windows)
	 */
	public static final boolean ISUNIX = 
		(!System.getProperty("os.name").toLowerCase().startsWith("windows"));
	/**
	 * Default size for buffers (NIO)
	 */ 
	public static final int BUFFERSIZEDEFAULT = 0x10000; // 64K
	/**
	 * CR LF<br>
	 * A User Telnet MUST be able to send any of the forms: CR LF, CR NUL, and LF. 
	 * A User Telnet on an ASCII host SHOULD have a user-controllable mode to send 
	 * either CR LF or CR NUL when the user presses the "end-of-line" key, and 
	 * CR LF SHOULD be the default. 
	 */
	public static final String CRLF = "\r\n";
	/**
	 * CR NUL
	 */
	public static final String CRNUL = "\r\0";
	/**
	 * LF
	 */
	public static final String LF = "\n";

	// Dynamic values
	/**
	 * List of all Command Channels to enable the close call on them using Netty ChannelGroup
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
	private OrderedMemoryAwareThreadPoolExecutor pipelineExecutor = null;
	/**
	 * Bootstrap for Command server
	 */
	private ServerBootstrap serverBootstrap = null;
	/**
	 * List of all Data Channels to enable the close call on them using Netty ChannelGroup
	 */
	private ChannelGroup dataChannelGroup = null;
	/**
	 * ExecutorService Data Passive Boss
	 */
	private final ExecutorService execPassiveDataBoss = Executors.newCachedThreadPool();
	/**
	 * ExecutorService Data Passive Worker
	 */
	private final ExecutorService execPassiveDataWorker = Executors.newCachedThreadPool();
	/**
	 * ChannelFactory for Data Passive part
	 */
	private ChannelFactory dataPassiveChannelFactory = null;
	/**
	 * ExecutorService Data Active Boss
	 */
	private final ExecutorService execActiveDataBoss = Executors.newCachedThreadPool();
	/**
	 * ExecutorService Data Active Worker
	 */
	private final ExecutorService execActiveDataWorker = Executors.newCachedThreadPool();
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
	private OrderedMemoryAwareThreadPoolExecutor pipelineDataExecutor = null;
	/**
	 * ServerBootStrap for Active connections
	 */
	private ClientBootstrap activeBootstrap = null;
	/**
	 * ClientBootStrap for Passive connections
	 */
	private ServerBootstrap passiveBootstrap = null;
	
	/**
	 * ExecutorService for PerformanceCounter
	 */
	private final ExecutorService execPerformanceCounter = Executors.newCachedThreadPool();
	/**
	 * Global PerformanceCounterFactory (set from global configuration)
	 */
	private PerformanceCounterFactory globalPerformanceCounterFactory = null;
	/**
	 * 
	 * @author frederic
	 * goldengate.ftp.core.config BindAddress
	 *
	 */
	public class BindAddress {
		/**
		 * Main Channel
		 */
		public Channel parentChannel = null;
		/**
		 * Number of binded Data connections
		 */
		public int nbBind = 0;
		/**
		 * Constructor
		 * @param channel
		 */
		public BindAddress(Channel channel) {
			this.parentChannel = channel;
			this.nbBind = 0;
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
		InternalLoggerFactory.setDefaultFactory(FtpInternalLoggerFactory.getDefaultFactory());
		// Command
		this.commandChannelGroup = new DefaultChannelGroup(configuration.fromClass.getName());
			//ChannelGroupFactory.getGroup(configuration.fromClass);
		this.commandChannelFactory = new NioServerSocketChannelFactory(
				this.execBoss, this.execWorker, this.configuration.SERVER_THREAD);
		// Data
		this.dataChannelGroup = new DefaultChannelGroup(configuration.fromClass.getName()+".data");
			//ChannelGroupFactory.getGroup(configuration.fromClass.getName()+".data");
		this.dataPassiveChannelFactory = new NioServerSocketChannelFactory(
				this.execPassiveDataBoss, this.execPassiveDataWorker, this.configuration.SERVER_THREAD);
		this.dataActiveChannelFactory = new NioClientSocketChannelFactory(
				this.execActiveDataBoss, this.execActiveDataWorker, 4);
		
		// Passive Data Connections
		this.passiveBootstrap = new ServerBootstrap(this.dataPassiveChannelFactory);
		this.passiveBootstrap.setPipelineFactory(new FtpDataPipelineFactory(configuration.dataBusinessHandler,configuration));
		this.passiveBootstrap.setOption("connectTimeoutMillis", configuration.TIMEOUTCON);
		this.passiveBootstrap.setOption("reuseAddress", true);
		this.passiveBootstrap.setOption("tcpNoDelay", true);
		this.passiveBootstrap.setOption("child.connectTimeoutMillis", configuration.TIMEOUTCON);
		this.passiveBootstrap.setOption("child.tcpNoDelay", true);
		this.passiveBootstrap.setOption("child.keepAlive", true);
		this.passiveBootstrap.setOption("child.reuseAddress", true);
		// Active Data Connections
		this.activeBootstrap = new ClientBootstrap(this.dataActiveChannelFactory);
		this.activeBootstrap.setPipelineFactory(new FtpDataPipelineFactory(configuration.dataBusinessHandler,configuration));
		this.activeBootstrap.setOption("connectTimeoutMillis", configuration.TIMEOUTCON);
		this.activeBootstrap.setOption("reuseAddress", true);
		this.activeBootstrap.setOption("tcpNoDelay", true);
		this.activeBootstrap.setOption("child.connectTimeoutMillis", configuration.TIMEOUTCON);
		this.activeBootstrap.setOption("child.tcpNoDelay", true);
		this.activeBootstrap.setOption("child.keepAlive", true);
		this.activeBootstrap.setOption("child.reuseAddress", true);
		// Main Command server
		this.serverBootstrap = new ServerBootstrap(this.getCommandChannelFactory());
		this.serverBootstrap.setPipelineFactory(new FtpPipelineFactory(this.configuration.businessHandler, configuration));
		this.serverBootstrap.setOption("child.tcpNoDelay", true);
		this.serverBootstrap.setOption("child.keepAlive", true);
		this.serverBootstrap.setOption("child.reuseAddress", true);
		this.serverBootstrap.setOption("child.connectTimeoutMillis", configuration.TIMEOUTCON);
		this.serverBootstrap.setOption("tcpNoDelay", true);
		this.serverBootstrap.setOption("reuseAddress", true);
		this.serverBootstrap.setOption("connectTimeoutMillis", configuration.TIMEOUTCON);
		
		FtpChannelUtils.addCommandChannel(this.serverBootstrap.bind(new InetSocketAddress(configuration.getServerPort())), configuration);

		// Init signal handler
		FtpSignalHandler.initSignalHandler(configuration);
		// Factory for TrafficShapingHandler
		this.globalPerformanceCounterFactory = new FtpPerformanceCounterFactory(this.execPerformanceCounter,
				this.configuration.getServerChannelWriteLimit(),this.configuration.getServerChannelReadLimit(),
				this.configuration.getServerGlobalWriteLimit(),this.configuration.getServerGlobalReadLimit(),
				this.configuration.getDelayLimit());
	}
	/**
	 * Shutdown the PerformanceCounterFactory and its executorService
	 *
	 */
	public void shutdownPerformanceCounterFactory() {
		this.globalPerformanceCounterFactory.stopGlobalPerformanceCounter();
		this.execPerformanceCounter.shutdownNow();
	}
	/**
	 * Add a session from a couple of addresses
	 * @param remote
	 * @param local
	 * @param session
	 */
	public void setNewFtpSession(InetAddress remote, InetSocketAddress local, FtpSession session) {
		logger.debug("SetNewSession");
		this.ftpSessionReference.setNewFtpSession(remote, local, session);
	}
	/**
	 * Return and remove the FtpSession
	 * @param channel
	 * @return the FtpSession if it exists associated to this channel
	 */
	public FtpSession getFtpSession(Channel channel) {
		logger.debug("getSession");
		return this.ftpSessionReference.getFtpSession(channel);
	}
	/**
	 * Remove the FtpSession
	 * @param remote
	 * @param local
	 */
	public void delFtpSession(InetAddress remote, InetSocketAddress local) {
		logger.debug("delSession");
		this.ftpSessionReference.delFtpSession(remote, local);
	}
	/**
	 * Try to add a Passive Channel listening to the specified local address 
	 * @param address
	 * @throws Reply425Exception in case the channel cannot be opened
	 */
	public void bindPassive(InetSocketAddress address) throws Reply425Exception {
		this.configuration.getLock().lock();
		try {
			BindAddress bindAddress = this.hashBindPassiveDataConn.get(address);
			if (bindAddress == null) {
				logger.info("Bind really to {}",address);
				Channel parentChannel = null;
				try {
					parentChannel = this.passiveBootstrap.bind(address);
				} catch (ChannelException e) {
					logger.warn("Cannot open passive connection {}",e.getMessage());
					throw new Reply425Exception("Cannot open a Passive Connection");
				}
				bindAddress = new BindAddress(parentChannel);
				FtpChannelUtils.addDataChannel(parentChannel, configuration);
			}
			bindAddress.nbBind++;
			logger.info("Bind number to {} is {}",address,bindAddress.nbBind);
			this.hashBindPassiveDataConn.put(address, bindAddress);
		} finally {
			this.configuration.getLock().unlock();
		}
	}
	/**
	 * Try to unbind (closing the parent channel) the Passive Channel listening to the specified local address if the last one.
	 * It returns only when the underlying parent channel is closed if this was the last session that wants to open on this local address.
	 * @param address
	 */
	public void unbindPassive(InetSocketAddress address) {
		this.configuration.getLock().lock();
		try {
			BindAddress bindAddress = this.hashBindPassiveDataConn.get(address);
			if (bindAddress != null) {
				bindAddress.nbBind--;
				logger.info("Bind number to {} left is {}",address,bindAddress.nbBind);
				if (bindAddress.nbBind == 0) {
					bindAddress.parentChannel.close().awaitUninterruptibly();
					FtpChannelUtils.removeDataChannel(bindAddress.parentChannel, configuration);
					bindAddress.parentChannel = null;
					this.hashBindPassiveDataConn.remove(address);
				}
			} else {
				logger.warn("No Bind to {}",address);
			}
		} finally {
			this.configuration.getLock().unlock();
		}
	}
	/**
	 * 
	 * @return the number of Binded Passive Connections
	 */
	public int getNbBindedPassive() {
		return this.hashBindPassiveDataConn.size();
	}
	/**
	 * Return the associated PipelineExecutor for Command Pipeline
	 * @return the Command Pipeline Executor
	 */
	public OrderedMemoryAwareThreadPoolExecutor getPipelineExecutor() {
		this.configuration.getLock().lock();
		try {
			if (this.pipelineExecutor == null) {
				// Memory limitation: no limit by channel, 1GB global, 100 ms of timeout
				this.pipelineExecutor = 
					new OrderedMemoryAwareThreadPoolExecutor(configuration.SERVER_THREAD*4,
							this.configuration.maxGlobalMemory/40,
							this.configuration.maxGlobalMemory/4,
							100,TimeUnit.MILLISECONDS,
							Executors.defaultThreadFactory());
			}
		} finally {
			this.configuration.getLock().unlock();
		}
		return this.pipelineExecutor;
	}
	/**
	 * Return the associated PipelineExecutor for Data Pipeline
	 * @return the Data Pipeline Executor
	 */
	public OrderedMemoryAwareThreadPoolExecutor getDataPipelineExecutor() {
		this.configuration.getLock().lock();
		try {
			if (this.pipelineDataExecutor == null) {
				// Memory limitation: no limit by channel, 1GB global, 100 ms of timeout
				this.pipelineDataExecutor = 
					new OrderedMemoryAwareThreadPoolExecutor(configuration.SERVER_THREAD*4,
							this.configuration.maxGlobalMemory/10,
							this.configuration.maxGlobalMemory,
							100,TimeUnit.MILLISECONDS,
							Executors.defaultThreadFactory());
			}
		} finally {
			this.configuration.getLock().unlock();
		}
		return this.pipelineDataExecutor;
	}
	/**
	 * 
	 * @return the ActiveBootstrap
	 */
	public ClientBootstrap getActiveBootstrap() {
		return this.activeBootstrap;
	}
	/**
	 * @return the commandChannelFactory
	 */
	public ChannelFactory getCommandChannelFactory() {
		return this.commandChannelFactory;
	}
	/**
	 * @return the commandChannelGroup
	 */
	public ChannelGroup getCommandChannelGroup() {
		return this.commandChannelGroup;
	}
	/**
	 * @return the dataPassiveChannelFactory
	 */
	public ChannelFactory getDataPassiveChannelFactory() {
		return this.dataPassiveChannelFactory;
	}
	/**
	 * @return the dataActiveChannelFactory
	 */
	public ChannelFactory getDataActiveChannelFactory() {
		return this.dataActiveChannelFactory;
	}
	/**
	 * @return the dataChannelGroup
	 */
	public ChannelGroup getDataChannelGroup() {
		return this.dataChannelGroup;
	}
	/**
	 * 
	 * @return The PerformanceCounterFactory
	 */
	public PerformanceCounterFactory getPerformanceCounterFactory() {
		return this.globalPerformanceCounterFactory;
	}
}
