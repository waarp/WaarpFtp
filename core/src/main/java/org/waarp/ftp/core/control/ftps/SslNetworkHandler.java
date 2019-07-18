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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import org.waarp.common.crypto.ssl.WaarpSslUtility;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
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
    private static final WaarpLogger logger = WaarpLoggerFactory.getLogger(SslNetworkHandler.class);

    /**
     * @param session
     */
    public SslNetworkHandler(FtpSession session) {
        super(session);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        logger.debug("Add channel to ssl " + channel.toString());
        WaarpSslUtility.addSslOpenedChannel(channel);
        getFtpSession().prepareSsl();
        super.channelRegistered(ctx);
    }

    /**
     * To be extended to inform of an error to SNMP support
     * 
     * @param error1
     * @param error2
     */
    protected void callForSnmp(String error1, String error2) {
        // ignore
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        if (!WaarpSslUtility.waitForHandshake(ctx.channel())) {
            callForSnmp("SSL Connection Error", "During Ssl Handshake");
            getFtpSession().setSsl(false);
            return;
        } else {
            getFtpSession().setSsl(true);
        }
        super.channelActive(ctx);
    }

}
