/**
 * Frederic Bregier LGPL 10 janv. 09 PORT.java
 * goldengate.ftp.core.command.access GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.rfc2389;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;

/**
 * OPTS command
 * 
 * @author frederic goldengate.ftp.core.command.service OPTS
 * 
 */
public class OPTS extends AbstractCommand {

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    @Override
    public void exec() throws FtpCommandAbstractException {
        String message = this.getFtpSession().getBusinessHandler()
                .getOptsMessage(getArgs());
        this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_214_HELP_MESSAGE,
                message);
    }

}
