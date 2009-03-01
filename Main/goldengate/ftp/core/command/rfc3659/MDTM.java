/**
 * Frederic Bregier LGPL 10 janv. 09 
 * PORT.java goldengate.ftp.core.command.access GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.rfc3659;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.command.exception.Reply501Exception;

/**
 * MDTM command
 * @author frederic
 * goldengate.ftp.core.command.service MDTM
 * 
 */
public class MDTM extends AbstractCommand {

	/**
	 */
	public MDTM() {
		super();
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() throws FtpCommandAbstractException {
		// First Check if any argument
		if (! this.hasArg()) {
			throw new Reply501Exception("Need a path as argument");
		}
		String arg = this.getArg();
		String time = this.getFtpSession().getFtpDir().getModificationTime(arg);
		this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_213_FILE_STATUS,time);
	}

}
