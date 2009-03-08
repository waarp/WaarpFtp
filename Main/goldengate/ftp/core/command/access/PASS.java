/**
 * Frederic Bregier LGPL 10 janv. 09 
 * USER.java goldengate.ftp.core.command.access GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.access;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpNextCommandReply;
import goldengate.ftp.core.command.exception.Reply421Exception;
import goldengate.ftp.core.command.exception.Reply501Exception;
import goldengate.ftp.core.command.exception.Reply530Exception;
import goldengate.ftp.core.utils.FtpCommandUtils;

/**
 * PASS command
 * @author frederic
 * goldengate.ftp.core.command.access PASS
 * 
 */
public class PASS extends AbstractCommand {

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() throws Reply421Exception, Reply501Exception, Reply530Exception {
		if (!this.hasArg()) {
			this.invalidCurrentCommand();
			throw new Reply501Exception("Need password as argument");
		}
		String password = this.getArg();
		if (this.getFtpSession().getFtpAuth() == null) {
			FtpCommandUtils.reinitFtpAuth(this.getFtpSession());
			throw new Reply530Exception("No user specified");
		}
		FtpNextCommandReply nextCommandReply = null;
		try {
			nextCommandReply = 
				this.getFtpSession().getFtpAuth().setPassword(password);
		} catch (Reply530Exception e) {
			FtpCommandUtils.reinitFtpAuth(this.getFtpSession());
			throw e;
		}
		this.setExtraNextCommand(nextCommandReply.command);
		this.getFtpSession().setReplyCode(nextCommandReply.reply,nextCommandReply.message);
	}

}
