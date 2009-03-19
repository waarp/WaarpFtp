/**
 * Frederic Bregier LGPL 18 janv. 09 Reply421Exception.java
 * goldengate.ftp.core.command.exception GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.exception;

import goldengate.ftp.core.command.FtpReplyCode;

/**
 * 425 Can't open data connection.
 * 
 * @author frederic goldengate.ftp.core.command.exception Reply425Exception
 * 
 */
public class Reply425Exception extends FtpCommandAbstractException {

    /**
     * serialVersionUID of long:
     */
    private static final long serialVersionUID = 425L;

    /**
     * 425 Can't open data connection.
     * 
     * @param message
     */
    public Reply425Exception(String message) {
        super(FtpReplyCode.REPLY_425_CANT_OPEN_DATA_CONNECTION, message);
    }

}
