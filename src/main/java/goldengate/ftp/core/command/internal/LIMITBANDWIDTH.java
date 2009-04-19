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
package goldengate.ftp.core.command.internal;

import goldengate.common.command.ReplyCode;
import goldengate.common.command.exception.Reply500Exception;
import goldengate.common.command.exception.Reply501Exception;
import goldengate.ftp.core.command.AbstractCommand;

/**
 * Internal limit bandwidth command that will change the global limit bandwidth
 *
 * @author Frederic Bregier
 *
 */
public class LIMITBANDWIDTH extends AbstractCommand {
    /*
     * (non-Javadoc)
     *
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    @Override
    public void exec() throws Reply501Exception, Reply500Exception {
        if (!getSession().getAuth().isAdmin()) {
            // not admin
            throw new Reply500Exception("Command Not Allowed");
        }
        if (!hasArg()) {
            // reset to default
            getConfiguration().changeNetworkLimit(0, 0);
            getSession().setReplyCode(ReplyCode.REPLY_200_COMMAND_OKAY,
                    "Limit reset to default");
            return;
        }
        String[] limits = getArgs();
        long writeLimit = 0;
        long readLimit = 0;
        try {
            if (limits.length == 1) {
                writeLimit = Long.parseLong(limits[0]);
                readLimit = writeLimit;
            } else {
                writeLimit = Long.parseLong(limits[0]);
                readLimit = Long.parseLong(limits[1]);
            }
        } catch (NumberFormatException e) {
            throw new Reply501Exception(getCommand() +
                    " ([write and read limits in b/s] | [write limit in b/s] [read limit in b/s]");
        }
        getConfiguration().changeNetworkLimit(writeLimit, readLimit);
        getSession().setReplyCode(ReplyCode.REPLY_200_COMMAND_OKAY,
                "Limit set to new values");
    }

}
