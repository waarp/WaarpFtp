/**
 * Frederic Bregier LGPL 18 janv. 09 
 * Reply421Exception.java goldengate.ftp.core.command.exception GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.exception;

import goldengate.ftp.core.command.FtpReplyCode;

/**
 * 553 Requested action not taken. File name not allowed.
 * @author frederic
 * goldengate.ftp.core.command.exception Reply553Exception
 * 
 */
public class Reply553Exception extends FtpCommandAbstractException {

	/**
	 * serialVersionUID of long: 
	 */
	private static final long serialVersionUID = 553L;

	/**
	 * 553 Requested action not taken. File name not allowed.
	 * @param message
	 */
	public Reply553Exception(String message) {
		super(FtpReplyCode.REPLY_553_REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED,
				message);
	}
	
}
