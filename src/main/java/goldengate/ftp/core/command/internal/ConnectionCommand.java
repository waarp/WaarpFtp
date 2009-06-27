/**
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3.0 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package goldengate.ftp.core.command.internal;

import goldengate.common.command.ReplyCode;
import goldengate.common.command.exception.CommandAbstractException;
import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpCommandCode;
import goldengate.ftp.core.session.FtpSession;

/**
 * Connection command: initialize the process of authentication
 *
 * @author Frederic Bregier
 *
 */
public class ConnectionCommand extends AbstractCommand {

    /**
     * Create a ConnectionCommand
     *
     * @param session
     */
    public ConnectionCommand(FtpSession session) {
        super();
        setArgs(session, "Connection", null, FtpCommandCode.Connection);
    }

    /*
     * (non-Javadoc)
     *
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    public void exec() throws CommandAbstractException {
        // Nothing to do except 220
        getSession().setReplyCode(ReplyCode.REPLY_220_SERVICE_READY, null);
    }

}
