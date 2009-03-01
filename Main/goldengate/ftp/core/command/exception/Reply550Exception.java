/**
 * Frederic Bregier LGPL 18 janv. 09 
 * Reply421Exception.java goldengate.ftp.core.command.exception GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.exception;

import goldengate.ftp.core.command.FtpReplyCode;

/**
 * 550 Requested action not taken. File unavailable (e.g., file not found,
 * no access).
 * @author frederic
 * goldengate.ftp.core.command.exception Reply550Exception
 * 
 */
public class Reply550Exception extends FtpCommandAbstractException {

	/**
	 * serialVersionUID of long: 
	 */
	private static final long serialVersionUID = 550L;

	/**
	 * 550 Requested action not taken. File unavailable (e.g., file not found,
	 * no access).
	 * @param message
	 */
	public Reply550Exception(String message) {
		super(FtpReplyCode.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
				message);
	}
	
}
