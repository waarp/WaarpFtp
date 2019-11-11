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
import org.waarp.ftp.core.command.AbstractCommand;
import org.waarp.ftp.core.command.FtpCommandCode;

/**
 * PROT command accepting only C or P argument
 * 
 * @author Frederic Bregier
 * 
 */
public class PROT extends AbstractCommand {

    @Override
    public void exec() throws CommandAbstractException {
        if (!getSession().isSslReady()) {
            // Not in SSL
            throw new Reply503Exception("Session not using SSL / TLS");
        }
        // First Check if any argument
        if (!hasArg()) {
            // Error since argument is needed
            throw new Reply501Exception("Missing Parameter: P or C");
        }
        String[] types = getArgs();
        if (types[0].equalsIgnoreCase("P")) {
            if (getSession().isDataSsl()
                    && getSession().getConfiguration().getFtpInternalConfiguration().isAcceptAuthProt()) {
                // Already SSL
                throw new Reply503Exception("Data already using SSL / TLS");
            }
            // Data will have SSL
            getSession().setDataSsl(true);
            getSession().setReplyCode(ReplyCode.REPLY_200_COMMAND_OKAY,
                    null);
        } else if (types[0].equalsIgnoreCase("C")
                && !getSession().getConfiguration().getFtpInternalConfiguration().isAcceptAuthProt()) {
            if (!getSession().isDataSsl()) {
                // Not in SSL
                throw new Reply503Exception("Data already not using SSL / TLS");
            }
            getSession().setDataSsl(false);
            getSession().setReplyCode(ReplyCode.REPLY_200_COMMAND_OKAY,
                    null);
        } else if (!getSession().getConfiguration().getFtpInternalConfiguration().isAcceptAuthProt()) {
            throw new Reply503Exception("Data is using SSL / TLS and cannot be removed due to Implicit mode");
        } else {
            throw new Reply504Exception("Unknown Parameter: " + types[0]);
        }
        if (!getSession().getAuth().isIdentified()) {
            setExtraNextCommand(FtpCommandCode.AUTH);
        }
    }

}
