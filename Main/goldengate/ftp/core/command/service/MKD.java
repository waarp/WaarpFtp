/**
 * Frederic Bregier LGPL 10 janv. 09 PORT.java
 * goldengate.ftp.core.command.access GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.service;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.command.exception.Reply501Exception;

/**
 * MKD command
 * 
 * @author frederic goldengate.ftp.core.command.service MKD
 * 
 */
public class MKD extends AbstractCommand {

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    @Override
    public void exec() throws FtpCommandAbstractException {
        // First Check if any argument
        if (!this.hasArg()) {
            throw new Reply501Exception("Need a path as argument");
        }
        String path = this.getArg();
        String newpath = this.getFtpSession().getFtpDir().mkdir(path);
        this.getFtpSession().setReplyCode(
                FtpReplyCode.REPLY_257_PATHNAME_CREATED,
                "\"" + newpath + "\" is created");
    }

}
