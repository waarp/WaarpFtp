/**
 * Frederic Bregier LGPL 11 janv. 09 FtpException.java
 * goldengate.ftp.core.exception GoldenGateFtp frederic
 */
package goldengate.ftp.core.exception;

/**
 * No transfer exception
 * 
 * @author frederic goldengate.ftp.core.exception FtpNoTransferException
 * 
 */
public class FtpNoTransferException extends Exception {

    /**
	 * 
	 */
    private static final long serialVersionUID = 977343700748516315L;

    /**
     * @param message
     */
    public FtpNoTransferException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public FtpNoTransferException(String message, Throwable cause) {
        super(message, cause);
    }

}
