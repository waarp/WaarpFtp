/**
 * Frederic Bregier LGPL 10 janv. 09 PORT.java
 * goldengate.ftp.core.command.access GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.service;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.command.exception.Reply450Exception;
import goldengate.ftp.core.command.exception.Reply501Exception;
import goldengate.ftp.core.command.exception.Reply553Exception;
import goldengate.ftp.core.file.FtpFile;
import goldengate.ftp.core.utils.FtpCommandUtils;

/**
 * STOR command
 * 
 * @author frederic goldengate.ftp.core.command.service STOR
 * 
 */
public class STOR extends AbstractCommand {

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    @Override
    public void exec() throws FtpCommandAbstractException {
        if (!this.hasArg()) {
            this.invalidCurrentCommand();
            throw new Reply501Exception("Need a pathname as argument");
        }
        String filename = this.getArg();
        FtpFile file = this.getFtpSession().getFtpDir()
                .setFile(filename, false);
        if (file != null) {
            if (file.store()) {
                FtpCommandUtils.openDataConnection(this.getFtpSession());
                this.getFtpSession().getDataConn().getFtpTransferControl()
                        .setNewFtpTransfer(getCode(), file);
                return;
            }
            // Cannot find file
            throw new Reply450Exception("Store operation not allowed");
        }
        // Filename not allowed
        throw new Reply553Exception("Filename not allowed");
    }

}
