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
package org.waarp.ftp.filesystembased;

import org.waarp.common.command.NextCommandReply;
import org.waarp.common.command.ReplyCode;
import org.waarp.common.command.exception.Reply421Exception;
import org.waarp.common.command.exception.Reply502Exception;
import org.waarp.common.command.exception.Reply530Exception;
import org.waarp.common.file.filesystembased.FilesystemBasedAuthImpl;
import org.waarp.ftp.core.file.FtpAuth;
import org.waarp.ftp.core.file.FtpDir;
import org.waarp.ftp.core.session.FtpSession;

/**
 * Filesystem implementation of a AuthInterface
 * 
 * @author Frederic Bregier
 * 
 */
public abstract class FilesystemBasedFtpAuth extends FilesystemBasedAuthImpl implements FtpAuth {

    /**
     * Account name
     */
    protected String account = null;

    /**
     * 
     * @param session
     */
    public FilesystemBasedFtpAuth(FtpSession session) {
        super(session);
    }

    /**
     * @return the account
     */
    public String getAccount() {
        return account;
    }

    /**
     * Set the account according to any implementation and could set the rootFromAuth. If NOOP is
     * returned, isIdentifed must be TRUE.
     * 
     * @param account
     * @return (NOOP,230) if the Account is OK, else return the following command that must follow
     *         and the associated reply
     * @throws Reply421Exception
     *             if there is a problem during the authentication
     * @throws Reply530Exception
     *             if there is a problem during the authentication
     * @throws Reply502Exception
     *             if there is a problem during the authentication
     */
    protected abstract NextCommandReply setBusinessAccount(String account)
            throws Reply421Exception, Reply530Exception, Reply502Exception;

    /**
     * @param account
     *            the account to set
     * @return (NOOP,230) if the Account is OK, else return the following command that must follow
     *         and the associated reply
     * @throws Reply421Exception
     *             if there is a problem during the authentication
     * @throws Reply530Exception
     *             if there is a problem during the authentication
     * @throws Reply502Exception
     */
    public NextCommandReply setAccount(String account)
            throws Reply421Exception, Reply530Exception, Reply502Exception {
        NextCommandReply next = setBusinessAccount(account);
        this.account = account;
        if (next.reply == ReplyCode.REPLY_230_USER_LOGGED_IN) {
            setRootFromAuth();
            session.getDir().initAfterIdentification();
        }
        return next;
    }

    /**
     * Set the root relative Path from current status of Authentication (should be the highest level
     * for the current authentication). If setBusinessRootFromAuth returns null, by default set
     * /user or /user/account.
     * 
     * @exception Reply421Exception
     *                if the business root is not available
     */
    private void setRootFromAuth() throws Reply421Exception {
        rootFromAuth = setBusinessRootFromAuth();
        if (rootFromAuth == null) {
            if (account == null) {
                rootFromAuth = FtpDir.SEPARATOR + user;
            } else {
                rootFromAuth = FtpDir.SEPARATOR + user +
                        FtpDir.SEPARATOR + account;
            }
        }
    }

    /**
     * Clean object
     * 
     */
    public void clear() {
        super.clear();
        account = null;
    }

    @Override
    public String getBaseDirectory() {
        return ((FtpSession) getSession()).getConfiguration()
                .getBaseDirectory();
    }
}
