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
package org.waarp.ftp.core.data.handler;

import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;

import org.waarp.common.crypto.ssl.WaarpSslUtility;
import org.waarp.common.exception.FileTransferException;
import org.waarp.common.exception.InvalidArgumentException;
import org.waarp.common.file.DataBlock;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.common.utility.WaarpStringUtils;
import org.waarp.ftp.core.config.FtpConfiguration;
import org.waarp.ftp.core.config.FtpInternalConfiguration;
import org.waarp.ftp.core.control.NetworkHandler;
import org.waarp.ftp.core.data.FtpTransfer;
import org.waarp.ftp.core.data.FtpTransferControl;
import org.waarp.ftp.core.exception.FtpNoConnectionException;
import org.waarp.ftp.core.exception.FtpNoFileException;
import org.waarp.ftp.core.exception.FtpNoTransferException;
import org.waarp.ftp.core.session.FtpSession;
import org.waarp.ftp.core.utils.FtpChannelUtils;

/**
 * Network handler for Data connections
 * 
 * @author Frederic Bregier
 * 
 */
public class DataNetworkHandler extends SimpleChannelInboundHandler<DataBlock> {
    /**
     * Internal Logger
     */
    private static final WaarpLogger logger = WaarpLoggerFactory
            .getLogger(DataNetworkHandler.class);

    /**
     * Business Data Handler
     */
    private DataBusinessHandler dataBusinessHandler = null;

    /**
     * Configuration
     */
    protected final FtpConfiguration configuration;

    /**
     * Is this Data Connection an Active or Passive one
     */
    private final boolean isActive;

    /**
     * Internal store for the SessionInterface
     */
    protected FtpSession session = null;

    /**
     * The associated Channel
     */
    private Channel dataChannel = null;

    /**
     * Pipeline
     */
    private ChannelPipeline channelPipeline = null;

    /**
     * The associated FtpTransfer
     */
    private volatile FtpTransfer ftpTransfer = null;
    
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
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Data Channel closed with a session ? "+(session !=null));
        if (session != null) {
            if (session.getDataConn().checkCorrectChannel(ctx.channel())) {
                session.getDataConn().getFtpTransferControl().setPreEndOfTransfer();
            } else {
                session.getDataConn().getFtpTransferControl().setTransferAbortedFromInternal(true);
            }
            session.getDataConn().unbindPassive();
            try {
                getDataBusinessHandler().executeChannelClosed();
                // release file and other permanent objects
                getDataBusinessHandler().clear();
            } catch (FtpNoConnectionException e1) {
            }
            dataBusinessHandler = null;
            channelPipeline = null;
            dataChannel = null;
        }
        super.channelInactive(ctx);
    }

    protected void setSession(Channel channel) {
        // First get the ftpSession from inetaddresses
        for (int i = 0; i < FtpInternalConfiguration.RETRYNB; i++) {
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
            WaarpSslUtility.closingSslChannel(channel);
            // Problem: control connection could not be directly informed!!!
            // Only timeout will occur
            return;
        }
    }

    /**
     * Initialize the Handler.
     * 
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channel.config().setAutoRead(false);
        if (session == null) {
            setSession(channel);
        }
        logger.debug("Data Channel opened as "+channel);
        if (session == null) {
            logger.debug("DataChannel immediately closed since no session is assigned");
            WaarpSslUtility.closingSslChannel(ctx.channel());
            return;
        }
        channelPipeline = ctx.pipeline();
        dataChannel = channel;
        dataBusinessHandler.setFtpSession(getFtpSession());
        FtpChannelUtils.addDataChannel(channel, session.getConfiguration());
        logger.debug("DataChannel connected: " + session.getReplyCode());
        if (session.getReplyCode().getCode() >= 400) {
            // shall not be except if an error early occurs
            switch (session.getCurrentCommand().getCode()) {
                case RETR:
                case APPE:
                case STOR:
                case STOU:
                    // close the data channel immediately
                    logger.debug("DataChannel immediately closed since " + session.getCurrentCommand().getCode()
                            + " is not ok at startup");
                    WaarpSslUtility.closingSslChannel(ctx.channel());
                    return;
                default:
                    break;
            }
        }
        if (isStillAlive()) {
            setCorrectCodec();
            unlockModeCodec();
            session.getDataConn().getFtpTransferControl().setOpenedDataChannel(channel, this);
            logger.debug("DataChannel fully configured");
        } else {
            // Cannot continue
            logger.debug("Connected but no more alive so will disconnect");
            session.getDataConn().getFtpTransferControl().setOpenedDataChannel(null, this);
            return;
        }
    }

    /**
     * Set the CODEC according to the mode. Must be called after each call of MODE, STRU or TYPE
     */
    public void setCorrectCodec() {
        FtpDataModeCodec modeCodec = (FtpDataModeCodec) channelPipeline
                .get(FtpDataInitializer.CODEC_MODE);
        FtpDataTypeCodec typeCodec = (FtpDataTypeCodec) channelPipeline
                .get(FtpDataInitializer.CODEC_TYPE);
        FtpDataStructureCodec structureCodec = (FtpDataStructureCodec) channelPipeline
                .get(FtpDataInitializer.CODEC_STRUCTURE);
        if (modeCodec == null || typeCodec == null || structureCodec == null) {
            return;
        }
        modeCodec.setMode(session.getDataConn().getMode());
        modeCodec.setStructure(session.getDataConn().getStructure());
        typeCodec.setFullType(session.getDataConn().getType(), session
                .getDataConn().getSubType());
        structureCodec.setStructure(session.getDataConn().getStructure());
        logger.debug("codec setup");
    }

    /**
     * Unlock the Mode Codec from openConnection of {@link FtpTransferControl}
     * 
     */
    public void unlockModeCodec() {
        FtpDataModeCodec modeCodec = (FtpDataModeCodec) channelPipeline
                .get(FtpDataInitializer.CODEC_MODE);
        modeCodec.setCodecReady();
    }

    /**
     * Default exception task: close the current connection after calling exceptionLocalCaught.
     * 
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (session == null) {
            logger.debug("Error without any session active {}", cause);
            return;
        }
        Throwable e1 = cause;
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
                    dataBusinessHandler.exceptionLocalCaught(e1);
                    if (session.getDataConn() != null) {
                        if (session.getDataConn().checkCorrectChannel(ctx.channel())) {
                            session.getDataConn().getFtpTransferControl()
                                    .setTransferAbortedFromInternal(true);
                        }
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
            logger.warn("Unexpected exception from Outband: {}", e1.getMessage(), e1);
        }
        if (dataBusinessHandler != null) {
            dataBusinessHandler.exceptionLocalCaught(e1);
        }
        if (session.getDataConn().checkCorrectChannel(ctx.channel())) {
            session.getDataConn().getFtpTransferControl()
                    .setTransferAbortedFromInternal(true);
        }
    }

    public void setFtpTransfer(FtpTransfer ftpTransfer) {
        this.ftpTransfer = ftpTransfer;
    }
    /**
     * Act as needed according to the receive DataBlock message
     * 
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, DataBlock dataBlock) {
        if (ftpTransfer == null) {
            for (int i = 0; i < 10; i++) {
                try {
                    ftpTransfer = session.getDataConn().getFtpTransferControl()
                                         .getExecutingFtpTransfer();
                    if (ftpTransfer != null) {
                        break;
                    }
                } catch (FtpNoTransferException e) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        break;
                    }
                    continue;
                }
            }
            if (ftpTransfer == null) {
                logger.debug("No ExecutionFtpTransfer found");
                session.getDataConn().getFtpTransferControl()
                    .setTransferAbortedFromInternal(true);
                return;
            }
        }
        try {
            if (isStillAlive()) {
                try {
                    ftpTransfer.getFtpFile().writeDataBlock(dataBlock);
                } catch (FtpNoFileException e1) {
                    logger.debug(e1);
                    session.getDataConn().getFtpTransferControl()
                            .setTransferAbortedFromInternal(true);
                    return;
                } catch (FileTransferException e1) {
                    logger.debug(e1);
                    session.getDataConn().getFtpTransferControl()
                            .setTransferAbortedFromInternal(true);
                }
            } else {
                // Shutdown
                session.getDataConn().getFtpTransferControl()
                        .setTransferAbortedFromInternal(true);
                WaarpSslUtility.closingSslChannel(ctx.channel());
            }
        } finally {
            dataBlock.getBlock().release();
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
        ByteBuf buffer = Unpooled.wrappedBuffer(message.getBytes(WaarpStringUtils.UTF8));
        dataBlock.setBlock(buffer);
        ChannelFuture future;
        logger.debug("Will write: " + buffer.toString(WaarpStringUtils.UTF8));
        try {
            future = dataChannel.writeAndFlush(dataBlock);
            future.await(FtpConfiguration.getDATATIMEOUTCON());
        } catch (InterruptedException e) {
            logger.debug("Interrupted", e);
            return false;
        }
        logger.debug("Write result: " + future.isSuccess(), future.cause());
        return future.isSuccess();
    }

    /**
     * If the service is going to shutdown, it sends back a 421 message to the connection
     * 
     * @return True if the service is alive, else False if the system is going down
     */
    private boolean isStillAlive() {
        if (session.getConfiguration().isShutdown()) {
            session.setExitErrorCode("Service is going down: disconnect");
            return false;
        }
        return true;
    }
}