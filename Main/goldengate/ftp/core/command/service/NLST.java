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
import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.utils.FtpCommandUtils;

import java.util.List;

/**
 * NLST command
 *
 * @author Frederic Bregier
 *
 */
public class NLST extends AbstractCommand {

    /*
     * (non-Javadoc)
     *
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    @Override
    public void exec() throws CommandAbstractException {
        String path = null;
        List<String> files = null;
        if (!hasArg()) {
            path = getSession().getDir().getPwd();
            files = getSession().getDir().list(path);
        } else {
            path = getArg();
            if (path.startsWith("-l") || path.startsWith("-L")) {
                // This should be a LIST command
                String[] paths = getArgs();
                if (paths.length > 1) {
                    files = getSession().getDir().listFull(paths[1], true);
                } else {
                    files = getSession().getDir().listFull(
                            getSession().getDir().getPwd(), true);
                }
            } else {
                files = getSession().getDir().list(path);
            }
        }
        FtpCommandUtils.openDataConnection(getSession());
        getSession().getDataConn().getFtpTransferControl()
                .setNewFtpTransfer(getCode(), files, path);
    }

}
