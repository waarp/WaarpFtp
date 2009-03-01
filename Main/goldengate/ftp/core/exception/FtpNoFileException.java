/**
 * Frederic Bregier LGPL 11 janv. 09 
 * FtpException.java goldengate.ftp.core.exception GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.exception;

/**
 * No file exception
 * @author frederic
 * goldengate.ftp.core.exception FtpNoFileException
 * 
 */
public class FtpNoFileException extends Exception {


	/**
	 * 
	 */
	private static final long serialVersionUID = -763134102928044471L;

	/**
	 * @param message
	 */
	public FtpNoFileException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FtpNoFileException(String message, Throwable cause) {
		super(message, cause);
	}

}
