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
package org.waarp.ftp.core.command.extension;

import org.waarp.common.command.ReplyCode;
import org.waarp.common.command.exception.CommandAbstractException;
import org.waarp.common.command.exception.Reply501Exception;
import org.waarp.common.digest.FilesystemBasedDigest;
import org.waarp.ftp.core.command.AbstractCommand;

/**
 * XMD5 command: takes a filename and returns the MD5 of the file
 * 
 * @author Frederic Bregier
 * 
 */
public class XMD5 extends AbstractCommand {

	/*
	 * (non-Javadoc)
	 * @see org.waarp.ftp.core.command.AbstractCommand#exec()
	 */
	public void exec() throws CommandAbstractException {
		if (!hasArg()) {
			invalidCurrentCommand();
			throw new Reply501Exception("Need a pathname as argument");
		}
		String filename = getArg();

		String crc = FilesystemBasedDigest.getHex(getSession().getDir().getMD5(
				filename));
		getSession().setReplyCode(
				ReplyCode.REPLY_250_REQUESTED_FILE_ACTION_OKAY,
				crc + " \"" + filename + "\" MD5");
	}

}
