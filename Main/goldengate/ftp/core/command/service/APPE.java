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
import goldengate.ftp.core.command.exception.Reply553Exception;
import goldengate.ftp.core.file.FtpFile;
import goldengate.ftp.core.utils.FtpCommandUtils;

/**
 * APPE command
 * @author frederic
 * goldengate.ftp.core.command.service APPE
 * 
 */
public class APPE extends AbstractCommand {

	/**
	 */
	public APPE() {
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
		FtpFile file = this.getFtpSession().getFtpDir().setFile(filename, true);
		if (file != null) {
			if (file.store()) {
				FtpCommandUtils.openDataConnection(this.getFtpSession());
				this.getFtpSession().getDataConn().getFtpTransferControl().
					setNewFtpTransfer(getCode(), file);
				return;
			}
			throw new Reply450Exception("Append operation not started");
		}
		throw new Reply553Exception("Filename not allowed");
	}

}
