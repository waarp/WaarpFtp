/**
 * Frederic Bregier LGPL 18 janv. 09 Reply421Exception.java
 * goldengate.ftp.core.command.exception GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.exception;

import goldengate.ftp.core.command.FtpReplyCode;

/**
 * 503 Bad sequence of commands.
 * 
 * @author frederic goldengate.ftp.core.command.exception Reply503Exception
 * 
 */
public class Reply503Exception extends FtpCommandAbstractException {

    /**
     * serialVersionUID of long:
     */
    private static final long serialVersionUID = 503L;

    /**
     * 503 Bad sequence of commands.
     * 
     * @param message
     */
    public Reply503Exception(String message) {
        super(FtpReplyCode.REPLY_503_BAD_SEQUENCE_OF_COMMANDS, message);
    }

}
