/**
 * Frederic Bregier LGPL 18 janv. 09 
 * Reply421Exception.java goldengate.ftp.core.command.exception GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.exception;

import goldengate.ftp.core.command.FtpReplyCode;

/**
 * 502 Command not implemented.
 * @author frederic
 * goldengate.ftp.core.command.exception Reply502Exception
 * 
 */
public class Reply502Exception extends FtpCommandAbstractException {

	/**
	 * serialVersionUID of long: 
	 */
	private static final long serialVersionUID = 502L;

	/**
	 * 502 Command not implemented.
	 * @param message
	 */
	public Reply502Exception(String message) {
		super(FtpReplyCode.REPLY_502_COMMAND_NOT_IMPLEMENTED,
				message);
	}
	
}
