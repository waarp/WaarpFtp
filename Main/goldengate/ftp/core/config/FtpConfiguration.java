/**
 * Frederic Bregier LGPL 10 janv. 09 
 * FtpConfiguration.java goldengate.ftp.core.config GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.config;

import goldengate.ftp.core.exception.FtpUnknownFieldException;
import goldengate.ftp.core.session.FtpSession;
import goldengate.ftp.core.utils.bandwith.ThroughputMonitor;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.channel.Channel;

/**
 * Abstract class for configuration
 * @author frederic
 * goldengate.ftp.core.config FtpConfiguration
 * 
 */
public abstract class FtpConfiguration {
	// FTP Configuration: Externals
	/**
	 * SERVER PORT
	 */
	private static final String SERVER_PORT = "FTP_SERVER_PORT";
	/**
	 * Base Directory
	 */
	private static final String BASE_DIRECTORY = "FTP_BASE_DIRECTORY";
	/**
	 * PASSWORD for SHUTDOWN
	 */
	private static final String FTP_PASSWORD = "FTP_PASSWORD";

	/**
	 * Global reference of configuration based on the main class (or web class for web support)
	 */
	protected static final ConcurrentHashMap<Class, FtpConfiguration> hashmapConfiguration = 
		new ConcurrentHashMap<Class, FtpConfiguration>();
	// END OF STATIC VALUES
	/**
	 * Internal configuration
	 */
	private final FtpInternalConfiguration internalConfiguration;
	/**
	 * True if the service is going to shutdown
	 */
	public boolean isShutdown = false;
	/**
	 * Default number of threads in pool for Server.
	 * The default value is for client for Executor in the Pipeline for Business logic. 
	 * Server will change this value on startup if not set.
	 */
	public int SERVER_THREAD = 8;
	/**
	 * Which class owns this configuration
	 */
	public Class fromClass = null;
	/**
	 * Which class will be used for DataBusinessHandler
	 */
	public Class dataBusinessHandler = null;
	/**
	 * Which class will be used for BusinessHandler
	 */
	public Class businessHandler = null;
	/**
	 * Internal Lock
	 */
	private final ReentrantLock lock = new ReentrantLock();
	/**
	 *  Nb of milliseconds after connexion is in timeout
	 */
	public int TIMEOUTCON = 30000;
	/**
	 *  Size by default of block size for receive/sending files.
	 *  Should be a multiple of 8192 (maximum = 64K due to block limitation to 2 bytes)
	 */
	public int BLOCKSIZE = 0x10000; // 64K
	/**
	 * Limit in Write byte/s to apply globally to the FTP Server
	 */
	protected long serverGlobalWriteLimit = ThroughputMonitor.DEFAULT_GLOBAL_LIMIT;
	/**
	 * Limit in Read byte/s to apply globally to the FTP Server
	 */
	protected long serverGlobalReadLimit = ThroughputMonitor.DEFAULT_GLOBAL_LIMIT;
	/**
	 * Limit in Write byte/s to apply by session to the FTP Server
	 */
	protected long serverSessionWriteLimit = ThroughputMonitor.DEFAULT_SESSION_LIMIT;
	/**
	 * Limit in Read byte/s to apply by session to the FTP Server
	 */
	protected long serverSessionReadLimit = ThroughputMonitor.DEFAULT_SESSION_LIMIT;
	/**
	 * Delay in ms between two checks
	 */
	protected long delayLimit = ThroughputMonitor.DEFAULT_DELAY;
	/**
	 * Should the file be deleted when the transfer is aborted on STOR like commands
	 */
	public boolean deleteOnAbort = false;
	
	/**
	 * General Configuration Object
	 */
	private final HashMap<String, Object> properties = new HashMap<String, Object>();
	/**
	 * Simple constructor
	 * @param classtype Owner
	 * @param businessHandler class that will be used for BusinessHandler
	 * @param dataBusinessHandler class that will be used for DataBusinessHandler
	 */
	public FtpConfiguration(Class classtype, Class businessHandler, Class dataBusinessHandler) {
		this.fromClass = classtype;
		this.dataBusinessHandler = dataBusinessHandler;
		this.businessHandler = businessHandler;
		this.internalConfiguration = new FtpInternalConfiguration(this);
	}
	/**
	 * 
	 * @param key
	 * @return The String property associated to the key
	 * @throws FtpUnknownFieldException
	 */
	public String getStringProperty(String key) throws FtpUnknownFieldException {
		String s = (String) properties.get(key);
		if (s == null) {
			throw new FtpUnknownFieldException("Property has no value: "+key);
		}
		return s;
	}
	/**
	 * 
	 * @param key
	 * @return The Integer property associated to the key
	 * @throws FtpUnknownFieldException
	 */
	public int getIntProperty(String key) throws FtpUnknownFieldException {
		Integer i = (Integer) properties.get(key);
		if (i == null) {
			throw new FtpUnknownFieldException("Property has no value: "+key);
		}
		return i;
	}
	/**
	 * 
	 * @param key
	 * @return The File property associated to the key
	 * @throws FtpUnknownFieldException
	 */
	public File getFileProperty(String key) throws FtpUnknownFieldException {
		File f = (File) properties.get(key);
		if (f == null) {
			throw new FtpUnknownFieldException("Property has no value: "+key);
		}
		return f;
	}
	/**
	 * 
	 * @param key
	 * @return The Object property associated to the key
	 * @throws FtpUnknownFieldException
	 */
	public Object getProperty(String key) throws FtpUnknownFieldException {
		Object o = properties.get(key);
		if (o == null) {
			throw new FtpUnknownFieldException("Property has no value: "+key);
		}
		return o;
	}
	/**
	 * 
	 * @return the TCP Port to listen in the Ftp Server
	 */
	public int getServerPort() {
		try {
			return this.getIntProperty(SERVER_PORT);
		} catch (FtpUnknownFieldException e) {
			return 21; // Default
		}
	}
	/**
	 * 
	 * @return the limit in Write byte/s to apply globally to the Ftp Server
	 */
	public long getServerGlobalWriteLimit() {
		return this.serverGlobalWriteLimit;
	}
	/**
	 * 
	 * @return the limit in Write byte/s to apply for each session to the Ftp Server
	 */
	public long getServerSessionWriteLimit() {
		return this.serverSessionWriteLimit;
	}
	/**
	 * 
	 * @return the limit in Read byte/s to apply globally to the Ftp Server
	 */
	public long getServerGlobalReadLimit() {
		return this.serverGlobalReadLimit;
	}
	/**
	 * 
	 * @return the limit in Read byte/s to apply for each session to the Ftp Server
	 */
	public long getServerSessionReadLimit() {
		return this.serverSessionReadLimit;
	}
	/**
	 * @return the delayLimit to apply between two check
	 */
	public long getDelayLimit() {
		return delayLimit;
	}
	/**
	 * Check the password for Shutdown
	 * @param password
	 * @return True if the password is OK
	 */
	public boolean checkPassword(String password) {
		String serverpassword;
		try {
			serverpassword = this.getStringProperty(FTP_PASSWORD);
		} catch (FtpUnknownFieldException e) {
			return false;
		}
		return serverpassword.equals(password);
	}
	/**
	 * Return the next available port for passive connections.
	 * @return the next available Port for Passive connections
	 */
	public abstract int getNextRangePort();
	/**
	 * 
	 * @return the Base Directory of this Ftp Server
	 */
	public String getBaseDirectory() {
		try {
			return this.getStringProperty(BASE_DIRECTORY);
		} catch (FtpUnknownFieldException e) {
			return null;
		}
	}
	/**
	 * 
	 * @param key
	 * @param s
	 */
	public void setStringProperty(String key, String s) {
		properties.put(key, s);
	}
	/**
	 * 
	 * @param key
	 * @param i
	 */
	public void setIntProperty(String key, int i) {
		properties.put(key, Integer.valueOf(i));
	}
	/**
	 * 
	 * @param key
	 * @param f
	 */
	public void setFileProperty(String key, File f) {
		properties.put(key, f);
	}
	/**
	 * 
	 * @param key
	 * @param o
	 */
	public void setProperty(String key, Object o) {
		properties.put(key, o);
	}
	/**
	 * @param port the new port
	 */
	public void setServerPort(int port) {
		this.setIntProperty(SERVER_PORT, port);
	}
	/**
	 * @param dir the new base directory
	 */
	public void setBaseDirectory(String dir) {
		this.setStringProperty(BASE_DIRECTORY, dir);
	}
	/**
	 * @param password the new password for shutdown
	 */
	public void setPassword(String password) {
		this.setStringProperty(FTP_PASSWORD, password);
	}
	
	/**
	 * @return the dataBusinessHandler
	 */
	public Class getDataBusinessHandler() {
		return this.dataBusinessHandler;
	}
	/**
	 * Initi internal configuration
	 *
	 */
	public void serverStartup() {
		this.internalConfiguration.serverStartup();
		this.internalConfiguration.getGlobalMonitor().changeConfiguration(null,
				this.serverGlobalWriteLimit, this.serverGlobalReadLimit, this.delayLimit);
		this.internalConfiguration.getGlobalMonitor().startMonitoring();
	}
	/**
	 * Compute number of threads for both client and server
	 * from the real number of available processors (double + 1) if the value
	 * is less than 64 threads.
	 *
	 */
	public void computeNbThreads() {
		int nb = Runtime.getRuntime().availableProcessors()*2+1;
		if (SERVER_THREAD < nb) {
	        SERVER_THREAD = nb;
		}
    }
	/**
	 * 
	 * @return the lock on configuration
	 */
	public Lock getLock() {
		return this.lock;
	}
	/**
	 * 
	 * @return the FtpInternalConfiguration
	 */
	public FtpInternalConfiguration getFtpInternalConfiguration() {
		return this.internalConfiguration;
	}
	/**
	 * Add a session from a couple of addresses
	 * @param remote
	 * @param local
	 * @param session
	 */
	public void setNewFtpSession(InetAddress remote, InetSocketAddress local, FtpSession session) {
		this.internalConfiguration.setNewFtpSession(remote, local, session);
	}
	/**
	 * Return and remove the FtpSession
	 * @param channel
	 * @return the FtpSession if it exists associated to this channel
	 */
	public FtpSession getFtpSession(Channel channel) {
		return this.internalConfiguration.getFtpSession(channel);
	}
	/**
	 * Remove the FtpSession
	 * @param remote
	 * @param local
	 */
	public void delFtpSession(InetAddress remote, InetSocketAddress local) {
		this.internalConfiguration.delFtpSession(remote, local);
	}
}
