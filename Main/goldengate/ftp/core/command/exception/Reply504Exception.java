/**
 * Frederic Bregier LGPL 18 janv. 09 
 * Reply421Exception.java goldengate.ftp.core.command.exception GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.exception;

import goldengate.ftp.core.command.FtpReplyCode;

/**
 * 504 Command not implemented for that parameter.
 * @author frederic
 * goldengate.ftp.core.command.exception Reply504Exception
 * 
 */
public class Reply504Exception extends FtpCommandAbstractException {

	/**
	 * serialVersionUID of long: 
	 */
	private static final long serialVersionUID = 504L;

	/**
	 * 504 Command not implemented for that parameter.
	 * @param message
	 */
	public Reply504Exception(String message) {
		super(FtpReplyCode.REPLY_504_COMMAND_NOT_IMPLEMENTED_FOR_THAT_PARAMETER,
				message);
	}
	
}
