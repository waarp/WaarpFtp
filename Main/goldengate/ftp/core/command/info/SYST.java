/**
 * Frederic Bregier LGPL 10 janv. 09 
 * PORT.java goldengate.ftp.core.command.access GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.info;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;

/**
 * SYST command
 * @author frederic
 * goldengate.ftp.core.command.service SYST
 * 
 */
public class SYST extends AbstractCommand {

	/**
	 */
	public SYST() {
		super();
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() {
		this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_215_NAME_SYSTEM_TYPE, 
				"UNIX Type: L8");
	}

}
