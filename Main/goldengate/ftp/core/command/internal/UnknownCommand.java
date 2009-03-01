/**
 * Frederic Bregier LGPL 10 janv. 09 
 * USER.java goldengate.ftp.core.command.access GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.internal;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;

/**
 * Unrecognized command (unknown command)
 * @author frederic
 * goldengate.ftp.core.command UnknownCommand
 * 
 */
public class UnknownCommand extends AbstractCommand {
	/**
	 * Internal Logger
	 */
	private static final FtpInternalLogger logger =
        FtpInternalLoggerFactory.getLogger(UnknownCommand.class);

	/**
	 */
	public UnknownCommand() {
		super();
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() {
		this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_500_SYNTAX_ERROR_COMMAND_UNRECOGNIZED, 
				"Unknown Command: "+this.getCommand()+" with argument: "+this.getArg());
		logger.warn(this.getFtpSession().getAnswer());
		this.invalidCurrentCommand();
	}
}
