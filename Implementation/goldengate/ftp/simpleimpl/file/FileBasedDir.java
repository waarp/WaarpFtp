/**
 * 
 */
package goldengate.ftp.simpleimpl.file;

import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.file.FtpFile;
import goldengate.ftp.core.session.FtpSession;
import goldengate.ftp.filesystembased.FilesystemBasedFtpDir;

/**
 * FtpFile implementation based on true directories and files
 * 
 * @author fbregier
 * 
 */
public class FileBasedDir extends FilesystemBasedFtpDir {
    /**
     * @param session
     */
    public FileBasedDir(FtpSession session) {
        super(session);
    }

    @Override
    protected FtpFile newFtpFile(String path, boolean append)
            throws FtpCommandAbstractException {
        return new FileBasedFile(this.getFtpSession(), this, path, append);
    }
}
