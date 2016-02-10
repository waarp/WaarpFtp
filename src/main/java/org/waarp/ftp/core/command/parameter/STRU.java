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
 * STRU command
 * 
 * @author Frederic Bregier
 * 
 */
public class STRU extends AbstractCommand {
    @Override
    public void exec() throws Reply501Exception, Reply504Exception {
        // First Check if any argument
        if (!hasArg()) {
            getSession().getDataConn().setStructure(
                    FtpArgumentCode.TransferStructure.FILE);
            getSession().setReplyCode(
                    ReplyCode.REPLY_200_COMMAND_OKAY,
                    "Structure set to " +
                            FtpArgumentCode.TransferStructure.FILE.name());
            return;
        }
        FtpArgumentCode.TransferStructure transferStructure;
        try {
            transferStructure = FtpArgumentCode.getTransferStructure(getArg()
                    .charAt(0));
        } catch (InvalidArgumentException e) {
            throw new Reply501Exception("Unrecognize Structure: " + getArg());
        }
        if (transferStructure == FtpArgumentCode.TransferStructure.FILE) {
            getSession().getDataConn().setStructure(transferStructure);
        } else if (transferStructure == FtpArgumentCode.TransferStructure.RECORD) {
            getSession().getDataConn().setStructure(transferStructure);
        } else {
            throw new Reply504Exception("Structure not implemented: " +
                    transferStructure.name());
        }
        getSession().setReplyCode(ReplyCode.REPLY_200_COMMAND_OKAY,
                "Structure set to " + transferStructure.name());
    }

}
