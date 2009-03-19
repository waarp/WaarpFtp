/**
 * 
 */
package goldengate.ftp.filesystembased.specific;

import java.io.File;

/**
 * JDK6 version of specific functions for Filesystem.
 * 
 * @author fbregier
 * 
 */
public class FilesystemBasedFtpDirJdk6 extends FilesystemBasedFtpDirJdkAbstract {
    /**
     * 
     * @param file
     * @return True if the file is executable
     */
    @Override
    public boolean canExecute(File file) {
        return file.canExecute();
    }

    /**
     * 
     * @param directory
     * @return the free space of the given Directory
     */
    @Override
    public long getFreeSpace(File directory) {
        try {
            return directory.getFreeSpace();
        } catch (Exception e) {
            return (-1);
        }
    }
}
