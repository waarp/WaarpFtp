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
package goldengate.common.file.filesystembased;

import goldengate.common.command.exception.CommandAbstractException;
import goldengate.common.command.exception.Reply502Exception;
import goldengate.common.command.exception.Reply530Exception;
import goldengate.common.exception.FileEndOfTransferException;
import goldengate.common.exception.FileTransferException;
import goldengate.common.exception.NoRestartException;
import goldengate.common.file.DataBlock;
import goldengate.common.file.DirInterface;
import goldengate.common.file.Restart;
import goldengate.common.file.SessionInterface;
import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * File implementation for Filesystem Based
 *
 * @author Frederic Bregier
 *
 */
public abstract class FilesystemBasedFileImpl implements
        goldengate.common.file.FileInterface {
    /**
     * Internal Logger
     */
    private static final GgInternalLogger logger = GgInternalLoggerFactory
            .getLogger(FilesystemBasedFileImpl.class);

    /**
     * SessionInterface
     */
    protected final SessionInterface session;

    /**
     * DirInterface associated with this file at creation. It is not necessary
     * the directory that owns this file.
     */
    private final FilesystemBasedDirImpl dir;

    /**
     * {@link FilesystemBasedAuthImpl}
     */
    private final FilesystemBasedAuthImpl auth;

    /**
     * Current file if any
     */
    protected String currentFile = null;

    /**
     * Is this Document ready to be accessed
     */
    protected boolean isReady = false;

    /**
     * Is this file in append mode
     */
    protected boolean isAppend = false;

    /**
     * @param session
     * @param dir
     *            It is not necessary the directory that owns this file.
     * @param path
     * @param append
     * @throws CommandAbstractException
     */
    public FilesystemBasedFileImpl(SessionInterface session,
            FilesystemBasedDirImpl dir, String path, boolean append)
            throws CommandAbstractException {
        this.session = session;
        auth = (FilesystemBasedAuthImpl) session.getAuth();
        this.dir = dir;
        currentFile = path;
        isReady = true;
        isAppend = append;
        File file = getFileFromPath(path);
        if (append) {
            try {
                setPosition(file.length());
            } catch (IOException e) {
                logger.error("Error during position:", e);
            }
        } else {
            try {
                setPosition(0);
            } catch (IOException e) {
            }
        }
    }

    public void clear() throws CommandAbstractException {
        closeFile();
        isReady = false;
        currentFile = null;
        isAppend = false;
    }

    public void checkIdentify() throws Reply530Exception {
        if (!getSession().getAuth().isIdentified()) {
            throw new Reply530Exception("User not authentified");
        }
    }

    public SessionInterface getSession() {
        return session;
    }

    public DirInterface getDir() {
        return dir;
    }

    /**
     * Get the File from this path, checking first its validity
     *
     * @param path
     * @return the FileInterface
     * @throws CommandAbstractException
     */
    protected File getFileFromPath(String path) throws CommandAbstractException {
        String newdir = getDir().validatePath(path);
        String truedir = auth.getAbsolutePath(newdir);
        return new File(truedir);
    }

    /**
     * Get the relative path (without mount point)
     *
     * @param file
     * @return the relative path
     */
    protected String getRelativePath(File file) {
        return auth.getRelativePath(FilesystemBasedDirImpl.normalizePath(file
                .getAbsolutePath()));
    }

    public boolean isDirectory() throws CommandAbstractException {
        checkIdentify();
        File dir1 = getFileFromPath(currentFile);
        return dir1.isDirectory();
    }

    public boolean isFile() throws CommandAbstractException {
        checkIdentify();
        return getFileFromPath(currentFile).isFile();
    }

    public String getFile() throws CommandAbstractException {
        checkIdentify();
        return currentFile;
    }

    public boolean closeFile() throws CommandAbstractException {
        if (bfileChannelIn != null) {
            try {
                bfileChannelIn.close();
            } catch (IOException e) {
            }
            bfileChannelIn = null;
        }
        if (bfileChannelOut != null) {
            try {
                bfileChannelOut.force(true);
            } catch (IOException e1) {
            }
            try {
                bfileChannelOut.close();
            } catch (IOException e) {
            }
            bfileChannelOut = null;
        }
        position = 0;
        isReady = false;
        // Do not clear the filename itself
        return true;
    }

    public boolean abortFile() throws CommandAbstractException {
        if (isInWriting() &&
                ((FilesystemBasedFileParameterImpl) getSession()
                        .getFileParameter()).deleteOnAbort) {
            delete();
        }
        closeFile();
        return true;
    }

    public long length() throws CommandAbstractException {
        checkIdentify();
        if (!isReady) {
            return -1;
        }
        if (!exists()) {
            return -1;
        }
        return getFileFromPath(currentFile).length();
    }

    public boolean isInReading() throws CommandAbstractException {
        if (!isReady) {
            return false;
        }
        return bfileChannelIn != null;
    }

    public boolean isInWriting() throws CommandAbstractException {
        if (!isReady) {
            return false;
        }
        return bfileChannelOut != null;
    }

    public boolean canRead() throws CommandAbstractException {
        checkIdentify();
        if (!isReady) {
            return false;
        }
        return getFileFromPath(currentFile).canRead();
    }

    public boolean canWrite() throws CommandAbstractException {
        checkIdentify();
        if (!isReady) {
            return false;
        }
        File file = getFileFromPath(currentFile);
        if (file.exists()) {
            return file.canWrite();
        }
        return file.getParentFile().canWrite();
    }

    public boolean exists() throws CommandAbstractException {
        checkIdentify();
        if (!isReady) {
            return false;
        }
        return getFileFromPath(currentFile).exists();
    }

    public boolean delete() throws CommandAbstractException {
        checkIdentify();
        if (!isReady) {
            return false;
        }
        if (!exists()) {
            return true;
        }
        closeFile();
        return getFileFromPath(currentFile).delete();
    }

    public boolean renameTo(String path) throws CommandAbstractException {
        checkIdentify();
        if (!isReady) {
            return false;
        }
        File file = getFileFromPath(currentFile);
        if (file.canRead()) {
            File newFile = getFileFromPath(path);
            if (newFile.getParentFile().canWrite()) {
                if (!file.renameTo(newFile)) {
                    FileOutputStream fileOutputStream;
                    try {
                        fileOutputStream = new FileOutputStream(newFile);
                    } catch (FileNotFoundException e) {
                        logger
                                .warn("Cannot find file: " + newFile.getName(),
                                        e);
                        return false;
                    }
                    FileChannel fileChannelOut = fileOutputStream.getChannel();
                    if (get(fileChannelOut)) {
                        delete();
                    } else {
                        logger.warn("Cannot write file: {}", newFile);
                        return false;
                    }
                }
                currentFile = getRelativePath(newFile);
                isReady = true;
                return true;
            }
        }
        return false;
    }

    public DataBlock getMarker() throws CommandAbstractException {
        throw new Reply502Exception("No marker implemented");
    }

    public boolean restartMarker(Restart restart)
            throws CommandAbstractException {
        try {
            long newposition = ((FilesystemBasedRestartImpl) restart)
                    .getPosition();
            try {
                setPosition(newposition);
            } catch (IOException e) {
                throw new Reply502Exception("Cannot set the marker position");
            }
            return true;
        } catch (NoRestartException e) {
        }
        return false;
    }

    public boolean retrieve() throws CommandAbstractException {
        checkIdentify();
        if (isReady) {
            restartMarker(getSession().getRestart());
            return canRead();
        }
        return false;
    }

    public boolean store() throws CommandAbstractException {
        checkIdentify();
        if (isReady) {
            restartMarker(getSession().getRestart());
            return canWrite();
        }
        return false;
    }

    public DataBlock readDataBlock() throws FileTransferException,
            FileEndOfTransferException {
        if (isReady) {
            DataBlock dataBlock = new DataBlock();
            ChannelBuffer buffer = null;
            buffer = getBlock(getSession().getBlockSize());
            if (buffer != null) {
                dataBlock.setBlock(buffer);
                if (dataBlock.getByteCount() < getSession().getBlockSize()) {
                    dataBlock.setEOF(true);
                }
                return dataBlock;
            }
        }
        throw new FileTransferException("No file is ready");
    }

    public void writeDataBlock(DataBlock dataBlock)
            throws FileTransferException {
        if (isReady) {
            if (dataBlock.isEOF()) {
                writeBlockEnd(dataBlock.getBlock());
                return;
            }
            writeBlock(dataBlock.getBlock());
            return;
        }
        throw new FileTransferException("No file is ready");
    }

    /**
     * Valid Position of this file
     */
    private long position = 0;

    /**
     * FileChannel Out
     */
    private FileChannel bfileChannelOut = null;

    /**
     * FileChannel In
     */
    private FileChannel bfileChannelIn = null;

    /**
     * Associated ByteBuffer
     */
    private ByteBuffer bbyteBuffer = null;

    /**
     * Return the current position in the FileInterface. In write mode, it is
     * the current file length.
     *
     * @return the position
     */
    public long getPosition() {
        return position;
    }

    /**
     * Change the position in the file.
     *
     * @param position
     *            the position to set
     * @throws IOException
     */
    public void setPosition(long position) throws IOException {
        if (bfileChannelIn != null) {
            bfileChannelIn = bfileChannelIn.position(position);
        }
        if (bfileChannelOut != null) {
            bfileChannelOut = bfileChannelOut.position(position);
        }
        this.position = position;
    }
    /**
     * Try to flush written data if possible
     */
    public void flush() {
        if (bfileChannelOut != null && isReady) {
            try {
                bfileChannelOut.force(false);
            } catch (IOException e1) {
            }
        }
    }
    /**
     * Write the current FileInterface with the given ChannelBuffer. The file is
     * not limited to 2^32 bytes since this write operation is in add mode.
     *
     * In case of error, the current already written blocks are maintained and
     * the position is not changed.
     *
     * @param buffer
     *            added to the file
     * @throws FileTransferException
     */
    private void writeBlock(ChannelBuffer buffer) throws FileTransferException {
        if (!isReady) {
            throw new FileTransferException("No file is ready");
        }
        // An empty buffer is allowed
        if (buffer == null) {
            return;// could do FileEndOfTransfer ?
        }
        if (bfileChannelOut == null) {
            bfileChannelOut = getFileChannel(true);
        }
        if (bfileChannelOut == null) {
            throw new FileTransferException("Internal error, file is not ready");
        }
        long bufferSize = buffer.readableBytes();
        ByteBuffer byteBuffer = buffer.toByteBuffer();
        long size = 0;
        while (size < bufferSize) {
            try {
                size += bfileChannelOut.write(byteBuffer);
            } catch (IOException e) {
                logger.error("Error during write:", e);
                try {
                    bfileChannelOut.close();
                } catch (IOException e1) {
                }
                bfileChannelOut = null;
                byteBuffer = null;
                // NO this.realFile.delete(); NO DELETE SINCE BY BLOCK IT CAN BE
                // REDO
                throw new FileTransferException("Internal error, file is not ready");
            }
        }
        boolean result = size == bufferSize;
        byteBuffer = null;
        if (!result) {
            try {
                bfileChannelOut.close();
            } catch (IOException e1) {
            }
            bfileChannelOut = null;
            // NO this.realFile.delete(); NO DELETE SINCE BY BLOCK IT CAN BE
            // REDO
            throw new FileTransferException("Internal error, file is not ready");
        }
        position += size;
    }

    /**
     * End the Write of the current FileInterface with the given ChannelBuffer.
     * The file is not limited to 2^32 bytes since this write operation is in
     * add mode.
     *
     * @param buffer
     *            added to the file
     * @throws FileTransferException
     */
    private void writeBlockEnd(ChannelBuffer buffer)
            throws FileTransferException {
        writeBlock(buffer);
        try {
            closeFile();
        } catch (CommandAbstractException e) {
        }
    }

    /**
     * Get the current block ChannelBuffer of the current FileInterface. There
     * is therefore no limitation of the file size to 2^32 bytes.
     *
     * The returned block is limited to sizeblock. If the returned block is less
     * than sizeblock length, it is the last block to read.
     *
     * @param sizeblock
     *            is the limit size for the block array
     * @return the resulting block ChannelBuffer (even empty)
     * @throws FileTransferException
     * @throws FileEndOfTransferException
     */
    private ChannelBuffer getBlock(int sizeblock) throws FileTransferException,
            FileEndOfTransferException {
        if (!isReady) {
            throw new FileTransferException("No file is ready");
        }
        if (bfileChannelIn == null) {
            bfileChannelIn = getFileChannel(false);
            if (bfileChannelIn != null) {
                if (bbyteBuffer != null) {
                    if (bbyteBuffer.capacity() != sizeblock) {
                        bbyteBuffer = null;
                        bbyteBuffer = ByteBuffer.allocate(sizeblock);
                    }
                } else {
                    bbyteBuffer = ByteBuffer.allocate(sizeblock);
                }
            }
        }
        if (bfileChannelIn == null) {
            throw new FileTransferException("Internal error, file is not ready");
        }
        int sizeout = 0;
        try {
            sizeout = bfileChannelIn.read(bbyteBuffer);
        } catch (IOException e) {
            logger.error("Error during get:", e);
            try {
                bfileChannelIn.close();
            } catch (IOException e1) {
            }
            bfileChannelIn = null;
            bbyteBuffer.clear();
            throw new FileTransferException("Internal error, file is not ready");
        }
        if (sizeout < sizeblock) {// last block
            try {
                bfileChannelIn.close();
            } catch (IOException e) {
            }
            bfileChannelIn = null;
            isReady = false;
        }
        if (sizeout <= 0) {
            bbyteBuffer.clear();
            throw new FileEndOfTransferException("End of file");
        }
        bbyteBuffer.flip();
        position += sizeout;
        ChannelBuffer buffer = ChannelBuffers.copiedBuffer(bbyteBuffer);
        bbyteBuffer.clear();
        return buffer;
    }

    /**
     * Write the FileInterface to the fileChannelOut, thus bypassing the
     * limitation of the file size to 2^32 bytes.
     *
     * This call closes the fileChannelOut with fileChannelOut.close() if the
     * operation is in success.
     *
     * @param fileChannelOut
     * @return True if OK, False in error.
     */
    protected boolean get(FileChannel fileChannelOut) {
        if (!isReady) {
            return false;
        }
        FileChannel fileChannelIn = getFileChannel(false);
        if (fileChannelIn == null) {
            return false;
        }
        long size = 0;
        long transfert = 0;
        try {
            size = fileChannelIn.size();
            transfert = fileChannelOut.transferFrom(fileChannelIn, 0, size);
            fileChannelOut.force(true);
            fileChannelIn.close();
            fileChannelIn = null;
            fileChannelOut.close();
        } catch (IOException e) {
            logger.error("Error during get:", e);
            if (fileChannelIn != null) {
                try {
                    fileChannelIn.close();
                } catch (IOException e1) {
                }
            }
            return false;
        }
        if (transfert == size) {
            position += size;
        }
        return transfert == size;
    }

    /**
     * Returns the FileChannel in Out mode (if isOut is True) or in In mode (if
     * isOut is False) associated with the current file.
     *
     * @param isOut
     * @return the FileChannel (OUT or IN)
     */
    protected FileChannel getFileChannel(boolean isOut) {
        if (!isReady) {
            return null;
        }
        File trueFile;
        try {
            trueFile = getFileFromPath(currentFile);
        } catch (CommandAbstractException e1) {
            return null;
        }
        FileChannel fileChannel;
        try {
            if (isOut) {
                if (position == 0) {
                    FileOutputStream fileOutputStream = new FileOutputStream(
                            trueFile);
                    fileChannel = fileOutputStream.getChannel();
                } else {
                    RandomAccessFile randomAccessFile = new RandomAccessFile(
                            trueFile, "rw");
                    fileChannel = randomAccessFile.getChannel();
                    fileChannel = fileChannel.position(position);
                }
            } else {
                if (!trueFile.exists()) {
                    return null;
                }
                FileInputStream fileInputStream = new FileInputStream(trueFile);
                fileChannel = fileInputStream.getChannel();
                if (position != 0) {
                    fileChannel = fileChannel.position(position);
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("FileInterface not found in getFileChannel:", e);
            return null;
        } catch (IOException e) {
            logger.error("Change position in getFileChannel:", e);
            return null;
        }
        return fileChannel;
    }
}
