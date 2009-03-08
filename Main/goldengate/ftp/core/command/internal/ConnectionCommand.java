/**
 * Frederic Bregier LGPL 10 janv. 09 
 * USER.java goldengate.ftp.core.command.access GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.internal;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpCommandCode;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.session.FtpSession;

/**
 * Connection command: initialize the process of authentification
 * @author frederic
 * goldengate.ftp.core.command ConnectionCommand
 * 
 */
public class ConnectionCommand extends AbstractCommand {

	/**
	 * Create a ConnectionCommand
	 * @param session
	 */
	public ConnectionCommand(FtpSession session) {
		super();
		this.setArgs(session, "Connection", null, FtpCommandCode.Connection);
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() throws FtpCommandAbstractException {
		// Nothing to do except 220
		this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_220_SERVICE_READY, null);
	}
	
}
