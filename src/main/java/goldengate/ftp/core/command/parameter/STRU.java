/**
 * Copyright 2009, Frederic Bregier, and individual contributors
 * by the @author tags. See the COPYRIGHT.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package goldengate.ftp.core.command.parameter;

import goldengate.common.command.ReplyCode;
import goldengate.common.command.exception.Reply501Exception;
import goldengate.common.command.exception.Reply504Exception;
import goldengate.common.exception.InvalidArgumentException;
import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpArgumentCode;

/**
 * STRU command
 *
 * @author Frederic Bregier
 *
 */
public class STRU extends AbstractCommand {

    /*
     * (non-Javadoc)
     *
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
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
            transferStructure = FtpArgumentCode.getTransferStructure(getArg().charAt(0));
        } catch (InvalidArgumentException e) {
            throw new Reply501Exception("Unrecognize Structure: " +
                    getArg());
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
