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
package goldengate.ftp.core.command.info;

import goldengate.common.command.ReplyCode;
import goldengate.common.command.exception.CommandAbstractException;
import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.exception.FtpNoFileException;
import goldengate.ftp.core.exception.FtpNoTransferException;
import goldengate.ftp.core.session.FtpFile;
import goldengate.ftp.core.utils.FtpChannelUtils;

import java.util.List;

/**
 * STAT command
 *
 * @author Frederic Bregier
 *
 */
public class STAT extends AbstractCommand {

    /*
     * (non-Javadoc)
     *
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    public void exec() throws CommandAbstractException {
        String path = null;
        String message = null;
        message = "STATUS information\nNo FtpFile currently in transfer\n";
        FtpFile file = null;
        try {
            file = getSession().getDataConn().getFtpTransferControl()
                    .getExecutingFtpTransfer().getFtpFile();
        } catch (FtpNoFileException e) {
        } catch (FtpNoTransferException e) {
        }
        if (file != null) {
            if (file.isInReading()) {
                message = "STATUS information\nFile currently in Retrieve transfer\n";
            } else if (file.isInWriting()) {
                message = "STATUS information\nFile currently in Store transfer\n";
            }
        }
        if (!hasArg()) {
            // Current status of connection
            message += getSession().getDataConn().getStatus();
            message += "\nControl: " +
                    FtpChannelUtils.nbCommandChannels(getConfiguration()) +
                    " Data: " +
                    FtpChannelUtils.nbDataChannels(getConfiguration()) +
                    " Binded: " +
                    getConfiguration().getFtpInternalConfiguration()
                            .getNbBindedPassive();
            message += "\nEnd of Status";
            getSession().setReplyCode(ReplyCode.REPLY_211_SYSTEM_STATUS_REPLY,
                    message);
        } else {
            // List of files from path
            path = getArg();
            List<String> filesInfo = getSession().getDir().listFull(path, true);
            StringBuilder builder = new StringBuilder();
            builder.append("List of files from ");
            builder.append(path);
            builder.append('\n');
            for (String newfileInfo: filesInfo) {
                builder.append(newfileInfo);
                builder.append('\n');
            }
            builder.append("End of Status");
            message += builder.toString();
            getSession().setReplyCode(ReplyCode.REPLY_212_DIRECTORY_STATUS,
                    message);
        }
    }

}
