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
package org.waarp.ftp.core.data.handler.ftps;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.SslHandler;

import org.waarp.common.crypto.ssl.WaarpSslUtility;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.ftp.core.config.FtpConfiguration;
import org.waarp.ftp.core.control.ftps.FtpsInitializer;
import org.waarp.ftp.core.data.handler.DataBusinessHandler;
import org.waarp.ftp.core.data.handler.DataNetworkHandler;
import org.waarp.ftp.core.utils.FtpChannelUtils;

/**
 * @author "Frederic Bregier"
 *
 */
public class SslDataNetworkHandler extends DataNetworkHandler {
	/**
	 * Internal Logger
	 */
	private static final WaarpLogger logger = WaarpLoggerFactory
			.getLogger(SslDataNetworkHandler.class);

	/**
	 * @param configuration
	 * @param handler
	 * @param active
	 */
	public SslDataNetworkHandler(FtpConfiguration configuration, DataBusinessHandler handler,
			boolean active) {
		super(configuration, handler, active);
	}

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		Channel channel = ctx.channel();
		logger.debug("Add channel to ssl");
		super.channelRegistered(ctx);
		channel.config().setAutoRead(false);
	}
	
	/**
	 * To be extended to inform of an error to SNMP support
	 * @param error1
	 * @param error2
	 */
	protected void callForSnmp(String error1, String error2) {
		// ignore
	}

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// Get the SslHandler in the current pipeline.
		Channel channel = ctx.channel();
		if (session == null) {
			setSession(channel);
		}
		if (session == null) {
			logger.error("Cannot find session for SSL");
			channel.close();
			return;
		}
		// Server: no renegotiation still, but possible clientAuthent
		// Mode is always as SSL Server mode.
        SslHandler sslHandler = 
                FtpsInitializer.waarpSslContextFactory.initInitializer(true,
                        FtpsInitializer.waarpSslContextFactory.needClientAuthentication(),
                        FtpChannelUtils.getRemoteInetSocketAddress(session.getControlChannel()).getAddress().getHostAddress(),
                        FtpChannelUtils.getRemoteInetSocketAddress(session.getControlChannel()).getPort());
        channel.pipeline().addFirst("ssl", sslHandler);
        channel.config().setAutoRead(true);
		WaarpSslUtility.addSslOpenedChannel(channel);
		// Get the SslHandler and begin handshake ASAP.
		logger.debug("SSL found but need handshake");
		// Fix where adding the handler is somehow slower than ready to process...
		Thread.sleep(10);
        if (! WaarpSslUtility.waitForHandshake(ctx.channel())) {
            callForSnmp("SSL Connection Error", "During Ssl Handshake");
        }
        logger.debug("End of initialization of SSL and data channel");
        super.channelActive(ctx);
	}

}
