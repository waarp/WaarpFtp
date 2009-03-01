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

/**
 * REST command
 * @author frederic
 * goldengate.ftp.core.command.service REST
 * 
 */
public class REST extends AbstractCommand {

	/**
	 */
	public REST() {
		super();
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() throws FtpCommandAbstractException {
		if (!this.hasArg()) {
			this.invalidCurrentCommand();
			throw new Reply501Exception("Need a Marker as argument");
		}
		String marker = this.getArg();
		if (this.getFtpSession().getFtpRestart().restartMarker(marker)) {
			this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_350_REQUESTED_FILE_ACTION_PENDING_FURTHER_INFORMATION,
					null);
			return;
		}
		// Marker in error
		throw new Reply501Exception("Marker is not allowed");
	}

}
