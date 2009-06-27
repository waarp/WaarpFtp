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
import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;
import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpCommandCode;
import goldengate.ftp.core.command.info.NOOP;

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
    private static final GgInternalLogger logger = GgInternalLoggerFactory
            .getLogger(IncorrectCommand.class);

    /*
     * (non-Javadoc)
     *
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    public void exec() {
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
    }
}
