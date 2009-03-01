/**
 * Frederic Bregier LGPL 31 janv. 09 
 * FtpFile.java goldengate.ftp.core.file GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.file;

import goldengate.ftp.core.command.FtpArgumentCode.TransferStructure;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.command.exception.Reply530Exception;
import goldengate.ftp.core.data.handler.FtpDataBlock;
import goldengate.ftp.core.exception.FtpFileEndOfTransferException;
import goldengate.ftp.core.exception.FtpFileTransferException;
import goldengate.ftp.core.exception.FtpNoConnectionException;
import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;
import goldengate.ftp.core.session.FtpSession;

import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.Channels;

/**
 * Base of Ftp File where only String are considered 
 * (no File since it can be something else than a file).
 * @author frederic
 * goldengate.ftp.core.file FtpFile
 * 
 */
public abstract class FtpFile {
	/**
	 * Internal Logger
	 */
	private static final FtpInternalLogger logger =
        FtpInternalLoggerFactory.getLogger(FtpFile.class);
	/**
	 * Ftp Session
	 */
	private FtpSession session = null;
	/**
	 * FtpDir associated with this file at creation.
	 * It is not necessary the directory that owns this file.
	 */
	private FtpDir ftpDir = null;
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
	 * Retrieve lock
	 */
	private ReentrantLock retrieveLock = new ReentrantLock();
	/**
	 * Constructor
	 * @param session
	 * @param dir It is not necessary the directory that owns this file.
	 * @param path
	 * @param append
	 */
	public FtpFile(FtpSession session, FtpDir dir, String path, boolean append) {
		this.session = session;
		this.ftpDir = dir;
		this.isReady = false;
		this.currentFile = path;
		this.isReady = true;
		this.isAppend = append;
	}
	/**
	 * Set empty this FtpFile, mark it unReady.
	 * @throws FtpCommandAbstractException 
	 */
	public void clear() throws FtpCommandAbstractException {
		this.closeFile();
		this.isReady = false;
		this.currentFile = null;
		this.isAppend = false;
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
	 * 
	 * @return the FtpDir associated at creation with this file
	 */
	public FtpDir getFtpDir() {
		return this.ftpDir;
	}
	/**
	 * Is the current File a directory and exists
	 * @return True if it is a directory and it exists
	 * @throws FtpCommandAbstractException
	 */
	public abstract boolean isDirectory() throws FtpCommandAbstractException;
	/**
	 * Is the current File a file and exists
	 * @return True if it is a file and it exists
	 * @throws FtpCommandAbstractException
	 */
	public abstract boolean isFile() throws FtpCommandAbstractException;
	
	// **************** Unique File part **************************
	/**
	 * 
	 * @return the path of the current File (without mount point if any)
	 * @throws FtpCommandAbstractException
	 */
	public abstract String getFile() throws FtpCommandAbstractException;
	/**
	 * Close the current File
	 * @return True if correctly closed
	 * @throws FtpCommandAbstractException
	 */
	public abstract boolean closeFile() throws FtpCommandAbstractException;
	/**
	 * 
	 * @return the length of the current File
	 * @throws FtpCommandAbstractException
	 */
	public abstract long length() throws FtpCommandAbstractException;
	/**
	 * @return True if the current File is in Writing process
	 * @throws FtpCommandAbstractException
	 */
	public abstract boolean isInWriting() throws FtpCommandAbstractException;
	/**
	 * 
	 * @return True if the current File is in Reading process
	 * @throws FtpCommandAbstractException
	 */
	public abstract boolean isInReading() throws FtpCommandAbstractException;
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
	 * Try to abort the current transfer if any
	 * @return True if everything is ok
	 * @throws FtpCommandAbstractException
	 */
	public abstract boolean abortFile() throws FtpCommandAbstractException;
	/**
	 * Ask to store the current File. This command returns quickly since it does not store really. 
	 * It prepares the connection and make a temporary answer.
	 * @return True if everything is ready
	 * @throws FtpCommandAbstractException
	 */
	public abstract boolean store() throws FtpCommandAbstractException;
	/**
	 * Ask to retrieve the current File. This command returns quickly since it does not retrieve really. 
	 * It prepares the connection.
	 * @return True if everything is ready
	 * @throws FtpCommandAbstractException
	 */
	public abstract boolean retrieve() throws FtpCommandAbstractException;
	/**
	 * Rename the current File into a new filename from argument
	 * @param path the new filename (path could be relative or absolute - without mount point)
	 * @return True if the operation is done successfully
	 * @throws FtpCommandAbstractException
	 */
	public abstract boolean renameTo(String path) throws FtpCommandAbstractException;
	/**
	 * Restart from a Marker for the current File if any. This function is to be called at
	 * the beginning of every transfer so in store and retrieve method.
	 * @param restart
	 * @return True if the Marker is OK
	 * @exception FtpCommandAbstractException
	 */
	protected abstract boolean restartMarker(FtpRestart restart) throws FtpCommandAbstractException;
	/**
	 * Create a restart from context for the current File
	 * @return the dataBlock to send to the client
	 * @exception FtpCommandAbstractException
	 */
	public abstract FtpDataBlock getMarker() throws FtpCommandAbstractException;
	/**
	 * Delete the current File.
	 * 
	 * @return True if OK, else False if not (or if the file never exists).
	 * @exception FtpCommandAbstractException
	 */
	public abstract boolean delete() throws FtpCommandAbstractException;
	
	// **************** File read or write part **************************
	/**
	 * Write a new block for Record
	 * @param dataBlock
	 * @throws FtpFileEndOfTransferException 
	 * @throws FtpFileTransferException 
	 */
	protected abstract void writeRecordFtpDataBlock(FtpDataBlock dataBlock) throws FtpFileEndOfTransferException, FtpFileTransferException;
	/**
	 * Read a new block for Record
	 * @return dataBlock
	 * @throws FtpFileEndOfTransferException 
	 * @throws FtpFileTransferException 
	 */
	protected abstract FtpDataBlock readRecordFtpDataBlock() throws FtpFileEndOfTransferException, FtpFileTransferException;
	/**
	 * Write a new block for File
	 * @param dataBlock
	 * @throws FtpFileEndOfTransferException 
	 * @throws FtpFileTransferException 
	 */
	protected abstract void writeFileFtpDataBlock(FtpDataBlock dataBlock) throws FtpFileEndOfTransferException, FtpFileTransferException;
	/**
	 * Read a new block for File 
	 * @return dataBlock
	 * @throws FtpFileEndOfTransferException 
	 * @throws FtpFileTransferException 
	 */
	protected abstract FtpDataBlock readFileFtpDataBlock() throws FtpFileEndOfTransferException, FtpFileTransferException;
	
	/**
	 * Function called by the DataNetworkHandler when it receives one FtpDataBlock (Store like command)
	 * @param dataBlock
	 * @throws FtpFileTransferException 
	 * @throws FtpFileEndOfTransferException 
	 */
	public void receiveDataBlock(FtpDataBlock dataBlock) throws FtpFileEndOfTransferException, FtpFileTransferException {
		if (this.session.getDataConn().getStructure() == TransferStructure.FILE) {
			this.writeFileFtpDataBlock(dataBlock);
		} else {
			this.writeRecordFtpDataBlock(dataBlock);
		}
	}
	/**
	 * Launch retrieve operation (internal method, should not be called directly)
	 *
	 */
	public void trueRetrieve() {
		this.retrieveLock.lock();
		try {
			if (! this.isReady) {
				return;
			}
			// First check if ready to run from Control
			try {
				this.session.getDataConn().getFtpTransferControl().waitForDataNetworkHandlerReady();
			} catch (InterruptedException e) {
				// bad thing
				logger.warn("DataNetworkHandler was not ready",e);
				return;
			}
			
			Channel channel = this.session.getDataConn().getCurrentDataChannel();
			if (this.session.getDataConn().getStructure() == TransferStructure.FILE) {
				// File
				FtpDataBlock block = null;
				try {
					block = this.readFileFtpDataBlock();
				} catch (FtpFileEndOfTransferException e) {
					// Last block (in fact, previous block was the last one, 
					// but it could be aligned with the block size so not detected)
					this.closeFile();
					this.session.getDataConn().getFtpTransferControl().setPreEndOfTransfer();
					return;
				}
				if (block == null) {
					// Last block (in fact, previous block was the last one, 
					// but it could be aligned with the block size so not detected)
					this.closeFile();
					this.session.getDataConn().getFtpTransferControl().setPreEndOfTransfer();
					return;
				}
				// While not last block
				ChannelFuture future = null;
				while ((block != null) && (! block.isEOF())) {
					future = Channels.write(channel, block);
					// Test if channel is writable in order to prevent OOM
					if (channel.isWritable()) {
						try {
							block = this.readFileFtpDataBlock();
						} catch (FtpFileEndOfTransferException e) {
							this.closeFile();
							// Wait for last write
							future.awaitUninterruptibly();
							this.session.getDataConn().getFtpTransferControl().setPreEndOfTransfer();
							return;
						}
					} else {
						return;// Wait for the next InterestChanged
					}
				}
				// Last block
				this.closeFile();
				if (block != null) {
					future = Channels.write(channel, block);
				}
				// Wait for last write
				future.awaitUninterruptibly();
				this.session.getDataConn().getFtpTransferControl().setPreEndOfTransfer();
			} else {
				// Record
				FtpDataBlock block = null;
				try {
					block = this.readRecordFtpDataBlock();
				} catch (FtpFileEndOfTransferException e) {
					// Last block
					this.closeFile();
					this.session.getDataConn().getFtpTransferControl().setPreEndOfTransfer();
					return;
				}
				if (block == null) {
					// Last block
					this.closeFile();
					this.session.getDataConn().getFtpTransferControl().setPreEndOfTransfer();
					return;
				}
				// While not last block
				ChannelFuture future = null;
				while ((block != null) && (! block.isEOF())) {
					future = Channels.write(channel, block);
					// Test if channel is writable in order to prevent OOM
					if (channel.isWritable()) {
						try {
							block = this.readRecordFtpDataBlock();
						} catch (FtpFileEndOfTransferException e) {
							// Last block
							this.closeFile();
							// Wait for last write
							future.awaitUninterruptibly();
							this.session.getDataConn().getFtpTransferControl().setPreEndOfTransfer();
							return;
						}
					} else {
						return;// Wait for the next InterestChanged
					}
				}
				// Last block
				this.closeFile();
				if (block != null) {
					future = Channels.write(channel, block);
				}
				// Wait for last write
				future.awaitUninterruptibly();
				this.session.getDataConn().getFtpTransferControl().setPreEndOfTransfer();
			}
		} catch (FtpFileTransferException e) {
			// An error occurs!
			this.session.getDataConn().getFtpTransferControl().setTransferAbortedFromInternal(true);
		} catch (FtpNoConnectionException e) {
			logger.error("Should not be",e);
			this.session.getDataConn().getFtpTransferControl().setTransferAbortedFromInternal(true);
		} catch (FtpCommandAbstractException e) {
			logger.error("Should not be",e);
			this.session.getDataConn().getFtpTransferControl().setTransferAbortedFromInternal(true);
		} finally {
			this.retrieveLock.unlock();
		}
	}
}
