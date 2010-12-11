/**
   This file is part of GoldenGate Project (named also GoldenGate or GG).

   Copyright 2009, Frederic Bregier, and individual contributors by the @author
   tags. See the COPYRIGHT.txt in the distribution for a full listing of
   individual contributors.

   All GoldenGate Project is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   GoldenGate is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with GoldenGate .  If not, see <http://www.gnu.org/licenses/>.
 */
package goldengate.ftp.filesystembased;

import goldengate.common.command.exception.CommandAbstractException;
import goldengate.common.exception.FileEndOfTransferException;
import goldengate.common.exception.FileTransferException;
import goldengate.common.file.DataBlock;
import goldengate.common.file.filesystembased.FilesystemBasedFileImpl;
import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;
import goldengate.ftp.core.exception.FtpNoConnectionException;
import goldengate.ftp.core.file.FtpFile;
import goldengate.ftp.core.session.FtpSession;

import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.Channels;

/**
 * Filesystem implementation of a FtpFile
 *
 * @author Frederic Bregier
 *
 */
public abstract class FilesystemBasedFtpFile extends FilesystemBasedFileImpl implements FtpFile {
    /**
     * Internal Logger
     */
    private static final GgInternalLogger logger = GgInternalLoggerFactory
            .getLogger(FilesystemBasedFtpFile.class);

    /**
     * Retrieve lock to ensure only one call at a time for one file
     */
    private final ReentrantLock retrieveLock = new ReentrantLock();

    /**
     * @param session
     * @param dir
     *            It is not necessary the directory that owns this file.
     * @param path
     * @param append
     * @throws CommandAbstractException
     */
    public FilesystemBasedFtpFile(FtpSession session,
            FilesystemBasedFtpDir dir, String path, boolean append)
            throws CommandAbstractException {
        super(session, dir, path, append);
    }

    @Override
    public long length() throws CommandAbstractException {
        long length = super.length();
        if (((FtpSession) getSession()).getDataConn()
                .isFileStreamBlockAsciiImage()) {
            long block = (long) Math.ceil((double) length /
                    (double) getSession().getBlockSize());
            length += (block + 3) * 3;
        }
        return length;
    }

    /**
     * Launch retrieve operation (internal method, should not be called
     * directly)
     *
     */
    public void trueRetrieve() {
        retrieveLock.lock();
        try {
            if (!isReady) {
                return;
            }
            // First check if ready to run from Control
            try {
                ((FtpSession) session).getDataConn().getFtpTransferControl()
                        .waitForDataNetworkHandlerReady();
            } catch (InterruptedException e) {
                // bad thing
                logger.warn("DataNetworkHandler was not ready", e);
                return;
            }

            Channel channel = ((FtpSession) session).getDataConn()
                    .getCurrentDataChannel();
            DataBlock block = null;
            try {
                block = readDataBlock();
            } catch (FileEndOfTransferException e) {
                // Last block (in fact, previous block was the last one,
                // but it could be aligned with the block size so not
                // detected)
                closeFile();
                ((FtpSession) session).getDataConn().getFtpTransferControl()
                        .setPreEndOfTransfer();
                return;
            }
            if (block == null) {
                // Last block (in fact, previous block was the last one,
                // but it could be aligned with the block size so not
                // detected)
                closeFile();
                ((FtpSession) session).getDataConn().getFtpTransferControl()
                        .setPreEndOfTransfer();
                return;
            }
            // While not last block
            ChannelFuture future = null;
            while (block != null && !block.isEOF()) {
                future = Channels.write(channel, block);
                // Test if channel is writable in order to prevent OOM
                if (channel.isWritable()) {
                    try {
                        block = readDataBlock();
                    } catch (FileEndOfTransferException e) {
                        closeFile();
                        // Wait for last write
                        try {
                            future.await();
                        } catch (InterruptedException e1) {
                            throw new FileTransferException("Interruption catched");
                        }
                        if (future.isSuccess()) {
                            ((FtpSession) session).getDataConn()
                                .getFtpTransferControl().setPreEndOfTransfer();
                        } else {
                            throw new FileTransferException("File transfer in error");
                        }
                        return;
                    }
                } else {
                    return;// Wait for the next InterestChanged
                }
                try {
                    future.await();
                } catch (InterruptedException e) {
                    closeFile();
                    throw new FileTransferException("Interruption catched");
                }
                if (! future.isSuccess()) {
                    closeFile();
                    throw new FileTransferException("File transfer in error");
                }
            }
            // Last block
            closeFile();
            if (block != null) {
                future = Channels.write(channel, block);
            }
            // Wait for last write
            if (future != null) {
                try {
                    future.await();
                } catch (InterruptedException e) {
                    throw new FileTransferException("Interruption catched");
                }
                if (future.isSuccess()) {
                    ((FtpSession) session).getDataConn().getFtpTransferControl()
                        .setPreEndOfTransfer();
                } else {
                    throw new FileTransferException("Write is not successful");
                }
            }
        } catch (FileTransferException e) {
            // An error occurs!
            ((FtpSession) session).getDataConn().getFtpTransferControl()
                    .setTransferAbortedFromInternal(true);
        } catch (FtpNoConnectionException e) {
            logger.error("Should not be", e);
            ((FtpSession) session).getDataConn().getFtpTransferControl()
                    .setTransferAbortedFromInternal(true);
        } catch (CommandAbstractException e) {
            logger.error("Should not be", e);
            ((FtpSession) session).getDataConn().getFtpTransferControl()
                    .setTransferAbortedFromInternal(true);
        } finally {
            retrieveLock.unlock();
        }
    }
}
