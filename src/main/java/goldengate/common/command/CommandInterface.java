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

import goldengate.common.command.exception.CommandAbstractException;
import goldengate.common.exception.InvalidArgumentException;
import goldengate.common.file.SessionInterface;

/**
 * Interface for Command
 *
 * @author Frederic Bregier
 *
 */
public interface CommandInterface {
    /**
     * Set the Command from the args
     *
     * @param session
     * @param command
     * @param arg
     * @param code
     */
    @SuppressWarnings("unchecked")
    public void setArgs(SessionInterface session, String command, String arg,
            Enum code);

    /**
     * Execute the command. This execution must set the replyCode in the session
     * to a correct value before returning.
     *
     * @exception CommandAbstractException
     *                in case of an FTP Error occurs
     */
    abstract public void exec() throws CommandAbstractException;

    /**
     * This function is intend to allow to force USER->PASS->ACCT->CDW for
     * instance
     *
     * @param extraNextCommand
     *            the extraNextCommand to set
     */
    @SuppressWarnings("unchecked")
    public void setExtraNextCommand(Enum extraNextCommand);

    /**
     * This function is called when a new command is received to check if this
     * new command is positive according to the previous command and status.
     *
     * @param newCommand
     * @return True if this new command is OK, else False
     */
    public abstract boolean isNextCommandValid(CommandInterface newCommand);

    /**
     * @return the object
     */
    public Object getObject();

    /**
     * @param object
     *            the object to set
     */
    public void setObject(Object object);

    /**
     * @return the arg
     */
    public String getArg();

    /**
     *
     * @return the list of arguments
     */
    public String[] getArgs();

    /**
     * Get an integer value from argument
     *
     * @param argx
     * @return the integer
     * @throws InvalidArgumentException
     *             if the argument is not an integer
     */
    public int getValue(String argx) throws InvalidArgumentException;

    /**
     * @return the command
     */
    public String getCommand();

    /**
     * Does this command has an argument
     *
     * @return True if it has an argument
     */
    public boolean hasArg();

    /**
     *
     * @return the current SessionInterface
     */
    public SessionInterface getSession();

    // some helpful functions
    /**
     * Set the previous command as the new current command (used after a
     * incorrect sequence of commands or unknown command). Also clear the
     * Restart object.
     *
     */
    public void invalidCurrentCommand();

    /**
     *
     * @return The GgCommandCode associated with this command
     */
    public Enum<?> getCode();
}
