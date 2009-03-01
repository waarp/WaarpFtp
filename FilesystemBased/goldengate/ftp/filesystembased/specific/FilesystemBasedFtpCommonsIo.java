/**
 * Frederic Bregier LGPL 28 févr. 09 
 * FilesystemBasedFtpCommonsIo.java goldengate.ftp.filesystembased.specific GoldenGateFtp
 * frederic
 */
package goldengate.ftp.filesystembased.specific;

import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 * This class enables to not set a dependencies on Apache Commons IO if wanted, but loosing freespace and wildcard support.
 * @author frederic
 * goldengate.ftp.filesystembased.specific FilesystemBasedFtpCommonsIo
 * 
 */
public class FilesystemBasedFtpCommonsIo {

	/**
	 * 
	 * @param pathname
	 * @return the free space of the given pathname
	 */
	public static long freeSpace(String pathname) {
		try {
			return (FileSystemUtils.freeSpaceKb(pathname)*1024);
		} catch (IOException e) {
			return (-1);
		}
	}
	/**
	 * 
	 * @param dir
	 * @return The associated FileFilter
	 */
	public static FileFilter getWildcardFileFilter(String dir) {
		return new WildcardFileFilter(dir);
	}
}
