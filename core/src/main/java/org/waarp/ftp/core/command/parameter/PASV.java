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
package org.waarp.ftp.core.command.parameter;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.waarp.common.command.ReplyCode;
import org.waarp.common.command.exception.Reply425Exception;
import org.waarp.common.command.exception.Reply501Exception;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.ftp.core.command.AbstractCommand;
import org.waarp.ftp.core.config.FtpInternalConfiguration;
import org.waarp.ftp.core.data.FtpDataAsyncConn;
import org.waarp.ftp.core.utils.FtpChannelUtils;

/**
 * PASV command
 * 
 * @author Frederic Bregier
 * 
 */
public class PASV extends AbstractCommand {
    /**
     * Internal Logger
     */
    private static final WaarpLogger logger = WaarpLoggerFactory
            .getLogger(PASV.class);

    @Override
    public void exec() throws Reply425Exception, Reply501Exception {
        // First Check if any argument
        if (hasArg()) {
            throw new Reply501Exception("No argument allowed");
        }
        // Take a new port: 3 attempts
        boolean isInit = false;
        for (int i = 1; i <= FtpInternalConfiguration.RETRYNB; i++) {
            int newport = FtpDataAsyncConn
                    .getNewPassivePort(getConfiguration());
            if (newport == -1) {
                throw new Reply425Exception("No port available");
            }
            if (getSession().getDataConn().isPassiveMode()) {
                // Previous mode was Passive so remove the current configuration
                InetSocketAddress local = getSession().getDataConn()
                        .getLocalAddress();
                InetAddress remote = getSession().getDataConn()
                        .getRemoteAddress().getAddress();
                getConfiguration().delFtpSession(remote, local);
            }
            logger.info("PASV: set Passive Port {}", newport);
            getSession().getDataConn().setLocalPort(newport);
            getSession().getDataConn().setPassive();
            // Init the connection
            try {
                if (getSession().getDataConn().initPassiveConnection()) {
                    isInit = true;
                    break;
                }
            } catch (Reply425Exception e) {
                logger.warn("Pasv refused at try: " + i +
                        " with port:  since {}" + newport, e.getMessage());
            }
        }
        if (!isInit) {
            throw new Reply425Exception("Passive mode not started");
        }
        // Return the address in Ftp format
        InetSocketAddress local = getSession().getDataConn().getLocalAddress();
        int servPort = local.getPort();
        String address = getSession().getConfiguration().getServerAddress();
        if (address == null) {
            address = local.getAddress().getHostAddress();
        }
        String slocal = "Entering Passive Mode (" +
                FtpChannelUtils.getAddress(address, servPort) + ")";
        InetAddress remote = getSession().getDataConn().getRemoteAddress()
                .getAddress();
        // Add the current FtpSession into the reference of session since the
        // client will open the connection
        getConfiguration().setNewFtpSession(remote, local, getSession());
        // prepare the validation of the next connection
        getSession().getDataConn().getFtpTransferControl().resetWaitForOpenedDataChannel();
        getSession().setReplyCode(ReplyCode.REPLY_227_ENTERING_PASSIVE_MODE,
                slocal);
        logger.info("PASV: answer ready on {}", slocal);
    }

}
