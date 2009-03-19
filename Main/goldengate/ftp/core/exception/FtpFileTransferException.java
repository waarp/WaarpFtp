/**
 * Frederic Bregier LGPL 11 janv. 09 FtpException.java
 * goldengate.ftp.core.exception GoldenGateFtp frederic
 */
package goldengate.ftp.core.exception;

/**
 * File Transfer exception (error during transfer from file point of view)
 * 
 * @author frederic goldengate.ftp.core.exception FtpFileTransferException
 * 
 */
public class FtpFileTransferException extends Exception {

    /**
	 * 
	 */
    private static final long serialVersionUID = 977343700748516315L;

    /**
     * @param message
     */
    public FtpFileTransferException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public FtpFileTransferException(String message, Throwable cause) {
        super(message, cause);
    }

}
