/**
 * Frederic Bregier LGPL 10 janv. 09 
 * NetworkHandler.java goldengate.ftp.core.control GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.control;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpCommandCode;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.command.internal.ConnectionCommand;
import goldengate.ftp.core.command.internal.IncorrectCommand;
import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;
import goldengate.ftp.core.session.FtpSession;
import goldengate.ftp.core.utils.FtpChannelUtils;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 * Main Network Handler (Control part) implmenting RFC 959, 775, 2389, 2428, 3659 and supports XCRC and XMD5 commands.
 * @author frederic
 * goldengate.ftp.core.control NetworkHandler
 * 
 */
@ChannelPipelineCoverage("one")
public class NetworkHandler extends SimpleChannelHandler {
	/**
	 * Internal Logger
	 */
	private static final FtpInternalLogger logger =
        FtpInternalLoggerFactory.getLogger(NetworkHandler.class);

	/**
	 * Business Handler
	 */
	private BusinessHandler businessHandler = null;
	/**
	 * Internal store for the Session
	 */
	private FtpSession session = null;
	/**
	 * The associated Channel
	 */
	private Channel controlChannel = null;
	/**
	 * Constructor from session
	 * @param session
	 */
	public NetworkHandler(FtpSession session) {
		this.session = session;
		this.businessHandler = session.getBusinessHandler();
		this.businessHandler.setNetworkHandler(this);
	}

	/**
	 * @return the businessHandler
	 */
	public BusinessHandler getBusinessHandler() {
		return businessHandler;
	}
	/**
	 * @return the session
	 */
	public FtpSession getFtpSession() {
		return session;
	}
	/**
	 * 
	 * @return the Control Channel
	 */
	public Channel getControlChannel() {
		return this.controlChannel;
	}
	/**
	 * Run firstly executeChannelClosed.
	 * @see org.jboss.netty.channel.SimpleChannelHandler#channelClosed(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		// Wait for any command running before closing (bad client sometimes don't wait for answer)
		int limit = 100;
		while (this.session.getDataConn().getFtpTransferControl().isFtpTransferExecuting()) {
			Thread.sleep(10);
			limit--;
			if (limit <= 0) {
				logger.warn("Waiting for transfer finished but 1s is not enough");
				break; // wait at most 1s
			}
		}
		this.businessHandler.executeChannelClosed();
		// release file and other permanent objects
		FtpChannelUtils.removeCommandChannel(e.getChannel(), this.session.getConfiguration());
		logger.debug("Channel closed: {}",this.session);
		this.businessHandler.clean();
		this.session.clean();
		this.businessHandler = null;
		//this.controlChannel = null; // to prevent when bad client goes wrong
		this.session = null;
		super.channelClosed(ctx, e);
	}
	/**
	 * Initialiaze the Handler.
	 * @see org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		Channel channel = e.getChannel();
		this.controlChannel = channel;
		this.session.setControlConnected();
		FtpChannelUtils.addCommandChannel(channel, this.session.getConfiguration());
		if (isStillAlive()) {
			// Make the first execution ready
			AbstractCommand command = new ConnectionCommand(this.getFtpSession());
			this.session.setNextCommand(command);
			// This command can change the next Command
			this.businessHandler.executeChannelConnected(channel);
			// Answer ready to continue from first command = Connection
			this.messageRunAnswer();
			this.getFtpSession().setReady(true);
		}
	}
	/**
	 * If the service is going to shutdown, it sends back a 421 message to the connection
	 * @return True if the service is alive, else False if the system is going down 
	 */
	private boolean isStillAlive() {
		if (this.session.getConfiguration().isShutdown) {
			this.session.setExitErrorCode("Service is going down: disconnect");
			this.writeFinalAnswer();
			return false;
		}
		return true;
	}
	/**
	 * Default exception task: close the current connection after calling exceptionLocalCaught and writing if possible the current replyCode.
	 * @see org.jboss.netty.channel.SimpleChannelHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		Throwable e1 = e.getCause();
		Channel channel = e.getChannel();
		if (this.session == null) {
			// should not be
			logger.warn("NO SESSION");
			return;
		}
		if (e1 instanceof ConnectException) {
			ConnectException e2 = (ConnectException) e1;
			logger.warn("Connection impossible since {} with Channel {}",e2.getMessage(),e.getChannel());
		} else if (e1 instanceof ChannelException) {
			ChannelException e2 = (ChannelException) e1;
			logger.warn("Connection (example: timeout) impossible since {} with Channel {}",e2.getMessage(),e.getChannel());
		} else if (e1 instanceof ClosedChannelException) {
			logger.warn("Connection closed before end");
		}else if (e1 instanceof FtpCommandAbstractException) {
			// FTP Exception: not close if not necessary
			FtpCommandAbstractException e2 = (FtpCommandAbstractException) e1;
			logger.warn("Command Error Reply", e2);
			this.session.setReplyCode(e2);
			this.businessHandler.afterRunCommandKo(e2);
			if (channel.isConnected()) {
				this.writeFinalAnswer();
			}
			return;
		} else if (e1 instanceof NullPointerException) {
			NullPointerException e2 = (NullPointerException) e1;
			logger.warn("Null pointer Exception",e2);
			try {
				if (this.session != null) {
					this.session.setExitErrorCode("Internal error: disconnect");
					if (this.businessHandler != null) {
						if (this.session.getDataConn() != null) {
							this.businessHandler.exceptionLocalCaught(e);
							if (channel.isConnected()) {
								this.writeFinalAnswer();
							}
						}
					}
				}
			} catch (NullPointerException e3) {}
			return;
		} else if (e1 instanceof IOException) {
			IOException e2 = (IOException) e1;
			logger.warn("Connection aborted with since {} with Channel {}",e2.getMessage(),e.getChannel());
		} else {
			logger.warn("Unexpected exception from downstream"+
					" Ref Channel: "+e.getChannel().toString(),(Exception)e1);
		}
		this.session.setExitErrorCode("Internal error: disconnect");
		this.businessHandler.exceptionLocalCaught(e);
		if (channel.isConnected()) {
			this.writeFinalAnswer();
		}
	}
	/**
	 * Simply call messageRun with the received message
	 * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
	 */
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		if (isStillAlive()) {
			// First wait for the initialization to be fully done
			while (! this.session.isReady()) {
				try {
					Thread.sleep(5);
				} catch (InterruptedException e1) {
				}
			}
			String message = (String) e.getMessage();
			AbstractCommand command = FtpCommandCode.getFromLine(this.getFtpSession(), message);
			// Default message
			this.session.setReplyCode(FtpReplyCode.REPLY_200_COMMAND_OKAY, null);
			logger.info("RECVMSG: {} CMD: {}",message,command.getCommand());
			// First check if the command is an ABORT, QUIT or STAT
			if (! FtpCommandCode.isSpecialCommand(command.getCode())) {
				// Now check if a transfer is on its way: illegal to have at same time two commands (except ABORT)
				if (this.session.getDataConn().getFtpTransferControl().isFtpTransferExecuting()) {
					this.session.setReplyCode(FtpReplyCode.REPLY_503_BAD_SEQUENCE_OF_COMMANDS,
							"Previous transfer command is not finished yet");
					this.writeIntermediateAnswer();
					return;
				}
			}
			if (this.session.getCurrentCommand().isNextCommandValid(command)) {
				this.session.setNextCommand(command);
				this.messageRunAnswer();
			} else {
				command = new IncorrectCommand();
				command.setArgs(this.getFtpSession(), message, null, FtpCommandCode.IncorrectSequence);
				this.session.setNextCommand(command);
				this.messageRunAnswer();
			}
		}
	}
	/**
	 * Write the current answer and eventually close channel if necessary (421 or 221)
	 * @return True if the channel is closed due to the code
	 */
	private boolean writeFinalAnswer() {
		if ((this.session.getReplyCode() == FtpReplyCode.REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION) ||
				(this.session.getReplyCode() == FtpReplyCode.REPLY_221_CLOSING_CONTROL_CONNECTION)) {
			logger.debug("Will close Control Connection since: {}",this.session.getAnswer());
			this.session.getDataConn().getFtpTransferControl().clear();
			this.writeIntermediateAnswer().awaitUninterruptibly();
			Channels.close(this.session.getControlChannel());
			return true;
		}
		this.writeIntermediateAnswer();
		return false;
	}
	/**
	 * Write an intermediate Answer from Business before last answer also set by the Business
	 * @return the ChannelFuture associated with the write
	 */
	public ChannelFuture writeIntermediateAnswer() {
		return Channels.write(this.controlChannel, this.session.getAnswer());
	}
	/**
	 * Execute one command and write the following answer
	 */
	private void messageRunAnswer() {
		try {
			this.businessHandler.beforeRunCommand();
			logger.info("Run {}",this.session.getCurrentCommand().getCommand());
			this.session.getCurrentCommand().exec();
			this.businessHandler.afterRunCommandOk();
		} catch (FtpCommandAbstractException e) {
			this.session.setReplyCode(e);
			this.businessHandler.afterRunCommandKo(e);
		}
		if (this.session.getCurrentCommand().getCode() != FtpCommandCode.INTERNALSHUTDOWN) {
			this.writeFinalAnswer();
		}
	}
}
