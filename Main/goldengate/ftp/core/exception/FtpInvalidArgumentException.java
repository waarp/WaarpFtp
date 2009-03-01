/**
 * Frederic Bregier LGPL 11 janv. 09 
 * FtpException.java goldengate.ftp.core.exception GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.exception;

/**
 * Argument invalid exception
 * @author frederic
 * goldengate.ftp.core.exception FtpInvalidArgumentException
 * 
 */
public class FtpInvalidArgumentException extends Exception {

	/**
	 * serialVersionUID of long: 
	 */
	private static final long serialVersionUID = -3817642817509722692L;

	/**
	 * @param message
	 */
	public FtpInvalidArgumentException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FtpInvalidArgumentException(String message, Throwable cause) {
		super(message, cause);
	}

}
