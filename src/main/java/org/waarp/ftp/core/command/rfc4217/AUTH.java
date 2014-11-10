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
package org.waarp.ftp.core.command.rfc4217;

import org.waarp.common.command.ReplyCode;
import org.waarp.common.command.exception.CommandAbstractException;
import org.waarp.common.command.exception.Reply501Exception;
import org.waarp.common.command.exception.Reply503Exception;
import org.waarp.common.command.exception.Reply504Exception;
import org.waarp.common.command.exception.Reply534Exception;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.ftp.core.command.AbstractCommand;

/**
 * AUTH command with 2 options TLS or SSL<br>
 * <br>
 * Security Association Setup AUTH TLS (Control) or AUTH SSL (Control and Data)<br>
 * 234*<br>
 * 502, 504, 534*, 431* 500, 501, 421<br>
 * <br>
 * AUTH TLS -> 234 -> USER or ([PBSZ 0] PROT P then USER) -> 2xy
 * 
 * 
 * @author Frederic Bregier
 * 
 */
public class AUTH extends AbstractCommand {
    /**
     * Internal Logger
     */
    private static final WaarpLogger logger = WaarpLoggerFactory
            .getLogger(AUTH.class);

    @Override
    public void exec() throws CommandAbstractException {
        if (!getSession().getConfiguration().getFtpInternalConfiguration().isAcceptAuthProt()) {
            throw new Reply534Exception("AUTH SSL / TLS not supported");
        }
        if (getSession().isSsl()) {
            // Already SSL
            throw new Reply503Exception("Session already using SSL / TLS");
        }
        // First Check if any argument
        if (!hasArg()) {
            // Error since argument is needed
            throw new Reply501Exception("Missing Parameter: TLS or SSL");
        }
        String[] types = getArgs();
        if (types[0].equalsIgnoreCase("TLS")) {
            // Only Command will have SSL
            logger.debug("Start TLS");
            getSession().rein();
            getSession().setNextCommand(this);
            getSession().setReplyCode(ReplyCode.REPLY_234_SECURITY_DATA_EXCHANGE_COMPLETE,
                    null);
        } else if (types[0].equalsIgnoreCase("SSL")) {
            // Both Command and Data will have SSL
            logger.debug("Start SSL");
            getSession().rein();
            getSession().setNextCommand(this);
            getSession().setReplyCode(ReplyCode.REPLY_234_SECURITY_DATA_EXCHANGE_COMPLETE,
                    null);
            getSession().setDataSsl(true);
        } else {
            throw new Reply504Exception("Unknown Parameter: " + types[0]);
        }
        logger.debug("End of AUTH " + types[0]);
    }

}
