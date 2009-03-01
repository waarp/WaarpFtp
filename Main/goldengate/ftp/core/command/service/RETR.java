/**
 * Frederic Bregier LGPL 10 janv. 09 
 * PORT.java goldengate.ftp.core.command.access GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.service;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.command.exception.Reply450Exception;
import goldengate.ftp.core.command.exception.Reply501Exception;
import goldengate.ftp.core.command.exception.Reply550Exception;
import goldengate.ftp.core.file.FtpFile;
import goldengate.ftp.core.utils.FtpCommandUtils;

/**
 * RETR command
 * @author frederic
 * goldengate.ftp.core.command.service RETR
 * 
 */
public class RETR extends AbstractCommand {

	/**
	 */
	public RETR() {
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
		FtpFile file = this.getFtpSession().getFtpDir().setFile(filename, false);
		if (file != null) {
			if (file.retrieve()) {
				FtpCommandUtils.openDataConnection(this.getFtpSession());
				this.getFtpSession().getDataConn().getFtpTransferControl().
					setNewFtpTransfer(getCode(), file);
				return;
			}
			// File does not exist
			throw new Reply450Exception("Retrieve operation not allowed");
		}
		// File name not allowed
		throw new Reply550Exception("Filename not allowed");
	}

}
