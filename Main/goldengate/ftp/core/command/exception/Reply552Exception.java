/**
 * Frederic Bregier LGPL 18 janv. 09 Reply421Exception.java
 * goldengate.ftp.core.command.exception GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.exception;

import goldengate.ftp.core.command.FtpReplyCode;

/**
 * 552 Requested file action aborted. Exceeded storage allocation (for current
 * directory or dataset).
 * 
 * @author frederic goldengate.ftp.core.command.exception Reply552Exception
 * 
 */
public class Reply552Exception extends FtpCommandAbstractException {

    /**
     * serialVersionUID of long:
     */
    private static final long serialVersionUID = 552L;

    /**
     * 552 Requested file action aborted. Exceeded storage allocation (for
     * current directory or dataset).
     * 
     * @param message
     */
    public Reply552Exception(String message) {
        super(
                FtpReplyCode.REPLY_552_REQUESTED_FILE_ACTION_ABORTED_EXCEEDED_STORAGE,
                message);
    }

}
