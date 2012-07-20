/**
 * This file is part of Waarp Project.
 * 
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author tags. See the
 * COPYRIGHT.txt in the distribution for a full listing of individual contributors.
 * 
 * All Waarp Project is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Waarp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Waarp . If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.waarp.ftp.core.data;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.waarp.common.command.ReplyCode;
import org.waarp.common.command.exception.CommandAbstractException;
import org.waarp.common.command.exception.Reply425Exception;
import org.waarp.common.crypto.ssl.WaarpSslUtility;
import org.waarp.common.future.WaarpChannelFuture;
import org.waarp.common.future.WaarpFuture;
import org.waarp.common.logging.WaarpInternalLogger;
import org.waarp.common.logging.WaarpInternalLoggerFactory;
import org.waarp.ftp.core.command.FtpCommandCode;
import org.waarp.ftp.core.command.service.ABOR;
import org.waarp.ftp.core.config.FtpInternalConfiguration;
import org.waarp.ftp.core.control.NetworkHandler;
import org.waarp.ftp.core.data.handler.DataNetworkHandler;
import org.waarp.ftp.core.exception.FtpNoConnectionException;
import org.waarp.ftp.core.exception.FtpNoFileException;
import org.waarp.ftp.core.exception.FtpNoTransferException;
import org.waarp.ftp.core.file.FtpFile;
import org.waarp.ftp.core.session.FtpSession;

/**
 * Main class that handles transfers and their execution
 * 
 * @author Frederic Bregier
 * 
 */
public class FtpTransferControl {
	/**
	 * Internal Logger
	 */
	private static final WaarpInternalLogger logger = WaarpInternalLoggerFactory
			.getLogger(FtpTransferControl.class);

	/**
	 * SessionInterface
	 */
	private final FtpSession session;

	/**
	 * Step in order to wait that the DataNetworkHandler is ready
	 */
	private volatile boolean isDataNetworkHandlerReady = false;

	/**
	 * The associated DataChannel
	 */
	private volatile Channel dataChannel = null;

	/**
	 * Waiter for the dataChannel to be opened
	 */
	private volatile WaarpChannelFuture waitForOpenedDataChannel = new WaarpChannelFuture(
			true);

	/**
	 * Waiter for the dataChannel to be closed
	 */
	private volatile WaarpFuture closedDataChannel = null;

	/**
	 * Is the current Command Finished (or previously current command)
	 */
	private volatile boolean isExecutingCommandFinished = true;
	/**
	 * Waiter for the Command finishing
	 */
	private volatile WaarpFuture commandFinishing = null;

	/**
	 * Current command executed
	 */
	private FtpTransfer executingCommand = null;

	/**
	 * Thread pool for execution of transfer command
	 */
	private ExecutorService executorService = null;

	/**
	 * Blocking step for the Executor in order to wait for the end of the command (internal wait,
	 * not to be used outside).
	 */
	private volatile WaarpFuture endOfCommand = null;

	/**
	 * A boolean to know if Check was called once
	 */
	private volatile boolean isCheckAlreadyCalled = false;

	/**
	 * 
	 * @param session
	 */
	public FtpTransferControl(FtpSession session) {
		this.session = session;
		endOfCommand = null;
	}

	// XXX DataNetworkHandler functions
	/**
	 * The DataNetworkHandler is ready (from setNewFtpExecuteTransfer)
	 * 
	 */
	private void setDataNetworkHandlerReady() {
		isCheckAlreadyCalled = false;
		if (isDataNetworkHandlerReady) {
			return;
		}
		isDataNetworkHandlerReady = true;
	}

	/**
	 * Wait for the DataNetworkHandler to be ready (from trueRetrieve of {@link FtpFile})
	 * 
	 * @throws InterruptedException
	 * 
	 */
	public void waitForDataNetworkHandlerReady() throws InterruptedException {
		if (!isDataNetworkHandlerReady) {
			// logger.debug("Wait for DataNetwork Ready over {}");
			throw new InterruptedException("Bad initialization");
		}
	}

	/**
	 * Set the new opened Channel (from channelConnected of {@link DataNetworkHandler})
	 * 
	 * @param channel
	 * @param dataNetworkHandler
	 */
	public void setOpenedDataChannel(Channel channel,
			DataNetworkHandler dataNetworkHandler) {
		logger.debug("SetOpenedDataChannel: "+(channel != null ? channel.getRemoteAddress() : "no channel"));
		if (channel != null) {
			session.getDataConn().setDataNetworkHandler(dataNetworkHandler);
			waitForOpenedDataChannel.setChannel(channel);
			waitForOpenedDataChannel.setSuccess();
		} else {
			waitForOpenedDataChannel.cancel();
		}
	}

	/**
	 * Wait that the new opened connection is ready (same method in {@link FtpDataAsyncConn} from
	 * openConnection)
	 * 
	 * @return the new opened Channel
	 * @throws InterruptedException
	 */
	public Channel waitForOpenedDataChannel() throws InterruptedException {
		Channel channel = null;
		if (waitForOpenedDataChannel.await(
				session.getConfiguration().TIMEOUTCON + 1000,
				TimeUnit.MILLISECONDS)) {
			if (waitForOpenedDataChannel.isSuccess()) {
				channel = waitForOpenedDataChannel.getChannel();
			} else {
				logger.warn("data connection is in error");
			}
		} else {
			logger.warn("Timeout occurs during data connection");
		}
		waitForOpenedDataChannel = new WaarpChannelFuture(true);
		return channel;
	}
	
	/**
	 * Allow to reset the waitForOpenedDataChannel
	 */
	public void resetWaitForOpenedDataChannel() {
		waitForOpenedDataChannel = new WaarpChannelFuture(true);
	}

	/**
	 * Set the closed Channel (from channelClosed of {@link DataNetworkHandler})
	 */
	public void setClosedDataChannel() {
		if (closedDataChannel != null) {
			closedDataChannel.setSuccess();
		}
	}

	/**
	 * Wait for the client to be connected (Passive) or Wait for the server to be connected to the
	 * client (Active)
	 * 
	 * @return True if the connection is OK
	 * @throws Reply425Exception
	 */
	public boolean openDataConnection() throws Reply425Exception {
		// Prepare this Data channel to be closed ;-)
		// In fact, prepare the future close op which should occur since it is
		// now opened
		closedDataChannel = new WaarpFuture(true);
		FtpDataAsyncConn dataAsyncConn = session.getDataConn();
		if (!dataAsyncConn.isStreamFile()) {
			// FIXME isConnected or isDNHReady ?
			if (dataAsyncConn.isConnected()) {
				// Already connected
				// logger.debug("Connection already open");
				session.setReplyCode(
						ReplyCode.REPLY_125_DATA_CONNECTION_ALREADY_OPEN,
						dataAsyncConn.getType().name() +
								" mode data connection already open");
				return true;
			}
		} else {
			// Stream, Data Connection should not be opened
			if (dataAsyncConn.isConnected()) {
				logger
						.error("Connection already open but should not since in Stream mode");
				setTransferAbortedFromInternal(false);
				throw new Reply425Exception(
						"Connection already open but should not since in Stream mode");
			}
		}
		// Need to open connection
		session.setReplyCode(ReplyCode.REPLY_150_FILE_STATUS_OKAY, "Opening " +
				dataAsyncConn.getType().name() + " mode data connection");
		if (dataAsyncConn.isPassiveMode()) {
			if (!dataAsyncConn.isBind()) {
				// No passive connection prepared
				throw new Reply425Exception(
						"No passive data connection prepared");
			}
			// Wait for the connection to be done by the client
			// logger.debug("Passive mode standby");
			try {
				dataChannel = waitForOpenedDataChannel();
				dataAsyncConn.setNewOpenedDataChannel(dataChannel);
			} catch (InterruptedException e) {
				logger.warn("Connection abort in passive mode", e);
				// Cannot open connection
				throw new Reply425Exception(
						"Cannot open passive data connection");
			}
			// logger.debug("Passive mode connected");
		} else {
			// Wait for the server to be connected to the client
			InetAddress inetAddress = dataAsyncConn.getLocalAddress()
					.getAddress();
			InetSocketAddress inetSocketAddress = dataAsyncConn
					.getRemoteAddress();
			if (session.getConfiguration().getFtpInternalConfiguration()
					.hasFtpSession(inetAddress, inetSocketAddress)) {
				throw new Reply425Exception(
						"Cannot open active data connection since remote address is already in use: "
								+
								inetSocketAddress);
			}
			// logger.debug("Active mode standby");
			ClientBootstrap clientBootstrap = session.getConfiguration()
					.getFtpInternalConfiguration().getActiveBootstrap(session.isDataSsl());
			session.getConfiguration().setNewFtpSession(inetAddress,
					inetSocketAddress, session);
			// Set the session for the future dataChannel
			String mylog = session.toString();
			logger.debug("DataConn for: " + session.getCurrentCommand().getCommand() + " to "
					+ inetSocketAddress.toString());
			ChannelFuture future = clientBootstrap.connect(inetSocketAddress,
					dataAsyncConn.getLocalAddress());
			try {
				future.await();
			} catch (InterruptedException e1) {
			}
			if (!future.isSuccess()) {
				logger.warn("Connection abort in active mode from future while session: " +
						session.toString() +
						"\nTrying connect to: " + inetSocketAddress.toString() +
						"\nWas: " + mylog,
						future.getCause());
				// Cannot open connection
				session.getConfiguration().delFtpSession(inetAddress,
						inetSocketAddress);
				throw new Reply425Exception(
						"Cannot open active data connection");
			}
			try {
				dataChannel = waitForOpenedDataChannel();
				dataAsyncConn.setNewOpenedDataChannel(dataChannel);
			} catch (InterruptedException e) {
				logger.warn("Connection abort in active mode", e);
				// Cannot open connection
				session.getConfiguration().delFtpSession(inetAddress,
						inetSocketAddress);
				throw new Reply425Exception(
						"Cannot open active data connection");
			}
			// logger.debug("Active mode connected");
		}
		if (dataChannel == null) {
			// Cannot have a new Data connection since shutdown
			if (!dataAsyncConn.isPassiveMode()) {
				session.getConfiguration().getFtpInternalConfiguration()
						.delFtpSession(
								dataAsyncConn.getLocalAddress().getAddress(),
								dataAsyncConn.getRemoteAddress());
			}
			throw new Reply425Exception(
					"Cannot open data connection, shuting down");
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
			session.getDataConn().getDataNetworkHandler().unlockModeCodec();
		} catch (FtpNoConnectionException e) {
			setTransferAbortedFromInternal(false);
			return;
		}
		// Run the command
		if (executorService == null) {
			executorService = Executors.newSingleThreadExecutor();
		}
		endOfCommand = new WaarpFuture(true);
		final ArrayList<Callable<Object>> tasks = new ArrayList<Callable<Object>>(1);
		tasks.add(Executors.callable(new FtpTransferExecutor(session,
				executingCommand)));
		try {
			executorService.invokeAll(tasks);
		} catch (InterruptedException e1) {
		}
		// XXX TRY FIX TO IMPROVE
		/*
		executorService.execute(new FtpTransferExecutor(session,
				executingCommand));
		*/
		try {
			commandFinishing.await();
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Add a new transfer to be executed. This is to be called from Command after connection is
	 * opened and before answering to the client that command is ready to be executed (for Store or
	 * Retrieve like operations).
	 * 
	 * @param command
	 * @param file
	 */
	public void setNewFtpTransfer(FtpCommandCode command, FtpFile file) {
		isExecutingCommandFinished = false;
		commandFinishing = new WaarpFuture(true);
		// logger.debug("setNewCommand: {}", command);
		setDataNetworkHandlerReady();
		executingCommand = new FtpTransfer(command, file);
		runExecutor();
		commandFinishing = null;
	}

	/**
	 * Add a new transfer to be executed. This is to be called from Command after connection is
	 * opened and before answering to the client that command is ready to be executed (for List like
	 * operations).
	 * 
	 * @param command
	 * @param list
	 * @param path
	 *            as Original Path
	 */
	public void setNewFtpTransfer(FtpCommandCode command, List<String> list,
			String path) {
		isExecutingCommandFinished = false;
		commandFinishing = new WaarpFuture(true);
		// logger.debug("setNewCommand: {}", command);
		setDataNetworkHandlerReady();
		executingCommand = new FtpTransfer(command, list, path);
		runExecutor();
		commandFinishing = null;
	}

	/**
	 * Is a command currently executing (called from {@link NetworkHandler} when a message is
	 * received to see if another transfer command is already in execution, which is not allowed)
	 * 
	 * @return True if a command is currently executing
	 */
	public boolean isFtpTransferExecuting() {
		return !isExecutingCommandFinished;
	}

	/**
	 * 
	 * @return the current executing FtpTransfer
	 * @throws FtpNoTransferException
	 */
	public FtpTransfer getExecutingFtpTransfer() throws FtpNoTransferException {
		if (executingCommand != null) {
			return executingCommand;
		}
		throw new FtpNoTransferException("No Command currently running");
	}

	/**
	 * 
	 * @return True if the current FtpTransfer is a Retrieve like transfer
	 * @throws FtpNoTransferException
	 * @throws CommandAbstractException
	 * @throws FtpNoFileException
	 */
	private boolean isExecutingRetrLikeTransfer()
			throws FtpNoTransferException, CommandAbstractException,
			FtpNoFileException {
		return FtpCommandCode.isRetrLikeCommand(getExecutingFtpTransfer()
				.getCommand()) &&
				getExecutingFtpTransfer().getFtpFile().isInReading();
	}

	/**
	 * Run the retrieve operation if necessary (called from channelInterestChanged in
	 * {@link DataNetworkHandler})
	 */
	public void runTrueRetrieve() {
		try {
			if (isExecutingRetrLikeTransfer()) {
				getExecutingFtpTransfer().getFtpFile().trueRetrieve();
			}
		} catch (CommandAbstractException e) {
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
		if (isCheckAlreadyCalled) {
			logger.warn("Check: ALREADY CALLED");
			return true;
		}
		if (isExecutingCommandFinished) {
			// already done
			logger.warn("Check: already Finished");
			if (commandFinishing != null) {
				commandFinishing.cancel();
			}
			throw new FtpNoTransferException("No transfer running");
		}
		if (!isDataNetworkHandlerReady) {
			// already done
			logger.warn("Check: already DNH not ready");
			throw new FtpNoTransferException("No connection");
		}
		isCheckAlreadyCalled = true;
		FtpTransfer executedTransfer = getExecutingFtpTransfer();
		// logger.debug("Check: command {}", executedTransfer.getCommand());
		// DNH is ready and Transfer is running
		if (FtpCommandCode.isListLikeCommand(executedTransfer.getCommand())) {
			if (executedTransfer.getStatus()) {
				// Special status for List Like command
				// logger.debug("Check: List OK");
				closeTransfer();
				return false;
			}
			// logger.debug("Check: List Ko");
			abortTransfer();
			return false;
		} else if (FtpCommandCode.isRetrLikeCommand(executedTransfer
				.getCommand())) {
			FtpFile file = null;
			try {
				file = executedTransfer.getFtpFile();
			} catch (FtpNoFileException e) {
				// logger.debug("Check: Retr no FtpFile for Retr");
				abortTransfer();
				return false;
			}
			try {
				if (file.isInReading()) {
					logger
							.debug("Check: Retr FtpFile still in reading KO");
					abortTransfer();
				} else {
					logger
							.debug("Check: Retr FtpFile no more in reading OK");
					closeTransfer();
				}
			} catch (CommandAbstractException e) {
				logger.warn("Retr Test is in Reading problem", e);
				closeTransfer();
			}
			return false;
		} else if (FtpCommandCode.isStoreLikeCommand(executedTransfer
				.getCommand())) {
			// logger.debug("Check: Store OK");
			closeTransfer();
			return false;
		} else {
			logger.warn("Check: Unknown command");
			abortTransfer();
		}
		return false;
	}

	/**
	 * Abort the current transfer
	 */
	private void abortTransfer() {
		// logger.debug("Will abort transfer and write: ", write);
		FtpFile file = null;
		FtpTransfer current = null;
		try {
			current = getExecutingFtpTransfer();
			file = current.getFtpFile();
			file.abortFile();
		} catch (FtpNoTransferException e) {
			logger.warn("Abort problem", e);
		} catch (FtpNoFileException e) {
		} catch (CommandAbstractException e) {
			logger.warn("Abort problem", e);
		}
		if (current != null) {
			current.setStatus(false);
		}
		endDataConnection();
		session.setReplyCode(
				ReplyCode.REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED,
				"Transfer aborted for " +
						(current == null ? "Unknown command" : current
								.toString()));
		if (current != null) {
			if (!FtpCommandCode.isListLikeCommand(current.getCommand())) {
				try {
					session.getBusinessHandler().afterTransferDoneBeforeAnswer(current);
				} catch (CommandAbstractException e) {
					session.setReplyCode(e);
				}
			}
		}
		finalizeExecution();
	}

	/**
	 * Finish correctly a transfer
	 * 
	 */
	private void closeTransfer() {
		// logger.debug("Will close transfer and write: {}", write);
		FtpFile file = null;
		FtpTransfer current = null;
		try {
			current = getExecutingFtpTransfer();
			file = current.getFtpFile();
			file.closeFile();
		} catch (FtpNoTransferException e) {
			logger.warn("Close problem", e);
		} catch (FtpNoFileException e) {
		} catch (CommandAbstractException e) {
			logger.warn("Close problem", e);
		}
		if (current != null) {
			current.setStatus(true);
		}
		if (session.getDataConn().isStreamFile()) {
			endDataConnection();
		}
		session.setReplyCode(ReplyCode.REPLY_250_REQUESTED_FILE_ACTION_OKAY,
				"Transfer correctly finished for " +
						(current == null ? "Unknown command" : current
								.toString()));
		if (current != null) {
			if (!FtpCommandCode.isListLikeCommand(current.getCommand())) {
				try {
					session.getBusinessHandler().afterTransferDoneBeforeAnswer(current);
				} catch (CommandAbstractException e) {
					session.setReplyCode(e);
				}
			} else {
				// Special wait to prevent fast LIST following by STOR or RETR command
				try {
					Thread.sleep(FtpInternalConfiguration.RETRYINMS);
				} catch (InterruptedException e) {
				}
			}
		}
		finalizeExecution();
	}

	/**
	 * Set the current transfer as finished. Called from {@link FtpTransferExecutor} when a transfer
	 * is over.
	 * 
	 */
	public void setEndOfTransfer() {
		try {
			checkFtpTransferStatus();
		} catch (FtpNoTransferException e) {
			return;
		}
	}

	/**
	 * To enable abort from internal error
	 * 
	 * @param write
	 *            True means the message is write back to the control command, false it is only
	 *            prepared
	 */
	public void setTransferAbortedFromInternal(boolean write) {
		// logger.debug("Set transfer aborted internal {}", write);
		abortTransfer();
		if (write) {
			session.getNetworkHandler().writeIntermediateAnswer();
		}
		if (endOfCommand != null) {
			endOfCommand.cancel();
		}
	}

	/**
	 * Called by messageReceived, channelClosed (from {@link DataNetworkHandler} ) and trueRetrieve
	 * (from {@link FtpFile}) when the transfer is over or by channelClosed
	 */
	public void setPreEndOfTransfer() {
		if (endOfCommand != null) {
			endOfCommand.setSuccess();
		}
	}

	/**
	 * Wait for the current transfer to finish, called from {@link FtpTransferExecutor}
	 * 
	 * @throws InterruptedException
	 */
	public void waitForEndOfTransfer() throws InterruptedException {
		if (endOfCommand != null) {
			endOfCommand.await();
			if (endOfCommand.isFailed()) {
				throw new InterruptedException("Transfer aborted");
			}
		}
		// logger.debug("waitEndOfCommand over");
	}

	// XXX ExecutorHandler functions
	/**
	 * Finalize execution
	 * 
	 */
	private void finalizeExecution() {
		// logger.debug("Finalize execution");
		isExecutingCommandFinished = true;
		if (commandFinishing != null) {
			commandFinishing.setSuccess();
		}
		executingCommand = null;
	}

	// XXX Finalize of Transfer
	/**
	 * End the data connection if any
	 */
	private void endDataConnection() {
		// logger.debug("End Data connection");
		if (isDataNetworkHandlerReady) {
			isDataNetworkHandlerReady = false;
			WaarpSslUtility.closingSslChannel(dataChannel);
			if (closedDataChannel != null) {
				try {
					closedDataChannel.await(session.getConfiguration().TIMEOUTCON,
							TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
				}
			}
			// logger.debug("waitForClosedDataChannel over");
			dataChannel = null;
		}
	}

	/**
	 * Clear the FtpTransferControl (called when the data connection must be over like from clear of
	 * {@link FtpDataAsyncConn}, abort from {@link ABOR} or ending control connection from
	 * {@link NetworkHandler}.
	 * 
	 */
	public void clear() {
		// logger.debug("Clear Ftp Transfer Control");
		endDataConnection();
		finalizeExecution();
		if (closedDataChannel != null) {
			closedDataChannel.cancel();
		}
		if (endOfCommand != null) {
			endOfCommand.cancel();
		}
		if (waitForOpenedDataChannel != null) {
			waitForOpenedDataChannel.cancel();
		}
		if (executorService != null) {
			executorService.shutdownNow();
			executorService = null;
		}
	}
}
