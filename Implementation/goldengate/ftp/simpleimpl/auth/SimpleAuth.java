/**
 * 
 */
package goldengate.ftp.simpleimpl.auth;

import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;

/**
 * Simple Authentication based on a previously load XML file. Not to be used in
 * production!
 * 
 * @author fbregier
 * 
 */
public class SimpleAuth {
    /**
     * Internal Logger
     */
    private static final FtpInternalLogger logger = FtpInternalLoggerFactory
            .getLogger(SimpleAuth.class);

    /**
     * User name
     */
    public String user = null;

    /**
     * Password
     */
    public String password = null;

    /**
     * Multiple accounts
     */
    public String[] accounts = null;

    /**
     * Is the current user an administrator (which can shutdown or change
     * bandwidth limitation)
     */
    public boolean isAdmin = false;

    /**
     * @param user
     * @param password
     * @param accounts
     */
    public SimpleAuth(String user, String password, String[] accounts) {
        this.user = user;
        this.password = password;
        this.accounts = accounts;
    }

    /**
     * Is the given password a valid one
     * 
     * @param newpassword
     * @return True if the password is valid (or any password is valid)
     */
    public boolean isPasswordValid(String newpassword) {
        if (this.password == null) {
            return true;
        }
        if (newpassword == null) {
            return false;
        }
        return this.password.equals(newpassword);
    }

    /**
     * Is the given account a valid one
     * 
     * @param account
     * @return True if the account is valid (or any account is valid)
     */
    public boolean isAccountValid(String account) {
        if (this.accounts == null) {
            logger.info("No account needed");
            return true;
        }
        if (account == null) {
            logger.info("No account given");
            return false;
        }
        for (String acct: this.accounts) {
            if (acct.equals(account)) {
                logger.info("Account found");
                return true;
            }
        }
        logger.info("No account found");
        return false;
    }

    /**
     * 
     * @param isAdmin
     *            True if the user should be an administrator
     */
    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
