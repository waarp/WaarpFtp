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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.util.concurrent.EventExecutorGroup;

import org.waarp.common.command.ReplyCode;
import org.waarp.common.utility.WaarpStringUtils;
import org.waarp.ftp.core.config.FtpConfiguration;
import org.waarp.ftp.core.session.FtpSession;

/**
 * Pipeline factory for Control command connection
 * 
 * @author Frederic Bregier
 * 
 */
public class FtpInitializer extends ChannelInitializer<SocketChannel> {
    /**
     * CRLF, CRNUL, LF delimiters
     */
    protected static final ByteBuf[] delimiter = new ByteBuf[] {
            Unpooled.wrappedBuffer(ReplyCode.CRLF.getBytes(WaarpStringUtils.UTF8)),
            Unpooled.wrappedBuffer(ReplyCode.CRNUL.getBytes(WaarpStringUtils.UTF8)),
            Unpooled.wrappedBuffer(ReplyCode.LF.getBytes(WaarpStringUtils.UTF8)) };

    protected static final FtpControlStringDecoder ftpControlStringDecoder = new FtpControlStringDecoder(
            WaarpStringUtils.UTF8);

    protected static final FtpControlStringEncoder ftpControlStringEncoder = new FtpControlStringEncoder(
            WaarpStringUtils.UTF8);

    /**
     * Business Handler Class if any (Target Mode only)
     */
    protected final Class<? extends BusinessHandler> businessHandler;

    /**
     * Configuration
     */
    protected final FtpConfiguration configuration;

    /**
     * Constructor which Initializes some data for Server only
     * 
     * @param businessHandler
     * @param configuration
     */
    public FtpInitializer(Class<? extends BusinessHandler> businessHandler,
            FtpConfiguration configuration) {
        this.businessHandler = businessHandler;
        this.configuration = configuration;
    }

    /**
     * Create the pipeline with Handler, ObjectDecoder, ObjectEncoder.
     */
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // Add the text line codec combination first,
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, delimiter));
        pipeline.addLast("decoder", ftpControlStringDecoder);
        pipeline.addLast("encoder", ftpControlStringEncoder);
        // Threaded execution for business logic
        EventExecutorGroup executorGroup = configuration.getFtpInternalConfiguration().getExecutor();
        // and then business logic. New one on every connection
        BusinessHandler newbusiness = businessHandler.newInstance();
        NetworkHandler newNetworkHandler = new NetworkHandler(new FtpSession(configuration, newbusiness));
        pipeline.addLast(executorGroup, "handler", newNetworkHandler);
    }
}
