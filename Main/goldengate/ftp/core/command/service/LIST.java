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
 * LIST command
 *
 * @author Frederic Bregier
 *
 */
public class LIST extends AbstractCommand {

    /*
     * (non-Javadoc)
     *
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    @Override
    public void exec() throws CommandAbstractException {
        String path = null;
        if (!hasArg()) {
            path = getSession().getDir().getPwd();
        } else {
            path = getArg();
        }
        List<String> filesInfo = getSession().getDir()
                .listFull(path, true);
        FtpCommandUtils.openDataConnection(getSession());
        getSession().getDataConn().getFtpTransferControl()
                .setNewFtpTransfer(getCode(), filesInfo, path);
    }

}
