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
package goldengate.common.file;

import goldengate.common.command.NextCommandReply;
import goldengate.common.command.exception.Reply421Exception;
import goldengate.common.command.exception.Reply530Exception;

/**
 * Interface for Authentication
 *
 * @author Frederic Bregier
 *
 */
public interface AuthInterface {
    /**
     *
     * @return the Ftp SessionInterface
     */
    public SessionInterface getSession();

    /**
     * @param user
     *            the user to set
     * @return (NOOP,230) if the user is OK, else return the following command
     *         that must follow (usually PASS) and the associated reply
     * @throws Reply421Exception
     *             if there is a problem during the authentication
     * @throws Reply530Exception
     *             if there is a problem during the authentication
     */
    public NextCommandReply setUser(String user) throws Reply421Exception,
            Reply530Exception;

    /**
     * @return the user
     */
    public String getUser();

    /**
     * @param password
     *            the password to set
     * @return (NOOP,230) if the Password is OK, else return the following
     *         command that must follow (usually ACCT) and the associated reply
     * @throws Reply421Exception
     *             if there is a problem during the authentication
     * @throws Reply530Exception
     *             if there is a problem during the authentication
     */
    public NextCommandReply setPassword(String password)
            throws Reply421Exception, Reply530Exception;

    /**
     * Is the current Authentication OK for full identification. It must be true
     * after a correct sequence of identification: At most, it is true when
     * setAccount is OK. It could be positive before (user name only,
     * user+password only).<br>
     * In the current implementation, as USER+PASS+ACCT are needed, it will be
     * true only after a correct ACCT.
     *
     * @return True if the user has a positive login, else False
     */
    public boolean isIdentified();

    /**
     *
     * @return True if the current authentication has an admin right (shutdown,
     *         bandwidth limitation)
     */
    public abstract boolean isAdmin();

    /**
     * Is the given complete relative Path valid from Authentication/Business
     * point of view.
     *
     * @param newPath
     * @return True if it is Valid
     */
    public abstract boolean isBusinessPathValid(String newPath);

    /**
     * Return the relative path for this account according to the Business
     * (without true root of mount).<br>
     *
     * @return Return the relative path for this account
     */
    public abstract String getBusinessPath();

    /**
     * Clean object
     *
     */
    public void clear();
}
