/**
 * Frederic Bregier LGPL 10 janv. 09 
 * PORT.java goldengate.ftp.core.command.access GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.service;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.command.exception.Reply452Exception;
import goldengate.ftp.core.command.exception.Reply501Exception;
import goldengate.ftp.core.exception.FtpInvalidArgumentException;

/**
 * ALLO command: test if enough space is disponible
 * @author frederic
 * goldengate.ftp.core.command.service ALLO
 * 
 */
public class ALLO extends AbstractCommand {

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() throws FtpCommandAbstractException {
		// First Check if any argument
		if (! this.hasArg()) {
			throw new Reply501Exception("Need a size as argument");
		}
		String [] args = this.getArgs();
		int size = 0;
		try {
			size = this.getValue(args[0]);
		} catch (FtpInvalidArgumentException e) {
			throw new Reply501Exception("Need a valid size as argument: "+args[0]);
		}
		long free = this.getFtpSession().getFtpDir().getFreeSpace();
		if ((free != -1) && (free < size)) {
			throw new Reply452Exception("Not enough space left");
		}
		if (free == -1) {
			this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_202_COMMAND_NOT_IMPLEMENTED,null);
		} else {
			this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_200_COMMAND_OKAY,
					"ALLO OK: "+free+" bytes available");
		}
	}

}
