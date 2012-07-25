/**
 * This file is part of Waarp Project.
 * 
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author tags. See the
 * COPYRIGHT.txt in the distribution for a full listing of individual contributors.
 * 
 * All Waarp Project is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Waarp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Waarp . If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.waarp.ftp.core.control;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.ssl.SslHandler;
import org.waarp.common.command.ReplyCode;
import org.waarp.common.command.exception.CommandAbstractException;
import org.waarp.common.command.exception.Reply503Exception;
import org.waarp.common.crypto.ssl.WaarpSslUtility;
import org.waarp.common.logging.WaarpInternalLogger;
import org.waarp.common.logging.WaarpInternalLoggerFactory;
import org.waarp.ftp.core.command.AbstractCommand;
import org.waarp.ftp.core.command.FtpCommandCode;
import org.waarp.ftp.core.command.access.USER;
import org.waarp.ftp.core.command.internal.ConnectionCommand;
import org.waarp.ftp.core.command.internal.IncorrectCommand;
import org.waarp.ftp.core.config.FtpInternalConfiguration;
import org.waarp.ftp.core.control.ftps.FtpsPipelineFactory;
import org.waarp.ftp.core.data.FtpTransferControl;
import org.waarp.ftp.core.session.FtpSession;
import org.waarp.ftp.core.utils.FtpChannelUtils;

/**
 * Main Network Handler (Control part) implementing RFC 959, 775, 2389, 2428, 3659 and supports XCRC
 * and XMD5 commands.
 * 
 * @author Frederic Bregier
 * 
 */
public class NetworkHandler extends SimpleChannelHandler {
	/**
	 * Internal Logger
	 */
	private static final WaarpInternalLogger logger = WaarpInternalLoggerFactory
			.getLogger(NetworkHandler.class);

	/**
	 * Business Handler
	 */
	private final BusinessHandler businessHandler;

	/**
	 * Internal store for the SessionInterface
	 */
	private final FtpSession session;

	/**
	 * The associated Channel
	 */
	private Channel controlChannel = null;

	/**
	 * Constructor from session
	 * 
	 * @param session
	 */
	public NetworkHandler(FtpSession session) {
		super();
		this.session = session;
		businessHandler = session.getBusinessHandler();
		businessHandler.setNetworkHandler(this);
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
		return controlChannel;
	}

	/**
	 * Run firstly executeChannelClosed.
	 * 
	 * @see org.jboss.netty.channel.SimpleChannelHandler#channelClosed(org.jboss.netty.channel.ChannelHandlerContext,
	 *      org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		if (session == null || session.getDataConn() == null ||
				session.getDataConn().getFtpTransferControl() == null) {
			super.channelClosed(ctx, e);
			return;
		}
		// Wait for any command running before closing (bad client sometimes
		// don't wait for answer)
		int limit = 100;
		while (session.getDataConn().getFtpTransferControl()
				.isFtpTransferExecuting()) {
			Thread.sleep(10);
			limit--;
			if (limit <= 0) {
				logger
						.warn("Waiting for transfer finished but 1s is not enough");
				break; // wait at most 1s
			}
		}
		businessHandler.executeChannelClosed();
		// release file and other permanent objects
		businessHandler.clear();
		session.clear();
		super.channelClosed(ctx, e);
	}

	/**
	 * Initialiaze the Handler.
	 * 
	 * @see org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext,
	 *      org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		Channel channel = e.getChannel();
		controlChannel = channel;
		session.setControlConnected();
		FtpChannelUtils.addCommandChannel(channel, session.getConfiguration());
		if (isStillAlive()) {
			// Make the first execution ready
			AbstractCommand command = new ConnectionCommand(getFtpSession());
			session.setNextCommand(command);
			// This command can change the next Command
			businessHandler.executeChannelConnected(channel);
			// Answer ready to continue from first command = Connection
			messageRunAnswer();
			getFtpSession().setReady(true);
		}
	}

	/**
	 * If the service is going to shutdown, it sends back a 421 message to the connection
	 * 
	 * @return True if the service is alive, else False if the system is going down
	 */
	private boolean isStillAlive() {
		if (session.getConfiguration().isShutdown) {
			session.setExitErrorCode("Service is going down: disconnect");
			writeFinalAnswer();
			return false;
		}
		return true;
	}

	/**
	 * Default exception task: close the current connection after calling exceptionLocalCaught and
	 * writing if possible the current replyCode.
	 * 
	 * @see org.jboss.netty.channel.SimpleChannelHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext,
	 *      org.jboss.netty.channel.ExceptionEvent)
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		Throwable e1 = e.getCause();
		Channel channel = e.getChannel();
		if (session == null) {
			// should not be
			logger.warn("NO SESSION", e1);
			return;
		}
		if (e1 instanceof ConnectException) {
			ConnectException e2 = (ConnectException) e1;
			logger.warn("Connection impossible since {} with Channel {}", e2
					.getMessage(), e.getChannel());
		} else if (e1 instanceof ChannelException) {
			ChannelException e2 = (ChannelException) e1;
			logger
					.warn(
							"Connection (example: timeout) impossible since {} with Channel {}",
							e2.getMessage(), e.getChannel());
		} else if (e1 instanceof ClosedChannelException) {
			logger.debug("Connection closed before end");
		} else if (e1 instanceof CommandAbstractException) {
			// FTP Exception: not close if not necessary
			CommandAbstractException e2 = (CommandAbstractException) e1;
			logger.warn("Command Error Reply {}", e2.getMessage());
			session.setReplyCode(e2);
			businessHandler.afterRunCommandKo(e2);
			if (channel.isConnected()) {
				writeFinalAnswer();
			}
			return;
		} else if (e1 instanceof NullPointerException) {
			NullPointerException e2 = (NullPointerException) e1;
			logger.warn("Null pointer Exception", e2);
			try {
				if (session != null) {
					session.setExitErrorCode("Internal error: disconnect");
					if (businessHandler != null &&
							session.getDataConn() != null) {
						businessHandler.exceptionLocalCaught(e);
						if (channel.isConnected()) {
							writeFinalAnswer();
						}
					}
				}
			} catch (NullPointerException e3) {
			}
			return;
		} else if (e1 instanceof IOException) {
			IOException e2 = (IOException) e1;
			logger.warn("Connection aborted since {} with Channel {}", e2
					.getMessage(), e.getChannel());
		} else {
			logger.warn("Unexpected exception from downstream Ref Channel: " + 
					e.getChannel().toString() +" Exception: "+e1.getMessage(), e1);
		}
		session.setExitErrorCode("Internal error: disconnect");
		businessHandler.exceptionLocalCaught(e);
		if (channel.isConnected()) {
			writeFinalAnswer();
		}
	}

	/**
	 * Simply call messageRun with the received message
	 * 
	 * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext,
	 *      org.jboss.netty.channel.MessageEvent)
	 */
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		if (isStillAlive()) {
			// First wait for the initialization to be fully done
			while (!session.isReady()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e1) {
				}
			}
			String message = (String) e.getMessage();
			AbstractCommand command = FtpCommandCode.getFromLine(
					getFtpSession(), message);
			logger.debug("RECVMSG: {} CMD: {}", message, command.getCommand());
			// First check if the command is an ABORT, QUIT or STAT
			if (!FtpCommandCode.isSpecialCommand(command.getCode())) {
				// Now check if a transfer is on its way: illegal to have at
				// same time two commands (except ABORT). Wait is at most 100
				// RETRYINMS=1s
				boolean notFinished = true;
				FtpTransferControl control = session.getDataConn().getFtpTransferControl();
				for (int i = 0; i < FtpInternalConfiguration.RETRYNB * 100; i++) {
					if (control.isFtpTransferExecuting() ||
							(!session.isCurrentCommandFinished())) {
						try {
							Thread.sleep(FtpInternalConfiguration.RETRYINMS);
						} catch (InterruptedException e1) {
							break;
						}
					} else {
						notFinished = false;
						break;
					}
				}
				if (notFinished) {
					session.setReplyCode(
							ReplyCode.REPLY_503_BAD_SEQUENCE_OF_COMMANDS,
							"Previous transfer command is not finished yet");
					businessHandler.afterRunCommandKo(
							new Reply503Exception(session.getReplyCode().getMesg()));
					writeIntermediateAnswer();
					return;
				}
			}
			// Default message
			session.setReplyCode(ReplyCode.REPLY_200_COMMAND_OKAY, null);
			// Special check for SSL AUTH/PBSZ/PROT/USER/PASS/ACCT
			if (FtpCommandCode.isSslOrAuthCommand(command.getCode())) {
				session.setNextCommand(command);
				messageRunAnswer();
				return;
			}
			if (session.getCurrentCommand().isNextCommandValid(command)) {
				session.setNextCommand(command);
				messageRunAnswer();
			} else {
				if (! session.getAuth().isIdentified()) {
					session.setReplyCode(ReplyCode.REPLY_530_NOT_LOGGED_IN, null);
					session.setNextCommand(new USER());
					writeFinalAnswer();
					return;
				}
				command = new IncorrectCommand();
				command.setArgs(getFtpSession(), message, null,
						FtpCommandCode.IncorrectSequence);
				session.setNextCommand(command);
				messageRunAnswer();
			}
		}
	}

	/**
	 * Write the current answer and eventually close channel if necessary (421 or 221)
	 * 
	 * @return True if the channel is closed due to the code
	 */
	private boolean writeFinalAnswer() {
		if (session.getReplyCode() == ReplyCode.REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION
				||
				session.getReplyCode() == ReplyCode.REPLY_221_CLOSING_CONTROL_CONNECTION) {
			session.getDataConn().getFtpTransferControl().clear();
			writeIntermediateAnswer().addListener(WaarpSslUtility.SSLCLOSE);
			return true;
		}
		writeIntermediateAnswer();
		session.setCurrentCommandFinished();
		return false;
	}

	/**
	 * Write an intermediate Answer from Business before last answer also set by the Business
	 * 
	 * @return the ChannelFuture associated with the write
	 */
	public ChannelFuture writeIntermediateAnswer() {
		return Channels.write(controlChannel, session.getAnswer());
	}

	/**
	 * To be extended to inform of an error to SNMP support
	 * @param error1
	 * @param error2
	 */
	protected void callForSnmp(String error1, String error2) {
		// ignore
	}
	
	/**
	 * Execute one command and write the following answer
	 */
	private void messageRunAnswer() {
		boolean error = false;
		logger.debug("Code: "+session.getCurrentCommand().getCode()+
				" ["+FtpCommandCode.AUTH+":"+FtpCommandCode.CCC+"]");
		try {
			businessHandler.beforeRunCommand();
			AbstractCommand command = session.getCurrentCommand();
			logger.debug("Run {}", command.getCommand());
			command.exec();
			businessHandler.afterRunCommandOk();
		} catch (CommandAbstractException e) {
			logger.debug("Command in error", e);
			error = true;
			session.setReplyCode(e);
			businessHandler.afterRunCommandKo(e);
		}
		logger.debug("Code: "+session.getCurrentCommand().getCode()+
				" ["+FtpCommandCode.AUTH+":"+FtpCommandCode.CCC+"]");
		if (error || session.getCurrentCommand().getCode() != FtpCommandCode.INTERNALSHUTDOWN) {
			if (session.getCurrentCommand().getCode() == FtpCommandCode.AUTH ||
					session.getCurrentCommand().getCode() == FtpCommandCode.CCC) {
				controlChannel.setReadable(false);
				ChannelFuture future = writeIntermediateAnswer();
				session.setCurrentCommandFinished();
				try {
					future.await();
				} catch (InterruptedException e) {
				}
			} else {
				writeFinalAnswer();
			}
		}
		if (! error) {
			if (session.getCurrentCommand().getCode() == FtpCommandCode.AUTH) {
				logger.debug("SSL to be added to pipeline");
				ChannelHandler sslHandler = controlChannel.getPipeline().getFirst();
				if (sslHandler instanceof SslHandler) {
					logger.debug("Already got a SslHandler");
				} else {
					// add the SSL support
					sslHandler =
							FtpsPipelineFactory.waarpSslContextFactory.initPipelineFactory(true,
									FtpsPipelineFactory.waarpSslContextFactory.needClientAuthentication(),
							false, 
							getFtpSession().getConfiguration().getFtpInternalConfiguration().getWorker());
					controlChannel.getPipeline().addFirst("SSL", sslHandler);
				}
				controlChannel.setReadable(true);
				ChannelFuture handshakeFuture;
				handshakeFuture = ((SslHandler) sslHandler).handshake();
				handshakeFuture.addListener(new ChannelFutureListener() {
					public void operationComplete(ChannelFuture future)
							throws Exception {
						logger.debug("Handshake: " + future.isSuccess(), future.getCause());
						if (!future.isSuccess()) {
							String error2 = future.getCause() != null ?
									future.getCause().getMessage() : "During Handshake";
							callForSnmp("SSL Connection Error", error2);
							future.getChannel().close();
						} else {
							session.setSsl(true);
						}
					}
				});
			} else if (session.getCurrentCommand().getCode() == FtpCommandCode.CCC) {
				logger.debug("SSL to be removed from pipeline");
				// remove the SSL support
				WaarpSslUtility.removingSslHandler(controlChannel);
			}
		}
		if (! controlChannel.isReadable()) {
			controlChannel.setReadable(true);
		}
	}
}
