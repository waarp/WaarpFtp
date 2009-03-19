/**
 * Frederic Bregier LGPL 18 janv. 09 Reply421Exception.java
 * goldengate.ftp.core.command.exception GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.exception;

import goldengate.ftp.core.command.FtpReplyCode;

/**
 * 421 Service not available, closing control connection. This may be a reply to
 * any command if the service knows it must shut down.
 * 
 * @author frederic goldengate.ftp.core.command.exception Reply421Exception
 * 
 */
public class Reply421Exception extends FtpCommandAbstractException {

    /**
     * serialVersionUID of long:
     */
    private static final long serialVersionUID = 421L;

    /**
     * 421 Service not available, closing control connection. This may be a
     * reply to any command if the service knows it must shut down.
     * 
     * @param message
     */
    public Reply421Exception(String message) {
        super(
                FtpReplyCode.REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
                message);
    }

}
