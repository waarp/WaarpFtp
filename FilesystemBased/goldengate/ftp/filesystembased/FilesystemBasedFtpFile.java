/**
 * Frederic Bregier LGPL 1 févr. 09 
 * FilesystemBasedFtpFile.java goldengate.ftp.core.file.filesystem GoldenGateFtp
 * frederic
 */
package goldengate.ftp.filesystembased;

import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.command.exception.Reply502Exception;
import goldengate.ftp.core.data.handler.FtpDataBlock;
import goldengate.ftp.core.exception.FtpFileEndOfTransferException;
import goldengate.ftp.core.exception.FtpFileTransferException;
import goldengate.ftp.core.exception.FtpNoRestartException;
import goldengate.ftp.core.file.FtpDir;
import goldengate.ftp.core.file.FtpFile;
import goldengate.ftp.core.file.FtpRestart;
import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;
import goldengate.ftp.core.session.FtpSession;

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
 * Filesystem implementation of a File
 * @author frederic
 * goldengate.ftp.core.file.filesystem FilesystemBasedFtpFile
 * 
 */
public abstract class FilesystemBasedFtpFile extends FtpFile {
	/**
	 * Internal Logger
	 */
	private static final FtpInternalLogger logger =
        FtpInternalLoggerFactory.getLogger(FilesystemBasedFtpFile.class);

	/**
	 * @param session
	 * @param dir It is not necessary the directory that owns this file.
	 * @param path
	 * @param append
	 * @throws FtpCommandAbstractException 
	 */
	public FilesystemBasedFtpFile(FtpSession session, FtpDir dir, String path, boolean append) 
		throws FtpCommandAbstractException {
		super(session,dir,path,append);
		File file = this.getFileFromPath(path);
		if (append) {
			try {
				this.setPosition(file.length());
			} catch (IOException e) {
				logger.error("Error during position:",e);
			}
		} else {
			try {
				this.setPosition(0);
			} catch (IOException e) {
			}
		}
	}
	/**
	 * Get the File from this path, checking first its validity
	 * @param path
	 * @return the File
	 * @throws FtpCommandAbstractException 
	 */
	protected File getFileFromPath(String path) throws FtpCommandAbstractException {
		String newdir = this.getFtpDir().validatePath(path);
		String truedir = ((FilesystemBasedFtpAuth) this.getFtpSession().getFtpAuth()).getAbsolutePath(newdir);
		return new File(truedir);
	}
	/**
	 * Get the relative path (without mount point)
	 * @param file
	 * @return the relative path
	 */
	protected String getRelativePath(File file) {
		return ((FilesystemBasedFtpAuth) this.getFtpSession().getFtpAuth()).getRelativePath(FilesystemBasedFtpDir.normalizePath(file.getAbsolutePath()));
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#isDirectory()
	 */
	@Override
	public boolean isDirectory() throws FtpCommandAbstractException {
		this.checkIdentify();
		File dir = getFileFromPath(this.currentFile);
		logger.debug("ISDIR: {} {}",dir,dir.isDirectory());
		return dir.isDirectory();
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#isFile()
	 */
	@Override
	public boolean isFile() throws FtpCommandAbstractException {
		this.checkIdentify();
		return getFileFromPath(this.currentFile).isFile();
	}
	
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#getFile()
	 */
	@Override
	public String getFile() throws FtpCommandAbstractException {
		this.checkIdentify();
		return this.currentFile;
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#closeFile()
	 */
	@Override
	public boolean closeFile() throws FtpCommandAbstractException {
		if (this.bfileChannelIn != null) {
			try {
				this.bfileChannelIn.close();
			} catch (IOException e) {
			}
			this.bfileChannelIn = null;
		}
		if (this.bfileChannelOut != null) {
			try {
				this.bfileChannelOut.close();
			} catch (IOException e) {
			}
			this.bfileChannelOut = null;
		}
		position = 0;
		this.isReady = false;
		// Do not clear the filename itself
		return true;
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#abort()
	 */
	@Override
	public boolean abortFile() throws FtpCommandAbstractException {
		if (this.isInWriting() && this.getFtpSession().getConfiguration().deleteOnAbort) {
			this.delete();
		}
		this.closeFile();
		return true;
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#length()
	 */
	@Override
	public long length() throws FtpCommandAbstractException {
		this.checkIdentify();
		if (!this.isReady) {
			return (-1);
		}
		if (! this.exists()) {
			return (-1);
		}
		long length = getFileFromPath(this.currentFile).length();
		if (this.getFtpSession().getDataConn().isFileStreamBlockAsciiImage()) {
			long block = (long) Math.ceil(((double)length)/((double)this.getFtpSession().getConfiguration().BLOCKSIZE));
			length += ((block+3)*3);
		}
		return length;
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#isInReading()
	 */
	@Override
	public boolean isInReading() throws FtpCommandAbstractException {
		if (!this.isReady) {
			return false;
		}
		return (this.bfileChannelIn != null);
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#isInWriting()
	 */
	@Override
	public boolean isInWriting() throws FtpCommandAbstractException {
		if (!this.isReady) {
			return false;
		}
		return (this.bfileChannelOut != null);
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#canRead()
	 */
	@Override
	public boolean canRead() throws FtpCommandAbstractException {
		this.checkIdentify();
		if (!this.isReady) {
			return false;
		}
		return getFileFromPath(this.currentFile).canRead();
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#canWrite()
	 */
	@Override
	public boolean canWrite() throws FtpCommandAbstractException {
		this.checkIdentify();
		if (!this.isReady) {
			return false;
		}
		File file = getFileFromPath(this.currentFile);
		if (file.exists()) {
			return file.canWrite();
		}
		return file.getParentFile().canWrite();
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#exists()
	 */
	@Override
	public boolean exists() throws FtpCommandAbstractException {
		this.checkIdentify();
		if (!this.isReady) {
			return false;
		}
		return getFileFromPath(this.currentFile).exists();
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#delete()
	 */
	@Override
	public boolean delete() throws FtpCommandAbstractException {
		this.checkIdentify();
		if (!this.isReady) {
			return true;
		}
		if (!this.exists()) {
			return true;
		}
		this.closeFile();
		return getFileFromPath(this.currentFile).delete();
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#renameTo(java.lang.String)
	 */
	@Override
	public boolean renameTo(String path) throws FtpCommandAbstractException {
		this.checkIdentify();
		if (!this.isReady) {
			return true;
		}
		File file = this.getFileFromPath(this.currentFile);
		if (file.canRead()) {
			logger.debug("Rename file {} to {}",file,path);
			File newFile = this.getFileFromPath(path);
			if (newFile.getParentFile().canWrite()) {
				if (! file.renameTo(newFile)) {
					logger.debug("file cannot be just renamed, to be moved: {}",file);
					FileOutputStream fileOutputStream;
					try {
						fileOutputStream = new FileOutputStream(newFile);
					} catch (FileNotFoundException e) {
						logger.warn("Cannot find file: "+newFile.getName(),e);
						return false;
					}
					FileChannel fileChannelOut = fileOutputStream.getChannel();
					if (this.get(fileChannelOut)) {
						this.delete();
					} else {
						logger.warn("Cannot write file: {}",newFile);
						return false;
					}
				}
				this.currentFile = this.getRelativePath(newFile);
				this.isReady = true;
				return true;
			}
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#getMarker()
	 */
	@Override
	public FtpDataBlock getMarker() throws FtpCommandAbstractException {
		throw new Reply502Exception("No marker implemented");
	}
	
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#restartMarker(goldengate.ftp.core.file.FtpRestart)
	 */
	@Override
	protected boolean restartMarker(FtpRestart restart) throws FtpCommandAbstractException {
		try {
			long newposition = ((FilesystemBasedFtpRestart) restart).getPosition();
			try {
				this.setPosition(newposition);
			} catch (IOException e) {
				throw new Reply502Exception("Cannot set the marker position");
			}
			return true;
		} catch (FtpNoRestartException e) {
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#retrieve()
	 */
	@Override
	public boolean retrieve() throws FtpCommandAbstractException {
		this.checkIdentify();
		if (this.isReady) {
			this.restartMarker(this.getFtpSession().getFtpRestart());
			return this.canRead();
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#store()
	 */
	@Override
	public boolean store() throws FtpCommandAbstractException {
		this.checkIdentify();
		if (this.isReady) {
			this.restartMarker(this.getFtpSession().getFtpRestart());
			return this.canWrite();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#readFileFtpDataBlock()
	 */
	@Override
	public FtpDataBlock readFileFtpDataBlock() throws FtpFileTransferException, FtpFileEndOfTransferException {
		if (this.isReady) {
			FtpDataBlock dataBlock = new FtpDataBlock();
			ChannelBuffer buffer = null;
			buffer = this.getBlock(this.getFtpSession().getConfiguration().BLOCKSIZE);
			if (buffer != null) {
				dataBlock.setBlock(buffer);
				if (dataBlock.getByteCount() < this.getFtpSession().getConfiguration().BLOCKSIZE) {
					dataBlock.setEOF(true);
				}
				return dataBlock;
			}
		}
		throw new FtpFileTransferException("No file is ready");
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#readRecordFtpDataBlock()
	 */
	@Override
	public FtpDataBlock readRecordFtpDataBlock() throws FtpFileTransferException, FtpFileEndOfTransferException {
		return this.readFileFtpDataBlock();
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#writeFileFtpDataBlock(goldengate.ftp.core.data.handler.FtpDataBlock)
	 */
	@Override
	public void writeFileFtpDataBlock(FtpDataBlock dataBlock) throws FtpFileTransferException, FtpFileEndOfTransferException {
		if (this.isReady) {
			if (dataBlock.isEOF()) {
				this.writeBlockEnd(dataBlock.getBlock());
				return;
			}
			this.writeBlock(dataBlock.getBlock());
			return;
		}
		throw new FtpFileTransferException("No file is ready");
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#writeRecordFtpDataBlock(goldengate.ftp.core.data.handler.FtpDataBlock)
	 */
	@Override
	public void writeRecordFtpDataBlock(FtpDataBlock dataBlock) throws FtpFileTransferException, FtpFileEndOfTransferException {
		this.writeFileFtpDataBlock(dataBlock);
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
	 * Return the current position in the File. In write mode, it is the current
	 * file length.
	 * 
	 * @return the position
	 */
	public long getPosition() {
		return this.position;
	}
	/**
	 * Change the position in the file.
	 * 
	 * @param position the position to set
	 * @throws IOException 
	 */
	public void setPosition(long position) throws IOException {
		if (this.bfileChannelIn != null) {
			bfileChannelIn = bfileChannelIn.position(position);
		}
		if (this.bfileChannelOut != null) {
			bfileChannelOut = bfileChannelOut.position(position);
		}
		this.position = position;
	}

	/**
	 * Write the current File with the given ChannelBuffer. The file is not limited to
	 * 2^32 bytes since this write operation is in add mode.
	 * 
	 * In case of error, the current already written blocks are maintained and the
	 * position is not changed.
	 * 
	 * @param buffer
	 *            added to the file
	 * @throws FtpFileTransferException 
	 */
	private void writeBlock(ChannelBuffer buffer) throws FtpFileTransferException {
		if (!this.isReady) {
			logger.debug("File not ready");
			throw new FtpFileTransferException("No file is ready");
		}
		// An empty buffer is allowed
		if (buffer == null) {
			return;// could do FileEndOfTransfer ?
		}
		if (this.bfileChannelOut == null) {
			this.bfileChannelOut = this.getFileChannel(true);
		}
		if (this.bfileChannelOut == null) {
			logger.debug("File cannot open File Channel");
			throw new FtpFileTransferException("Internal error, file is not ready");
		}
		long bufferSize = buffer.readableBytes();
		ByteBuffer byteBuffer = buffer.toByteBuffer();
		long size = 0;
		try {
			size = this.bfileChannelOut.write(byteBuffer);
		} catch (IOException e) {
			logger.error("Error during write:",e);
			try {
				this.bfileChannelOut.close();
			} catch (IOException e1) {
			}
			this.bfileChannelOut = null;
			byteBuffer = null;
			// NO this.realFile.delete(); NO DELETE SINCE BY BLOCK IT CAN BE REDO
			logger.debug("File cannot write");
			throw new FtpFileTransferException("Internal error, file is not ready");
		}
		boolean result = (size == bufferSize);
		byteBuffer = null;
		if (!result) {
			try {
				this.bfileChannelOut.close();
			} catch (IOException e1) {
			}
			this.bfileChannelOut = null;
			// NO this.realFile.delete(); NO DELETE SINCE BY BLOCK IT CAN BE REDO
			logger.debug("File cannot fully write");
			throw new FtpFileTransferException("Internal error, file is not ready");
		}
		position += size;
	}
	/**
	 * End the Write of the current File with the given ChannelBuffer. The file is not
	 * limited to 2^32 bytes since this write operation is in add mode.
	 * 
	 * @param buffer
	 *            added to the file
	 * @throws FtpFileTransferException 
	 */
	private void writeBlockEnd(ChannelBuffer buffer) throws FtpFileTransferException {
		writeBlock(buffer);
		try {
			this.closeFile();
		} catch (FtpCommandAbstractException e) {
		}
	}
	/**
	 * Get the current block ChannelBuffer of the current File. There is therefore no
	 * limitation of the file size to 2^32 bytes.
	 * 
	 * The returned block is limited to sizeblock. If the returned block is less
	 * than sizeblock length, it is the last block to read.
	 * 
	 * @param sizeblock
	 *            is the limit size for the block array
	 * @return the resulting block ChannelBuffer (even empty)
	 * @throws FtpFileTransferException 
	 * @throws FtpFileEndOfTransferException 
	 */
	private ChannelBuffer getBlock(int sizeblock) throws FtpFileTransferException, FtpFileEndOfTransferException {
		if (!this.isReady) {
			throw new FtpFileTransferException("No file is ready");
		}
		if (this.bfileChannelIn == null) {
			this.bfileChannelIn = this.getFileChannel(false);
			if (this.bfileChannelIn != null) {
				if (this.bbyteBuffer != null) {
					if (this.bbyteBuffer.capacity() != sizeblock) {
						this.bbyteBuffer = null;
						this.bbyteBuffer = ByteBuffer.allocate(sizeblock);
					}
				} else {
					this.bbyteBuffer = ByteBuffer.allocate(sizeblock);
				}
			}
		}
		if (this.bfileChannelIn == null) {
			throw new FtpFileTransferException("Internal error, file is not ready");
		}
		int sizeout = 0;
		try {
			sizeout = this.bfileChannelIn.read(this.bbyteBuffer);
		} catch (IOException e) {
			logger.error("Error during get:",e);
			try {
				this.bfileChannelIn.close();
			} catch (IOException e1) {
			}
			this.bfileChannelIn = null;
			this.bbyteBuffer.clear();
			throw new FtpFileTransferException("Internal error, file is not ready");
		}
		if (sizeout < sizeblock) {// last block
			try {
				this.bfileChannelIn.close();
			} catch (IOException e) {
			}
			this.bfileChannelIn = null;
			this.isReady = false;
			logger.debug("Get size:"+sizeout+" and ask for:"+sizeblock);
		}
		if (sizeout <= 0) {
			this.bbyteBuffer.clear();
			throw new FtpFileEndOfTransferException("End of file");
		}
		this.bbyteBuffer.flip();
		position += sizeout;
		ChannelBuffer buffer = ChannelBuffers.copiedBuffer(bbyteBuffer);
		this.bbyteBuffer.clear();
		return buffer;
	}
	/**
	 * Write the File to the fileChannelOut, thus bypassing the limitation
	 * of the file size to 2^32 bytes.
	 * 
	 * This call closes the fileChannelOut with fileChannelOut.close() if the
	 * operation is in success.
	 * 
	 * @param fileChannelOut
	 * @return True if OK, False in error.
	 */
	private boolean get(FileChannel fileChannelOut) {
		if (!this.isReady) {
			return false;
		}
		FileChannel fileChannelIn = this.getFileChannel(false);
		if (fileChannelIn == null) {
			return false;
		}
		long size = 0;
		long transfert = 0;
		try {
			size = fileChannelIn.size();
			fileChannelOut.force(true);
			transfert = fileChannelOut.transferFrom(fileChannelIn, 0, size);
			fileChannelIn.close();
			fileChannelIn = null;
			fileChannelOut.close();
		} catch (IOException e) {
			logger.error("Error during get:",e);
			try {
				fileChannelIn.close();
			} catch (IOException e1) {
			}
			return false;
		}
		if (transfert == size) {
			position += size;
		}
		return (transfert == size);
	}
	/**
	 * Returns the FileChannel in Out mode (if isOut is True) or in In mode (if
	 * isOut is False) associated with the current file.
	 * 
	 * @param isOut
	 * @return the FileChannel (OUT or IN)
	 */
	private FileChannel getFileChannel(boolean isOut) {
		if (!this.isReady) {
			return null;
		}
		File trueFile;
		try {
			trueFile = this.getFileFromPath(this.currentFile);
		} catch (FtpCommandAbstractException e1) {
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
				FileInputStream fileInputStream = new FileInputStream(
						trueFile);
				fileChannel = fileInputStream.getChannel();
				if (position != 0) {
					fileChannel = fileChannel.position(position);
				}
			}
		} catch (FileNotFoundException e) {
			logger.error("File not found in getFileChannel:",e);
			return null;
		} catch (IOException e) {
			logger.error("Change position in getFileChannel:",e);
			return null;
		}
		return fileChannel;
	}
}
