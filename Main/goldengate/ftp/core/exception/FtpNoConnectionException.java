/**
 * Frederic Bregier LGPL 11 janv. 09 
 * FtpException.java goldengate.ftp.core.exception GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.exception;

/**
 * No Connection exception
 * @author frederic
 * goldengate.ftp.core.exception FtpNoConnectionException
 * 
 */
public class FtpNoConnectionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4067644127312790263L;

	/**
	 * @param message
	 */
	public FtpNoConnectionException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FtpNoConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

}
