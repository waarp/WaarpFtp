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
import org.waarp.ftp.core.command.AbstractCommand;
import org.waarp.ftp.core.command.FtpCommandCode;

/**
 * PBSZ command accepting only 0 as parameter
 * 
 * @author Frederic Bregier
 * 
 */
public class PBSZ extends AbstractCommand {

    @Override
    public void exec() throws CommandAbstractException {
        if (!getSession().isSslReady()) {
            // Not SSL
            throw new Reply503Exception("Session not using SSL / TLS");
        }
        // First Check if any argument
        if (!hasArg()) {
            // Error since argument is needed
            throw new Reply501Exception("Missing Parameter: 0");
        }
        String[] types = getArgs();
        if (!types[0].equalsIgnoreCase("0")) {
            // Only 0 allowed
            throw new Reply501Exception("Unknown Parameter: " + types[0]);
        }
        setExtraNextCommand(FtpCommandCode.PROT);
        getSession().setReplyCode(ReplyCode.REPLY_200_COMMAND_OKAY,
                null);
    }

}
