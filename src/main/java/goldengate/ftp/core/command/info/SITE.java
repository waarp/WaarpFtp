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
package goldengate.ftp.core.command.info;

import goldengate.common.command.ReplyCode;
import goldengate.common.command.exception.CommandAbstractException;
import goldengate.common.command.exception.Reply501Exception;
import goldengate.common.command.exception.Reply502Exception;
import goldengate.common.command.exception.Reply503Exception;
import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpCommandCode;
import goldengate.ftp.core.command.extension.XCRC;
import goldengate.ftp.core.command.extension.XMD5;
import goldengate.ftp.core.command.extension.XSHA1;
import goldengate.ftp.core.command.internal.IncorrectCommand;

/**
 * SITE command: implements some specific command like {@link XMD5} {@link XCRC}
 * {@link XSHA1} as if they were called directly
 *
 * @author Frederic Bregier
 *
 */
public class SITE extends AbstractCommand {

    /*
     * (non-Javadoc)
     *
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    public void exec() throws CommandAbstractException {
        if (!hasArg()) {
            invalidCurrentCommand();
            throw new Reply501Exception("Need a command at least as argument");
        }
        // Now check what is the command as if we were in the NetworkHandler
        AbstractCommand command = FtpCommandCode.getFromLine(getSession(),
                getArg());
        // Default message
        getSession().setReplyCode(ReplyCode.REPLY_200_COMMAND_OKAY, null);
        // First check if the command is an extension command
        if (FtpCommandCode.isExtensionCommand(command.getCode())) {
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
