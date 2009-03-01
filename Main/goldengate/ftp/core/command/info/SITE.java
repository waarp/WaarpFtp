/**
 * Frederic Bregier LGPL 10 janv. 09 
 * PORT.java goldengate.ftp.core.command.access GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.info;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpCommandCode;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.command.exception.Reply501Exception;
import goldengate.ftp.core.command.exception.Reply502Exception;
import goldengate.ftp.core.command.exception.Reply503Exception;
import goldengate.ftp.core.command.extension.XCRC;
import goldengate.ftp.core.command.extension.XMD5;
import goldengate.ftp.core.command.extension.XSHA1;
import goldengate.ftp.core.command.internal.IncorrectCommand;

/**
 * SITE command: implements some specific command like {@link XMD5} {@link XCRC} {@link XSHA1} as if they were called directly
 * @author frederic
 * goldengate.ftp.core.command.service SITE
 * 
 */
public class SITE extends AbstractCommand {
	/**
	 */
	public SITE() {
		super();
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() throws FtpCommandAbstractException {
		if (!this.hasArg()) {
			this.invalidCurrentCommand();
			throw new Reply501Exception("Need a command at least as argument");
		}
		// Now check what is the command as if we were in the NetworkHandler
		AbstractCommand command = FtpCommandCode.getFromLine(this.getFtpSession(), this.getArg());
		// Default message
		this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_200_COMMAND_OKAY, null);
		// First check if the command is an extension command
		if (FtpCommandCode.isExtensionCommand(command.getCode())) {
			// Now check if a transfer is on its way: illegal to have at same time two commands
			if (this.getFtpSession().getDataConn().getFtpTransferControl().isFtpTransferExecuting()) {
				throw new Reply503Exception("Previous transfer command is not finished yet");
			}
		} else {
			throw new Reply502Exception("Command not implemented: "+this.getArg());
		}
		// Command is OK, set it as current by first undo current command then set it as next after testing validity
		this.getFtpSession().setPreviousAsCurrentCommand();
		if (this.getFtpSession().getCurrentCommand().isNextCommandValid(command)) {
			this.getFtpSession().setNextCommand(command);
			this.getFtpSession().getBusinessHandler().beforeRunCommand();
			command.exec();
		} else {
			command = new IncorrectCommand();
			command.setArgs(this.getFtpSession(), this.getArg(), null, FtpCommandCode.IncorrectSequence);
			this.getFtpSession().setNextCommand(command);
			this.getFtpSession().getBusinessHandler().beforeRunCommand();
			command.exec();
		}
	}
}
