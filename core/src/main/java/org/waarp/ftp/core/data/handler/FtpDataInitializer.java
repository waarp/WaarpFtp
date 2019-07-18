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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;

import org.waarp.ftp.core.command.FtpArgumentCode.TransferMode;
import org.waarp.ftp.core.command.FtpArgumentCode.TransferStructure;
import org.waarp.ftp.core.command.FtpArgumentCode.TransferSubType;
import org.waarp.ftp.core.command.FtpArgumentCode.TransferType;
import org.waarp.ftp.core.config.FtpConfiguration;

/**
 * Pipeline Factory for Data Network.
 * 
 * @author Frederic Bregier
 * 
 */
public class FtpDataInitializer extends ChannelInitializer<SocketChannel> {
    /**
     * Mode Codec
     */
    public static final String CODEC_MODE = "MODE";

    /**
     * Limit Codec
     */
    public static final String CODEC_LIMIT = "LIMITATION";

    /**
     * Type Codec
     */
    public static final String CODEC_TYPE = "TYPE";

    /**
     * Structure Codec
     */
    public static final String CODEC_STRUCTURE = "STRUCTURE";

    /**
     * Pipeline Executor Codec
     */
    public static final String PIPELINE_EXECUTOR = "pipelineExecutor";

    /**
     * Handler Codec
     */
    public static final String HANDLER = "handler";

    protected static final FtpDataTypeCodec ftpDataTypeCodec = new FtpDataTypeCodec(
            TransferType.ASCII, TransferSubType.NONPRINT);

    protected static final FtpDataStructureCodec ftpDataStructureCodec = new FtpDataStructureCodec(
            TransferStructure.FILE);

    /**
     * Business Handler Class
     */
    protected final Class<? extends DataBusinessHandler> dataBusinessHandler;

    /**
     * Configuration
     */
    protected final FtpConfiguration configuration;

    /**
     * Is this factory for Active mode
     */
    protected final boolean isActive;

    /**
     * Constructor which Initializes some data
     * 
     * @param dataBusinessHandler
     * @param configuration
     * @param active
     */
    public FtpDataInitializer(
            Class<? extends DataBusinessHandler> dataBusinessHandler,
            FtpConfiguration configuration, boolean active) {
        this.dataBusinessHandler = dataBusinessHandler;
        this.configuration = configuration;
        isActive = active;
    }

    /**
     * Create the pipeline with Handler, ObjectDecoder, ObjectEncoder.
     * 
     */
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // Add default codec but they will change during the channelConnected
        pipeline.addFirst(CODEC_MODE, new FtpDataModeCodec(TransferMode.STREAM,
                TransferStructure.FILE));
        pipeline.addLast(CODEC_LIMIT, configuration
                .getFtpInternalConfiguration()
                .getGlobalTrafficShapingHandler());
        ChannelTrafficShapingHandler limitChannel =
                configuration
                        .getFtpInternalConfiguration()
                        .newChannelTrafficShapingHandler();
        if (limitChannel != null) {
            pipeline.addLast(CODEC_LIMIT + "CHANNEL", limitChannel);
        }
        pipeline.addLast(CODEC_TYPE, ftpDataTypeCodec);
        pipeline.addLast(CODEC_STRUCTURE, ftpDataStructureCodec);
        // and then business logic. New one on every connection
        DataBusinessHandler newbusiness = dataBusinessHandler.newInstance();
        DataNetworkHandler newNetworkHandler = new DataNetworkHandler(
                configuration, newbusiness, isActive);
        pipeline.addLast(configuration.getFtpInternalConfiguration().getDataExecutor(),
                HANDLER, newNetworkHandler);
    }
}
