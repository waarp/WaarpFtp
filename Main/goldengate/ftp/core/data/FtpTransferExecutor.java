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
package goldengate.ftp.core.data;

import goldengate.common.command.ReplyCode;
import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;
import goldengate.ftp.core.command.FtpCommandCode;
import goldengate.ftp.core.exception.FtpNoConnectionException;
import goldengate.ftp.core.exception.FtpNoFileException;
import goldengate.ftp.core.session.FtpSession;

import java.util.List;

/**
 * Class that implements the execution of the Transfer itself.
 *
 * @author Frederic Bregier
 *
 */
public class FtpTransferExecutor implements Runnable {
    /**
     * Internal Logger
     */
    private static final GgInternalLogger logger = GgInternalLoggerFactory
            .getLogger(FtpTransferExecutor.class);

    /**
     * Ftp SessionInterface
     */
    private final FtpSession session;

    /**
     * FtpTransfer
     */
    private final FtpTransfer executeTransfer;

    /**
     * Create an executor and launch it
     *
     * @param session
     * @param executeTransfer
     */
    public FtpTransferExecutor(FtpSession session, FtpTransfer executeTransfer) {
        this.session = session;
        this.executeTransfer = executeTransfer;
        if (this.executeTransfer == null) {
            this.session.getDataConn().getFtpTransferControl()
                    .setEndOfTransfer();
            logger.error("No Execution to do");
            return;
        }
    }

    /**
     * Internal method, should not be called directly
     */
    public void run() {
        if (executeTransfer == null) {
            session.getDataConn().getFtpTransferControl()
                    .setEndOfTransfer();
            logger.error("No Execution to do");
            return;
        }
        try {
            runNextCommand();
        } catch (InterruptedException e) {
            logger.error("Executor Interrupted", e);
        }
    }

    /**
     * Run the next command or wait for the next
     *
     * @throws InterruptedException
     */
    private void runNextCommand() throws InterruptedException {
        if (FtpCommandCode
                .isStoreLikeCommand(executeTransfer.getCommand())) {
            // The command is implicitly done by receiving message
            logger.debug("Command launch: {} {}", executeTransfer
                    .getCommand(), session);
            waitForCommand();
            // Store set end
            try {
                session.getDataConn().getFtpTransferControl()
                        .setEndOfTransfer();
            } catch (NullPointerException e) {
                // ignore, due probably to an already clean session
            }
            logger.debug("Command finished: {} {}", executeTransfer
                    .getCommand(), session);
        } else if (FtpCommandCode.isListLikeCommand(executeTransfer
                .getCommand())) {
            // No wait for Command since the answer is already there
            logger.debug("Command launch: {} {}", executeTransfer
                    .getCommand(), session);
            List<String> list = executeTransfer.getInfo();
            StringBuilder builder = new StringBuilder();
            for (String newfileInfo: list) {
                builder.append(newfileInfo);
                builder.append(ReplyCode.CRLF);
            }
            if (builder.length() == 0) {
                builder.append(ReplyCode.CRLF);
            }
            String message = builder.toString();
            boolean status = false;
            try {
                status = session.getDataConn().getDataNetworkHandler()
                        .writeMessage(message);
            } catch (FtpNoConnectionException e) {
                logger.error("No Connection but should not be!", e);
            }
            // Set status for check, no wait for the command
            executeTransfer.setStatus(status);
            // must explicitly set the end and no wait
            session.getDataConn().getFtpTransferControl()
                    .setEndOfTransfer();
            logger.debug("Command finished: {} {}", executeTransfer
                    .getCommand(), session);
        } else if (FtpCommandCode.isRetrLikeCommand(executeTransfer
                .getCommand())) {
            // The command must be launched
            logger.debug("Command launch: {} {}", executeTransfer
                    .getCommand(), session);
            try {
                executeTransfer.getFtpFile().trueRetrieve();
            } catch (FtpNoFileException e) {
                // an error occurs
                session.getDataConn().getFtpTransferControl()
                        .setEndOfTransfer();
            }
            waitForCommand();
            // RETR set end
            try {
                session.getDataConn().getFtpTransferControl()
                        .setEndOfTransfer();
            } catch (NullPointerException e) {
                // ignore, due probably to an already clean session
            }
            logger.debug("Command finished: {} {}", executeTransfer
                    .getCommand(), session);
        } else {
            // This is an error as unknown transfer command
            logger.debug("Unknown transfer command: {}", executeTransfer);
            session.getDataConn().getFtpTransferControl()
                    .setEndOfTransfer();
        }
    }

    /**
     * Wait for the command to finish
     *
     * @throws InterruptedException
     *
     */
    private void waitForCommand() throws InterruptedException {
        session.getDataConn().getFtpTransferControl()
                .waitForEndOfTransfer();
    }
}
