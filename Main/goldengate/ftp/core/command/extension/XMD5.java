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
 * XMD5 command: takes a filename and returns the MD5 of the file
 * @author frederic
 * goldengate.ftp.core.command.service XMD5
 * 
 */
public class XMD5 extends AbstractCommand {

	/**
	 */
	public XMD5() {
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
		
		String crc = FtpDir.getHex(this.getFtpSession().getFtpDir().getMD5(filename));
		this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_250_REQUESTED_FILE_ACTION_OKAY,
				crc+" \""+filename+"\" MD5");
	}

}
