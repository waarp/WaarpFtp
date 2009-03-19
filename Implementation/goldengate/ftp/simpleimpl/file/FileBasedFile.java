/**
 * 
 */
package goldengate.ftp.simpleimpl.file;

import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.file.FtpDir;
import goldengate.ftp.core.session.FtpSession;
import goldengate.ftp.filesystembased.FilesystemBasedFtpFile;

import java.io.File;

/**
 * FtpFile implementation based on true directories and files
 * 
 * @author fbregier
 * 
 */
public class FileBasedFile extends FilesystemBasedFtpFile {
    /**
     * @param session
     * @param dir
     *            It is not necessary the directory that owns this file.
     * @param path
     * @param append
     * @throws FtpCommandAbstractException
     */
    public FileBasedFile(FtpSession session, FtpDir dir, String path,
            boolean append) throws FtpCommandAbstractException {
        super(session, dir, path, append);
    }

    /**
     * This method is a good to have in a true File implementation.
     * 
     * @return the File associated with the current File operation
     */
    public File getTrueFile() {
        try {
            return this.getFileFromPath(getFile());
        } catch (FtpCommandAbstractException e) {
            return null;
        }
    }
}
