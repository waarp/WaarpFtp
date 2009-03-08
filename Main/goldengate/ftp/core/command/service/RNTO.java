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
import goldengate.ftp.core.command.exception.Reply553Exception;
import goldengate.ftp.core.exception.FtpNoFileException;
import goldengate.ftp.core.exception.FtpNoTransferException;
import goldengate.ftp.core.file.FtpFile;

/**
 * RNTO command
 * @author frederic
 * goldengate.ftp.core.command.service RNTO
 * 
 */
public class RNTO extends AbstractCommand {

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
		FtpFile file = null;
		try {
			file = this.getFtpSession().getDataConn().getFtpTransferControl().getExecutingFtpTransfer().getFtpFile();
		} catch (FtpNoFileException e) {
		} catch (FtpNoTransferException e) {
		}
		if (file != null) {
			String previousName = file.getFile();
			if (file.renameTo(filename)) {
				this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_250_REQUESTED_FILE_ACTION_OKAY,
						"\""+filename+"\" as new file name for \""+previousName+"\"");
				return;
			}
		}
		// File name not allowed or not found
		throw new Reply553Exception("Filename not allowed");
	}

}
