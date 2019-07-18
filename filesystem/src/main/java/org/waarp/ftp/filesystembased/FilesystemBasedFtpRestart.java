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
package org.waarp.ftp.filesystembased;

import org.waarp.common.command.exception.CommandAbstractException;
import org.waarp.common.command.exception.Reply502Exception;
import org.waarp.common.file.filesystembased.FilesystemBasedRestartImpl;
import org.waarp.ftp.core.command.FtpArgumentCode.TransferMode;
import org.waarp.ftp.core.command.FtpArgumentCode.TransferStructure;
import org.waarp.ftp.core.command.FtpArgumentCode.TransferType;
import org.waarp.ftp.core.data.FtpDataAsyncConn;
import org.waarp.ftp.core.session.FtpSession;

/**
 * Filesystem implementation of a Restart.<br>
 * Only FILE+STREAM is supported (byte position in file).
 * 
 * @author Frederic Bregier
 * 
 */
public class FilesystemBasedFtpRestart extends FilesystemBasedRestartImpl {
    /**
     * @param session
     */
    public FilesystemBasedFtpRestart(FtpSession session) {
        super(session);
    }

    @Override
    public boolean restartMarker(String marker) throws CommandAbstractException {
        FtpDataAsyncConn dataConn = ((FtpSession) getSession()).getDataConn();
        if (dataConn.getStructure() == TransferStructure.FILE &&
                dataConn.getMode() == TransferMode.STREAM &&
                dataConn.getType() != TransferType.LENGTH) {
            long newposition = 0;
            String[] args = marker.split(" ");
            try {
                newposition = Long.parseLong(args[0]);
                if (args.length > 1) {
                    limit = Integer.parseInt(args[1]);
                }
            } catch (NumberFormatException e) {
                throw new Reply502Exception(
                        "Marker must be length in byte as a position");
            }
            position = newposition;
            setSet(true);
            return true;
        }
        throw new Reply502Exception(
                "Marker not implemented for such Mode, Type and Structure");
    }
}
