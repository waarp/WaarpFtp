/**
 * Frederic Bregier LGPL 10 janv. 09 
 * PORT.java goldengate.ftp.core.command.access GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.service;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.exception.FtpNoTransferException;

/**
 * ABOR command
 * @author frederic
 * goldengate.ftp.core.command.service ABOR
 * 
 */
public class ABOR extends AbstractCommand {


	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() throws FtpCommandAbstractException {
		// First check if the data connection is opened
		if (this.getFtpSession().getDataConn().isConnected()) {
			// Now check if the data connection is currently used
			try {
				this.getFtpSession().getDataConn().getFtpTransferControl().getExecutingFtpTransfer();
			} catch (FtpNoTransferException e) {
				this.getFtpSession().getDataConn().getFtpTransferControl().clear();
				this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_226_CLOSING_DATA_CONNECTION,null);
				return;
			}
			this.getFtpSession().getDataConn().getFtpTransferControl().setTransferAbortedFromInternal(false);
			return;
		}
		this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_226_CLOSING_DATA_CONNECTION,null);
	}

}
