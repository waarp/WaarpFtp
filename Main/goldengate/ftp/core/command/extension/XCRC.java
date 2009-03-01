/**
 * Frederic Bregier LGPL 10 janv. 09 
 * PORT.java goldengate.ftp.core.command.access GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.extension;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.command.exception.Reply501Exception;

/**
 * XCRC command: takes a filename and returns the CRC of the file
 * @author frederic
 * goldengate.ftp.core.command.service XCRC
 * 
 */
public class XCRC extends AbstractCommand {

	/**
	 */
	public XCRC() {
		super();
	}

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
		
		String crc = Long.toString(this.getFtpSession().getFtpDir().getCRC(filename));
		this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_250_REQUESTED_FILE_ACTION_OKAY,
				crc+" \""+filename+"\" CRC");
	}

}
