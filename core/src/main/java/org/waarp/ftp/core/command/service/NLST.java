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

import java.util.List;

import org.waarp.common.command.exception.CommandAbstractException;
import org.waarp.ftp.core.command.AbstractCommand;

/**
 * NLST command
 * 
 * @author Frederic Bregier
 * 
 */
public class NLST extends AbstractCommand {
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
        getSession().openDataConnection();
        getSession().getDataConn().getFtpTransferControl().setNewFtpTransfer(
                getCode(), files, path);
    }

}
