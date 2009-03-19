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
 * NLST command
 * 
 * @author frederic goldengate.ftp.core.command.service NLST
 * 
 */
public class NLST extends AbstractCommand {

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    @Override
    public void exec() throws FtpCommandAbstractException {
        String path = null;
        List<String> files = null;
        if (!this.hasArg()) {
            path = this.getFtpSession().getFtpDir().getPwd();
            files = this.getFtpSession().getFtpDir().list(path);
        } else {
            path = this.getArg();
            if (path.startsWith("-l") || path.startsWith("-L")) {
                // This should be a LIST command
                String[] paths = this.getArgs();
                if (paths.length > 1) {
                    files = this.getFtpSession().getFtpDir().listFull(paths[1],
                            true);
                } else {
                    files = this.getFtpSession().getFtpDir().listFull(
                            this.getFtpSession().getFtpDir().getPwd(), true);
                }
            } else {
                files = this.getFtpSession().getFtpDir().list(path);
            }
        }
        FtpCommandUtils.openDataConnection(this.getFtpSession());
        this.getFtpSession().getDataConn().getFtpTransferControl()
                .setNewFtpTransfer(getCode(), files, path);
    }

}
