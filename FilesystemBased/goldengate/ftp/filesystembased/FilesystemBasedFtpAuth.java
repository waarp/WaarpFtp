/**
 * Frederic Bregier LGPL 1 févr. 09 FilesystemBasedFtpAuth.java
 * goldengate.ftp.core.auth.filesystem GoldenGateFtp frederic
 */
package goldengate.ftp.filesystembased;

import goldengate.ftp.core.auth.FtpAuth;
import goldengate.ftp.core.command.exception.Reply421Exception;
import goldengate.ftp.core.file.FtpDir;
import goldengate.ftp.core.session.FtpSession;

import java.io.File;

/**
 * Ftp Authentication implementation with a Mount point for the global
 * filesystem of ftp users. Business root directory is /user/account or /user.
 * 
 * @author frederic goldengate.ftp.core.auth.filesystem FilesystemBasedFtpAuth
 * 
 */
public abstract class FilesystemBasedFtpAuth extends FtpAuth {

    /**
     * @param session
     */
    public FilesystemBasedFtpAuth(FtpSession session) {
        super(session);
    }

    /**
     * Return the full path as a String (with mount point).
     * 
     * @param path
     *            relative path including business one (may be null or empty)
     * @return the full path as a String
     */
    public String getAbsolutePath(String path) {
        if ((path == null) || (path.length() == 0)) {
            return this.getFtpSession().getConfiguration().getBaseDirectory();
        }
        return this.getFtpSession().getConfiguration().getBaseDirectory() +
                FtpDir.SEPARATOR + path;
    }

    /**
     * Return the relative path from a file (without mount point)
     * 
     * @param file
     *            (full path with mount point)
     * @return the relative path from a file
     */
    public String getRelativePath(String file) {
        // Work around Windows path '\'
        return file.replaceFirst(FilesystemBasedFtpDir.normalizePath(this
                .getFtpSession().getConfiguration().getBaseDirectory()), "");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * goldengate.ftp.core.auth.FtpAuth#isBusinessPathValid(java.lang.String)
     */
    @Override
    public boolean isBusinessPathValid(String newPath) {
        if (newPath == null) return false;
        return newPath.startsWith(this.getBusinessPath());
    }

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.auth.FtpAuth#setBusinessPathFromAuth()
     */
    @Override
    protected String setBusinessRootFromAuth() throws Reply421Exception {
        String path = null;
        if (this.account == null) {
            path = FtpDir.SEPARATOR + this.user;
        } else {
            path = FtpDir.SEPARATOR + this.user + FtpDir.SEPARATOR + this.account;
        }
        String fullpath = getAbsolutePath(path);
        File file = new File(fullpath);
        if (!file.isDirectory()) {
            throw new Reply421Exception("Filesystem not ready");
        }
        return path;
    }

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.auth.FtpAuth#getBusinessPathStore()
     */
    @Override
    public String getBusinessPath() {
        return this.rootFromAuth;
    }
}
