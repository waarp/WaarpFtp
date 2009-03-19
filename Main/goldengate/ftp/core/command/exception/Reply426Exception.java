/**
 * Frederic Bregier LGPL 18 janv. 09 Reply421Exception.java
 * goldengate.ftp.core.command.exception GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.exception;

import goldengate.ftp.core.command.FtpReplyCode;

/**
 * 426 Connection closed, transfer aborted.
 * 
 * @author frederic goldengate.ftp.core.command.exception Reply426Exception
 * 
 */
public class Reply426Exception extends FtpCommandAbstractException {

    /**
     * serialVersionUID of long:
     */
    private static final long serialVersionUID = 426L;

    /**
     * 426 Connection closed), transfer aborted.
     * 
     * @param message
     */
    public Reply426Exception(String message) {
        super(FtpReplyCode.REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED,
                message);
    }

}
