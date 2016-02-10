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
import org.waarp.common.command.exception.CommandAbstractException;
import org.waarp.common.command.exception.Reply501Exception;
import org.waarp.common.command.exception.Reply502Exception;
import org.waarp.common.command.exception.Reply503Exception;
import org.waarp.ftp.core.command.AbstractCommand;
import org.waarp.ftp.core.command.FtpCommandCode;
import org.waarp.ftp.core.command.extension.XCRC;
import org.waarp.ftp.core.command.extension.XMD5;
import org.waarp.ftp.core.command.extension.XSHA1;
import org.waarp.ftp.core.command.internal.IncorrectCommand;

/**
 * SITE command: implements some specific command like {@link XMD5} {@link XCRC} {@link XSHA1} as if
 * they were called directly
 * 
 * @author Frederic Bregier
 * 
 */
public class SITE extends AbstractCommand {

    @Override
    public void exec() throws CommandAbstractException {
        if (!hasArg()) {
            invalidCurrentCommand();
            throw new Reply501Exception("Need a command at least as argument");
        }
        // First check if this command is a special extension
        AbstractCommand command = getSession().getBusinessHandler().
                getSpecializedSiteCommand(getSession(), getArg());
        boolean special = true;
        if (command == null) {
            // Now check what is the command as if we were in the NetworkHandler
            command = FtpCommandCode.getFromLine(getSession(),
                    getArg());
            special = false;
        }
        // Default message
        getSession().setReplyCode(ReplyCode.REPLY_200_COMMAND_OKAY, null);
        // First check if the command is an extension command
        if (special || FtpCommandCode.isExtensionCommand(command.getCode())) {
            // Now check if a transfer is on its way: illegal to have at same
            // time two commands
            if (getSession().getDataConn().getFtpTransferControl()
                    .isFtpTransferExecuting()) {
                throw new Reply503Exception(
                        "Previous transfer command is not finished yet");
            }
        } else {
            throw new Reply502Exception("Command not implemented: " + getArg());
        }
        // Command is OK, set it as current by first undo current command then
        // set it as next after testing validity
        getSession().setPreviousAsCurrentCommand();
        if (getSession().getCurrentCommand().isNextCommandValid(command)) {
            getSession().setNextCommand(command);
            getSession().getBusinessHandler().beforeRunCommand();
            command.exec();
        } else {
            command = new IncorrectCommand();
            command.setArgs(getSession(), getArg(), null,
                    FtpCommandCode.IncorrectSequence);
            getSession().setNextCommand(command);
            getSession().getBusinessHandler().beforeRunCommand();
            command.exec();
        }
    }
}
