/**
 * Frederic Bregier LGPL 10 janv. 09 PORT.java
 * goldengate.ftp.core.command.access GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.parameter;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpArgumentCode;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.Reply501Exception;
import goldengate.ftp.core.command.exception.Reply504Exception;
import goldengate.ftp.core.exception.FtpInvalidArgumentException;

/**
 * STRU command
 * 
 * @author frederic goldengate.ftp.core.command.parameter STRU
 * 
 */
public class STRU extends AbstractCommand {

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    @Override
    public void exec() throws Reply501Exception, Reply504Exception {
        // First Check if any argument
        if (!this.hasArg()) {
            this.getFtpSession().getDataConn().setStructure(
                    FtpArgumentCode.TransferStructure.FILE);
            this.getFtpSession().setReplyCode(
                    FtpReplyCode.REPLY_200_COMMAND_OKAY,
                    "Structure set to " +
                            FtpArgumentCode.TransferStructure.FILE.name());
            return;
        }
        FtpArgumentCode.TransferStructure transferStructure;
        try {
            transferStructure = FtpArgumentCode.getTransferStructure(this
                    .getArg().charAt(0));
        } catch (FtpInvalidArgumentException e) {
            throw new Reply501Exception("Unrecognize Structure: " +
                    this.getArg());
        }
        if (transferStructure == FtpArgumentCode.TransferStructure.FILE) {
            this.getFtpSession().getDataConn().setStructure(transferStructure);
        } else if (transferStructure == FtpArgumentCode.TransferStructure.RECORD) {
            this.getFtpSession().getDataConn().setStructure(transferStructure);
        } else {
            throw new Reply504Exception("Structure not implemented: " +
                    transferStructure.name());
        }
        this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_200_COMMAND_OKAY,
                "Structure set to " + transferStructure.name());
    }

}
