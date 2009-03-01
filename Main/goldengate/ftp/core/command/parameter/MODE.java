/**
 * Frederic Bregier LGPL 10 janv. 09 
 * PORT.java goldengate.ftp.core.command.access GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.parameter;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpArgumentCode;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.Reply501Exception;
import goldengate.ftp.core.command.exception.Reply504Exception;
import goldengate.ftp.core.exception.FtpInvalidArgumentException;

/**
 * MODE command
 * @author frederic
 * goldengate.ftp.core.command.parameter MODE
 * 
 */
public class MODE extends AbstractCommand {

	/**
	 */
	public MODE() {
		super();
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() throws Reply501Exception, Reply504Exception {
		// First Check if any argument
		if (! this.hasArg()) {
			// Default
			this.getFtpSession().getDataConn().setMode(FtpArgumentCode.TransferMode.STREAM);
			this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_200_COMMAND_OKAY,
					"Mode set to "+FtpArgumentCode.TransferMode.STREAM.name());
			return;
		}
		FtpArgumentCode.TransferMode transferMode;
		try {
			transferMode = FtpArgumentCode.getTransferMode(this.getArg().charAt(0));
		} catch (FtpInvalidArgumentException e) {
			throw new Reply501Exception("Unrecognize Mode: "+this.getArg());
		}
		if (transferMode == FtpArgumentCode.TransferMode.BLOCK) {
			this.getFtpSession().getDataConn().setMode(transferMode);
		} else if (transferMode == FtpArgumentCode.TransferMode.STREAM) {
			this.getFtpSession().getDataConn().setMode(transferMode);
		} else {
			throw new Reply504Exception("Mode not implemented: "+transferMode.name());
		}
		this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_200_COMMAND_OKAY,
				"Mode set to "+transferMode.name());
	}

}
