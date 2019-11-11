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

import org.waarp.common.command.ReplyCode;
import org.waarp.common.command.exception.Reply501Exception;
import org.waarp.common.command.exception.Reply504Exception;
import org.waarp.common.exception.InvalidArgumentException;
import org.waarp.ftp.core.command.AbstractCommand;
import org.waarp.ftp.core.command.FtpArgumentCode;

/**
 * MODE command
 * 
 * @author Frederic Bregier
 * 
 */
public class MODE extends AbstractCommand {
    @Override
    public void exec() throws Reply501Exception, Reply504Exception {
        // First Check if any argument
        if (!hasArg()) {
            // Default
            getSession().getDataConn().setMode(
                    FtpArgumentCode.TransferMode.STREAM);
            getSession()
                    .setReplyCode(
                            ReplyCode.REPLY_200_COMMAND_OKAY,
                            "Mode set to " +
                                    FtpArgumentCode.TransferMode.STREAM.name());
            return;
        }
        FtpArgumentCode.TransferMode transferMode;
        try {
            transferMode = FtpArgumentCode.getTransferMode(getArg().charAt(0));
        } catch (InvalidArgumentException e) {
            throw new Reply501Exception("Unrecognize Mode: " + getArg());
        }
        if (transferMode == FtpArgumentCode.TransferMode.BLOCK) {
            getSession().getDataConn().setMode(transferMode);
        } else if (transferMode == FtpArgumentCode.TransferMode.STREAM) {
            getSession().getDataConn().setMode(transferMode);
        } else {
            throw new Reply504Exception("Mode not implemented: " +
                    transferMode.name());
        }
        getSession().setReplyCode(ReplyCode.REPLY_200_COMMAND_OKAY,
                "Mode set to " + transferMode.name());
    }

}
