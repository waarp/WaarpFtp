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
package goldengate.ftp.core.command.access;

import goldengate.common.command.NextCommandReply;
import goldengate.common.command.exception.Reply421Exception;
import goldengate.common.command.exception.Reply501Exception;
import goldengate.common.command.exception.Reply530Exception;
import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.utils.FtpCommandUtils;

/**
 * PASS command
 *
 * @author Frederic Bregier
 *
 */
public class PASS extends AbstractCommand {

    /*
     * (non-Javadoc)
     *
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    @Override
    public void exec() throws Reply421Exception, Reply501Exception,
            Reply530Exception {
        if (!hasArg()) {
            invalidCurrentCommand();
            throw new Reply501Exception("Need password as argument");
        }
        String password = getArg();
        if (getSession().getAuth() == null) {
            FtpCommandUtils.reinitFtpAuth(getSession());
            throw new Reply530Exception("No user specified");
        }
        NextCommandReply nextCommandReply = null;
        try {
            nextCommandReply = getSession().getAuth()
                    .setPassword(password);
        } catch (Reply530Exception e) {
            FtpCommandUtils.reinitFtpAuth(getSession());
            throw e;
        }
        setExtraNextCommand(nextCommandReply.command);
        getSession().setReplyCode(nextCommandReply.reply,
                nextCommandReply.message);
    }

}
