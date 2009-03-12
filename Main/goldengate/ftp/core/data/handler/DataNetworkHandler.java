/**
 * Frederic Bregier LGPL 10 janv. 09 
 * DataNetworkHandler.java goldengate.ftp.core.control GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.data.handler;

import goldengate.ftp.core.config.FtpConfiguration;
import goldengate.ftp.core.control.NetworkHandler;
import goldengate.ftp.core.data.FtpTransferControl;
import goldengate.ftp.core.exception.FtpFileEndOfTransferException;
import goldengate.ftp.core.exception.FtpFileTransferException;
import goldengate.ftp.core.exception.FtpInvalidArgumentException;
import goldengate.ftp.core.exception.FtpNoConnectionException;
import goldengate.ftp.core.exception.FtpNoFileException;
import goldengate.ftp.core.exception.FtpNoTransferException;
import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;
import goldengate.ftp.core.session.FtpSession;
import goldengate.ftp.core.utils.FtpChannelUtils;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 * Network handler for Data connections
 * @author frederic
 * goldengate.ftp.core.control DataNetworkHandler
 * 
 */
@ChannelPipelineCoverage("one")
public class DataNetworkHandler extends SimpleChannelHandler {
	/**
	 * Internal Logger
	 */
	private static final FtpInternalLogger logger =
        FtpInternalLoggerFactory.getLogger(DataNetworkHandler.class);

	/**
	 * Business Data Handler
	 */
	private DataBusinessHandler dataBusinessHandler = null;
	/**
	 * Configuration
	 */
	private final FtpConfiguration configuration;
	/**
	 * Internal store for the Session
	 */
	private FtpSession session = null;
	/**
	 * The associated Channel
	 */
	private Channel dataChannel = null;
	/**
	 * Pipeline
	 */
	private ChannelPipeline channelPipeline = null;
	/**
	 * True when the DataNetworkHandler is fully ready (to prevent action before ready)
	 */
	private boolean isReady = false;
	
	/**
	 * Constructor from DataBusinessHandler
	 * @param configuration
	 * @param handler
	 */
	public DataNetworkHandler(FtpConfiguration configuration, DataBusinessHandler handler) {
		super();
		this.configuration = configuration;
		this.dataBusinessHandler = handler;
		this.dataBusinessHandler.setDataNetworkHandler(this);
	}
	
	/**
	 * @return the dataBusinessHandler
	 * @throws FtpNoConnectionException 
	 */
	public DataBusinessHandler getDataBusinessHandler() throws FtpNoConnectionException {
		if (this.dataBusinessHandler == null) {
			throw new FtpNoConnectionException("No Data Connection active");
		}
		return dataBusinessHandler;
	}
	/**
	 * @return the session
	 */
	public FtpSession getFtpSession() {
		return session;
	}
	/**
	 * 
	 * @return the NetworkHandler associated with the control connection
	 */
	public NetworkHandler getNetworkHandler() {
		return this.session.getBusinessHandler().getNetworkHandler();
	}
	
	/**
	 * Run firstly executeChannelClosed.
	 * @throws Exception 
	 * @see org.jboss.netty.channel.SimpleChannelHandler#channelClosed(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		if (this.session != null) {
			logger.debug("Channel closed, about to set it down");
			this.session.getDataConn().getFtpTransferControl().setPreEndOfTransfer();
			this.session.getDataConn().unbindPassive();
			FtpChannelUtils.removeDataChannel(e.getChannel(), this.session.getConfiguration());
			try {
				this.getDataBusinessHandler().executeChannelClosed();
				// release file and other permanent objects
				this.getDataBusinessHandler().clean();
			} catch (FtpNoConnectionException e1) {
			}
			logger.debug("Channel closed inform closed");
			this.session.getDataConn().getFtpTransferControl().setClosedDataChannel();
			this.dataBusinessHandler = null;
			this.channelPipeline = null;
			this.dataChannel = null;
			logger.debug("Channel closed: finish");
		}
		super.channelClosed(ctx, e);
	}
	/**
	 * Initialiaze the Handler.
	 * @see org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		Channel channel = e.getChannel();
		// First get the ftpSession from inetaddresses
		this.session = this.configuration.getFtpSession(channel);
		if (this.session == null) {
			// Not found !!!
			logger.error("Session not found!");
			Channels.close(channel).awaitUninterruptibly();
			return;
		}
		logger.debug("Start DataNetwork");
		this.channelPipeline = ctx.getPipeline();
		this.dataChannel = channel;
		this.dataBusinessHandler.setFtpSession(this.getFtpSession());
		FtpChannelUtils.addDataChannel(channel, this.session.getConfiguration());
		if (isStillAlive()) {
			this.setCorrectCodec();
			this.session.getDataConn().getFtpTransferControl().setOpenedDataChannel(channel, this);
		} else {
			// Cannot continue
			this.session.getDataConn().getFtpTransferControl().setOpenedDataChannel(null, this);
			return;
		}
		this.isReady = true;
		logger.debug("End of Start DataNetwork");
	}
	/**
	 * Set the CODEC according to the mode. Must be called after each call of MODE, STRU or TYPE
	 */
	public void setCorrectCodec() {
		FtpDataModeCodec modeCodec = (FtpDataModeCodec) this.channelPipeline.get(FtpDataPipelineFactory.CODEC_MODE);
		FtpDataTypeCodec typeCodec = (FtpDataTypeCodec) this.channelPipeline.get(FtpDataPipelineFactory.CODEC_TYPE);
		FtpDataStructureCodec structureCodec = (FtpDataStructureCodec) this.channelPipeline.get(FtpDataPipelineFactory.CODEC_STRUCTURE);
		modeCodec.setMode(this.session.getDataConn().getMode());
		modeCodec.setStructure(this.session.getDataConn().getStructure());
		typeCodec.setFullType(this.session.getDataConn().getType(), this.session.getDataConn().getSubType());
		structureCodec.setStructure(this.session.getDataConn().getStructure());
		logger.debug("Set Correct Codec: {}",this.session.getDataConn());
	}
	/**
	 * Unlock the Mode Codec from openConnection of {@link FtpTransferControl}
	 *
	 */
	public void unlockModeCodec() {
		FtpDataModeCodec modeCodec = (FtpDataModeCodec) this.channelPipeline.get("MODE");
		modeCodec.setCodecReady();
	}
	/**
	 * Default exception task: close the current connection after calling exceptionLocalCaught.
	 * @see org.jboss.netty.channel.SimpleChannelHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		if (this.session == null) {
			e.getCause().printStackTrace();
			return;
		}
		Throwable e1 = e.getCause();
		if (e1 instanceof ConnectException) {
			ConnectException e2 = (ConnectException) e1;
			logger.warn("Connection impossible since {}",e2.getMessage());
		} else if (e1 instanceof ChannelException) {
			ChannelException e2 = (ChannelException) e1;
			logger.warn("Connection (example: timeout) impossible since {}",e2.getMessage());
		} else if (e1 instanceof ClosedChannelException) {
			logger.warn("Connection closed before end");
		} else if (e1 instanceof FtpInvalidArgumentException) {
			FtpInvalidArgumentException e2 = (FtpInvalidArgumentException) e1;
			logger.warn("Bad configuration in Codec in "+e2.getMessage(),e2);
		} else if (e1 instanceof NullPointerException) {
			NullPointerException e2 = (NullPointerException) e1;
			logger.warn("Null pointer Exception",e2);
			try {
				if (this.dataBusinessHandler != null) {
					this.dataBusinessHandler.exceptionLocalCaught(e);
					if (this.session.getDataConn() != null) {
						this.session.getDataConn().getFtpTransferControl().setTransferAbortedFromInternal(true);
					}
				}
			} catch (NullPointerException e3) {}
			return;
		} else if (e1 instanceof IOException) {
			IOException e2 = (IOException) e1;
			logger.warn("Connection aborted since {}",e2.getMessage());
		} else {
			logger.warn("Unexpected exception from downstream:",(Exception)e1);
		}
		if (this.dataBusinessHandler != null) {
			this.dataBusinessHandler.exceptionLocalCaught(e);
		}
		this.session.getDataConn().getFtpTransferControl().setTransferAbortedFromInternal(true);
	}
	
	/**
	 * To enable continues of Retrieve operation (prevent OOM)
	 * @see org.jboss.netty.channel.SimpleChannelHandler#channelInterestChanged(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelInterestChanged(ChannelHandlerContext arg0, ChannelStateEvent arg1) {
		int op = arg1.getChannel().getInterestOps();
		if ((op == Channel.OP_NONE) || (op == Channel.OP_READ)) {
			if (this.isReady) {
				this.session.getDataConn().getFtpTransferControl().runTrueRetrieve();
			}
		}
	}
	/**
	 * Act as needed according to the receive FtpDataBlock message
	 * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
	 */
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		if (isStillAlive()) {
			FtpDataBlock dataBlock = (FtpDataBlock) e.getMessage();
			try {
				this.session.getDataConn().getFtpTransferControl().
					getExecutingFtpTransfer().getFtpFile().receiveDataBlock(dataBlock);
			} catch (FtpNoFileException e1) {
				logger.debug("NoFile",e1);
				this.session.getDataConn().getFtpTransferControl().setTransferAbortedFromInternal(true);
				return;
			} catch (FtpNoTransferException e1) {
				logger.debug("NoTransfer",e1);
				this.session.getDataConn().getFtpTransferControl().setTransferAbortedFromInternal(true);
				return;
			} catch (FtpFileEndOfTransferException e1) {
				if (dataBlock.isEOF()) {
					this.session.getDataConn().getFtpTransferControl().setPreEndOfTransfer();
				}
			} catch (FtpFileTransferException e1) {
				logger.debug("TransferException",e1);
				this.session.getDataConn().getFtpTransferControl().setTransferAbortedFromInternal(true);
			}
		} else {
			// Shutdown
			this.session.getDataConn().getFtpTransferControl().setTransferAbortedFromInternal(true);
		}
	}
	/**
	 * Write a simple message (like LIST) and wait for it
	 * @param message
	 * @return True if the message is correctly written
	 */
	public boolean writeMessage(String message) {
		FtpDataBlock dataBlock = new FtpDataBlock();
		dataBlock.setEOF(true);
		ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(message.getBytes());
		dataBlock.setBlock(buffer);
		logger.debug("Message to be sent: {}",message);
		return Channels.write(this.dataChannel, dataBlock).awaitUninterruptibly().isSuccess();
	}
	/**
	 * If the service is going to shutdown, it sends back a 421 message to the connection
	 * @return True if the service is alive, else False if the system is going down 
	 */
	private boolean isStillAlive() {
		if (this.session.getConfiguration().isShutdown) {
			this.session.setExitErrorCode("Service is going down: disconnect");
			return false;
		}
		return true;
	}
}
