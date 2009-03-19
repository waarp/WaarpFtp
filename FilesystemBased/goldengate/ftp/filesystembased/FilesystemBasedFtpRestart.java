/**
 * 
 */
package goldengate.ftp.filesystembased;

import goldengate.ftp.core.command.FtpArgumentCode.TransferMode;
import goldengate.ftp.core.command.FtpArgumentCode.TransferStructure;
import goldengate.ftp.core.command.FtpArgumentCode.TransferType;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.command.exception.Reply502Exception;
import goldengate.ftp.core.data.FtpDataAsyncConn;
import goldengate.ftp.core.exception.FtpNoRestartException;
import goldengate.ftp.core.file.FtpRestart;
import goldengate.ftp.core.session.FtpSession;

/**
 * Filesystem implementation of a FtpRestart.<br>
 * Only FILE+STREAM is supported (byte position in file).
 * 
 * @author fbregier
 * 
 */
public class FilesystemBasedFtpRestart extends FtpRestart {
    /**
     * Valid Position for the next current file
     */
    private long position = -1;

    /**
     * @param session
     */
    public FilesystemBasedFtpRestart(FtpSession session) {
        super(session);
    }

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.file.FtpRestart#restartMarker(java.lang.String)
     */
    @Override
    public boolean restartMarker(String marker)
            throws FtpCommandAbstractException {
        FtpDataAsyncConn dataConn = this.getFtpSession().getDataConn();
        if ((dataConn.getStructure() == TransferStructure.FILE) &&
                (dataConn.getMode() == TransferMode.STREAM) &&
                (dataConn.getType() != TransferType.LENGTH)) {
            long newposition = 0;
            try {
                newposition = Long.parseLong(marker);
            } catch (NumberFormatException e) {
                throw new Reply502Exception(
                        "Marker must be length in byte as a position");
            }
            this.position = newposition;
            this.setSet(true);
            return true;
        }
        throw new Reply502Exception(
                "Marker not implemented for such Mode, Type and Structure");
    }

    /**
     * 
     * @return the position from a previous REST command
     * @throws FtpNoRestartException
     *             if no REST command was issued before
     */
    public long getPosition() throws FtpNoRestartException {
        if (this.isSet()) {
            this.setSet(false);
            return this.position;
        }
        throw new FtpNoRestartException("Restart is not set");
    }
}
