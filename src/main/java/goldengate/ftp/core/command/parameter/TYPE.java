/**
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3.0 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package goldengate.ftp.core.command.parameter;

import goldengate.common.command.ReplyCode;
import goldengate.common.command.exception.Reply501Exception;
import goldengate.common.command.exception.Reply504Exception;
import goldengate.common.exception.InvalidArgumentException;
import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpArgumentCode;
import goldengate.ftp.core.command.FtpArgumentCode.TransferSubType;

/**
 * TYPE command
 *
 * @author Frederic Bregier
 *
 */
public class TYPE extends AbstractCommand {

    /*
     * (non-Javadoc)
     *
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    public void exec() throws Reply501Exception, Reply504Exception {
        // First Check if any argument
        if (!hasArg()) {
            getSession().getDataConn().setType(
                    FtpArgumentCode.TransferType.ASCII);
            getSession().getDataConn().setSubType(TransferSubType.NONPRINT);
            getSession().setReplyCode(
                    ReplyCode.REPLY_200_COMMAND_OKAY,
                    "Type set to " + FtpArgumentCode.TransferType.ASCII.name() +
                            " " + TransferSubType.NONPRINT);
            return;
        }
        FtpArgumentCode.TransferType transferType;
        String[] types = getArgs();
        try {
            transferType = FtpArgumentCode.getTransferType(types[0].charAt(0));
        } catch (InvalidArgumentException e) {
            throw new Reply501Exception("Unrecognize Type: " + getArg());
        }
        if (transferType == FtpArgumentCode.TransferType.ASCII) {
            getSession().getDataConn().setType(transferType);
        } else if (transferType == FtpArgumentCode.TransferType.IMAGE) {
            getSession().getDataConn().setType(transferType);
        } else {
            throw new Reply504Exception("Type not implemented: " +
                    transferType.name());
        }
        // Look at the subtype or format control
        if (types.length > 2) {
            TransferSubType transferSubType = null;
            for (int i = 1; i < types.length; i ++) {
                if (types[i].length() != 0) {
                    try {
                        transferSubType = FtpArgumentCode
                                .getTransferSubType(types[i].charAt(0));
                    } catch (InvalidArgumentException e) {
                        throw new Reply501Exception(
                                "Unrecognize Format Control: " + types[i]);
                    }
                    if (transferSubType != TransferSubType.NONPRINT) {
                        throw new Reply504Exception(
                                "Format Control not implemented: " +
                                        transferSubType.name());
                    }
                }
            }
            getSession().getDataConn().setSubType(TransferSubType.NONPRINT);
        } else {
            getSession().getDataConn().setSubType(TransferSubType.NONPRINT);
        }
        getSession().setReplyCode(
                ReplyCode.REPLY_200_COMMAND_OKAY,
                "Type set to " + transferType.name() + " " +
                        TransferSubType.NONPRINT);
    }

}
