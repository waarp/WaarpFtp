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
import goldengate.ftp.core.file.FtpFile;

/**
 * RNFR command
 * @author frederic
 * goldengate.ftp.core.command.service RNFR
 * 
 */
public class RNFR extends AbstractCommand {

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() throws FtpCommandAbstractException {
		if (!this.hasArg()) {
			this.invalidCurrentCommand();
			throw new Reply501Exception("Need a pathname as argument");
		}
		String filename = this.getArg();
		FtpFile file = this.getFtpSession().getFtpDir().setFile(filename, false);
		if ((file != null) && (file.exists())) {
			this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_350_REQUESTED_FILE_ACTION_PENDING_FURTHER_INFORMATION,
					"Need RNTO command");
			return;
		}
		// File name not allowed or not found
		throw new Reply550Exception("Filename not allowed");
	}

}
