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
 * Unimplemented command
 * @author frederic
 * goldengate.ftp.core.command UnimplementedCommand
 * 
 */
public abstract class UnimplementedCommand extends AbstractCommand {
	/**
	 * Internal Logger
	 */
	private static final FtpInternalLogger logger =
        FtpInternalLoggerFactory.getLogger(UnimplementedCommand.class);

	/**
	 */
	public UnimplementedCommand() {
		super();
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() {
		this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_502_COMMAND_NOT_IMPLEMENTED, 
				"Unimplemented Command: "+this.getCommand()+" with argument: "+this.getArg());
		logger.warn(this.getFtpSession().getAnswer());
		this.invalidCurrentCommand();
	}
}
