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
import java.util.concurrent.RejectedExecutionException;

import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import org.waarp.common.command.ReplyCode;
import org.waarp.common.command.exception.CommandAbstractException;
import org.waarp.common.command.exception.Reply421Exception;
import org.waarp.common.command.exception.Reply503Exception;
import org.waarp.common.crypto.ssl.WaarpSslUtility;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.ftp.core.command.AbstractCommand;
import org.waarp.ftp.core.command.FtpCommandCode;
import org.waarp.ftp.core.command.access.USER;
import org.waarp.ftp.core.command.internal.ConnectionCommand;
import org.waarp.ftp.core.command.internal.IncorrectCommand;
import org.waarp.ftp.core.control.ftps.FtpsInitializer;
import org.waarp.ftp.core.data.FtpTransferControl;
import org.waarp.ftp.core.exception.FtpNoConnectionException;
import org.waarp.ftp.core.session.FtpSession;
import org.waarp.ftp.core.utils.FtpChannelUtils;

/**
 * Main Network Handler (Control part) implementing RFC 959, 775, 2389, 2428, 3659 and supports XCRC
 * and XMD5 commands.
 * 
 * @author Frederic Bregier
 * 
 */
public class NetworkHandler extends SimpleChannelInboundHandler<String> {
    /**
     * Internal Logger
     */
    private static final WaarpLogger logger = WaarpLoggerFactory.getLogger(NetworkHandler.class);

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
     * ChannelHandlerContext that could be used whenever needed
     */
    private volatile ChannelHandlerContext ctx;

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
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (session == null || session.getDataConn() == null ||
                session.getDataConn().getFtpTransferControl() == null) {
            super.channelInactive(ctx);
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
                logger.warn("Waiting for transfer finished but 1s is not enough");
                break; // wait at most 1s
            }
        }
        businessHandler.executeChannelClosed();
        // release file and other permanent objects
        businessHandler.clear();
        session.clear();
        super.channelInactive(ctx);
    }

    /**
     * Initialize the Handler.
     * 
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        Channel channel = ctx.channel();
        controlChannel = channel;
        session.setControlConnected();
        FtpChannelUtils.addCommandChannel(channel, session.getConfiguration());
        if (isStillAlive(ctx)) {
            // Make the first execution ready
            AbstractCommand command = new ConnectionCommand(getFtpSession());
            session.setNextCommand(command);
            // This command can change the next Command
            businessHandler.executeChannelConnected(channel);
            // Answer ready to continue from first command = Connection
            messageRunAnswer(ctx);
            getFtpSession().setReady(true);
        }
    }

    /**
     * If the service is going to shutdown, it sends back a 421 message to the connection
     * 
     * @return True if the service is alive, else False if the system is going down
     */
    private boolean isStillAlive(ChannelHandlerContext ctx) {
        if (session.getConfiguration().isShutdown()) {
            session.setExitErrorCode("Service is going down: disconnect");
            writeFinalAnswer(ctx);
            return false;
        }
        return true;
    }

    /**
     * Default exception task: close the current connection after calling exceptionLocalCaught and
     * writing if possible the current replyCode.
     * 
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.ctx = ctx;
        Throwable e1 = cause;
        Channel channel = ctx.channel();
        if (session == null) {
            // should not be
            logger.warn("NO SESSION", e1);
            return;
        }
        if (e1 instanceof ConnectException) {
            ConnectException e2 = (ConnectException) e1;
            logger.warn("Connection impossible since {} with Channel {}", e2
                    .getMessage(), channel);
        } else if (e1 instanceof ChannelException) {
            ChannelException e2 = (ChannelException) e1;
            logger
                    .warn(
                            "Connection (example: timeout) impossible since {} with Channel {}",
                            e2.getMessage(), channel);
        } else if (e1 instanceof ClosedChannelException) {
            logger.debug("Connection closed before end");
            session.setExitErrorCode("Internal error: disconnect");
            if (channel.isActive()) {
                writeFinalAnswer(ctx);
            }
            return;
        } else if (e1 instanceof CommandAbstractException) {
            // FTP Exception: not close if not necessary
            CommandAbstractException e2 = (CommandAbstractException) e1;
            logger.warn("Command Error Reply {}", e2.getMessage());
            session.setReplyCode(e2);
            businessHandler.afterRunCommandKo(e2);
            if (channel.isActive()) {
                writeFinalAnswer(ctx);
            }
            return;
        } else if (e1 instanceof NullPointerException) {
            NullPointerException e2 = (NullPointerException) e1;
            logger.warn("Null pointer Exception: " + ctx.channel().toString(), e2);
            try {
                if (session != null) {
                    session.setExitErrorCode("Internal error: disconnect");
                    if (businessHandler != null &&
                            session.getDataConn() != null) {
                        businessHandler.exceptionLocalCaught(e1);
                        if (channel.isActive()) {
                            writeFinalAnswer(ctx);
                        }
                    }
                }
            } catch (NullPointerException e3) {
            }
            return;
        } else if (e1 instanceof IOException) {
            IOException e2 = (IOException) e1;
            logger.warn("Connection aborted since {} with Channel {}", e2
                    .getMessage(), channel);
            logger.warn(e1);
        } else if (e1 instanceof RejectedExecutionException) {
            logger.debug("Rejected execution (shutdown) from {}", channel);
            return;
        } else {
            logger.warn("Unexpected exception from Outband Ref Channel: " +
                    channel.toString() + " Exception: " + e1.getMessage(), e1);
        }
        session.setExitErrorCode("Internal error: disconnect");
        businessHandler.exceptionLocalCaught(e1);
        if (channel.isActive()) {
            writeFinalAnswer(ctx);
        }
    }

    /**
     * Simply call messageRun with the received message
     * 
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, String e) {
        this.ctx = ctx;
        if (isStillAlive(ctx)) {
            // First wait for the initialization to be fully done
            if (!session.isReady()) {
                session.setReplyCode(ReplyCode.REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION, null);
                businessHandler.afterRunCommandKo(new Reply421Exception(session.getReplyCode().getMesg()));
                writeIntermediateAnswer(ctx);
                return;
            }
            String message = e;
            AbstractCommand command = FtpCommandCode.getFromLine(getFtpSession(), message);
            logger.debug("RECVMSG: {} CMD: {} " + command.getCode(), message, command.getCommand());
            // First check if the command is an ABORT, QUIT or STAT
            if (!FtpCommandCode.isSpecialCommand(command.getCode())) {
                // Now check if a transfer is on its way: illegal to have at
                // same time two commands (except ABORT). Wait is at most 100x
                // RETRYINMS=1s
                FtpTransferControl control = session.getDataConn().getFtpTransferControl();
                boolean notFinished = control.waitFtpTransferExecuting();
                if (notFinished) {
                    session.setReplyCode(
                            ReplyCode.REPLY_503_BAD_SEQUENCE_OF_COMMANDS,
                            "Previous transfer command is not finished yet");
                    businessHandler.afterRunCommandKo(
                            new Reply503Exception(session.getReplyCode().getMesg()));
                    writeIntermediateAnswer(ctx);
                    return;
                }
            }
            // Default message
            session.setReplyCode(ReplyCode.REPLY_200_COMMAND_OKAY, null);
            // Special check for SSL AUTH/PBSZ/PROT/USER/PASS/ACCT
            if (FtpCommandCode.isSslOrAuthCommand(command.getCode())) {
                session.setNextCommand(command);
                messageRunAnswer(ctx);
                return;
            }
            if (session.getCurrentCommand().isNextCommandValid(command)) {
                logger.debug("Previous: " + session.getCurrentCommand().getCode() +
                        " Next: " + command.getCode());
                session.setNextCommand(command);
                messageRunAnswer(ctx);
            } else {
                if (!session.getAuth().isIdentified()) {
                    session.setReplyCode(ReplyCode.REPLY_530_NOT_LOGGED_IN, null);
                    session.setNextCommand(new USER());
                    writeFinalAnswer(ctx);
                    return;
                }
                command = new IncorrectCommand();
                command.setArgs(getFtpSession(), message, null,
                        FtpCommandCode.IncorrectSequence);
                session.setNextCommand(command);
                messageRunAnswer(ctx);
            }
        }
    }

    /**
     * Write the current answer and eventually close channel if necessary (421 or 221)
     * 
     * @return True if the channel is closed due to the code
     */
    private boolean writeFinalAnswer(ChannelHandlerContext ctx) {
        if (session.getReplyCode() == ReplyCode.REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION
                ||
                session.getReplyCode() == ReplyCode.REPLY_221_CLOSING_CONTROL_CONNECTION) {
            session.getDataConn().getFtpTransferControl().clear();
            writeIntermediateAnswer(ctx).addListener(WaarpSslUtility.SSLCLOSE);
            return true;
        }
        writeIntermediateAnswer(ctx);
        session.setCurrentCommandFinished();
        return false;
    }

    /**
     * Write an intermediate Answer from Business before last answer also set by the Business
     * 
     * @return the ChannelFuture associated with the write
     */
    public ChannelFuture writeIntermediateAnswer(ChannelHandlerContext ctx) {
        logger.debug("Answer: " + session.getAnswer());
        return ctx.writeAndFlush(session.getAnswer());
    }

    /**
     * Write an intermediate Answer from Business before last answer also set by the Business
     * 
     * @return the ChannelFuture associated with the write
     */
    public ChannelFuture writeIntermediateAnswer() {
        return writeIntermediateAnswer(ctx);
    }

    /**
     * To be extended to inform of an error to SNMP support
     * 
     * @param error1
     * @param error2
     */
    protected void callForSnmp(String error1, String error2) {
        // ignore
    }

    /**
     * Execute one command and write the following answer
     */
    private void messageRunAnswer(final ChannelHandlerContext ctx) {
        boolean error = false;
        logger.debug("Code: " + session.getCurrentCommand().getCode());
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
        logger.debug("Code: " + session.getCurrentCommand().getCode() +
                " [" + session.getReplyCode() + "]");
        if (error) {
            if (session.getCurrentCommand().getCode() != FtpCommandCode.INTERNALSHUTDOWN) {
                writeFinalAnswer(ctx);
            }
            // In error so Check that Data is closed
            if (session.getDataConn().isActive()) {
                logger.debug("Closing DataChannel while command is in error");
                try {
                    session.getDataConn().getCurrentDataChannel().close();
                } catch (FtpNoConnectionException e) {
                    // ignore
                }
            }
            return;
        }
        if (session.getCurrentCommand().getCode() == FtpCommandCode.AUTH ||
                session.getCurrentCommand().getCode() == FtpCommandCode.CCC) {
            controlChannel.config().setAutoRead(false);
            ChannelFuture future = writeIntermediateAnswer(ctx);
            session.setCurrentCommandFinished();
            if (session.getCurrentCommand().getCode() == FtpCommandCode.AUTH) {
                logger.debug("SSL to be added to pipeline");
                ChannelHandler sslHandler = ctx.pipeline().first();
                if (sslHandler instanceof SslHandler) {
                    logger.debug("Already got a SslHandler");
                } else {
                    logger.debug("Add Explicitely SSL support to Command");
                    // add the SSL support
                    sslHandler =
                            FtpsInitializer.waarpSslContextFactory.initInitializer(true,
                                    FtpsInitializer.waarpSslContextFactory.needClientAuthentication());
                    session.prepareSsl();
                    WaarpSslUtility.addSslHandler(future, ctx.pipeline(), sslHandler,
                            new GenericFutureListener<Future<? super Channel>>() {
                                public void operationComplete(Future<? super Channel> future) throws Exception {
                                    if (!future.isSuccess()) {
                                        String error2 = future.cause() != null ?
                                                future.cause().getMessage() : "During Handshake";
                                        logger.error("Cannot finalize Ssl Command channel " + error2);
                                        callForSnmp("SSL Connection Error", error2);
                                        session.setSsl(false);
                                        ctx.close();
                                    } else {
                                        logger.debug("End of initialization of SSL and command channel: "
                                                + ctx.channel());
                                        session.setSsl(true);
                                    }
                                }
                            });
                }
            } else if (session.getCurrentCommand().getCode() == FtpCommandCode.CCC) {
                logger.debug("SSL to be removed from pipeline");
                // remove the SSL support
                session.prepareSsl();
                WaarpSslUtility.removingSslHandler(future, controlChannel, false);
            }
        } else if (session.getCurrentCommand().getCode() != FtpCommandCode.INTERNALSHUTDOWN) {
            writeFinalAnswer(ctx);
        }
    }
}
