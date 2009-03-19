/**
 * Frederic Bregier LGPL 18 janv. 09 Reply421Exception.java
 * goldengate.ftp.core.command.exception GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.exception;

import goldengate.ftp.core.command.FtpReplyCode;

/**
 * 501 Syntax error in parameters or arguments.
 * 
 * @author frederic goldengate.ftp.core.command.exception Reply501Exception
 * 
 */
public class Reply501Exception extends FtpCommandAbstractException {

    /**
     * serialVersionUID of long:
     */
    private static final long serialVersionUID = 501L;

    /**
     * 501 Syntax error in parameters or arguments.
     * 
     * @param message
     */
    public Reply501Exception(String message) {
        super(FtpReplyCode.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                message);
    }

}
