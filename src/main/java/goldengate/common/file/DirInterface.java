/**
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3.0 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package goldengate.common.file;

import goldengate.common.command.exception.CommandAbstractException;
import goldengate.common.command.exception.Reply530Exception;

import java.util.List;

/**
 * Interface for Directory support
 *
 * @author Frederic Bregier
 *
 */
public interface DirInterface {
    /**
     * FileInterface separator for external
     */
    public static final String SEPARATOR = "/";

    /**
     * FileInterface separator for external
     */
    public static final char SEPARATORCHAR = '/';

    /**
     *
     * @return the current value of Options for MLSx
     */
    public OptsMLSxInterface getOptsMLSx();

    /**
     * Set empty this FtpDir, mark it unReady.
     */
    public void clear();

    /**
     * Init DirInterface after authentication is done
     *
     */
    public void initAfterIdentification();

    /**
     * Check if the authentication is correct
     *
     * @throws Reply530Exception
     */
    public void checkIdentify() throws Reply530Exception;

    /**
     *
     * @return the FtpSession
     */
    public SessionInterface getSession();

    // **************** Directory part **************************
    /**
     * Construct and Check if the given path is valid from business point of
     * view (see {@link AuthInterface})
     *
     * @param path
     * @return the construct and validated path (could be different than the one
     *         given as argument, example: '..' are removed)
     * @throws CommandAbstractException
     */
    public abstract String validatePath(String path)
            throws CommandAbstractException;

    /**
     * @return the current PWD
     * @exception CommandAbstractException
     */
    public abstract String getPwd() throws CommandAbstractException;

    /**
     * Change directory with the one given as argument
     *
     * @param path
     * @return True if the change is valid
     * @throws CommandAbstractException
     */
    public abstract boolean changeDirectory(String path)
            throws CommandAbstractException;

    /**
     * Change for parent directory
     *
     * @return True if the change is valid
     * @throws CommandAbstractException
     */
    public abstract boolean changeParentDirectory()
            throws CommandAbstractException;

    /**
     * Create the directory associated with the String as path
     *
     * @param directory
     * @return the full path of the new created directory
     * @exception CommandAbstractException
     */
    public abstract String mkdir(String directory)
            throws CommandAbstractException;

    /**
     * Delete the directory associated with the String as path
     *
     * @param directory
     * @return the full path of the new deleted directory
     * @exception CommandAbstractException
     */
    public abstract String rmdir(String directory)
            throws CommandAbstractException;

    /**
     * Is the given path a directory and exists
     *
     * @param path
     * @return True if it is a directory and it exists
     * @throws CommandAbstractException
     */
    public abstract boolean isDirectory(String path)
            throws CommandAbstractException;

    /**
     * Is the given path a file and exists
     *
     * @param path
     * @return True if it is a file and it exists
     * @throws CommandAbstractException
     */
    public abstract boolean isFile(String path) throws CommandAbstractException;

    /**
     * Return the Modification time for the path
     *
     * @param path
     * @return the Modification time as a String YYYYMMDDHHMMSS.sss
     * @throws CommandAbstractException
     */
    public abstract String getModificationTime(String path)
            throws CommandAbstractException;

    /**
     * List all files from the given path (could be a file or a directory)
     *
     * @param path
     * @return the list of paths
     * @throws CommandAbstractException
     */
    public abstract List<String> list(String path)
            throws CommandAbstractException;

    /**
     * List all files with other informations from the given path (could be a
     * file or a directory)
     *
     * @param path
     * @param lsFormat
     *            True if ls Format, else MLSx format
     * @return the list of paths and other informations
     * @throws CommandAbstractException
     */
    public abstract List<String> listFull(String path, boolean lsFormat)
            throws CommandAbstractException;

    /**
     * Give for 1 file all informations from the given path (could be a file or
     * a directory)
     *
     * @param path
     * @param lsFormat
     *            True if ls Format, else MLSx format
     * @return the path and other informations
     * @throws CommandAbstractException
     */
    public abstract String fileFull(String path, boolean lsFormat)
            throws CommandAbstractException;

    /**
     *
     * @return the free space of the current Directory
     * @throws CommandAbstractException
     */
    public abstract long getFreeSpace() throws CommandAbstractException;

    // **************** Unique FileInterface part **************************
    /**
     * Create a new FtpFile
     *
     * @param path
     * @param append
     * @return the new Ftp FileInterface
     * @throws CommandAbstractException
     */
    public abstract FileInterface newFile(String path, boolean append)
            throws CommandAbstractException;

    /**
     * Set a path as the current FileInterface
     *
     * @param path
     * @param append
     *            True if this file is supposed to be in append mode (APPE),
     *            False in any other cases
     * @return the Ftp FileInterface if it is correctly initiate
     * @throws CommandAbstractException
     */
    public abstract FileInterface setFile(String path, boolean append)
            throws CommandAbstractException;

    /**
     * Set a new unique path as the current FileInterface from the current
     * Directory (STOU)
     *
     * @return the Ftp FileInterface if it is correctly initiate
     * @throws CommandAbstractException
     */
    public abstract FileInterface setUniqueFile()
            throws CommandAbstractException;

    /**
     * @return True if the current FileInterface is ready for reading
     * @throws CommandAbstractException
     */
    public abstract boolean canRead() throws CommandAbstractException;

    /**
     *
     * @return True if the current FileInterface is ready for writing
     * @throws CommandAbstractException
     */
    public abstract boolean canWrite() throws CommandAbstractException;

    /**
     *
     * @return True if the current FileInterface exists
     * @throws CommandAbstractException
     */
    public abstract boolean exists() throws CommandAbstractException;

    /**
     * Get the CRC of the given FileInterface
     *
     * @param path
     * @return the CRC
     * @throws CommandAbstractException
     */
    public abstract long getCRC(String path) throws CommandAbstractException;

    /**
     * Get the MD5 of the given FileInterface
     *
     * @param path
     * @return the MD5
     * @throws CommandAbstractException
     */
    public abstract byte[] getMD5(String path) throws CommandAbstractException;

    /**
     * Get the SHA-1 of the given FileInterface
     *
     * @param path
     * @return the SHA-1
     * @throws CommandAbstractException
     */
    public abstract byte[] getSHA1(String path) throws CommandAbstractException;
}
