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
import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.exception.FtpNoTransferException;

/**
 * ABOR command
 *
 * @author Frederic Bregier
 *
 */
public class ABOR extends AbstractCommand {

    /*
     * (non-Javadoc)
     *
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    public void exec() throws CommandAbstractException {
        // First check if the data connection is opened
        if (getSession().getDataConn().isConnected()) {
            // Now check if the data connection is currently used
            try {
                getSession().getDataConn().getFtpTransferControl()
                        .getExecutingFtpTransfer();
            } catch (FtpNoTransferException e) {
                getSession().getDataConn().getFtpTransferControl().clear();
                getSession().setReplyCode(
                        ReplyCode.REPLY_226_CLOSING_DATA_CONNECTION, null);
                return;
            }
            getSession().getDataConn().getFtpTransferControl()
                    .setTransferAbortedFromInternal(false);
            return;
        }
        getSession().setReplyCode(ReplyCode.REPLY_226_CLOSING_DATA_CONNECTION,
                null);
    }

}
