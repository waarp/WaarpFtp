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
package org.waarp.ftp.core.command.info;

import org.waarp.common.command.ReplyCode;
import org.waarp.ftp.core.command.AbstractCommand;
import org.waarp.ftp.core.command.FtpCommandCode;
import org.waarp.ftp.core.session.FtpSession;

/**
 * NOOP command
 * 
 * @author Frederic Bregier
 * 
 */
public class NOOP extends AbstractCommand {
    public NOOP() {
        super();
    }

    /**
     * Constructor for empty NOOP
     * 
     * @param session
     */
    public NOOP(FtpSession session) {
        super();
        setArgs(session, FtpCommandCode.NOOP.name(), null, FtpCommandCode.NOOP);
    }

    @Override
    public void exec() {
        getSession().setReplyCode(ReplyCode.REPLY_200_COMMAND_OKAY, null);
    }

}
