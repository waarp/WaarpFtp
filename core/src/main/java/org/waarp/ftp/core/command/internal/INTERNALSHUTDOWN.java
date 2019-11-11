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
package org.waarp.ftp.core.command.internal;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.waarp.common.command.ReplyCode;
import org.waarp.common.command.exception.Reply500Exception;
import org.waarp.common.command.exception.Reply501Exception;
import org.waarp.common.crypto.ssl.WaarpSslUtility;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.ftp.core.command.AbstractCommand;
import org.waarp.ftp.core.config.FtpConfiguration;
import org.waarp.ftp.core.utils.FtpChannelUtils;

/**
 * Internal shutdown command that will shutdown the FTP service with a password
 * 
 * @author Frederic Bregier
 * 
 */
public class INTERNALSHUTDOWN extends AbstractCommand {
    /**
     * Internal Logger
     */
    private static final WaarpLogger logger = WaarpLoggerFactory
            .getLogger(INTERNALSHUTDOWN.class);

    /**
     * 
     * @author Frederic Bregier
     * 
     */
    private static class ShutdownChannelFutureListener implements
            ChannelFutureListener {

        private final FtpConfiguration configuration;

        protected ShutdownChannelFutureListener(FtpConfiguration configuration) {
            this.configuration = configuration;
        }

        public void operationComplete(ChannelFuture arg0) throws Exception {
            WaarpSslUtility.closingSslChannel(arg0.channel());
            FtpChannelUtils.teminateServer(configuration);
        }

    }

    @Override
    public void exec() throws Reply501Exception, Reply500Exception {
        if (!getSession().getAuth().isAdmin()) {
            // not admin
            throw new Reply500Exception("Command Not Allowed");
        }
        if (!hasArg()) {
            throw new Reply501Exception("Shutdown Need password");
        }
        String password = getArg();
        if (!getConfiguration().checkPassword(password)) {
            throw new Reply501Exception("Shutdown Need a correct password");
        }
        logger.warn("Shutdown...");
        getSession().setReplyCode(
                ReplyCode.REPLY_221_CLOSING_CONTROL_CONNECTION,
                "System shutdown");
        getSession().getNetworkHandler().writeIntermediateAnswer().addListener(
                new ShutdownChannelFutureListener(getConfiguration()));
    }

}
