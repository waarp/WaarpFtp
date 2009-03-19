/**
 * Frederic Bregier LGPL 18 janv. 09 Reply421Exception.java
 * goldengate.ftp.core.command.exception GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.exception;

import goldengate.ftp.core.command.FtpReplyCode;

/**
 * 530 Not logged in.
 * 
 * @author frederic goldengate.ftp.core.command.exception Reply530Exception
 * 
 */
public class Reply530Exception extends FtpCommandAbstractException {

    /**
     * serialVersionUID of long:
     */
    private static final long serialVersionUID = 530L;

    /**
     * 530 Not logged in.
     * 
     * @param message
     */
    public Reply530Exception(String message) {
        super(FtpReplyCode.REPLY_530_NOT_LOGGED_IN, message);
    }

}
