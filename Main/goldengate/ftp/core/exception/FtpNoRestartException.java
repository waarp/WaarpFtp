/**
 * Frederic Bregier LGPL 11 janv. 09 FtpException.java
 * goldengate.ftp.core.exception GoldenGateFtp frederic
 */
package goldengate.ftp.core.exception;

/**
 * No restart exception
 * 
 * @author frederic goldengate.ftp.core.exception FtpNoRestartException
 * 
 */
public class FtpNoRestartException extends Exception {

    /**
	 * 
	 */
    private static final long serialVersionUID = -1400965989265245071L;

    /**
     * @param message
     */
    public FtpNoRestartException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public FtpNoRestartException(String message, Throwable cause) {
        super(message, cause);
    }

}
