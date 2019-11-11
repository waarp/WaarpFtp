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
package org.waarp.ftp.core.command.info;

import java.util.List;

import org.waarp.common.command.ReplyCode;
import org.waarp.common.command.exception.CommandAbstractException;
import org.waarp.ftp.core.command.AbstractCommand;
import org.waarp.ftp.core.exception.FtpNoFileException;
import org.waarp.ftp.core.exception.FtpNoTransferException;
import org.waarp.ftp.core.file.FtpFile;
import org.waarp.ftp.core.utils.FtpChannelUtils;

/**
 * STAT command
 * 
 * @author Frederic Bregier
 * 
 */
public class STAT extends AbstractCommand {
    @Override
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
            StringBuilder builder = new StringBuilder().append("List of files from ").append(path).append('\n');
            for (String newfileInfo : filesInfo) {
                builder.append(newfileInfo).append('\n');
            }
            builder.append("End of Status");
            message += builder.toString();
            getSession().setReplyCode(ReplyCode.REPLY_212_DIRECTORY_STATUS,
                    message);
        }
    }

}
