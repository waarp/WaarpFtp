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
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.handler.ssl.SslHandler;
import org.waarp.common.crypto.ssl.WaarpSslUtility;
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


	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		Channel channel = e.getChannel();
		logger.debug("Add channel to ssl " +channel.getId());
		WaarpSslUtility.addSslOpenedChannel(channel);
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
		// Get the SslHandler in the current pipeline.
		// We added it in NetworkSslServerPipelineFactory.
		final ChannelHandler handler = ctx.getPipeline().getFirst();
		if (handler instanceof SslHandler) {
			final SslHandler sslHandler = (SslHandler) handler;
			if (sslHandler.isIssueHandshake()) {
				// client side
				WaarpSslUtility.setStatusSslConnectedChannel(ctx.getChannel(), true);
			} else {
				// server side
				// Get the SslHandler and begin handshake ASAP.
				// Get notified when SSL handshake is done.
				if (! WaarpSslUtility.runHandshake(ctx.getChannel())) {
					callForSnmp("SSL Connection Error", "During Ssl Handshake");
				}
			}				
		} else {
			logger.error("SSL Not found");
		}
		getFtpSession().setSsl(true);
		super.channelConnected(ctx, e);
	}

}
