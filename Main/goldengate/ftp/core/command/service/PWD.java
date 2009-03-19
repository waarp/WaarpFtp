/**
 * Frederic Bregier LGPL 10 janv. 09 PORT.java
 * goldengate.ftp.core.command.access GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.service;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;

/**
 * PWD command
 * 
 * @author frederic goldengate.ftp.core.command.service PWD
 * 
 */
public class PWD extends AbstractCommand {

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    @Override
    public void exec() throws FtpCommandAbstractException {
        this.getFtpSession().setReplyCode(
                FtpReplyCode.REPLY_257_PATHNAME_CREATED,
                "\"" + this.getFtpSession().getFtpDir().getPwd() +
                        "\" is current directory");
    }

}
