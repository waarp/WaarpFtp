/**
 * Frederic Bregier LGPL 10 janv. 09 PORT.java
 * goldengate.ftp.core.command.access GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.service;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.utils.FtpCommandUtils;

import java.util.List;

/**
 * LIST command
 * 
 * @author frederic goldengate.ftp.core.command.service LIST
 * 
 */
public class LIST extends AbstractCommand {

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    @Override
    public void exec() throws FtpCommandAbstractException {
        String path = null;
        if (!this.hasArg()) {
            path = this.getFtpSession().getFtpDir().getPwd();
        } else {
            path = this.getArg();
        }
        List<String> filesInfo = this.getFtpSession().getFtpDir().listFull(
                path, true);
        FtpCommandUtils.openDataConnection(this.getFtpSession());
        this.getFtpSession().getDataConn().getFtpTransferControl()
                .setNewFtpTransfer(getCode(), filesInfo, path);
    }

}
