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
package goldengate.ftp.core.utils;

import goldengate.common.command.exception.Reply425Exception;
import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.internal.ConnectionCommand;
import goldengate.ftp.core.session.FtpSession;

/**
 * Some useful commands to simplify call from several Commands.
 *
 * @author Frederic Bregier
 *
 */
public class FtpCommandUtils {
    /**
     * Reinitialize the authentication to the connection step
     *
     * @param session
     */
    public static void reinitFtpAuth(FtpSession session) {
        AbstractCommand connectioncommand = new ConnectionCommand(session);
        session.setNextCommand(connectioncommand);
        session.getAuth().clean();
        session.getDataConn().clear();
    }

    /**
     * Try to open a connection. Do the intermediate reply if any (150) and the
     * final one (125)
     *
     * @param session
     * @throws Reply425Exception
     *             if the connection cannot be opened
     */
    public static void openDataConnection(FtpSession session)
            throws Reply425Exception {
        session.getDataConn().getFtpTransferControl().openDataConnection();
    }
}
