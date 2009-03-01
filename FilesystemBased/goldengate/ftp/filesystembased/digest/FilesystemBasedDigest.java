/**
 * Frederic Bregier LGPL 28 févr. 09 
 * FilesystemBasedDigest.java goldengate.ftp.filesystembased.digest GoldenGateFtp
 * frederic
 */
package goldengate.ftp.filesystembased.digest;

import goldengate.ftp.core.file.FtpDir;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;
import goldengate.ftp.core.logging.FtpJdkLoggerFactory;
import goldengate.ftp.filesystembased.config.FilesystemBasedFtpConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;

/**
 * Class implementing digest like MD5, SHA1.
 * MD5 is based on the Fast MD5 implementation, without C library support, but can be revert to JVM native digest.
 * @author frederic
 * goldengate.ftp.filesystembased.digest FilesystemBasedDigest
 * 
 */
public class FilesystemBasedDigest {
	/**
	 * 
	 * @param dig1
	 * @param dig2
	 * @return True if the two digest are equals
	 */
	public static boolean digestEquals(byte[] dig1, byte[] dig2) {
		return MessageDigest.isEqual(dig1, dig2);
	}
	/**
	 * 
	 * @param dig1
	 * @param dig2
	 * @return True if the two digest are equals
	 */
	public static boolean digestEquals(String dig1, byte[] dig2) {
		byte []bdig1 = FtpDir.getFromHex(dig1);
		return MessageDigest.isEqual(bdig1, dig2);
	}
	/**
	 * MD5 Algorithm
	 */
	private static final String ALGO_MD5 = "MD5";
	/**
	 * SHA-1 Algorithm
	 */
	private static final String ALGO_SHA1 = "SHA-1"; 
	/**
	 * get the byte array of the MD5 for the given File using Nio access
	 * @param f
	 * @return the byte array representing the MD5
	 * @throws IOException
	 */
	public static byte[] getHashMd5Nio(File f) throws IOException {
		if (FilesystemBasedFtpConfiguration.useFastMd5) {
			return MD5.getHashNio(f);
		}
		return getHashNio(f, ALGO_MD5);
	}
	/**
	 * get the byte array of the MD5 for the given File using standard access
	 * @param f
	 * @return the byte array representing the MD5
	 * @throws IOException
	 */
	public static byte[] getHashMd5(File f) throws IOException {
		if (FilesystemBasedFtpConfiguration.useFastMd5) {
			return MD5.getHash(f);
		}
		return getHash(f, ALGO_MD5);
	}
	/**
	 * get the byte array of the SHA-1 for the given File using Nio access
	 * @param f
	 * @return the byte array representing the SHA-1
	 * @throws IOException
	 */
	public static byte[] getHashSha1Nio(File f) throws IOException {
		return getHashNio(f, ALGO_SHA1);
	}
	/**
	 * get the byte array of the SHA-1 for the given File using standard access
	 * @param f
	 * @return the byte array representing the SHA-1
	 * @throws IOException
	 */
	public static byte[] getHashSha1(File f) throws IOException {
		return getHash(f, ALGO_SHA1);
	}
	/**
	 * Get the Digest for the file using the specified algorithm using Nio access
	 * @param f
	 * @param algo
	 * @return the digest
	 * @throws IOException
	 */
	private static byte[] getHashNio(File f, String algo) throws IOException {
		if (!f.exists()) throw new FileNotFoundException(f.toString());
		InputStream close_me = null;
		try {
			long buf_size = f.length();
			if (buf_size < 512) buf_size = 512;
			if (buf_size > 65536) buf_size = 65536;
			FileInputStream in = new FileInputStream(f);
			close_me = in;
			FileChannel fileChannel = in.getChannel();
			byte[] buf = new byte[(int)buf_size];
			ByteBuffer bb = ByteBuffer.wrap(buf);
			//ByteBuffer bb = ByteBuffer.allocate((int) buf_size);
			MessageDigest digest = null;
			try {
				digest = MessageDigest.getInstance(algo);
			} catch (NoSuchAlgorithmException e) {
				new FileNotFoundException(algo+" Algorithm not supported by this JVM");
			}
			int size = 0;
			while ((size = fileChannel.read(bb)) >= 0) {
				//bb.rewind();
				//digest.update(bb);
				digest.update(buf,0,size);
				bb.clear();
			}
			fileChannel.close();
			fileChannel= null;
			in = null;
			close_me = null;
			bb = null;
			//byte []buf = digest.digest();
			buf = digest.digest();
			digest = null;
			return buf;
		} catch (IOException e) {
			if (close_me != null) try { close_me.close(); } catch (Exception e2) {}
			throw e;
		}
	}
	/**
	 * Get the Digest for the file using the specified algorithm using Standard access
	 * @param f
	 * @param algo
	 * @return the digest
	 * @throws IOException
	 */
	private static byte[] getHash(File f, String algo) throws IOException {
		if (!f.exists()) throw new FileNotFoundException(f.toString());
		InputStream close_me = null;
		try {
			long buf_size = f.length();
			if (buf_size < 512) buf_size = 512;
			if (buf_size > 65536) buf_size = 65536;
			byte[] buf = new byte[(int) buf_size];
			FileInputStream in = new FileInputStream(f);
			close_me = in;
			MessageDigest digest = null;
			try {
				digest = MessageDigest.getInstance(algo);
			} catch (NoSuchAlgorithmException e) {
				new FileNotFoundException(algo+" Algorithm not supported by this JVM");
			}
			int read = 0;
			while ((read = in.read(buf)) >= 0) {
				digest.update(buf, 0, read);
			}
			in.close();
			in = null;
			close_me = null;
			buf = null;
			buf = digest.digest();
			digest = null;
			return buf;
		} catch (IOException e) {
			if (close_me != null) try { close_me.close(); } catch (Exception e2) {}
			throw e;
		}
	}
	
	/**
	 * Test function
	 * @param argv with 2 arguments as filename to hash and full path to the Native Library
	 */
	public static void main(String argv[]) {
		if (argv.length < 1) {
			System.err.println("Not enough argument: <full path to the filename to hash> ");
			return;
		}
		FtpInternalLoggerFactory.setDefaultFactory(new FtpJdkLoggerFactory(Level.WARNING));
		MD5.initNativeLibrary("D:/NEWJARS/goldengate/lib/arch/win32_x86/MD5.dll");
		File file = new File(argv[0]);
		System.out.println("File: "+file.getAbsolutePath());
		byte[] bmd5;
		// one time for nothing
		FilesystemBasedFtpConfiguration.useFastMd5 = false;
		long start = System.currentTimeMillis();
		try {
			bmd5 = getHashMd5Nio(file);
		} catch (IOException e1) {
			System.err.println("Cannot compute "+ALGO_MD5+" for "+argv[1]);
			return;
		}
		long end = System.currentTimeMillis();
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
		}
		System.out.println("Start testing");
		
		// JVM Nio MD5
		start = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			try {
				bmd5 = getHashMd5Nio(file);
			} catch (IOException e1) {
				System.err.println("Cannot compute "+ALGO_MD5+" for "+argv[1]);
				return;
			}
		}
		end = System.currentTimeMillis();
		System.out.println("Algo Nio JVM "+ALGO_MD5+" is "+FtpDir.getHex(bmd5)+" in "+(end-start)+" ms");
		// Fast Nio MD5
		FilesystemBasedFtpConfiguration.useFastMd5 = true;
		start = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			try {
				bmd5 = getHashMd5Nio(file);
			} catch (IOException e1) {
				System.err.println("Cannot compute "+ALGO_MD5+" for "+argv[1]);
				return;
			}
		}
		end = System.currentTimeMillis();
		System.out.println("Algo Nio Fast "+ALGO_MD5+" is "+FtpDir.getHex(bmd5)+" in "+(end-start)+" ms");

		// JVM MD5
		FilesystemBasedFtpConfiguration.useFastMd5 = false;
		start = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			try {
				bmd5 = getHashMd5(file);
			} catch (IOException e1) {
				System.err.println("Cannot compute "+ALGO_MD5+" for "+argv[1]);
				return;
			}
		}
		end = System.currentTimeMillis();
		System.out.println("Algo JVM "+ALGO_MD5+" is "+FtpDir.getHex(bmd5)+" in "+(end-start)+" ms");
		// Fast MD5
		FilesystemBasedFtpConfiguration.useFastMd5 = true;
		start = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			try {
				bmd5 = getHashMd5(file);
			} catch (IOException e1) {
				System.err.println("Cannot compute "+ALGO_MD5+" for "+argv[1]);
				return;
			}
		}
		end = System.currentTimeMillis();
		System.out.println("Algo Fast "+ALGO_MD5+" is "+FtpDir.getHex(bmd5)+" in "+(end-start)+" ms");

		// JVM Nio SHA1
		start = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			try {
				bmd5 = getHashSha1Nio(file);
			} catch (IOException e1) {
				System.err.println("Cannot compute "+ALGO_SHA1+" for "+argv[1]);
				return;
			}
		}
		end = System.currentTimeMillis();
		System.out.println("Algo Nio JVM "+ALGO_SHA1+" is "+FtpDir.getHex(bmd5)+" in "+(end-start)+" ms");
		// JVM SHA1
		start = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			try {
				bmd5 = getHashSha1(file);
			} catch (IOException e1) {
				System.err.println("Cannot compute "+ALGO_SHA1+" for "+argv[1]);
				return;
			}
		}
		end = System.currentTimeMillis();
		System.out.println("Algo JVM "+ALGO_SHA1+" is "+FtpDir.getHex(bmd5)+" in "+(end-start)+" ms");
	}
}
