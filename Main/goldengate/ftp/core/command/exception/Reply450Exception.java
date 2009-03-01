/**
 * Frederic Bregier LGPL 18 janv. 09 
 * Reply421Exception.java goldengate.ftp.core.command.exception GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.exception;

import goldengate.ftp.core.command.FtpReplyCode;

/**
 * 450 Requested file action not taken. File unavailable (e.g., file busy).
 * @author frederic
 * goldengate.ftp.core.command.exception Reply450Exception
 * 
 */
public class Reply450Exception extends FtpCommandAbstractException {

	/**
	 * serialVersionUID of long: 
	 */
	private static final long serialVersionUID = 450L;

	/**
	 * 450 Requested file action not taken. File unavailable (e.g., file busy).
	 * @param message
	 */
	public Reply450Exception(String message) {
		super(FtpReplyCode.REPLY_450_REQUESTED_FILE_ACTION_NOT_TAKEN,
				message);
	}
	
}
