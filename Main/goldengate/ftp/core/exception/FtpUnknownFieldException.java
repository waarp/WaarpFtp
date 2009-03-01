/**
 * Frederic Bregier LGPL 11 janv. 09 
 * FtpException.java goldengate.ftp.core.exception GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.exception;

/**
 * Unknown Field exception
 * @author frederic
 * goldengate.ftp.core.exception FtpUnknownFieldException
 * 
 */
public class FtpUnknownFieldException extends Exception {
	/**
	 * serialVersionUID of long: 
	 */
	private static final long serialVersionUID = 6752182711992342555L;

	/**
	 * @param message
	 */
	public FtpUnknownFieldException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FtpUnknownFieldException(String message, Throwable cause) {
		super(message, cause);
	}

}
