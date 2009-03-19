/**
 * Frederic Bregier LGPL 11 janv. 09 FtpException.java
 * goldengate.ftp.core.exception GoldenGateFtp frederic
 */
package goldengate.ftp.core.exception;

/**
 * File End Of Transfer exception: the end of the transfer is reached from file
 * point of view
 * 
 * @author frederic goldengate.ftp.core.exception FtpFileEndOfTransferException
 * 
 */
public class FtpFileEndOfTransferException extends Exception {

    /**
	 * 
	 */
    private static final long serialVersionUID = 977343700748516315L;

    /**
     * @param message
     */
    public FtpFileEndOfTransferException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public FtpFileEndOfTransferException(String message, Throwable cause) {
        super(message, cause);
    }

}
