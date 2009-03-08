/**
 * 
 */
package goldengate.ftp.core.data;

import goldengate.ftp.core.command.FtpCommandCode;
import goldengate.ftp.core.config.FtpInternalConfiguration;
import goldengate.ftp.core.exception.FtpNoConnectionException;
import goldengate.ftp.core.exception.FtpNoFileException;
import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;
import goldengate.ftp.core.session.FtpSession;

import java.util.List;

/**
 * Class that implements the execution of the Transfer itself.
 * @author fbregier
 *
 */
public class FtpTransferExecutor implements Runnable {
	/**
	 * Internal Logger
	 */
	private static final FtpInternalLogger logger =
        FtpInternalLoggerFactory.getLogger(FtpTransferExecutor.class);
	/**
	 * Ftp Session
	 */
	private final FtpSession session;
	/**
	 * FtpTransfer 
	 */
	private final FtpTransfer executeTransfer;
	/**
	 * Create an executor and launch it
	 * @param session
	 * @param executeTransfer
	 */
	public FtpTransferExecutor(FtpSession session, FtpTransfer executeTransfer) {
		this.session = session;
		this.executeTransfer = executeTransfer;
		if (this.executeTransfer == null) {
			this.session.getDataConn().getFtpTransferControl().setEndOfTransfer();
			logger.error("No Execution to do");
			return;
		}
	}
	
	/**
	 * Internal method, should not be called directly
	 */
	public void run() {
		if (this.executeTransfer == null) {
			this.session.getDataConn().getFtpTransferControl().setEndOfTransfer();
			logger.error("No Execution to do");
			return;
		}
		try {
			this.runNextCommand();
		} catch (InterruptedException e) {
			logger.error("Executor Interrupted",e);
		}
	}

	/**
	 * Run the next command or wait for the next
	 * @throws InterruptedException 
	 */
	private void runNextCommand() throws InterruptedException {
		if (FtpCommandCode.isStoreLikeCommand(executeTransfer.getCommand())) {
			// The command is implicitely done by receiving message
			logger.debug("Command launch: {} {}",executeTransfer.getCommand(),this.session);
			this.waitForCommand();
			// Store set end
			this.session.getDataConn().getFtpTransferControl().setEndOfTransfer();
			logger.debug("Command finished: {} {}",executeTransfer.getCommand(),this.session);
		} else if (FtpCommandCode.isListLikeCommand(executeTransfer.getCommand())) {
			// No wait for Command since the answer is already there
			logger.debug("Command launch: {} {}",executeTransfer.getCommand(),this.session);
			List<String> list = executeTransfer.getInfo();
			StringBuilder builder = new StringBuilder();
			for (String newfileInfo : list) {
				builder.append(newfileInfo);
				builder.append(FtpInternalConfiguration.CRLF);
			}
			if (builder.length() == 0) {
				builder.append(FtpInternalConfiguration.CRLF);
			}
			String message = builder.toString();
			boolean status = false;
			try {
				status = this.session.getDataConn().getDataNetworkHandler().writeMessage(message);
			} catch (FtpNoConnectionException e) {
				logger.error("No Connection but should not be!",e);
			}
			// Set status for check, no wait for the command
			executeTransfer.setStatus(status);
			// must explicitely set the end and no wait
			this.session.getDataConn().getFtpTransferControl().setEndOfTransfer();
			logger.debug("Command finished: {} {}",executeTransfer.getCommand(),this.session);
		} else if (FtpCommandCode.isRetrLikeCommand(executeTransfer.getCommand())) {
			// The command must be launched
			logger.debug("Command launch: {} {}",executeTransfer.getCommand(),this.session);
			try {
				executeTransfer.getFtpFile().trueRetrieve();
			} catch (FtpNoFileException e) {
				// an error occurs
				this.session.getDataConn().getFtpTransferControl().setEndOfTransfer();
			}
			this.waitForCommand();
			// RETR set end
			this.session.getDataConn().getFtpTransferControl().setEndOfTransfer();
			logger.debug("Command finished: {} {}",executeTransfer.getCommand(),this.session);
		} else {
			// This is an error as unknown transfer command
			logger.debug("Unknown transfer command: {}",executeTransfer);
			this.session.getDataConn().getFtpTransferControl().setEndOfTransfer();
		}
	}
	/**
	 * Wait for the command to finish
	 * @throws InterruptedException 
	 *
	 */
	private void waitForCommand() throws InterruptedException {
		this.session.getDataConn().getFtpTransferControl().waitForEndOfTransfer();
	}
}
