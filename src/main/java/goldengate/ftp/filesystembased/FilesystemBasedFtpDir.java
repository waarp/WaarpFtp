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
import goldengate.common.file.filesystembased.FilesystemBasedDirImpl;
import goldengate.common.file.filesystembased.FilesystemBasedOptsMLSxImpl;
import goldengate.ftp.core.file.FtpDir;
import goldengate.ftp.core.file.FtpFile;
import goldengate.ftp.core.session.FtpSession;

/**
 * Filesystem implementation of a FtpDir
 *
 * @author Frederic Bregier
 *
 */
public abstract class FilesystemBasedFtpDir extends FilesystemBasedDirImpl implements FtpDir {
    /**
     *
     * @param session
     */
    public FilesystemBasedFtpDir(FtpSession session) {
        super(session, new FilesystemBasedOptsMLSxImpl());
    }

    public FtpFile setUniqueFile()
        throws CommandAbstractException {
        return (FtpFile) super.setUniqueFile();
    }
    public FtpFile setFile(String path,
            boolean append) throws CommandAbstractException {
        return (FtpFile) super.setFile(path, append);
    }
}
