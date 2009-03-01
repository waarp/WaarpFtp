/**
 * Frederic Bregier LGPL 31 janv. 09 
 * FtpFile.java goldengate.ftp.core.file GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.file;

import goldengate.ftp.core.auth.FtpAuth;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.command.exception.Reply530Exception;
import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;
import goldengate.ftp.core.session.FtpSession;

import java.util.List;

/**
 * Base of Ftp Dir where only String are considered 
 * (no File since it can be something else than a file).
 * @author frederic
 * goldengate.ftp.core.file FtpFile
 * 
 */
public abstract class FtpDir {
	/**
	 * Internal Logger
	 */
	private static final FtpInternalLogger logger =
        FtpInternalLoggerFactory.getLogger(FtpDir.class);
	/**
	 * File separator for external
	 */
	public static final String SEPARATOR = "/";
	/**
	 * File separator for external
	 */
	public static final char SEPARATORCHAR = '/';

	/**
	 * Ftp Session
	 */
	private FtpSession session = null;
	/**
	 * Curent Directory
	 */
	protected String currentDir = null;
	/**
	 * Opts command for MLSx. 
	 * (-1) means not supported, 0 supported but not active, 1 supported and active
	 */
	protected FtpOptsMLSx optsMLSx = new FtpOptsMLSx();
	/**
	 * Constructor
	 * @param session
	 */
	public FtpDir(FtpSession session) {
		this.session = session;
	}
	/**
	 * 
	 * @return the current value of Options for MLSx
	 */
	public FtpOptsMLSx getOptsMLSx() {
		return this.optsMLSx;
	}
	/**
	 * Set empty this FtpDir, mark it unReady.
	 */
	public void clear() {
		this.currentDir = null;
	}
	/**
	 * Init FtpFile after authentification is done
	 *
	 */
	public void initAfterIdentification() {
		logger.debug("Init after identification");
		this.currentDir = this.getFtpSession().getFtpAuth().getBusinessPath();;
	}
	/**
	 * Check if the authentification is correct
	 * @throws Reply530Exception
	 */
	protected void checkIdentify() throws Reply530Exception {
		if (! this.getFtpSession().getFtpAuth().isIdentified()) {
			throw new Reply530Exception("User not authentified");
		}
	}
	
	// **************** Directory part **************************
	/**
	 * 
	 * @return the FtpSession
	 */
	protected FtpSession getFtpSession() {
		return this.session;
	}
	/**
	 * Construct and Check if the given path is valid from business point of view (see {@link FtpAuth})
	 * @param path
	 * @return the construct and validated path (could be different than the one given as argument, example: '..' are removed)
	 * @throws FtpCommandAbstractException
	 */
	public abstract String validatePath(String path) throws FtpCommandAbstractException;
	/**
	 * @return the current PWD
	 * @exception FtpCommandAbstractException
	 */
	public abstract String getPwd() throws FtpCommandAbstractException;
	/**
	 * Change directory with the one given as argument
	 * @param path
	 * @return True if the change is valid
	 * @throws FtpCommandAbstractException
	 */
	public abstract boolean changeDirectory(String path) throws FtpCommandAbstractException;
	/**
	 * Change for parent directory
	 * @return True if the change is valid
	 * @throws FtpCommandAbstractException
	 */
	public abstract boolean changeParentDirectory() throws FtpCommandAbstractException;
	/**
	 * Create the directory associated with the String as path
	 * 
	 * @param directory
	 * @return the full path of the new created directory
	 * @exception FtpCommandAbstractException
	 */
	public abstract String mkdir(String directory) throws FtpCommandAbstractException;
	/**
	 * Delete the directory associated with the String as path
	 * 
	 * @param directory
	 * @return the full path of the new deleted directory
	 * @exception FtpCommandAbstractException
	 */
	public abstract String rmdir(String directory) throws FtpCommandAbstractException;
	/**
	 * Is the given path a directory and exists
	 * @param path
	 * @return True if it is a directory and it exists
	 * @throws FtpCommandAbstractException
	 */
	public abstract boolean isDirectory(String path) throws FtpCommandAbstractException;
	/**
	 * Is the given path a file and exists
	 * @param path
	 * @return True if it is a file and it exists
	 * @throws FtpCommandAbstractException
	 */
	public abstract boolean isFile(String path) throws FtpCommandAbstractException;
	/**
	 * Return the Modification time for the path
	 * @param path
	 * @return the Modification time as a String YYYYMMDDHHMMSS.sss
	 * @throws FtpCommandAbstractException
	 */
	public abstract String getModificationTime(String path) throws FtpCommandAbstractException;
	/**
	 * List all files from the given path (could be a file or a directory)
	 * @param path
	 * @return the list of paths
	 * @throws FtpCommandAbstractException
	 */
	public abstract List<String> list(String path) throws FtpCommandAbstractException;
	/**
	 * List all files with other informations from the given path (could be a file or a directory)
	 * @param path
	 * @param lsFormat True if ls Format, else MLSx format
	 * @return the list of paths and other informations
	 * @throws FtpCommandAbstractException
	 */
	public abstract List<String> listFull(String path, boolean lsFormat) throws FtpCommandAbstractException;
	/**
	 * Give for 1 file all informations from the given path (could be a file or a directory)
	 * @param path
	 * @param lsFormat True if ls Format, else MLSx format
	 * @return the path and other informations
	 * @throws FtpCommandAbstractException
	 */
	public abstract String fileFull(String path, boolean lsFormat) throws FtpCommandAbstractException;
	/**
	 * 
	 * @return the free space of the current Directory
	 * @throws FtpCommandAbstractException
	 */
	public abstract long getFreeSpace() throws FtpCommandAbstractException;

	
	// **************** Unique File part **************************
	/**
	 * Create a new FtpFile
	 * @param path
	 * @param append
	 * @return the new Ftp File 
	 * @throws FtpCommandAbstractException
	 */
	protected abstract FtpFile newFtpFile(String path, boolean append) throws FtpCommandAbstractException;
	/**
	 * Set a path as the current File
	 * @param path
	 * @param append True if this file is supposed to be in append mode (APPE), False in any other cases
	 * @return the Ftp File if it is correctly initiate
	 * @throws FtpCommandAbstractException
	 */
	public abstract FtpFile setFile(String path, boolean append) throws FtpCommandAbstractException;
	/**
	 * Set a new unique path as the current File from the current Directory (STOU)
	 * @return the Ftp File if it is correctly initiate
	 * @throws FtpCommandAbstractException
	 */
	public abstract FtpFile setUniqueFile() throws FtpCommandAbstractException;
	/**
	 * @return True if the current File is ready for reading
	 * @throws FtpCommandAbstractException
	 */
	public abstract boolean canRead() throws FtpCommandAbstractException;
	/**
	 * 
	 * @return True if the current File is ready for writing
	 * @throws FtpCommandAbstractException
	 */
	public abstract boolean canWrite() throws FtpCommandAbstractException;
	/**
	 * 
	 * @return True if the current File exists
	 * @throws FtpCommandAbstractException
	 */
	public abstract boolean exists() throws FtpCommandAbstractException;
	/**
	 * Get the CRC of the given File
	 * @param path
	 * @return the CRC
	 * @throws FtpCommandAbstractException
	 */
	public abstract long getCRC(String path) throws FtpCommandAbstractException;
	/**
	 * Get the MD5 of the given File
	 * @param path
	 * @return the MD5
	 * @throws FtpCommandAbstractException
	 */
	public abstract byte[] getMD5(String path) throws FtpCommandAbstractException;
	/**
	 * Get the SHA-1 of the given File
	 * @param path
	 * @return the SHA-1
	 * @throws FtpCommandAbstractException
	 */
	public abstract byte[] getSHA1(String path) throws FtpCommandAbstractException;
	/**
	 * Internal representation of Hexadecimal Code
	 */
	private static final char[] HEX_CHARS = {'0', '1', '2', '3',
		'4', '5', '6', '7',
		'8', '9', 'a', 'b',
		'c', 'd', 'e', 'f',};
	
	/**
	 * Get the hexadecimal representation as a String of the array of bytes
	 * @param hash
	 * @return the hexadecimal representation as a String of the array of bytes
	 */
	public static String getHex(byte[] hash) {
		char buf[] = new char[hash.length * 2];
		for (int i = 0, x = 0; i < hash.length; i++) {
			buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
			buf[x++] = HEX_CHARS[hash[i] & 0xf];
		}
		return new String(buf);
	}
	/**
	 * Get the array of bytes representation of the hexadecimal String
	 * @param hex
	 * @return the array of bytes representation of the hexadecimal String
	 */
	public static byte[] getFromHex(String hex) {
		byte from[] = hex.getBytes();
		byte hash[] = new byte[from.length/2];
		for (int i = 0, x = 0; i < hash.length; i++) {
			byte code1 = from[x++];
			byte code2 = from[x++];
			if (code1 >= HEX_CHARS[10])
				code1 -= (HEX_CHARS[10]-10);
			else
				code1 -= HEX_CHARS[0];
			if (code2 >= HEX_CHARS[10])
				code2 -= (HEX_CHARS[10]-10);
			else
				code2 -= HEX_CHARS[0];
			hash[i] = (byte)((code1 << 4)+code2);
		}
		return hash;
	}
}
