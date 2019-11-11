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
package org.waarp.ftp.core.command.service;

import org.waarp.common.command.exception.CommandAbstractException;
import org.waarp.common.command.exception.Reply450Exception;
import org.waarp.common.command.exception.Reply501Exception;
import org.waarp.common.command.exception.Reply553Exception;
import org.waarp.ftp.core.command.AbstractCommand;
import org.waarp.ftp.core.file.FtpFile;
import org.waarp.ftp.core.session.FtpSession;

/**
 * APPE command
 * 
 * @author Frederic Bregier
 * 
 */
public class APPE extends AbstractCommand {
    @Override
    public void exec() throws CommandAbstractException {
        if (!hasArg()) {
            invalidCurrentCommand();
            throw new Reply501Exception("Need a pathname as argument");
        }
        String filename = FtpSession.getBasename(getArg());
        FtpFile file = getSession().getDir().setFile(filename, true);
        if (file != null) {
            if (file.store()) {
                getSession().openDataConnection();
                getSession().getDataConn().getFtpTransferControl()
                        .setNewFtpTransfer(getCode(), file);
                return;
            }
            throw new Reply450Exception("Append operation not started");
        }
        throw new Reply553Exception("Filename not allowed");
    }

}
