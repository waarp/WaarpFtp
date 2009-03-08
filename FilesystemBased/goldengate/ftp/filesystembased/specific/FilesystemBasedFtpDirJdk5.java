/**
 * 
 */
package goldengate.ftp.filesystembased.specific;


import goldengate.ftp.filesystembased.config.FilesystemBasedFtpConfiguration;

import java.io.File;

/**
 * JDK5 version of specific functions for Filesystem.<br>
 * Note: this class depends on Apache commons Io.
 * @author fbregier
 *
 */
public class FilesystemBasedFtpDirJdk5 extends FilesystemBasedFtpDirJdkAbstract {
	/**
	 * 
	 * @param file
	 * @return True if the file is executable
	 */
	public boolean canExecute(File file) {
		return false;
	}
	/**
	 * 
	 * @param directory
	 * @return the free space of the given Directory
	 */
	public long getFreeSpace(File directory) {
		if (FilesystemBasedFtpConfiguration.ueApacheCommonsIo) {
			return FilesystemBasedFtpCommonsIo.freeSpace(directory.getAbsolutePath());
		}
		return (-1);
	}
}
