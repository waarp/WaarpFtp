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
package org.waarp.ftp.simpleimpl.file;

import org.waarp.common.command.exception.CommandAbstractException;
import org.waarp.ftp.core.file.FtpFile;
import org.waarp.ftp.core.session.FtpSession;
import org.waarp.ftp.filesystembased.FilesystemBasedFtpDir;

/**
 * FtpFile implementation based on true directories and files
 * 
 * @author Frederic Bregier
 * 
 */
public class FileBasedDir extends FilesystemBasedFtpDir {
    /**
     * @param session
     */
    public FileBasedDir(FtpSession session) {
        super(session);
    }

    public FtpFile newFile(String path, boolean append)
            throws CommandAbstractException {
        return new FileBasedFile((FtpSession) getSession(), this, path, append);
    }
}
