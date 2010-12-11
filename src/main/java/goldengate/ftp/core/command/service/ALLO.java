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
package goldengate.ftp.core.command.service;

import goldengate.common.command.ReplyCode;
import goldengate.common.command.exception.CommandAbstractException;
import goldengate.common.command.exception.Reply452Exception;
import goldengate.common.command.exception.Reply501Exception;
import goldengate.common.exception.InvalidArgumentException;
import goldengate.ftp.core.command.AbstractCommand;

/**
 * ALLO command: test if enough space is disponible
 *
 * @author Frederic Bregier
 *
 */
public class ALLO extends AbstractCommand {

    /*
     * (non-Javadoc)
     *
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    public void exec() throws CommandAbstractException {
        // First Check if any argument
        if (!hasArg()) {
            throw new Reply501Exception("Need a size as argument");
        }
        String[] args = getArgs();
        int size = 0;
        try {
            size = getValue(args[0]);
        } catch (InvalidArgumentException e) {
            throw new Reply501Exception("Need a valid size as argument: " +
                    args[0]);
        }
        long free = getSession().getDir().getFreeSpace();
        if (free != -1 && free < size) {
            throw new Reply452Exception("Not enough space left");
        }
        if (free == -1) {
            getSession().setReplyCode(
                    ReplyCode.REPLY_202_COMMAND_NOT_IMPLEMENTED, null);
        } else {
            getSession().setReplyCode(ReplyCode.REPLY_200_COMMAND_OKAY,
                    "ALLO OK: " + free + " bytes available");
        }
    }

}
