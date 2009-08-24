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
package goldengate.common.file.filesystembased;

import goldengate.common.command.NextCommandReply;
import goldengate.common.command.ReplyCode;
import goldengate.common.command.exception.Reply421Exception;
import goldengate.common.command.exception.Reply530Exception;
import goldengate.common.file.AuthInterface;
import goldengate.common.file.DirInterface;
import goldengate.common.file.SessionInterface;

/**
 * Authentication implementation for Filesystem Based
 *
 * @author Frederic Bregier
 *
 */
public abstract class FilesystemBasedAuthImpl implements AuthInterface {
    /**
     * User name
     */
    protected String user = null;

    /**
     * Password
     */
    protected String password = null;

    /**
     * Is Identified
     */
    protected boolean isIdentified = false;

    /**
     * SessionInterface
     */
    protected final SessionInterface session;

    /**
     * Relative Path after Authentication
     */
    protected String rootFromAuth = null;

    /**
     * @param session
     */
    public FilesystemBasedAuthImpl(SessionInterface session) {
        this.session = session;
        isIdentified = false;
    }

    /**
     * @return the session
     */
    public SessionInterface getSession() {
        return session;
    }

    /**
     * Set the user according to any implementation and could set the
     * rootFromAuth. If NOOP is returned, isIdentifed must be TRUE.
     *
     * @param user
     * @return (NOOP,230) if the user is OK, else return the following command
     *         that must follow (usually PASS) and the associated reply
     * @throws Reply421Exception
     *             if there is a problem during the authentication
     * @throws Reply530Exception
     *             if there is a problem during the authentication
     */
    protected abstract NextCommandReply setBusinessUser(String user)
            throws Reply421Exception, Reply530Exception;

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
            Reply530Exception {
        NextCommandReply next = setBusinessUser(user);
        this.user = user;
        if (next.reply == ReplyCode.REPLY_230_USER_LOGGED_IN) {
            setRootFromAuth();
            session.getDir().initAfterIdentification();
        }
        return next;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * Set the password according to any implementation and could set the
     * rootFromAuth. If NOOP is returned, isIdentifed must be TRUE.
     *
     * @param password
     * @return (NOOP,230) if the Password is OK, else return the following
     *         command that must follow (usually ACCT) and the associated reply
     * @throws Reply421Exception
     *             if there is a problem during the authentication
     * @throws Reply530Exception
     *             if there is a problem during the authentication
     */
    protected abstract NextCommandReply setBusinessPassword(String password)
            throws Reply421Exception, Reply530Exception;

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
            throws Reply421Exception, Reply530Exception {
        NextCommandReply next = setBusinessPassword(password);
        this.password = password;
        if (next.reply == ReplyCode.REPLY_230_USER_LOGGED_IN) {
            setRootFromAuth();
            session.getDir().initAfterIdentification();
        }
        return next;
    }

    /**
     * Set the Authentication to Identified or Not
     *
     * @param isIdentified
     */
    protected void setIsIdentified(boolean isIdentified) {
        this.isIdentified = isIdentified;
    }

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
    public boolean isIdentified() {
        return isIdentified;
    }

    /**
     *
     * @return the root relative path from authentication if any or null if the
     *         default is used (default is /user or /user/account)
     * @exception Reply421Exception
     *                if the business root is not available
     */
    protected abstract String setBusinessRootFromAuth()
            throws Reply421Exception;

    /**
     * Set the root relative Path from current status of Authentication (should
     * be the highest level for the current authentication). If
     * setBusinessRootFromAuth returns null, by default set /user or
     * /user/account.
     *
     * @exception Reply421Exception
     *                if the business root is not available
     */
    private void setRootFromAuth() throws Reply421Exception {
        rootFromAuth = setBusinessRootFromAuth();
        if (rootFromAuth == null) {
            rootFromAuth = DirInterface.SEPARATOR + user;
        }
    }

    public String getBusinessPath() {
        return rootFromAuth;
    }

    /**
     * Business implementation of clean
     *
     */
    protected abstract void businessClean();

    /**
     * Clean object
     *
     */
    public void clear() {
        businessClean();
        user = null;
        password = null;
        rootFromAuth = null;
        isIdentified = false;
    }

    /**
     * Return the mount point
     *
     * @return the mount point
     */
    protected abstract String getBaseDirectory();

    /**
     * Return the full path as a String (with mount point).
     *
     * @param path
     *            relative path including business one (may be null or empty)
     * @return the full path as a String
     */
    public String getAbsolutePath(String path) {
        if (path == null || path.length() == 0) {
            return getBaseDirectory();
        }
        return getBaseDirectory() + DirInterface.SEPARATOR + path;
    }

    /**
     * Return the relative path from a file (without mount point)
     *
     * @param file
     *            (full path with mount point)
     * @return the relative path from a file
     */
    public String getRelativePath(String file) {
        // Work around Windows path '\'
        return file.replaceFirst(FilesystemBasedDirImpl
                .normalizePath(getBaseDirectory()), "");
    }
}
