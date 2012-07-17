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

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.handler.ssl.SslHandler;
import org.waarp.common.logging.WaarpInternalLogger;
import org.waarp.common.logging.WaarpInternalLoggerFactory;
import org.waarp.ftp.core.control.NetworkHandler;
import org.waarp.ftp.core.session.FtpSession;

/**
 * @author "Frederic Bregier"
 *
 */
public class SslNetworkHandler extends NetworkHandler {
	/**
	 * Internal Logger
	 */
	private static final WaarpInternalLogger logger = WaarpInternalLoggerFactory
			.getLogger(SslNetworkHandler.class);

	/**
	 * @param session
	 */
	public SslNetworkHandler(FtpSession session) {
		super(session);
	}

	/**
	 * Remover from SSL HashMap
	 */
	private static final ChannelFutureListener remover = new ChannelFutureListener() {
		public void operationComplete(
				ChannelFuture future) {
			logger.debug("SSL finishing: "+future.getChannel().getId());
		}
	};

	/**
	 * Add the Channel as SSL handshake is over
	 * 
	 * @param channel
	 */
	private static void addSslConnectedChannel(Channel channel) {
		channel.getCloseFuture().addListener(remover);
	}


	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		Channel channel = e.getChannel();
		logger.debug("Add channel to ssl " +channel.getId());
		addSslConnectedChannel(channel);
		super.channelOpen(ctx, e);
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
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		// Get the SslHandler in the current pipeline.
		// We added it in NetworkSslServerPipelineFactory.
		final SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);
		if (sslHandler != null) {
			// Get the SslHandler and begin handshake ASAP.
			// Get notified when SSL handshake is done.
			ChannelFuture handshakeFuture;
			handshakeFuture = sslHandler.handshake();
			try {
				handshakeFuture.await();
			} catch (InterruptedException e1) {
			}
			logger.debug("Handshake: " + handshakeFuture.isSuccess(), handshakeFuture.getCause());
			if (!handshakeFuture.isSuccess()) {
				String error2 = handshakeFuture.getCause() != null ?
						handshakeFuture.getCause().getMessage() : "During Handshake";
				callForSnmp("SSL Connection Error", error2);
				handshakeFuture.getChannel().close();
				return;
			}
		} else {
			logger.error("SSL Not found");
		}
		super.channelConnected(ctx, e);
	}

}
