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

import org.waarp.common.command.ReplyCode;
import org.waarp.ftp.core.command.AbstractCommand;

/**
 * QUIT command
 * 
 * @author Frederic Bregier
 * 
 */
public class QUIT extends AbstractCommand {
    @Override
    public void exec() {
        getSession().rein();
        getSession().setNextCommand(this);
        getSession().setReplyCode(
                ReplyCode.REPLY_221_CLOSING_CONTROL_CONNECTION, null);
    }

}
