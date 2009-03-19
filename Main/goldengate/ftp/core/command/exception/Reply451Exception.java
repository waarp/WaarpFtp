/**
 * Frederic Bregier LGPL 18 janv. 09 Reply421Exception.java
 * goldengate.ftp.core.command.exception GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.exception;

import goldengate.ftp.core.command.FtpReplyCode;

/**
 * 451 Requested action aborted: local error in processing.
 * 
 * @author frederic goldengate.ftp.core.command.exception Reply451Exception
 * 
 */
public class Reply451Exception extends FtpCommandAbstractException {

    /**
     * serialVersionUID of long:
     */
    private static final long serialVersionUID = 451L;

    /**
     * 451 Requested action aborted: local error in processing.
     * 
     * @param message
     */
    public Reply451Exception(String message) {
        super(FtpReplyCode.REPLY_451_REQUESTED_ACTION_ABORTED, message);
    }

}
