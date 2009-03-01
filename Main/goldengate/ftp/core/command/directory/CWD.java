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
import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;

/**
 * CWD command
 * @author frederic
 * goldengate.ftp.core.command.directory CWD
 * 
 */
public class CWD extends AbstractCommand {
	/**
	 * Internal Logger
	 */
	private static final FtpInternalLogger logger =
        FtpInternalLoggerFactory.getLogger(CWD.class);
	/**
	 */
	public CWD() {
		super();
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() throws FtpCommandAbstractException {
		FtpDir current = this.getFtpSession().getFtpDir();
		if (current == null) {
			logger.warn("not identidied");
			throw new Reply530Exception("Not authentificated");
		}
		String nextDir = this.getArg();
		if (!this.hasArg()) {
			nextDir = "/";
		}
		if (current.changeDirectory(nextDir)) {
			this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_250_REQUESTED_FILE_ACTION_OKAY,
					"\""+current.getPwd()+"\" is the new current directory");
		} else {
			this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,null);
		}
	}

}
