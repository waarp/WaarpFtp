/**
 * Frederic Bregier LGPL 18 janv. 09 Reply421Exception.java
 * goldengate.ftp.core.command.exception GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.exception;

import goldengate.ftp.core.command.FtpReplyCode;

/**
 * 500 Syntax error, command unrecognized. This may include errors such as
 * command line too long.
 * 
 * @author frederic goldengate.ftp.core.command.exception Reply500Exception
 * 
 */
public class Reply500Exception extends FtpCommandAbstractException {

    /**
     * serialVersionUID of long:
     */
    private static final long serialVersionUID = 500L;

    /**
     * 500 Syntax error, command unrecognized. This may include errors such as
     * command line too long.
     * 
     * @param message
     */
    public Reply500Exception(String message) {
        super(FtpReplyCode.REPLY_500_SYNTAX_ERROR_COMMAND_UNRECOGNIZED, message);
    }

}
