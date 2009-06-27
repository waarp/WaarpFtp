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
package goldengate.common.command;

/**
 * Used by Authentication step in order to allow a specific command to be
 * accepted after the current command. If null is specified, any command is
 * valid. Specify also the reply code and the associated message.
 *
 * @author Frederic Bregier
 *
 */
public class NextCommandReply {
    /**
     * Command to be accepted next time
     */
    public Enum<?> command = null;

    /**
     * Reply to do to the Ftp client
     */
    public ReplyCode reply = null;

    /**
     * Message
     */
    public String message = null;

    /**
     * @param command
     * @param reply
     * @param message
     */
    public NextCommandReply(Enum<?> command, ReplyCode reply, String message) {
        this.command = command;
        this.reply = reply;
        this.message = message;
    }
}
