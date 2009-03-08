/**
 * Frederic Bregier LGPL 10 janv. 09 
 * USER.java goldengate.ftp.core.command.access GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.access;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;

/**
 * QUIT command
 * @author frederic
 * goldengate.ftp.core.command.access QUIT
 * 
 */
public class QUIT extends AbstractCommand {

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() {
		this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_221_CLOSING_CONTROL_CONNECTION,null);
	}

}
