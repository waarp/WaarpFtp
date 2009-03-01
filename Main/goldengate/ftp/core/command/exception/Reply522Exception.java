/**
 * Frederic Bregier LGPL 18 janv. 09 
 * Reply421Exception.java goldengate.ftp.core.command.exception GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.exception;

import goldengate.ftp.core.command.FtpReplyCode;

/**
 * 522 Extended Port Failure - unknown network protocol.
 * @author frederic
 * goldengate.ftp.core.command.exception Reply504Exception
 * 
 */
public class Reply522Exception extends FtpCommandAbstractException {

	/**
	 * serialVersionUID of long: 
	 */
	private static final long serialVersionUID = 522L;

	/**
	 * 504 Command not implemented for that parameter.
	 * @param message
	 */
	public Reply522Exception(String message) {
		super(FtpReplyCode.REPLY_522_EXTENDED_PORT_FAILURE_UNKNOWN_NETWORK_PROTOCOL,
				message);
	}
	
}
