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
package org.waarp.ftp.core.command.rfc2428;

import java.net.InetSocketAddress;

import org.waarp.common.command.ReplyCode;
import org.waarp.common.command.exception.Reply501Exception;
import org.waarp.common.command.exception.Reply522Exception;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.ftp.core.command.AbstractCommand;
import org.waarp.ftp.core.utils.FtpChannelUtils;

/**
 * EPRT command
 * 
 * @author Frederic Bregier
 * 
 */
public class EPRT extends AbstractCommand {
    private static final WaarpLogger logger = WaarpLoggerFactory.getInstance(EPRT.class);

    @Override
    public void exec() throws Reply501Exception, Reply522Exception {
        // First Check if any argument
        if (!hasArg()) {
            InetSocketAddress inetSocketAddress = getSession().getDataConn().getRemoteAddress();
            logger.debug("Active connect to " + inetSocketAddress);
            getSession().getDataConn().setActive(inetSocketAddress);
            getSession().setReplyCode(
                    ReplyCode.REPLY_200_COMMAND_OKAY,
                    "EPRT command successful on (" +
                            FtpChannelUtils.get2428Address(inetSocketAddress) +
                            ")");
            return;
        }
        // Check if Inet Address is OK

        InetSocketAddress inetSocketAddress = FtpChannelUtils.get2428InetSocketAddress(getArg());
        if (inetSocketAddress == null) {
            // ERROR
            throw new Reply522Exception("Can't get SocketAddress from " + getArg());
        }
        // No Check if the Client address is the same as given
        // OK now try to initialize connection (not open)
        logger.debug("Active connect to " + inetSocketAddress);
        getSession().getDataConn().setActive(inetSocketAddress);
        getSession()
                .setReplyCode(
                        ReplyCode.REPLY_200_COMMAND_OKAY,
                        "EPRT command successful on (" +
                                FtpChannelUtils
                                        .get2428Address(inetSocketAddress) +
                                ")");
    }
}
