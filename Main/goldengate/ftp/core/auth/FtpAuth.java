/**
 * 
 */
package goldengate.ftp.core.auth;

import goldengate.ftp.core.command.FtpNextCommandReply;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.Reply421Exception;
import goldengate.ftp.core.command.exception.Reply502Exception;
import goldengate.ftp.core.command.exception.Reply530Exception;
import goldengate.ftp.core.file.FtpDir;
import goldengate.ftp.core.session.FtpSession;
import goldengate.ftp.filesystembased.FilesystemBasedFtpAuth;

/**
 * Base of Ftp Authentication with root directory from authentication (usually
 * /user/account). No mount point is considered here (see
 * {@link FilesystemBasedFtpAuth}).
 * 
 * @author fbregier
 * 
 */
public abstract class FtpAuth {
    /**
     * User name
     */
    protected String user = null;

    /**
     * Password
     */
    protected String password = null;

    /**
     * Account name
     */
    protected String account = null;

    /**
     * Is Identified
     */
    protected boolean isIdentified = false;

    /**
     * Ftp Session
     */
    private final FtpSession session;

    /**
     * Relative Path after Authentication
     */
    protected String rootFromAuth = null;

    /**
     * Create a FtpAuth
     * 
     * @param session
     */
    public FtpAuth(FtpSession session) {
        this.session = session;
        this.isIdentified = false;
    }

    /**
     * 
     * @return the Ftp Session
     */
    public FtpSession getFtpSession() {
        return this.session;
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
    protected abstract FtpNextCommandReply setBusinessUser(String user)
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
    public FtpNextCommandReply setUser(String user) throws Reply421Exception,
            Reply530Exception {
        FtpNextCommandReply next = this.setBusinessUser(user);
        this.user = user;
        if (next.reply == FtpReplyCode.REPLY_230_USER_LOGGED_IN) {
            this.setRootFromAuth();
            this.session.getFtpDir().initAfterIdentification();
        }
        return next;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return this.user;
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
    protected abstract FtpNextCommandReply setBusinessPassword(String password)
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
    public FtpNextCommandReply setPassword(String password)
            throws Reply421Exception, Reply530Exception {
        FtpNextCommandReply next = this.setBusinessPassword(password);
        this.password = password;
        if (next.reply == FtpReplyCode.REPLY_230_USER_LOGGED_IN) {
            this.setRootFromAuth();
            this.session.getFtpDir().initAfterIdentification();
        }
        return next;
    }

    /**
     * @return the account
     */
    public String getAccount() {
        return this.account;
    }

    /**
     * Set the account according to any implementation and could set the
     * rootFromAuth. If NOOP is returned, isIdentifed must be TRUE.
     * 
     * @param account
     * @return (NOOP,230) if the Account is OK, else return the following
     *         command that must follow and the associated reply
     * @throws Reply421Exception
     *             if there is a problem during the authentication
     * @throws Reply530Exception
     *             if there is a problem during the authentication
     * @throws Reply502Exception
     *             if there is a problem during the authentication
     */
    protected abstract FtpNextCommandReply setBusinessAccount(String account)
            throws Reply421Exception, Reply530Exception, Reply502Exception;

    /**
     * @param account
     *            the account to set
     * @return (NOOP,230) if the Account is OK, else return the following
     *         command that must follow and the associated reply
     * @throws Reply421Exception
     *             if there is a problem during the authentication
     * @throws Reply530Exception
     *             if there is a problem during the authentication
     * @throws Reply502Exception
     */
    public FtpNextCommandReply setAccount(String account)
            throws Reply421Exception, Reply530Exception, Reply502Exception {
        FtpNextCommandReply next = this.setBusinessAccount(account);
        this.account = account;
        if (next.reply == FtpReplyCode.REPLY_230_USER_LOGGED_IN) {
            this.setRootFromAuth();
            this.session.getFtpDir().initAfterIdentification();
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
        return this.isIdentified;
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
        this.rootFromAuth = setBusinessRootFromAuth();
        if (this.rootFromAuth == null) {
            if (this.account == null) {
                this.rootFromAuth = FtpDir.SEPARATOR + this.user;
            } else {
                this.rootFromAuth = FtpDir.SEPARATOR + this.user +
                        FtpDir.SEPARATOR + this.account;
            }
        }
    }

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
     * Business implementation of clean
     * 
     */
    protected abstract void businessClean();

    /**
     * Clean object
     * 
     */
    public void clean() {
        this.businessClean();
        this.user = null;
        this.account = null;
        this.password = null;
        this.rootFromAuth = null;
        this.isIdentified = false;
    }
}
