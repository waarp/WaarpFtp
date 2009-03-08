/**
 * Frederic Bregier LGPL 10 janv. 09 
 * USER.java goldengate.ftp.core.command.access GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.directory;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.command.exception.Reply530Exception;
import goldengate.ftp.core.file.FtpDir;

/**
 * CDUP command
 * @author frederic
 * goldengate.ftp.core.command.directory CDUP
 * 
 */
public class CDUP extends AbstractCommand {

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() throws FtpCommandAbstractException {
		FtpDir current = this.getFtpSession().getFtpDir();
		if (current == null) {
			throw new Reply530Exception("Not authentificated");
		}
		if (current.changeParentDirectory()) {
			this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_250_REQUESTED_FILE_ACTION_OKAY,
					"\""+current.getPwd()+"\" is the new current directory");
		} else {
			this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,null);
		}
	}

}
