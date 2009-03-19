/**
 * Frederic Bregier LGPL 10 janv. 09 PORT.java
 * goldengate.ftp.core.command.access GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.rfc2389;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;

/**
 * FEAT command
 * 
 * @author frederic goldengate.ftp.core.command.service FEAT
 * 
 */
public class FEAT extends AbstractCommand {

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    @Override
    public void exec() {
        this.getFtpSession().setReplyCode(
                FtpReplyCode.REPLY_211_SYSTEM_STATUS_REPLY,
                this.getFtpSession().getBusinessHandler().getFeatMessage());
    }

}
