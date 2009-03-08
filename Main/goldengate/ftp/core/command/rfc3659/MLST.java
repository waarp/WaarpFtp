/**
 * Frederic Bregier LGPL 10 janv. 09 
 * PORT.java goldengate.ftp.core.command.access GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.rfc3659;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;

/**
 * MLST command
 * @author frederic
 * goldengate.ftp.core.command.service MLST
 * 
 */
public class MLST extends AbstractCommand {

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() throws FtpCommandAbstractException {
		// First Check if any argument
		String path = null;
		if (! this.hasArg()) {
			path = this.getFtpSession().getFtpDir().getPwd();
		} else {
			path = this.getArg();
		}
		String message = this.getFtpSession().getFtpDir().fileFull(path, false);
		this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_250_REQUESTED_FILE_ACTION_OKAY,message);
	}

}
