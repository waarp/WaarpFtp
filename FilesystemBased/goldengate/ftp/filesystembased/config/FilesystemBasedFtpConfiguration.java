/**
 * Frederic Bregier LGPL 1 mars 09 
 * FilesystemBasedFtpConfiguration.java goldengate.ftp.filesystembased.config GoldenGateFtp
 * frederic
 */
package goldengate.ftp.filesystembased.config;

import goldengate.ftp.core.config.FtpConfiguration;

/**
 * FtpConfiguration specifics for Filesystem Based File and Directories
 * @author frederic
 * goldengate.ftp.filesystembased.config FilesystemBasedFtpConfiguration
 * 
 */
public abstract class FilesystemBasedFtpConfiguration extends FtpConfiguration {
	/**
	 * Should the Ftp Server use the Apache Commons Io or not: if not wildcard and freespace (ALLO) will not be supported.
	 */
	public static boolean ueApacheCommonsIo = true;
	/**
	 * Should a file MD5 be computed using FastMD5
	 */
	public static boolean useFastMd5 = true;
	/**
	 * If using Fast MD5, should we used the binary JNI library, empty meaning no. FastMD5 is up to 50% fastest than JVM.
	 */
	public static String fastMd5Path = null;
	/**
	 * Should a file MD5 SHA1 be computed using NIO. In low usage, direct access is faster. In high usage, it might be better to use Nio.
	 */
	public static boolean useNio = false;
	/**
	 * @param classtype
	 * @param businessHandler
	 * @param dataBusinessHandler
	 */
	public FilesystemBasedFtpConfiguration(Class classtype,
			Class businessHandler, Class dataBusinessHandler) {
		super(classtype, businessHandler, dataBusinessHandler);
	}
}
