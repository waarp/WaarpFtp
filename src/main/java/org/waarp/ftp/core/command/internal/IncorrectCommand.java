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
package org.waarp.ftp.core.command.internal;

import org.waarp.common.command.ReplyCode;
import org.waarp.common.command.exception.Reply503Exception;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.ftp.core.command.AbstractCommand;
import org.waarp.ftp.core.command.FtpCommandCode;
import org.waarp.ftp.core.command.info.NOOP;

/**
 * Incorrect command
 * 
 * @author Frederic Bregier
 * 
 */
public class IncorrectCommand extends AbstractCommand {
    /**
     * Internal Logger
     */
    private static final WaarpLogger logger = WaarpLoggerFactory
            .getLogger(IncorrectCommand.class);

    @Override
    public void exec() throws Reply503Exception {
        getSession().setReplyCode(
                ReplyCode.REPLY_503_BAD_SEQUENCE_OF_COMMANDS,
                "Bas sequence of commands: " + getCommand() + " following " +
                        getSession().getPreviousCommand().getCommand());
        logger.warn(getSession().getAnswer());
        if (getSession().getPreviousCommand().getCode() != FtpCommandCode.Connection &&
                getSession().getPreviousCommand().getCode() != FtpCommandCode.PASS &&
                getSession().getPreviousCommand().getCode() != FtpCommandCode.USER) {
            getSession().setNextCommand(new NOOP(getSession()));
        } else {
            invalidCurrentCommand();
        }
        throw new Reply503Exception(getSession().getReplyCode().getMesg());
    }
}
