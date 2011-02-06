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
package goldengate.ftp.core.data.handler;

import goldengate.common.exception.FileTransferException;
import goldengate.common.exception.InvalidArgumentException;
import goldengate.common.file.DataBlock;
import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;
import goldengate.ftp.core.config.FtpConfiguration;
import goldengate.ftp.core.config.FtpInternalConfiguration;
import goldengate.ftp.core.control.NetworkHandler;
import goldengate.ftp.core.data.FtpTransferControl;
import goldengate.ftp.core.exception.FtpNoConnectionException;
import goldengate.ftp.core.exception.FtpNoFileException;
import goldengate.ftp.core.exception.FtpNoTransferException;
import goldengate.ftp.core.session.FtpSession;
import goldengate.ftp.core.utils.FtpChannelUtils;

import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 * Network handler for Data connections
 *
 * @author Frederic Bregier
 *
 */
public class DataNetworkHandler extends SimpleChannelHandler {
    /**
     * Internal Logger
     */
    private static final GgInternalLogger logger = GgInternalLoggerFactory
            .getLogger(DataNetworkHandler.class);

    /**
     * Business Data Handler
     */
    private DataBusinessHandler dataBusinessHandler = null;

    /**
     * Configuration
     */
    private final FtpConfiguration configuration;

    /**
     * Is this Data Connection an Active or Passive one
     */
    private final boolean isActive;

    /**
     * Internal store for the SessionInterface
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
     * True when the DataNetworkHandler is fully ready (to prevent action before
     * ready)
     */
    private boolean isReady = false;

    /**
     * Constructor from DataBusinessHandler
     *
     * @param configuration
     * @param handler
     * @param active
     */
    public DataNetworkHandler(FtpConfiguration configuration,
            DataBusinessHandler handler, boolean active) {
        super();
        this.configuration = configuration;
        dataBusinessHandler = handler;
        dataBusinessHandler.setDataNetworkHandler(this);
        isActive = active;
    }

    /**
     * @return the dataBusinessHandler
     * @throws FtpNoConnectionException
     */
    public DataBusinessHandler getDataBusinessHandler()
            throws FtpNoConnectionException {
        if (dataBusinessHandler == null) {
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
        return session.getBusinessHandler().getNetworkHandler();
    }

    /**
     * Run firstly executeChannelClosed.
     *
     * @throws Exception
     * @see org.jboss.netty.channel.SimpleChannelHandler#channelClosed(org.jboss.netty.channel.ChannelHandlerContext,
     *      org.jboss.netty.channel.ChannelStateEvent)
     */
    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        if (session != null) {
            session.getDataConn().getFtpTransferControl().setPreEndOfTransfer();
            session.getDataConn().unbindPassive();
            try {
                getDataBusinessHandler().executeChannelClosed();
                // release file and other permanent objects
                getDataBusinessHandler().clear();
            } catch (FtpNoConnectionException e1) {
            }
            session.getDataConn().getFtpTransferControl()
                    .setClosedDataChannel();
            dataBusinessHandler = null;
            channelPipeline = null;
            dataChannel = null;
        }
        super.channelClosed(ctx, e);
    }

    /**
     * Initialize the Handler.
     *
     * @see org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext,
     *      org.jboss.netty.channel.ChannelStateEvent)
     */
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        Channel channel = e.getChannel();
        // First get the ftpSession from inetaddresses
        for (int i = 0; i < FtpInternalConfiguration.RETRYNB; i ++) {
            session = configuration.getFtpSession(channel, isActive);
            if (session == null) {
                logger.warn("Session not found at try " + i);
                try {
                    Thread.sleep(FtpInternalConfiguration.RETRYINMS);
                } catch (InterruptedException e1) {
                    break;
                }
            } else {
                break;
            }
        }
        if (session == null) {
            // Not found !!!
            logger.error("Session not found!");
            Channels.close(channel);
            // Problem: control connection could not be directly informed!!!
            // Only timeout will occur
            return;
        }
        channelPipeline = ctx.getPipeline();
        dataChannel = channel;
        dataBusinessHandler.setFtpSession(getFtpSession());
        FtpChannelUtils.addDataChannel(channel, session.getConfiguration());
        if (isStillAlive()) {
            setCorrectCodec();
            session.getDataConn().getFtpTransferControl().setOpenedDataChannel(
                    channel, this);
        } else {
            // Cannot continue
            session.getDataConn().getFtpTransferControl().setOpenedDataChannel(
                    null, this);
            return;
        }
        isReady = true;
    }

    /**
     * Set the CODEC according to the mode. Must be called after each call of
     * MODE, STRU or TYPE
     */
    public void setCorrectCodec() {
        FtpDataModeCodec modeCodec = (FtpDataModeCodec) channelPipeline
                .get(FtpDataPipelineFactory.CODEC_MODE);
        FtpDataTypeCodec typeCodec = (FtpDataTypeCodec) channelPipeline
                .get(FtpDataPipelineFactory.CODEC_TYPE);
        FtpDataStructureCodec structureCodec = (FtpDataStructureCodec) channelPipeline
                .get(FtpDataPipelineFactory.CODEC_STRUCTURE);
        modeCodec.setMode(session.getDataConn().getMode());
        modeCodec.setStructure(session.getDataConn().getStructure());
        typeCodec.setFullType(session.getDataConn().getType(), session
                .getDataConn().getSubType());
        structureCodec.setStructure(session.getDataConn().getStructure());
    }

    /**
     * Unlock the Mode Codec from openConnection of {@link FtpTransferControl}
     *
     */
    public void unlockModeCodec() {
        FtpDataModeCodec modeCodec = (FtpDataModeCodec) channelPipeline
                .get("MODE");
        modeCodec.setCodecReady();
    }

    /**
     * Default exception task: close the current connection after calling
     * exceptionLocalCaught.
     *
     * @see org.jboss.netty.channel.SimpleChannelHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext,
     *      org.jboss.netty.channel.ExceptionEvent)
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        if (session == null) {
            logger.warn("Error without any session active {}", e.getCause());
            return;
        }
        Throwable e1 = e.getCause();
        if (e1 instanceof ConnectException) {
            ConnectException e2 = (ConnectException) e1;
            logger.warn("Connection impossible since {}", e2.getMessage());
        } else if (e1 instanceof ChannelException) {
            ChannelException e2 = (ChannelException) e1;
            logger.warn("Connection (example: timeout) impossible since {}", e2
                    .getMessage());
        } else if (e1 instanceof ClosedChannelException) {
            logger.debug("Connection closed before end");
        } else if (e1 instanceof InvalidArgumentException) {
            InvalidArgumentException e2 = (InvalidArgumentException) e1;
            logger.warn("Bad configuration in Codec in {}", e2.getMessage());
        } else if (e1 instanceof NullPointerException) {
            NullPointerException e2 = (NullPointerException) e1;
            logger.warn("Null pointer Exception", e2);
            try {
                if (dataBusinessHandler != null) {
                    dataBusinessHandler.exceptionLocalCaught(e);
                    if (session.getDataConn() != null) {
                        session.getDataConn().getFtpTransferControl()
                                .setTransferAbortedFromInternal(true);
                    }
                }
            } catch (NullPointerException e3) {
            }
            return;
        } else if (e1 instanceof CancelledKeyException) {
            CancelledKeyException e2 = (CancelledKeyException) e1;
            logger.warn("Connection aborted since {}", e2.getMessage());
            // XXX TODO FIXME is it really what we should do ?
            // No action
            return;
        } else if (e1 instanceof IOException) {
            IOException e2 = (IOException) e1;
            logger.warn("Connection aborted since {}", e2.getMessage());
        } else if (e1 instanceof NotYetConnectedException) {
            NotYetConnectedException e2 = (NotYetConnectedException) e1;
            logger.debug("Ignore this exception {}", e2.getMessage());
            return;
        } else if (e1 instanceof BindException) {
            BindException e2 = (BindException) e1;
            logger.warn("Address already in use {}", e2.getMessage());
        } else if (e1 instanceof ConnectException) {
            ConnectException e2 = (ConnectException) e1;
            logger.warn("Timeout occurs {}", e2.getMessage());
        } else {
            logger.warn("Unexpected exception from downstream: {}", e1.getMessage());
        }
        if (dataBusinessHandler != null) {
            dataBusinessHandler.exceptionLocalCaught(e);
        }
        session.getDataConn().getFtpTransferControl()
                .setTransferAbortedFromInternal(true);
    }

    /**
     * To enable continues of Retrieve operation (prevent OOM)
     *
     * @see org.jboss.netty.channel.SimpleChannelHandler#channelInterestChanged(org.jboss.netty.channel.ChannelHandlerContext,
     *      org.jboss.netty.channel.ChannelStateEvent)
     */
    @Override
    public void channelInterestChanged(ChannelHandlerContext arg0,
            ChannelStateEvent arg1) {
        int op = arg1.getChannel().getInterestOps();
        if (op == Channel.OP_NONE || op == Channel.OP_READ) {
            if (isReady) {
                session.getDataConn().getFtpTransferControl().runTrueRetrieve();
            }
        }
    }

    /**
     * Act as needed according to the receive DataBlock message
     *
     * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext,
     *      org.jboss.netty.channel.MessageEvent)
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        if (isStillAlive()) {
            DataBlock dataBlock = (DataBlock) e.getMessage();
            try {
                session.getDataConn().getFtpTransferControl()
                        .getExecutingFtpTransfer().getFtpFile().writeDataBlock(
                                dataBlock);
                /*try {
                    session.getDataConn().getFtpTransferControl()
                        .getExecutingFtpTransfer().getFtpFile().flush();
                } catch (IOException e1) {
                    session.getDataConn().getFtpTransferControl()
                        .setTransferAbortedFromInternal(true);
                    return;
                }*/
            } catch (FtpNoFileException e1) {
                session.getDataConn().getFtpTransferControl()
                        .setTransferAbortedFromInternal(true);
                return;
            } catch (FtpNoTransferException e1) {
                session.getDataConn().getFtpTransferControl()
                        .setTransferAbortedFromInternal(true);
                return;
            } catch (FileTransferException e1) {
                session.getDataConn().getFtpTransferControl()
                        .setTransferAbortedFromInternal(true);
            }
        } else {
            // Shutdown
            session.getDataConn().getFtpTransferControl()
                    .setTransferAbortedFromInternal(true);
        }
    }

    /**
     * Write a simple message (like LIST) and wait for it
     *
     * @param message
     * @return True if the message is correctly written
     */
    public boolean writeMessage(String message) {
        DataBlock dataBlock = new DataBlock();
        dataBlock.setEOF(true);
        ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(message.getBytes());
        dataBlock.setBlock(buffer);
        ChannelFuture future;
        try {
            future = Channels.write(dataChannel, dataBlock).await();
        } catch (InterruptedException e) {
            return false;
        }
        return future.isSuccess();
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
            return false;
        }
        return true;
    }
}
