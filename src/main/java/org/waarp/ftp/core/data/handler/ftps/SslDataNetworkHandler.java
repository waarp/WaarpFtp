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

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.ssl.SslHandler;
import org.waarp.common.logging.WaarpInternalLogger;
import org.waarp.common.logging.WaarpInternalLoggerFactory;
import org.waarp.ftp.core.config.FtpConfiguration;
import org.waarp.ftp.core.control.ftps.FtpsPipelineFactory;
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
	private static final WaarpInternalLogger logger = WaarpInternalLoggerFactory
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
	/**
	 * Remover from SSL HashMap
	 */
	private static final ChannelFutureListener remover = new ChannelFutureListener() {
		public void operationComplete(
				ChannelFuture future) {
			logger.debug("SSL remover");
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
		logger.debug("Add channel to ssl");
		addSslConnectedChannel(channel);
		super.channelOpen(ctx, e);
		channel.setReadable(false);
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
		Channel channel = e.getChannel();
		if (session == null) {
			setSession(channel);
		}
		if (session == null) {
			logger.error("Cannot find session for SSL");
			Channels.close(channel);
			return;
		}
		if (configuration.getFtpInternalConfiguration().isAcceptAuthProt()) {
			// Server: no renegotiation still, but possible clientAuthent
			SslHandler sslHandler = 
					FtpsPipelineFactory.waarpSslContextFactory.initPipelineFactory(true,
							FtpsPipelineFactory.waarpSslContextFactory.needClientAuthentication(),
							false, FtpChannelUtils.getRemoteInetSocketAddress(session.getControlChannel()).getAddress().getHostAddress(),
							FtpChannelUtils.getRemoteInetSocketAddress(session.getControlChannel()).getPort(),
							configuration.getFtpInternalConfiguration().getWorker());
			channel.getPipeline().addFirst("ssl", sslHandler);
		}
		channel.setReadable(true);
		logger.debug("SSL to be found");
		final SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);
		if (sslHandler != null) {
			// Get the SslHandler and begin handshake ASAP.
			logger.debug("SSL found but need handshake");
			ChannelFuture handshakeFuture = sslHandler.handshake();
			if (configuration.getFtpInternalConfiguration().isAcceptAuthProt()) {
				try {
					handshakeFuture.await();
				} catch (InterruptedException e1) {
				}
			}

			// XXX FIXME note: if we wait for handshake, ftp client blocks !
			logger.debug("SSL found");
		} else {
			logger.error("SSL Not found");
			// Cannot continue
			setSession(e.getChannel());
			if (session != null) {
				session.getDataConn().getFtpTransferControl().setOpenedDataChannel(
					null, this);
			}
			return;
		}
		logger.debug("Now continue with normal task");
		super.channelConnected(ctx, e);
		logger.debug("End of initialization of SSL and data channel");
	}

}
