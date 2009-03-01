/**
 * Frederic Bregier LGPL 10 janv. 09 
 * PORT.java goldengate.ftp.core.command.access GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.info;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;

/**
 * HELP command
 * @author frederic
 * goldengate.ftp.core.command.service HELP
 * 
 */
public class HELP extends AbstractCommand {

	/**
	 */
	public HELP() {
		super();
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() {
		this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_214_HELP_MESSAGE, 
				this.getFtpSession().getBusinessHandler().getHelpMessage(getArg()));
	}

}
