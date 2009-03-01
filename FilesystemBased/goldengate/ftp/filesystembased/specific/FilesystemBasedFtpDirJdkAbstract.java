/**
 * 
 */
package goldengate.ftp.filesystembased.specific;

import java.io.File;

/**
 * Abstract class to allow specific function depending on the underlying JDK to used.
 * @author fbregier
 *
 */
public abstract class FilesystemBasedFtpDirJdkAbstract {
	/**
	 * 
	 * @param directory
	 * @return the free space of the given Directory
	 */
	public abstract long getFreeSpace(File directory);
	/**
	 * Result of ls on File
	 * @param file
	 * @return True if the file is executable
	 */
	public abstract boolean canExecute(File file);
}
