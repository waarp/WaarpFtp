/**
 * Frederic Bregier LGPL 10 janv. 09 
 * PORT.java goldengate.ftp.core.command.access GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.parameter;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpArgumentCode;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.FtpArgumentCode.TransferSubType;
import goldengate.ftp.core.command.exception.Reply501Exception;
import goldengate.ftp.core.command.exception.Reply504Exception;
import goldengate.ftp.core.exception.FtpInvalidArgumentException;

/**
 * TYPE command
 * @author frederic
 * goldengate.ftp.core.command.parameter TYPE
 * 
 */
public class TYPE extends AbstractCommand {

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() throws Reply501Exception, Reply504Exception {
		// First Check if any argument
		if (! this.hasArg()) {
			this.getFtpSession().getDataConn().setType(FtpArgumentCode.TransferType.ASCII);
			this.getFtpSession().getDataConn().setSubType(TransferSubType.NONPRINT);
			this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_200_COMMAND_OKAY,
					"Type set to "+FtpArgumentCode.TransferType.ASCII.name()+" "+TransferSubType.NONPRINT);
			return;
		}
		FtpArgumentCode.TransferType transferType;
		String []types = this.getArgs();
		try {
			transferType = FtpArgumentCode.getTransferType(types[0].charAt(0));
		} catch (FtpInvalidArgumentException e) {
			throw new Reply501Exception("Unrecognize Type: "+this.getArg());
		}
		if (transferType == FtpArgumentCode.TransferType.ASCII) {
			this.getFtpSession().getDataConn().setType(transferType);
		} else if (transferType == FtpArgumentCode.TransferType.IMAGE) {
			this.getFtpSession().getDataConn().setType(transferType);
		} else {
			throw new Reply504Exception("Type not implemented: "+transferType.name());
		}
		// Look at the subtype or format control
		if (types.length > 2) {
			TransferSubType transferSubType = null;
			for (int i = 1; i < types.length; i++) {
				if (types[i].length() != 0) {
					try {
						transferSubType = FtpArgumentCode.getTransferSubType(types[i].charAt(0));
					} catch (FtpInvalidArgumentException e) {
						throw new Reply501Exception("Unrecognize Format Control: "+types[i]);
					}
					if (transferSubType != TransferSubType.NONPRINT) {
						throw new Reply504Exception("Format Control not implemented: "+transferSubType.name());
					}
				}
			}
			this.getFtpSession().getDataConn().setSubType(TransferSubType.NONPRINT);
		} else {
			this.getFtpSession().getDataConn().setSubType(TransferSubType.NONPRINT);
		}
		this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_200_COMMAND_OKAY,
				"Type set to "+transferType.name()+" "+TransferSubType.NONPRINT);
	}

}
