/**
 * Frederic Bregier LGPL 10 janv. 09 PORT.java
 * goldengate.ftp.core.command.access GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.info;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.exception.FtpNoFileException;
import goldengate.ftp.core.exception.FtpNoTransferException;
import goldengate.ftp.core.file.FtpFile;
import goldengate.ftp.core.utils.FtpChannelUtils;

import java.util.List;

/**
 * STAT command
 * 
 * @author frederic goldengate.ftp.core.command.service STAT
 * 
 */
public class STAT extends AbstractCommand {

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    @Override
    public void exec() throws FtpCommandAbstractException {
        String path = null;
        String message = null;
        message = "STATUS information\nNo File currently in transfer\n";
        FtpFile file = null;
        try {
            file = this.getFtpSession().getDataConn().getFtpTransferControl()
                    .getExecutingFtpTransfer().getFtpFile();
        } catch (FtpNoFileException e) {
        } catch (FtpNoTransferException e) {
        }
        if (file != null) {
            if (file.isInReading()) {
                message = "STATUS information\nFile currently in Retrieve transfer\n";
            } else if (file.isInWriting()) {
                message = "STATUS information\nFile currently in Store transfer\n";
            }
        }
        if (!this.hasArg()) {
            // Current status of connection
            message += this.getFtpSession().getDataConn().getStatus();
            message += "\nControl: " +
                    FtpChannelUtils.nbDataChannels(this.getConfiguration()) +
                    " Data: " +
                    FtpChannelUtils.nbDataChannels(this.getConfiguration()) +
                    " Binded: " +
                    this.getConfiguration().getFtpInternalConfiguration()
                            .getNbBindedPassive();
            message += "\nEnd of Status";
            this.getFtpSession().setReplyCode(
                    FtpReplyCode.REPLY_211_SYSTEM_STATUS_REPLY, message);
        } else {
            // List of files from path
            path = this.getArg();
            List<String> filesInfo = this.getFtpSession().getFtpDir().listFull(
                    path, true);
            StringBuilder builder = new StringBuilder();
            builder.append("List of files from ");
            builder.append(path);
            builder.append('\n');
            for (String newfileInfo: filesInfo) {
                builder.append(newfileInfo);
                builder.append('\n');
            }
            builder.append("End of Status");
            message += builder.toString();
            this.getFtpSession().setReplyCode(
                    FtpReplyCode.REPLY_212_DIRECTORY_STATUS, message);
        }
    }

}
