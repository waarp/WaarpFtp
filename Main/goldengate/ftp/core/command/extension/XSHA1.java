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
import goldengate.ftp.core.file.FtpDir;

/**
 * XSHA1 command: takes a filename and returns the SHA-1 of the file
 * @author frederic
 * goldengate.ftp.core.command.service XSHA1
 * 
 */
public class XSHA1 extends AbstractCommand {

	/**
	 */
	public XSHA1() {
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
		
		String crc = FtpDir.getHex(this.getFtpSession().getFtpDir().getSHA1(filename));
		this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_250_REQUESTED_FILE_ACTION_OKAY,
				crc+" \""+filename+"\" SHA-1");
	}

}
