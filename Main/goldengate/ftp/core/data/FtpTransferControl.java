/**
 * Frederic Bregier LGPL 19 févr. 09 FtpTransferControl.java
 * goldengate.ftp.core.data.handler GoldenGateFtp frederic
 */
package goldengate.ftp.core.data;

import goldengate.ftp.core.command.FtpCommandCode;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.command.exception.Reply425Exception;
import goldengate.ftp.core.command.service.ABOR;
import goldengate.ftp.core.config.FtpInternalConfiguration;
import goldengate.ftp.core.control.NetworkHandler;
import goldengate.ftp.core.data.handler.DataNetworkHandler;
import goldengate.ftp.core.exception.FtpNoConnectionException;
import goldengate.ftp.core.exception.FtpNoFileException;
import goldengate.ftp.core.exception.FtpNoTransferException;
import goldengate.ftp.core.file.FtpFile;
import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;
import goldengate.ftp.core.session.FtpSession;
import goldengate.ftp.core.utils.FtpCommandUtils;
import goldengate.ftp.core.utils.FtpFuture;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.Channels;

/**
 * Main class that handles transfers and their execution
 * 
 * @author frederic goldengate.ftp.core.data.handler FtpTransferControl
 * 
 */
public class FtpTransferControl {
    /**
     * Internal Logger
     */
    private static final FtpInternalLogger logger = FtpInternalLoggerFactory
            .getLogger(FtpTransferControl.class);

    /**
     * Session
     */
    private final FtpSession session;

    /**
     * Lock for Transfer Control
     */
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Step in order to wait that the DataNetworkHandler is ready
     */
    private boolean isDataNetworkHandlerReady = false;

    /**
     * The associated DataChannel
     */
    private Channel dataChannel = null;

    /**
     * Blocking step in order to wait that the DataNetworkHandler is ready
     */
    private FtpFuture dataNetworkHandlerReady = null;

    /**
     * Concurrent list to wait for the dataChannel to be opened
     */
    private final LinkedBlockingQueue<Channel> waitForOpenedDataChannel = new LinkedBlockingQueue<Channel>();

    /**
     * Concurrent list to wait for the dataChannel to be closed
     */
    private FtpFuture closedDataChannel = null;

    /**
     * Is the current Command Finished (or previously current command)
     */
    private boolean isExecutingCommandFinished = true;

    /**
     * Current command executed
     */
    private FtpTransfer executingCommand = null;

    /**
     * Thread pool for execution of transfer command
     */
    private ExecutorService executorService = null;

    /**
     * Blocking step for the Executor in order to wait for the end of the
     * command.
     */
    private FtpFuture endOfCommand = null;

    /**
     * A boolean to know if Check was called once
     */
    private boolean isCheckAlreadyCalled = false;

    /**
     * 
     * @param session
     */
    public FtpTransferControl(FtpSession session) {
        this.session = session;
        this.dataNetworkHandlerReady = new FtpFuture();
        this.closedDataChannel = new FtpFuture();
        this.endOfCommand = null;
    }

    // XXX DataNetworkHandler functions
    /**
     * The DataNetworkHandler is ready (from setNewFtpExecuteTransfer)
     * 
     */
    private void setDataNetworkHandlerReady() {
        this.isCheckAlreadyCalled = false;
        if (this.isDataNetworkHandlerReady) {
            return;
        }
        this.dataNetworkHandlerReady.setSuccess();
        this.isDataNetworkHandlerReady = true;
    }

    /**
     * Wait for the DataNetworkHandler to be ready (from trueRetrieve of
     * {@link FtpFile})
     * 
     * @throws InterruptedException
     * 
     */
    public void waitForDataNetworkHandlerReady() throws InterruptedException {
        if (!this.isDataNetworkHandlerReady) {
            FtpFuture future = this.dataNetworkHandlerReady.await();
            this.dataNetworkHandlerReady = new FtpFuture();
            logger.debug("Wait for DataNetwork Ready over {}", future
                    .isSuccess());
        }
    }

    /**
     * Set the new opened Channel (from channelConnected of
     * {@link DataNetworkHandler})
     * 
     * @param channel
     * @param dataNetworkHandler
     */
    public void setOpenedDataChannel(Channel channel,
            DataNetworkHandler dataNetworkHandler) {
        this.session.getDataConn().setDataNetworkHandler(dataNetworkHandler);
        if (channel != null) {
            this.waitForOpenedDataChannel.add(channel);
        } else {
            this.waitForOpenedDataChannel.add(this.session.getControlChannel());
        }
    }

    /**
     * Wait that the new opened connection is ready (same method in
     * {@link FtpDataAsyncConn} from openConnection)
     * 
     * @return the new opened Channel
     * @throws InterruptedException
     */
    public Channel waitForOpenedDataChannel() throws InterruptedException {
        Channel channel = this.waitForOpenedDataChannel.take();
        logger.debug("Wait for New opened Data Channel over");
        if (this.session.getControlChannel() == channel) {
            return null;
        }
        return channel;
    }

    /**
     * Set the closed Channel (from channelClosed of {@link DataNetworkHandler})
     */
    public void setClosedDataChannel() {
        this.closedDataChannel.setSuccess();
    }

    /**
     * Wait for the client to be connected (Passive) or Wait for the server to
     * be connected to the client (Active) (called from {@link FtpCommandUtils}
     * with same method)
     * 
     * @return True if the connection is OK
     * @throws Reply425Exception
     */
    public boolean openDataConnection() throws Reply425Exception {
        this.lock.lock();
        try {
            FtpDataAsyncConn dataAsyncConn = this.session.getDataConn();
            if (!dataAsyncConn.isStreamFile()) {
                // FIXME isConnected or isDNHReady ?
                if (dataAsyncConn.isConnected()) {
                    // Already connected
                    logger.debug("Connection already open");
                    this.session
                            .setReplyCode(
                                    FtpReplyCode.REPLY_125_DATA_CONNECTION_ALREADY_OPEN,
                                    dataAsyncConn.getType().name() +
                                            " mode data connection already open");
                    return true;
                }
            } else {
                // Stream, Data Connection should not be opened
                if (dataAsyncConn.isConnected()) {
                    logger
                            .error("Connection already open but should not since in Stream mode");
                    this.setTransferAbortedFromInternal(false);
                    return false;
                }
            }
            // Need to open connection
            this.session.setReplyCode(FtpReplyCode.REPLY_150_FILE_STATUS_OKAY,
                    "Opening " + dataAsyncConn.getType().name() +
                            " mode data connection");
            if (dataAsyncConn.isPassiveMode()) {
                // Wait for the connection to be done by the client
                logger.debug("Passive mode standby");
                try {
                    this.dataChannel = dataAsyncConn.waitForOpenedDataChannel();
                } catch (InterruptedException e) {
                    logger.warn("Connection abort in passive mode", e);
                    // Cannot open connection
                    throw new Reply425Exception(
                            "Cannot open passive data connection");
                }
                logger.debug("Passive mode connected");
            } else {
                // Wait for the server to be connected to the client
                logger.debug("Active mode standby");
                ChannelFuture future = null;
                for (int i = 0; i < FtpInternalConfiguration.RETRYNB; i++) {
                    ClientBootstrap clientBootstrap = this.session
                            .getConfiguration().getFtpInternalConfiguration()
                            .getActiveBootstrap();
                    // Set the session for the future dataChannel
                    this.session.getConfiguration().getFtpInternalConfiguration()
                            .setNewFtpSession(
                                    dataAsyncConn.getRemoteAddress().getAddress(),
                                    dataAsyncConn.getLocalAddress(), this.session);
                    future = clientBootstrap.connect(dataAsyncConn
                            .getRemoteAddress(), dataAsyncConn.getLocalAddress());
                    future.awaitUninterruptibly().getChannel();
                    if (future.isSuccess()) {
                        // Wait for the server to be fully connected to the client
                        try {
                            this.dataChannel = dataAsyncConn
                                    .waitForOpenedDataChannel();
                        } catch (InterruptedException e) {
                            logger.warn("Connection abort in active mode", e);
                            // Cannot open connection
                            throw new Reply425Exception(
                                    "Cannot open active data connection");
                        }
                        logger.debug("Active mode connected");
                        break;
                    }
                }
                if (! future.isSuccess()) {
                    logger.error("Can't do Active connection:", future.getCause());
                    // Cannot open connection
                    throw new Reply425Exception(
                            "Cannot open active data connection");
                }
            }
            if (this.dataChannel == null) {
                // Cannot have a new Data connection since shutdown
                throw new Reply425Exception(
                        "Cannot open data connection, shuting down");
            }
        } finally {
            this.lock.unlock();
        }
        return true;
    }

    // XXX FtpTransfer functions
    /**
     * Run the command from an executor
     */
    private void runExecutor() {
        // Unlock Mode Codec
        try {
            this.session.getDataConn().getDataNetworkHandler()
                    .unlockModeCodec();
        } catch (FtpNoConnectionException e) {
            this.setTransferAbortedFromInternal(false);
            return;
        }
        // Run the command
        if (this.executorService == null) {
            this.executorService = Executors.newSingleThreadExecutor();
        }
        this.endOfCommand = new FtpFuture(true);
        this.executorService.execute(new FtpTransferExecutor(this.session,
                this.executingCommand));
    }

    /**
     * Add a new transfer to be executed. This is to be called from Command
     * after connection is opened and before answering to the client that
     * command is ready to be executed (for Store or Retrieve like operations).
     * 
     * @param command
     * @param file
     */
    public void setNewFtpTransfer(FtpCommandCode command, FtpFile file) {
        this.lock.lock();
        try {
            this.isExecutingCommandFinished = false;
            logger.debug("setNewCommand: {}", command);
            this.setDataNetworkHandlerReady();
            this.executingCommand = new FtpTransfer(command, file);
            this.runExecutor();
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Add a new transfer to be executed. This is to be called from Command
     * after connection is opened and before answering to the client that
     * command is ready to be executed (for List like operations).
     * 
     * @param command
     * @param list
     * @param path
     *            as Original Path
     */
    public void setNewFtpTransfer(FtpCommandCode command, List<String> list,
            String path) {
        this.lock.lock();
        try {
            this.isExecutingCommandFinished = false;
            logger.debug("setNewCommand: {}", command);
            this.setDataNetworkHandlerReady();
            this.executingCommand = new FtpTransfer(command, list, path);
            this.runExecutor();
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Is a command currently executing (called from {@link NetworkHandler} when
     * a message is received to see if another transfer command is already in
     * execution, which is not allowed)
     * 
     * @return True if a command is currently executing
     */
    public boolean isFtpTransferExecuting() {
        return (!this.isExecutingCommandFinished);
    }

    /**
     * 
     * @return the current executing FtpTransfer
     * @throws FtpNoTransferException
     */
    public FtpTransfer getExecutingFtpTransfer() throws FtpNoTransferException {
        if (this.executingCommand != null) {
            return this.executingCommand;
        }
        throw new FtpNoTransferException("No Command currently running");
    }

    /**
     * 
     * @return True if the current FtpTransfer is a Retrieve like transfer
     * @throws FtpNoTransferException
     * @throws FtpCommandAbstractException
     * @throws FtpNoFileException
     */
    private boolean isExecutingRetrLikeTransfer()
            throws FtpNoTransferException, FtpCommandAbstractException,
            FtpNoFileException {
        return (FtpCommandCode.isRetrLikeCommand(this.getExecutingFtpTransfer()
                .getCommand()) && this.getExecutingFtpTransfer().getFtpFile()
                .isInReading());
    }

    /**
     * Run the retrieve operation if necessary (called from
     * channelInterestChanged in {@link DataNetworkHandler})
     */
    public void runTrueRetrieve() {
        try {
            if (this.isExecutingRetrLikeTransfer()) {
                this.getExecutingFtpTransfer().getFtpFile().trueRetrieve();
            }
        } catch (FtpCommandAbstractException e) {
        } catch (FtpNoTransferException e) {
        } catch (FtpNoFileException e) {
        }
    }

    /**
     * Called when a transfer is finished from setEndOfTransfer
     * 
     * @return True if it was already called before
     * @throws FtpNoTransferException
     */
    private boolean checkFtpTransferStatus() throws FtpNoTransferException {
        if (this.isCheckAlreadyCalled) {
            logger.warn("Check: ALREADY CALLED");
            return true;
        }
        if (this.isExecutingCommandFinished) {
            // already done
            logger.warn("Check: already Finished");
            throw new FtpNoTransferException("No transfer running");
        }
        if (!this.isDataNetworkHandlerReady) {
            // already done
            logger.warn("Check: already DNH not ready");
            throw new FtpNoTransferException("No connection");
        }
        this.lock.lock();
        try {
            this.isCheckAlreadyCalled = true;
            FtpTransfer executedTransfer = this.getExecutingFtpTransfer();
            logger.debug("Check: command {}", executedTransfer.getCommand());
            // DNH is ready and Transfer is running
            if (FtpCommandCode.isListLikeCommand(executedTransfer.getCommand())) {
                if (executedTransfer.getStatus()) {
                    // Special status for List Like command
                    logger.debug("Check: List OK");
                    this.closeTransfer(true);
                    return false;
                }
                logger.debug("Check: List Ko");
                this.abortTransfer(true);
                return false;
            } else if (FtpCommandCode.isRetrLikeCommand(executedTransfer
                    .getCommand())) {
                FtpFile file = null;
                try {
                    file = executedTransfer.getFtpFile();
                } catch (FtpNoFileException e) {
                    logger.debug("Check: Retr no File for Retr");
                    this.abortTransfer(true);
                    return false;
                }
                try {
                    if (file.isInReading()) {
                        logger.debug("Check: Retr File still in reading KO");
                        this.abortTransfer(true);
                    } else {
                        logger.debug("Check: Retr File no more in reading OK");
                        this.closeTransfer(true);
                    }
                } catch (FtpCommandAbstractException e) {
                    logger.warn("Retr Test is in Reading problem", e);
                    this.closeTransfer(true);
                }
                return false;
            } else if (FtpCommandCode.isStoreLikeCommand(executedTransfer
                    .getCommand())) {
                logger.debug("Check: Store OK");
                this.closeTransfer(true);
                return false;
            } else {
                logger.warn("Check: Unknown command");
                this.abortTransfer(true);
            }
            return false;
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Abort the current transfer
     * 
     * @param write
     *            True means the message is write back to the control command,
     *            false it is only prepared
     */
    private void abortTransfer(boolean write) {
        logger.debug("Will abort transfer and write: ", write);
        FtpFile file = null;
        FtpTransfer current = null;
        try {
            current = this.getExecutingFtpTransfer();
            file = current.getFtpFile();
            file.abortFile();
        } catch (FtpNoTransferException e) {
            logger.warn("Abort problem", e);
        } catch (FtpNoFileException e) {
        } catch (FtpCommandAbstractException e) {
            logger.warn("Abort problem", e);
        }
        if (current != null) {
            current.setStatus(false);
        }
        this.endDataConnection();
        this.session.setReplyCode(
                FtpReplyCode.REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED,
                "Transfer aborted for " +
                        (current == null? "Unknown command" : current
                                .toString()));
        if (write) {
            this.session.getNetworkHandler().writeIntermediateAnswer();
        }
        this.finalizeExecution();
        if (current != null) {
            if (!FtpCommandCode.isListLikeCommand(current.getCommand())) {
                this.session.getBusinessHandler().afterTransferDone(current);
            }
        }
    }

    /**
     * Finish correctly a transfer
     * 
     * @param write
     *            True means the message is write back to the control command,
     *            false it is only prepared
     */
    private void closeTransfer(boolean write) {
        logger.debug("Will close transfer and write: {}", write);
        FtpFile file = null;
        FtpTransfer current = null;
        try {
            current = this.getExecutingFtpTransfer();
            file = current.getFtpFile();
            file.closeFile();
        } catch (FtpNoTransferException e) {
            logger.warn("Close problem", e);
        } catch (FtpNoFileException e) {
        } catch (FtpCommandAbstractException e) {
            logger.warn("Close problem", e);
        }
        if (current != null) {
            current.setStatus(true);
        }
        if (this.session.getDataConn().isStreamFile()) {
            this.endDataConnection();
        }
        this.session.setReplyCode(
                FtpReplyCode.REPLY_250_REQUESTED_FILE_ACTION_OKAY,
                "Transfer correctly finished for " +
                        (current == null? "Unknown command" : current
                                .toString()));
        if (write) {
            this.session.getNetworkHandler().writeIntermediateAnswer();
        }
        this.finalizeExecution();
        if (current != null) {
            if (!FtpCommandCode.isListLikeCommand(current.getCommand())) {
                this.session.getBusinessHandler().afterTransferDone(current);
            }
        }
    }

    /**
     * Set the current transfer as finished. Called from
     * {@link FtpTransferExecutor} when a transfer is over.
     * 
     */
    public void setEndOfTransfer() {
        this.lock.lock();
        try {
            try {
                this.checkFtpTransferStatus();
            } catch (FtpNoTransferException e) {
                return;
            }
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * To enable abort from internal error
     * 
     * @param write
     *            True means the message is write back to the control command,
     *            false it is only prepapred
     */
    public void setTransferAbortedFromInternal(boolean write) {
        logger.debug("Set transfer aborted internal {}", write);
        this.lock.lock();
        try {
            this.abortTransfer(write);
            this.endOfCommand.cancel();
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Called by messageReceived, channelClosed (from {@link DataNetworkHandler}
     * ) and trueRetrieve (from {@link FtpFile}) when the transfer is over FIXME
     * or by channelClosed
     */
    public void setPreEndOfTransfer() {
        this.endOfCommand.setSuccess();
    }

    /**
     * Wait for the current transfer to finish, called from
     * {@link FtpTransferExecutor}
     * 
     * @throws InterruptedException
     */
    public void waitForEndOfTransfer() throws InterruptedException {
        this.endOfCommand.await();
        logger.debug("waitEndOfCommand over");
    }

    // XXX ExecutorHandler functions
    /**
     * Finalize execution
     * 
     */
    private void finalizeExecution() {
        logger.debug("Finalize execution");
        this.isExecutingCommandFinished = true;
        this.executingCommand = null;
    }

    // XXX Finalize of Transfer
    /**
     * End the data connection if any
     */
    private void endDataConnection() {
        logger.debug("End Data connection");
        this.lock.lock();
        try {
            if (this.isDataNetworkHandlerReady) {
                this.isDataNetworkHandlerReady = false;
                Channels.close(this.dataChannel);
                this.closedDataChannel.awaitUninterruptibly();
                // set ready for a new connection
                this.closedDataChannel = new FtpFuture();
                logger.debug("waitForClosedDataChannel over");
                this.dataChannel = null;
            }
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Clear the FtpTransferControl (called when the data connection must be
     * over like from clear of {@link FtpDataAsyncConn}, abort from {@link ABOR}
     * or ending control connection from {@link NetworkHandler}.
     * 
     */
    public void clear() {
        logger.debug("Clear Ftp Transfer Control");
        this.endDataConnection();
        this.finalizeExecution();
        this.dataNetworkHandlerReady = null;
        this.closedDataChannel = null;
        this.endOfCommand = null;
        this.waitForOpenedDataChannel.clear();
        if (this.executorService != null) {
            this.executorService.shutdownNow();
            this.executorService = null;
        }
    }
}
