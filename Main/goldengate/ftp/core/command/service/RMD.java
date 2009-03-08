/**
 * Frederic Bregier LGPL 10 janv. 09 
 * PORT.java goldengate.ftp.core.command.access GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.service;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.command.exception.Reply501Exception;
import goldengate.ftp.core.command.exception.Reply550Exception;

/**
 * RMD command
 * @author frederic
 * goldengate.ftp.core.command.service RMD
 * 
 */
public class RMD extends AbstractCommand {

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() throws Reply550Exception, FtpCommandAbstractException {
		// First Check if any argument
		if (! this.hasArg()) {
			throw new Reply501Exception("Need a path as argument");
		}
		String path = this.getArg();
		String pastdir = this.getFtpSession().getFtpDir().rmdir(path);
		this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_250_REQUESTED_FILE_ACTION_OKAY,
				"\""+pastdir+"\" is deleted");
	}

}
