/**
 * Frederic Bregier LGPL 18 janv. 09 
 * Reply421Exception.java goldengate.ftp.core.command.exception GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.exception;

import goldengate.ftp.core.command.FtpReplyCode;

/**
 * 452 Requested action not taken. Insufficient storage space in system.
 * @author frederic
 * goldengate.ftp.core.command.exception Reply452Exception
 * 
 */
public class Reply452Exception extends FtpCommandAbstractException {

	/**
	 * serialVersionUID of long: 
	 */
	private static final long serialVersionUID = 452L;

	/**
	 * 452 Requested action not taken. Insufficient storage space in system.
	 * @param message
	 */
	public Reply452Exception(String message) {
		super(FtpReplyCode.REPLY_452_REQUESTED_ACTION_NOT_TAKEN,
				message);
	}
	
}
