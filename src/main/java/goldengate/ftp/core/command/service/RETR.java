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
package goldengate.ftp.core.command.service;

import goldengate.common.command.exception.CommandAbstractException;
import goldengate.common.command.exception.Reply450Exception;
import goldengate.common.command.exception.Reply501Exception;
import goldengate.common.command.exception.Reply550Exception;
import goldengate.common.file.FileInterface;
import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.utils.FtpCommandUtils;

/**
 * RETR command
 *
 * @author Frederic Bregier
 *
 */
public class RETR extends AbstractCommand {

    /*
     * (non-Javadoc)
     *
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    @Override
    public void exec() throws CommandAbstractException {
        if (!hasArg()) {
            invalidCurrentCommand();
            throw new Reply501Exception("Need a pathname as argument");
        }
        String filename = getArg();
        FileInterface file = getSession().getDir()
                .setFile(filename, false);
        if (file != null) {
            if (file.retrieve()) {
                FtpCommandUtils.openDataConnection(getSession());
                getSession().getDataConn().getFtpTransferControl()
                        .setNewFtpTransfer(getCode(), file);
                return;
            }
            // FileInterface does not exist
            throw new Reply450Exception("Retrieve operation not allowed");
        }
        // FileInterface name not allowed
        throw new Reply550Exception("Filename not allowed");
    }

}
