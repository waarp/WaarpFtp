/**
 * 
 */
package goldengate.ftp.simpleimpl.auth;

import goldengate.ftp.core.command.FtpCommandCode;
import goldengate.ftp.core.command.FtpNextCommandReply;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.Reply421Exception;
import goldengate.ftp.core.command.exception.Reply502Exception;
import goldengate.ftp.core.command.exception.Reply530Exception;
import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;
import goldengate.ftp.core.session.FtpSession;
import goldengate.ftp.filesystembased.FilesystemBasedFtpAuth;
import goldengate.ftp.simpleimpl.config.FileBasedConfiguration;

/**
 * FtpAuth implementation based on a list of (user/password/account) stored
 * in a xml file load at startup from configuration.
 * Not to be used in production!
 * @author fbregier
 *
 */
public class FileBasedAuth extends FilesystemBasedFtpAuth {
	/**
	 * Internal Logger
	 */
	private static final FtpInternalLogger logger =
        FtpInternalLoggerFactory.getLogger(FileBasedAuth.class);
	/**
	 * Current authentification
	 */
	private SimpleAuth currentAuth = null;
	/**
	 * @param session
	 */
	public FileBasedAuth(FtpSession session) {
		super(session);
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.auth.FtpAuth#businessClean()
	 */
	@Override
	protected void businessClean() {
		this.currentAuth = null;
	}
	/**
 	 * @param user the user to set
	 * @return (NOOP,230) if the user is OK, else return the following command that must follow (usually PASS) and the associated reply 
	 * @throws Reply421Exception if there is a problem during the authentification
	 * @throws Reply530Exception if there is a problem during the authentification
	 * @see goldengate.ftp.core.auth.FtpAuth#setBusinessUser(java.lang.String)
	 */
	@Override
	protected FtpNextCommandReply setBusinessUser(String user)
			throws Reply421Exception, Reply530Exception {
		SimpleAuth auth = ((FileBasedConfiguration) this.getFtpSession().getConfiguration()).getSimpleAuth(user);
		if (auth == null) {
			this.setIsIdentified(false);
			this.currentAuth = null;
			throw new Reply530Exception("User name not allowed");
		}
		this.currentAuth = auth;
		logger.debug("User: {}",user);
		return new FtpNextCommandReply(FtpCommandCode.PASS,
				FtpReplyCode.REPLY_331_USER_NAME_OKAY_NEED_PASSWORD,null);
	}
	/**
	 * Set tha password according to any implementation and could set the rootFromAuth.
	 * If NOOP is returned, isIdentifed must be TRUE.
	 * A special case is implemented for test user.
	 * @param password
	 * @return (NOOP,230) if the Password is OK, else return the following command that must follow (usually ACCT)  and the associated reply
	 * @throws Reply421Exception if there is a problem during the authentification
	 * @throws Reply530Exception if there is a problem during the authentification
	 * @see goldengate.ftp.core.auth.FtpAuth#setBusinessPassword(java.lang.String)
	 */
	@Override
	protected FtpNextCommandReply setBusinessPassword(String password)
			throws Reply421Exception, Reply530Exception {
		if (this.currentAuth == null) {
			this.setIsIdentified(false);
			throw new Reply530Exception("PASS needs a USER first");
		}
		if (this.currentAuth.isPasswordValid(password)) {
			if (this.user.equals("test")) {
				logger.debug("User test");
				try {
					return this.setAccount("test");
				} catch (Reply502Exception e) {
				}
			}
			return new FtpNextCommandReply(FtpCommandCode.ACCT,
					FtpReplyCode.REPLY_332_NEED_ACCOUNT_FOR_LOGIN,null);
		}
		throw new Reply530Exception("Password is not valid");
	}
	/**
	 * Set the account according to any implementation and could set the rootFromAuth.
	 * If NOOP is returned, isIdentifed must be TRUE.
	 * @param account
	 * @return (NOOP,230) if the Account is OK, else return the following command that must follow  and the associated reply
	 * @throws Reply421Exception if there is a problem during the authentification
	 * @throws Reply530Exception if there is a problem during the authentification
	 * @see goldengate.ftp.core.auth.FtpAuth#setBusinessAccount(java.lang.String)
	 */
	@Override
	protected FtpNextCommandReply setBusinessAccount(String account)
			throws Reply421Exception, Reply530Exception {
		if (this.currentAuth == null) {
			throw new Reply530Exception("ACCT needs a USER first");
		}
		if (this.currentAuth.isAccountValid(account)) {
			logger.debug("Account: {}",account);
			this.setIsIdentified(true);
			logger.warn("User {} is authentified with account {}",this.user, account);
			return new FtpNextCommandReply(FtpCommandCode.NOOP,
					FtpReplyCode.REPLY_230_USER_LOGGED_IN,null);
		}
		throw new Reply530Exception("Account is not valid");
	}
}
