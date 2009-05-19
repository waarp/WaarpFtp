/**
 * Copyright 2009, Frederic Bregier, and individual contributors
 * by the @author tags. See the COPYRIGHT.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package goldengate.ftp.core.control;

import goldengate.common.command.ReplyCode;
import goldengate.common.command.exception.CommandAbstractException;
import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;
import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpCommandCode;
import goldengate.ftp.core.command.internal.ConnectionCommand;
import goldengate.ftp.core.command.internal.IncorrectCommand;
import goldengate.ftp.core.config.FtpInternalConfiguration;
import goldengate.ftp.core.session.FtpSession;
import goldengate.ftp.core.utils.FtpChannelUtils;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 * Main Network Handler (Control part) implementing RFC 959, 775, 2389, 2428,
 * 3659 and supports XCRC and XMD5 commands.
 *
 * @author Frederic Bregier
 *
 */
@ChannelPipelineCoverage("one")
public class NetworkHandler extends SimpleChannelHandler {
    /**
     * Internal Logger
     */
    private static final GgInternalLogger logger = GgInternalLoggerFactory
            .getLogger(NetworkHandler.class);

    /**
     * Business Handler
     */
    private BusinessHandler businessHandler = null;

    /**
     * Internal store for the SessionInterface
     */
    private FtpSession session = null;

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
            limit --;
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
        // businessHandler = null;
        // this.controlChannel = null; // to prevent when bad client goes wrong
        // session = null;
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
        FtpChannelUtils.addCommandChannel(channel, session
                .getConfiguration());
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
     * If the service is going to shutdown, it sends back a 421 message to the
     * connection
     *
     * @return True if the service is alive, else False if the system is going
     *         down
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
     * Default exception task: close the current connection after calling
     * exceptionLocalCaught and writing if possible the current replyCode.
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
            logger.warn("NO SESSION",e1);
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
            logger.warn("Connection closed before end");
        } else if (e1 instanceof CommandAbstractException) {
            // FTP Exception: not close if not necessary
            CommandAbstractException e2 = (CommandAbstractException) e1;
            logger.warn("Command Error Reply", e2);
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
                    if (businessHandler != null) {
                        if (session.getDataConn() != null) {
                            businessHandler.exceptionLocalCaught(e);
                            if (channel.isConnected()) {
                                writeFinalAnswer();
                            }
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
            logger.warn("Unexpected exception from downstream" +
                    " Ref Channel: " + e.getChannel().toString(), e1);
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
                    Thread.sleep(5);
                } catch (InterruptedException e1) {
                }
            }
            String message = (String) e.getMessage();
            AbstractCommand command = FtpCommandCode.getFromLine(getFtpSession(), message);
            // Default message
            session.setReplyCode(ReplyCode.REPLY_200_COMMAND_OKAY, null);
            logger.info("RECVMSG: {} CMD: {}", message, command.getCommand());
            // First check if the command is an ABORT, QUIT or STAT
            if (!FtpCommandCode.isSpecialCommand(command.getCode())) {
                // Now check if a transfer is on its way: illegal to have at
                // same time two commands (except ABORT). Wait is at most 100 RETRYINMS=1s
                boolean notFinished = true;
                for (int i = 0; i < FtpInternalConfiguration.RETRYNB*100; i ++) {
                    if (session.getDataConn().getFtpTransferControl()
                            .isFtpTransferExecuting()) {
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
                    writeIntermediateAnswer();
                    return;
                }
            }
            if (session.getCurrentCommand().isNextCommandValid(command)) {
                session.setNextCommand(command);
                messageRunAnswer();
            } else {
                command = new IncorrectCommand();
                command.setArgs(getFtpSession(), message, null,
                        FtpCommandCode.IncorrectSequence);
                session.setNextCommand(command);
                messageRunAnswer();
            }
        }
    }

    /**
     * Write the current answer and eventually close channel if necessary (421
     * or 221)
     *
     * @return True if the channel is closed due to the code
     */
    private boolean writeFinalAnswer() {
        if (session.getReplyCode() == ReplyCode.REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION ||
                session.getReplyCode() == ReplyCode.REPLY_221_CLOSING_CONTROL_CONNECTION) {
            //logger.debug("Will close Control Connection since: {}",
                    //session.getAnswer());
            session.getDataConn().getFtpTransferControl().clear();
            writeIntermediateAnswer().addListener(
                    ChannelFutureListener.CLOSE);
            return true;
        }
        writeIntermediateAnswer();
        return false;
    }

    /**
     * Write an intermediate Answer from Business before last answer also set by
     * the Business
     *
     * @return the ChannelFuture associated with the write
     */
    public ChannelFuture writeIntermediateAnswer() {
        return Channels.write(controlChannel, session.getAnswer());
    }

    /**
     * Execute one command and write the following answer
     */
    private void messageRunAnswer() {
        try {
            businessHandler.beforeRunCommand();
            logger
                    .info("Run {}", session.getCurrentCommand()
                            .getCommand());
            session.getCurrentCommand().exec();
            businessHandler.afterRunCommandOk();
        } catch (CommandAbstractException e) {
            session.setReplyCode(e);
            businessHandler.afterRunCommandKo(e);
        }
        if (session.getCurrentCommand().getCode() != FtpCommandCode.INTERNALSHUTDOWN) {
            writeFinalAnswer();
        }
    }
}
