/**
 * Copyright 2009, Frederic Bregier, and individual contributors
 * by the @author tags. See the COPYRIGHT.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package goldengate.common.digest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class implementing digest like MD5, SHA1. MD5 is based on the Fast MD5
 * implementation, without C library support, but can be revert to JVM native
 * digest.
 *
 * @author Frederic Bregier
 *
 */
public class FilesystemBasedDigest {
    /**
     * Should a file MD5 be computed using FastMD5
     */
    public static boolean useFastMd5 = true;

    /**
     * If using Fast MD5, should we used the binary JNI library, empty meaning
     * no. FastMD5 is up to 50% fastest than JVM.
     */
    public static String fastMd5Path = null;

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
        byte[] bdig1 = getFromHex(dig1);
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
     * get the byte array of the MD5 for the given FileInterface using Nio
     * access
     *
     * @param f
     * @return the byte array representing the MD5
     * @throws IOException
     */
    public static byte[] getHashMd5Nio(File f) throws IOException {
        if (useFastMd5) {
            return MD5.getHashNio(f);
        }
        return getHashNio(f, ALGO_MD5);
    }

    /**
     * get the byte array of the MD5 for the given FileInterface using standard
     * access
     *
     * @param f
     * @return the byte array representing the MD5
     * @throws IOException
     */
    public static byte[] getHashMd5(File f) throws IOException {
        if (useFastMd5) {
            return MD5.getHash(f);
        }
        return getHash(f, ALGO_MD5);
    }

    /**
     * get the byte array of the SHA-1 for the given FileInterface using Nio
     * access
     *
     * @param f
     * @return the byte array representing the SHA-1
     * @throws IOException
     */
    public static byte[] getHashSha1Nio(File f) throws IOException {
        return getHashNio(f, ALGO_SHA1);
    }

    /**
     * get the byte array of the SHA-1 for the given FileInterface using
     * standard access
     *
     * @param f
     * @return the byte array representing the SHA-1
     * @throws IOException
     */
    public static byte[] getHashSha1(File f) throws IOException {
        return getHash(f, ALGO_SHA1);
    }

    /**
     * Get the Digest for the file using the specified algorithm using Nio
     * access
     *
     * @param f
     * @param algo
     * @return the digest
     * @throws IOException
     */
    private static byte[] getHashNio(File f, String algo) throws IOException {
        if (!f.exists()) {
            throw new FileNotFoundException(f.toString());
        }
        InputStream close_me = null;
        try {
            long buf_size = f.length();
            if (buf_size < 512) {
                buf_size = 512;
            }
            if (buf_size > 65536) {
                buf_size = 65536;
            }
            FileInputStream in = new FileInputStream(f);
            close_me = in;
            FileChannel fileChannel = in.getChannel();
            byte[] buf = new byte[(int) buf_size];
            ByteBuffer bb = ByteBuffer.wrap(buf);
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance(algo);
            } catch (NoSuchAlgorithmException e) {
                throw new FileNotFoundException(algo +
                        " Algorithm not supported by this JVM");
            }
            int size = 0;
            while ((size = fileChannel.read(bb)) >= 0) {
                digest.update(buf, 0, size);
                bb.clear();
            }
            fileChannel.close();
            fileChannel = null;
            in = null;
            close_me = null;
            bb = null;
            buf = digest.digest();
            digest = null;
            return buf;
        } catch (IOException e) {
            if (close_me != null) {
                try {
                    close_me.close();
                } catch (Exception e2) {
                }
            }
            throw e;
        }
    }

    /**
     * Get the Digest for the file using the specified algorithm using Standard
     * access
     *
     * @param f
     * @param algo
     * @return the digest
     * @throws IOException
     */
    private static byte[] getHash(File f, String algo) throws IOException {
        if (!f.exists()) {
            throw new FileNotFoundException(f.toString());
        }
        InputStream close_me = null;
        try {
            long buf_size = f.length();
            if (buf_size < 512) {
                buf_size = 512;
            }
            if (buf_size > 65536) {
                buf_size = 65536;
            }
            byte[] buf = new byte[(int) buf_size];
            FileInputStream in = new FileInputStream(f);
            close_me = in;
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance(algo);
            } catch (NoSuchAlgorithmException e) {
                throw new FileNotFoundException(algo +
                        " Algorithm not supported by this JVM");
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
            if (close_me != null) {
                try {
                    close_me.close();
                } catch (Exception e2) {
                }
            }
            throw e;
        }
    }

    /**
     * Internal representation of Hexadecimal Code
     */
    private static final char[] HEX_CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c',
            'd', 'e', 'f', };

    /**
     * Get the hexadecimal representation as a String of the array of bytes
     *
     * @param hash
     * @return the hexadecimal representation as a String of the array of bytes
     */
    public static String getHex(byte[] hash) {
        char buf[] = new char[hash.length * 2];
        for (int i = 0, x = 0; i < hash.length; i ++) {
            buf[x ++] = HEX_CHARS[hash[i] >>> 4 & 0xf];
            buf[x ++] = HEX_CHARS[hash[i] & 0xf];
        }
        return new String(buf);
    }

    /**
     * Get the array of bytes representation of the hexadecimal String
     *
     * @param hex
     * @return the array of bytes representation of the hexadecimal String
     */
    public static byte[] getFromHex(String hex) {
        byte from[] = hex.getBytes();
        byte hash[] = new byte[from.length / 2];
        for (int i = 0, x = 0; i < hash.length; i ++) {
            byte code1 = from[x ++];
            byte code2 = from[x ++];
            if (code1 >= HEX_CHARS[10]) {
                code1 -= HEX_CHARS[10] - 10;
            } else {
                code1 -= HEX_CHARS[0];
            }
            if (code2 >= HEX_CHARS[10]) {
                code2 -= HEX_CHARS[10] - 10;
            } else {
                code2 -= HEX_CHARS[0];
            }
            hash[i] = (byte) ((code1 << 4) + code2);
        }
        return hash;
    }

    /**
     * Test function
     *
     * @param argv
     *            with 2 arguments as filename to hash and full path to the
     *            Native Library
     */
    public static void main(String argv[]) {
        if (argv.length < 1) {
            System.err
                    .println("Not enough argument: <full path to the filename to hash> ");
            return;
        }
        MD5
                .initNativeLibrary("D:/NEWJARS/goldengate/lib/arch/win32_x86/MD5.dll");
        File file = new File(argv[0]);
        System.out.println("FileInterface: " + file.getAbsolutePath());
        byte[] bmd5;
        // one time for nothing
        useFastMd5 = false;
        long start = System.currentTimeMillis();
        try {
            bmd5 = getHashMd5Nio(file);
        } catch (IOException e1) {
            System.err
                    .println("Cannot compute " + ALGO_MD5 + " for " + argv[1]);
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
        for (int i = 0; i < 100; i ++) {
            try {
                bmd5 = getHashMd5Nio(file);
            } catch (IOException e1) {
                System.err.println("Cannot compute " + ALGO_MD5 + " for " +
                        argv[1]);
                return;
            }
        }
        end = System.currentTimeMillis();
        System.out.println("Algo Nio JVM " + ALGO_MD5 + " is " + getHex(bmd5) +
                " in " + (end - start) + " ms");
        // Fast Nio MD5
        useFastMd5 = true;
        start = System.currentTimeMillis();
        for (int i = 0; i < 100; i ++) {
            try {
                bmd5 = getHashMd5Nio(file);
            } catch (IOException e1) {
                System.err.println("Cannot compute " + ALGO_MD5 + " for " +
                        argv[1]);
                return;
            }
        }
        end = System.currentTimeMillis();
        System.out.println("Algo Nio Fast " + ALGO_MD5 + " is " + getHex(bmd5) +
                " in " + (end - start) + " ms");

        // JVM MD5
        useFastMd5 = false;
        start = System.currentTimeMillis();
        for (int i = 0; i < 100; i ++) {
            try {
                bmd5 = getHashMd5(file);
            } catch (IOException e1) {
                System.err.println("Cannot compute " + ALGO_MD5 + " for " +
                        argv[1]);
                return;
            }
        }
        end = System.currentTimeMillis();
        System.out.println("Algo JVM " + ALGO_MD5 + " is " + getHex(bmd5) +
                " in " + (end - start) + " ms");
        // Fast MD5
        useFastMd5 = true;
        start = System.currentTimeMillis();
        for (int i = 0; i < 100; i ++) {
            try {
                bmd5 = getHashMd5(file);
            } catch (IOException e1) {
                System.err.println("Cannot compute " + ALGO_MD5 + " for " +
                        argv[1]);
                return;
            }
        }
        end = System.currentTimeMillis();
        System.out.println("Algo Fast " + ALGO_MD5 + " is " + getHex(bmd5) +
                " in " + (end - start) + " ms");

        // JVM Nio SHA1
        start = System.currentTimeMillis();
        for (int i = 0; i < 100; i ++) {
            try {
                bmd5 = getHashSha1Nio(file);
            } catch (IOException e1) {
                System.err.println("Cannot compute " + ALGO_SHA1 + " for " +
                        argv[1]);
                return;
            }
        }
        end = System.currentTimeMillis();
        System.out.println("Algo Nio JVM " + ALGO_SHA1 + " is " + getHex(bmd5) +
                " in " + (end - start) + " ms");
        // JVM SHA1
        start = System.currentTimeMillis();
        for (int i = 0; i < 100; i ++) {
            try {
                bmd5 = getHashSha1(file);
            } catch (IOException e1) {
                System.err.println("Cannot compute " + ALGO_SHA1 + " for " +
                        argv[1]);
                return;
            }
        }
        end = System.currentTimeMillis();
        System.out.println("Algo JVM " + ALGO_SHA1 + " is " + getHex(bmd5) +
                " in " + (end - start) + " ms");
    }

}
