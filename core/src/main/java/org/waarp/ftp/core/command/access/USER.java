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
package org.waarp.ftp.core.command.access;

import org.waarp.common.command.NextCommandReply;
import org.waarp.common.command.exception.Reply421Exception;
import org.waarp.common.command.exception.Reply501Exception;
import org.waarp.common.command.exception.Reply530Exception;
import org.waarp.ftp.core.command.AbstractCommand;

/**
 * USER command
 * 
 * @author Frederic Bregier
 * 
 */
public class USER extends AbstractCommand {
    @Override
    public void exec() throws Reply501Exception, Reply421Exception,
            Reply530Exception {
        if (!hasArg()) {
            getSession().reinitFtpAuth();
            throw new Reply501Exception("Need a username as argument");
        }
        String username = getArg();
        NextCommandReply nextCommandReply;
        try {
            nextCommandReply = getSession().getAuth().setUser(username);
        } catch (Reply530Exception e) {
            getSession().reinitFtpAuth();
            throw e;
        }
        setExtraNextCommand(nextCommandReply.command);
        getSession().setReplyCode(nextCommandReply.reply,
                nextCommandReply.message);
    }

}
