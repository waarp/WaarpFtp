/**
   This file is part of GoldenGate Project (named also GoldenGate or GG).

   Copyright 2009, Frederic Bregier, and individual contributors by the @author
   tags. See the COPYRIGHT.txt in the distribution for a full listing of
   individual contributors.

   All GoldenGate Project is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   GoldenGate is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with GoldenGate .  If not, see <http://www.gnu.org/licenses/>.
 */
package goldengate.ftp.filesystembased;

import goldengate.common.command.exception.CommandAbstractException;
import goldengate.common.command.exception.Reply502Exception;
import goldengate.common.file.filesystembased.FilesystemBasedRestartImpl;
import goldengate.ftp.core.command.FtpArgumentCode.TransferMode;
import goldengate.ftp.core.command.FtpArgumentCode.TransferStructure;
import goldengate.ftp.core.command.FtpArgumentCode.TransferType;
import goldengate.ftp.core.data.FtpDataAsyncConn;
import goldengate.ftp.core.session.FtpSession;

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
            try {
                newposition = Long.parseLong(marker);
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
