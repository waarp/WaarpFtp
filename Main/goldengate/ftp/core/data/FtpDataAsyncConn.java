/**
 * 
 */
package goldengate.ftp.core.data;

import goldengate.ftp.core.command.FtpArgumentCode;
import goldengate.ftp.core.command.FtpArgumentCode.TransferMode;
import goldengate.ftp.core.command.FtpArgumentCode.TransferStructure;
import goldengate.ftp.core.command.FtpArgumentCode.TransferType;
import goldengate.ftp.core.command.exception.Reply425Exception;
import goldengate.ftp.core.config.FtpConfiguration;
import goldengate.ftp.core.data.handler.DataNetworkHandler;
import goldengate.ftp.core.exception.FtpNoConnectionException;
import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;
import goldengate.ftp.core.session.FtpSession;
import goldengate.ftp.core.utils.FtpChannelUtils;
import goldengate.ftp.core.utils.bandwith.ThroughputMonitor;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.Channels;


/**
 * Main class that handles Data connection using asynchronous connection with Netty
 * @author fbregier
 *
 */
public class FtpDataAsyncConn {
	/**
	 * Internal Logger
	 */
	private static final FtpInternalLogger logger =
        FtpInternalLoggerFactory.getLogger(FtpDataAsyncConn.class);
	/**
	 * Session
	 */
	private final FtpSession session;
	/**
	 * Current Data Network Handler
	 */
	private DataNetworkHandler dataNetworkHandler = null;
	/**
	 * Data Channel with the client
	 */
	private Channel dataChannel = null;
	/**
	 * External address of the client (active)
	 */
	private InetSocketAddress remoteAddress = null;
	/**
	 *  Local listening address for the server (passive)
	 */
	private InetSocketAddress localAddress = null;
	/**
	 * Active: the connection is done from the Server to the Client on this remotePort
	 * Passive: not used 
	 */
	private int remotePort = -1;
	/**
	 * Active: the connection is done from the Server from this localPort to the Client
	 * Passive: the connection is done from the Client to the Server on this localPort 
	 */
	private int localPort = -1;
	/**
	 * Is the connection passive
	 */
	private boolean passiveMode = false;
	/**
	 * Is the server binded (active or passive, but mainly passive)
	 */
	private boolean isBind = false;
	/**
	 * The FtpTransferControl
	 */
	private final FtpTransferControl transferControl;
	/**
	 * Session Monitor (set from constructor)
	 */
	private ThroughputMonitor sessionMonitor = null;

	/**
	 * Current TransferType. Default ASCII
	 */
	private FtpArgumentCode.TransferType transferType = FtpArgumentCode.TransferType.ASCII;
	/**
	 * Current TransferSubType. Default NONPRINT
	 */
	private FtpArgumentCode.TransferSubType transferSubType = FtpArgumentCode.TransferSubType.NONPRINT;
	/**
	 * Current TransferStructure. Default FILE
	 */
	private FtpArgumentCode.TransferStructure transferStructure = FtpArgumentCode.TransferStructure.FILE;
	/**
	 * Current TransferMode. Default Stream
	 */
	private FtpArgumentCode.TransferMode transferMode = FtpArgumentCode.TransferMode.STREAM;

	/**
	 * Constructor for Active session by default
	 * @param session
	 */
	public FtpDataAsyncConn(FtpSession session) {
		this.session = session;
		this.dataChannel = null;
		this.remoteAddress =
			FtpChannelUtils.getRemoteInetSocketAddress(this.session.getControlChannel());
		this.remotePort = this.remoteAddress.getPort();
		this.setDefaultLocalPort();
		this.localAddress = 
			new InetSocketAddress(FtpChannelUtils.getLocalInetAddress(
					this.session.getControlChannel()),this.localPort);
		this.passiveMode = false;
		this.isBind = false;
		this.transferControl = new FtpTransferControl(session);
		this.sessionMonitor = 
			this.session.getConfiguration().getFtpInternalConfiguration().getNewSessionMonitor();
	}
	/**
	 * Clear the Data Connection
	 *
	 */
	public void clear() {
		this.unbindPassive();
		this.transferControl.clear();
		this.sessionMonitor.stopMonitoring();
		this.sessionMonitor = null;
		this.passiveMode = false;
		this.remotePort = -1;
		this.localPort = -1;
	}
	/**
	 * Set the local port to the default (20)
	 *
	 */
	private void setDefaultLocalPort() {
		this.setLocalPort(this.session.getConfiguration().getServerPort()-1);// Default L-1
	}
	/**
	 * Set the Local Port (Active or Passive)
	 * @param localPort
	 */
	public void setLocalPort(int localPort) {
		this.localPort = localPort; 
	}
	/**
	 * @return the local address
	 */
	public InetSocketAddress getLocalAddress() {
		return localAddress;
	}
	/**
	 * @return the remote address
	 */
	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}
	/**
	 * 
	 * @return the session Monitor
	 */
	public ThroughputMonitor getSessionMonitor() {
		return this.sessionMonitor;
	}
	/**
	 * @return the remotePort
	 */
	public int getRemotePort() {
		return remotePort;
	}
	/**
	 * @return the localPort
	 */
	public int getLocalPort() {
		return localPort;
	}
	/**
	 * Change to active connection (don't change localPort)
	 * @param address remote address
	 */
	public void setActive(InetSocketAddress address) {
		this.unbindPassive();
		this.remoteAddress = address;
		this.passiveMode = false;
		this.isBind = false;
		this.remotePort = this.remoteAddress.getPort();
	}
	/**
	 * Change to passive connection (all necessaries informations like local port should have been set)
	 */
	public void setPassive() {
		this.unbindPassive();
		this.localAddress = new InetSocketAddress(
				FtpChannelUtils.getLocalInetAddress(this.session.getControlChannel()),
				this.localPort);
		this.passiveMode = true;
		this.isBind = false;
		logger.debug("Passive prepared");
	}
	
	/**
	 * @return the passiveMode
	 */
	public boolean isPassiveMode() {
		return passiveMode;
	}
	/**
	 * 
	 * @return True if the connection is bind (active = connected, passive = not necesseraly connected)
	 */
	public boolean isBind() {
		return this.isBind;
	}
	/**
	 * Is the Data dataChannel connected
	 * @return True if the dataChannel is connected
	 */
	public boolean isConnected() {
		return ((this.dataChannel != null) && (this.dataChannel.isConnected()));
	}
	/**
	 * @return the transferMode
	 */
	public FtpArgumentCode.TransferMode getMode() {
		return this.transferMode;
	}
	/**
	 * @param transferMode the transferMode to set
	 */
	public void setMode(FtpArgumentCode.TransferMode transferMode) {
		this.transferMode = transferMode;
		this.setCorrectCodec();
	}
	/**
	 * @return the transferStructure
	 */
	public FtpArgumentCode.TransferStructure getStructure() {
		return this.transferStructure;
	}
	/**
	 * @param transferStructure the transferStructure to set
	 */
	public void setStructure(FtpArgumentCode.TransferStructure transferStructure) {
		this.transferStructure = transferStructure;
		this.setCorrectCodec();
	}
	/**
	 * @return the transferSubType
	 */
	public FtpArgumentCode.TransferSubType getSubType() {
		return this.transferSubType;
	}
	/**
	 * @param transferSubType the transferSubType to set
	 */
	public void setSubType(FtpArgumentCode.TransferSubType transferSubType) {
		this.transferSubType = transferSubType;
		this.setCorrectCodec();
	}
	/**
	 * @return the transferType
	 */
	public FtpArgumentCode.TransferType getType() {
		return this.transferType;
	}
	/**
	 * @param transferType the transferType to set
	 */
	public void setType(FtpArgumentCode.TransferType transferType) {
		this.transferType = transferType;
		this.setCorrectCodec();
	}
	/**
	 * 
	 * @return True if the current mode for data connection is File + (Stream or Block) + (Ascii or Image)
	 */
	public boolean isFileStreamBlockAsciiImage() {
		return ((this.transferStructure == TransferStructure.FILE) && 
				((this.transferMode == TransferMode.STREAM) || (this.transferMode == TransferMode.BLOCK)) &&
				((this.transferType == TransferType.ASCII) || (this.transferType == TransferType.IMAGE)));
	}
	/**
	 * 
	 * @return True if the current mode for data connection is Stream
	 */
	public boolean isStreamFile() {
		return ((this.transferMode == TransferMode.STREAM) && 
				(this.transferStructure == TransferStructure.FILE));
	}
	/**
	 * This function must be called after any changes of parameters,
	 * ie after MODE, STRU, TYPE
	 *
	 */
	private void setCorrectCodec() {
		try {
			this.getDataNetworkHandler().setCorrectCodec();
		} catch (FtpNoConnectionException e) {
		}				
	}
	/**
	 * Unbind passive connection when close the Data Channel (from channelClosed())
	 *
	 */
	public void unbindPassive() {
		if (this.isBind && this.passiveMode) {
			this.isBind = false;
			if ((this.dataChannel != null) && (this.dataChannel.isConnected())) {
				logger.debug("PASSIVE MODE CLOSE");
				Channels.close(this.dataChannel).awaitUninterruptibly();
			}
			logger.debug("Passive mode unbind");
			this.session.getConfiguration().
				getFtpInternalConfiguration().unbindPassive(getLocalAddress());
			// Previous mode was Passive so remove the current configuration if any
			InetSocketAddress local = this.getLocalAddress();
			InetAddress remote = this.remoteAddress.getAddress();
			this.session.getConfiguration().delFtpSession(remote, local);
		}
		this.dataChannel = null;
		this.dataNetworkHandler = null;		
	}
	/**
	 * Initialize the socket from Server side (only used in Passive)
	 * @return True if OK
	 * @throws Reply425Exception 
	 */
	public boolean initPassiveConnection() throws Reply425Exception {
		this.unbindPassive();
		if (this.passiveMode) {
			// Connection is enable but the client will do the real connection
			this.session.getConfiguration().
				getFtpInternalConfiguration().bindPassive(getLocalAddress());
			logger.debug("Passive mode ready");
			this.isBind = true;
			return true;
		}
		// Connection is already prepared
		return true;
	}
	/**
	 * Return the current Data Channel
	 * @return the current Data Channel
	 * @throws FtpNoConnectionException 
	 */
	public Channel getCurrentDataChannel() throws FtpNoConnectionException {
		if (this.dataChannel == null) {
			throw new FtpNoConnectionException("No Data Connection active");
		}
		return this.dataChannel;
	}
	/**
	 * 
	 * @return the DataNetworkHandler
	 * @throws FtpNoConnectionException 
	 */
	public DataNetworkHandler getDataNetworkHandler() throws FtpNoConnectionException {
		if (this.dataNetworkHandler == null) {
			throw new FtpNoConnectionException("No Data Connection active");
		}
		return this.dataNetworkHandler;
	}
	/**
	 * 
	 * @param dataNetworkHandler the {@link DataNetworkHandler} to set
	 */
	public void setDataNetworkHandler(DataNetworkHandler dataNetworkHandler) {
		this.dataNetworkHandler = dataNetworkHandler;
	}
	/**
	 * 
	 * @param configuration
	 * @return a new Passive Port
	 */
	public static int getNewPassivePort(FtpConfiguration configuration) {
		return configuration.getNextRangePort();
	}
	/**
	 * @return The current status in String of the different parameters
	 */
	public String getStatus() {
		StringBuilder builder = new StringBuilder("Data connection: ");
		builder.append((isConnected()?"connected ":"not connected "));
		builder.append((isBind()?"bind ":"not bind "));
		builder.append((isPassiveMode()?"passive mode":"active mode"));
		builder.append('\n');
		builder.append("Mode: ");
		builder.append(this.transferMode.name());
		builder.append('\n');
		builder.append("Structure: ");
		builder.append(this.transferStructure.name());
		builder.append('\n');
		builder.append("Type: ");
		builder.append(this.transferType.name());
		builder.append(' ');
		builder.append(this.transferSubType.name());
		return builder.toString();
	}
	/**
	 * 
	 */
	public String toString() {
		return getStatus().replace('\n', ' ');
	}
	/**
	 * 
	 * @return the FtpTransferControl
	 */
	public FtpTransferControl getFtpTransferControl() {
		return this.transferControl;
	}
	/**
	 * Get the Data Channel from the channelConnected method
	 * @return the new Data Channel
	 * @throws InterruptedException
	 * @throws Reply425Exception
	 */
	public Channel waitForOpenedDataChannel() throws InterruptedException, Reply425Exception {
		this.dataChannel = this.transferControl.waitForOpenedDataChannel();
		if (this.dataChannel == null) {
			String curmode = null;
			if (this.isPassiveMode()) {
				curmode = "passive";
			} else {
				curmode = "active";
			}
			logger.debug("Connection impossible in {} mode", curmode);
			// Cannot open connection
			throw new Reply425Exception("Cannot open "+curmode+" data connection");
		}
		this.isBind = true;
		return this.dataChannel;
	}
}
