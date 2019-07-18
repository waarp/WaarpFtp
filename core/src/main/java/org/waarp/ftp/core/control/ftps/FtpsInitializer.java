/**
   This file is part of Waarp Project.

   Copyright 2009, Frederic Bregier, and individual contributors by the @author
   tags. See the COPYRIGHT.txt in the distribution for a full listing of
   individual contributors.

   All Waarp Project is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Waarp is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Waarp .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.waarp.ftp.core.control.ftps;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.EventExecutorGroup;

import org.waarp.common.crypto.ssl.WaarpSecureKeyStore;
import org.waarp.common.crypto.ssl.WaarpSslContextFactory;
import org.waarp.ftp.core.config.FtpConfiguration;
import org.waarp.ftp.core.control.BusinessHandler;
import org.waarp.ftp.core.control.FtpInitializer;
import org.waarp.ftp.core.session.FtpSession;

/**
 * @author "Frederic Bregier"
 *
 */
public class FtpsInitializer extends FtpInitializer {

    public static WaarpSslContextFactory waarpSslContextFactory;
    public static WaarpSecureKeyStore waarpSecureKeyStore;

    /**
     * Constructor which Initializes some data for Server only
     * 
     * @param businessHandler
     * @param configuration
     */
    public FtpsInitializer(Class<? extends BusinessHandler> businessHandler,
            FtpConfiguration configuration) {
        super(businessHandler, configuration);
    }

    /**
     * Create the pipeline with Handler, ObjectDecoder, ObjectEncoder.
     * 
     */
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // Server: no renegotiation still, but possible clientAuthent
        SslHandler handler = waarpSslContextFactory.initInitializer(true,
                waarpSslContextFactory.needClientAuthentication());
        pipeline.addLast("SSL", handler);
        // Add the text line codec combination first,
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, delimiter));
        pipeline.addLast("decoder", ftpControlStringDecoder);
        pipeline.addLast("encoder", ftpControlStringEncoder);
        // Threaded execution for business logic

        EventExecutorGroup executorGroup = configuration.getFtpInternalConfiguration().getExecutor();
        // and then business logic. New one on every connection
        BusinessHandler newbusiness = businessHandler.newInstance();
        SslNetworkHandler newNetworkHandler = new SslNetworkHandler(new FtpSession(
                configuration, newbusiness));
        pipeline.addLast(executorGroup, "handler", newNetworkHandler);
    }
}
