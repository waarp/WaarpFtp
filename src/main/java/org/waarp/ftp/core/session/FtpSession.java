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
package org.waarp.ftp.core.session;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import io.netty.channel.Channel;

import org.waarp.common.command.CommandInterface;
import org.waarp.common.command.ReplyCode;
import org.waarp.common.command.exception.CommandAbstractException;
import org.waarp.common.command.exception.Reply425Exception;
import org.waarp.common.file.FileParameterInterface;
import org.waarp.common.file.Restart;
import org.waarp.common.file.SessionInterface;
import org.waarp.common.future.WaarpFuture;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.ftp.core.command.AbstractCommand;
import org.waarp.ftp.core.command.FtpArgumentCode;
import org.waarp.ftp.core.command.FtpArgumentCode.TransferSubType;
import org.waarp.ftp.core.command.internal.ConnectionCommand;
import org.waarp.ftp.core.config.FtpConfiguration;
import org.waarp.ftp.core.control.BusinessHandler;
import org.waarp.ftp.core.control.NetworkHandler;
import org.waarp.ftp.core.data.FtpDataAsyncConn;
import org.waarp.ftp.core.exception.FtpNoConnectionException;
import org.waarp.ftp.core.file.FtpAuth;
import org.waarp.ftp.core.file.FtpDir;

/**
 * Main class that stores any information that must be accessible from anywhere during the
 * connection of one user.
 * 
 * @author Frederic Bregier
 * 
 */
public class FtpSession implements SessionInterface {
    /**
     * Internal Logger
     */
    private static final WaarpLogger logger = WaarpLoggerFactory.getLogger(FtpSession.class);
    /**
     * Business Handler
     */
    private final BusinessHandler businessHandler;

    /**
     * Associated global configuration
     */
    private final FtpConfiguration configuration;

    /**
     * Associated Binary connection
     */
    private volatile FtpDataAsyncConn dataConn = null;

    /**
     * Ftp Authentication
     */
    private FtpAuth ftpAuth = null;

    /**
     * Ftp DirInterface configuration and access
     */
    private FtpDir ftpDir = null;

    /**
     * Previous Command
     */
    private AbstractCommand previousCommand = null;

    /**
     * Current Command
     */
    private AbstractCommand currentCommand = null;
    /**
     * Is the current command finished
     */
    private volatile boolean isCurrentCommandFinished = true;

    /**
     * Associated Reply Code
     */
    private ReplyCode replyCode = null;

    /**
     * Real text for answer
     */
    private String answer = null;

    /**
     * Current Restart information
     */
    private Restart restart = null;

    /**
     * Is the control ready to accept command
     */
    private final WaarpFuture isReady = new WaarpFuture(true);

    /**
     * Is the current session using SSL on Control
     */
    private volatile boolean isSsl = false;
    /**
     * Is the current session will using SSL on Control
     */
    private volatile WaarpFuture waitForSsl = null;
    /**
     * WIll all data be using SSL
     */
    private volatile boolean isDataSsl = false;

    /**
     * Constructor
     * 
     * @param configuration
     * @param handler
     */
    public FtpSession(FtpConfiguration configuration, BusinessHandler handler) {
        this.configuration = configuration;
        businessHandler = handler;
    }

    /**
     * @return the businessHandler
     */
    public BusinessHandler getBusinessHandler() {
        return businessHandler;
    }

    /**
     * Get the configuration
     * 
     * @return the configuration
     */
    public FtpConfiguration getConfiguration() {
        return configuration;
    }

    public FtpDir getDir() {
        return ftpDir;
    }

    /**
     * @return the Data Connection
     */
    public FtpDataAsyncConn getDataConn() {
        return dataConn;
    }

    public FtpAuth getAuth() {
        return ftpAuth;
    }

    public Restart getRestart() {
        return restart;
    }

    /**
     * This function is called when the Command Channel is connected (from channelConnected of the
     * NetworkHandler)
     */
    public void setControlConnected() {
        dataConn = new FtpDataAsyncConn(this);
        // AuthInterface must be done before FtpFile
        ftpAuth = businessHandler.getBusinessNewAuth();
        ftpDir = businessHandler.getBusinessNewDir();
        restart = businessHandler.getBusinessNewRestart();
    }

    /**
     * Special initialization (FtpExec with Https session)
     * 
     * @param auth
     * @param dir
     * @param restart
     */
    public void setSpecialInit(FtpAuth auth, FtpDir dir, Restart restart) {
        this.ftpAuth = auth;
        this.ftpDir = dir;
        this.restart = restart;
    }

    /**
     * @return the Control channel
     */
    public Channel getControlChannel() {
        return getNetworkHandler().getControlChannel();
    }

    /**
     * 
     * @return The network handler associated with control
     */
    public NetworkHandler getNetworkHandler() {
        if (businessHandler != null) {
            return businessHandler.getNetworkHandler();
        }
        return null;
    }

    /**
     * Set the new current command
     * 
     * @param command
     */
    public void setNextCommand(CommandInterface command) {
        previousCommand = currentCommand;
        currentCommand = (AbstractCommand) command;
        isCurrentCommandFinished = false;
    }

    /**
     * @return the currentCommand
     */
    public AbstractCommand getCurrentCommand() {
        return currentCommand;
    }

    /**
     * @return the previousCommand
     */
    public AbstractCommand getPreviousCommand() {
        return previousCommand;
    }

    /**
     * Set the previous command as the new current command (used after a incorrect sequence of
     * commands or unknown command)
     * 
     */
    public void setPreviousAsCurrentCommand() {
        currentCommand = previousCommand;
        isCurrentCommandFinished = true;
    }

    /**
     * 
     * @return True if the Current Command is already Finished (ready to accept a new one)
     */
    public boolean isCurrentCommandFinished() {
        return isCurrentCommandFinished;
    }

    /**
     * Set the Current Command as finished
     */
    public void setCurrentCommandFinished() {
        this.isCurrentCommandFinished = true;
    }

    /**
     * @return the answer
     */
    public String getAnswer() {
        if (answer == null) {
            if (replyCode == null) {
                answer = ReplyCode.REPLY_000_SPECIAL_NOSTATUS.getMesg();
            } else {
                answer = replyCode.getMesg();
            }
        }
        return answer;
    }

    /**
     * @param replyCode
     *            the replyCode to set
     * @param answer
     */
    public void setReplyCode(ReplyCode replyCode, String answer) {
        this.replyCode = replyCode;
        if (answer != null) {
            this.answer = ReplyCode.getFinalMsg(replyCode.getCode(), answer);
        } else {
            this.answer = replyCode.getMesg();
        }
    }

    /**
     * @param exception
     */
    public void setReplyCode(CommandAbstractException exception) {
        this.setReplyCode(exception.code, exception.message);
    }

    /**
     * Set Exit code after an error
     * 
     * @param answer
     */
    public void setExitErrorCode(String answer) {
        this.setReplyCode(ReplyCode.REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
                answer);
    }

    /**
     * Set Exit normal code
     * 
     * @param answer
     */
    public void setExitNormalCode(String answer) {
        this.setReplyCode(ReplyCode.REPLY_221_CLOSING_CONTROL_CONNECTION,
                answer);
    }

    /**
     * @return the replyCode
     */
    public ReplyCode getReplyCode() {
        return replyCode;
    }

    public void clear() {
        if (dataConn != null) {
            dataConn.clear();
        }
        if (ftpDir != null) {
            ftpDir.clear();
        }
        if (ftpAuth != null) {
            ftpAuth.clear();
        }
        previousCommand = null;
        replyCode = null;
        answer = null;
        isReady.cancel();
    }

    /**
     * @return True if the Control is ready to accept command
     */
    public boolean isReady() {
        return isReady.awaitUninterruptibly().isSuccess();
    }

    /**
     * @param isReady
     *            the isReady to set
     */
    public void setReady(boolean isReady) {
        if (isReady) {
            this.isReady.setSuccess();
        } else {
            this.isReady.cancel();
        }
    }

    @Override
    public String toString() {
        String mesg = "FtpSession: ";
        if (ftpAuth != null) {
            mesg += "User: " + ftpAuth.getUser() + "/" + ftpAuth.getAccount() + " ";
        }
        if (previousCommand != null) {
            mesg += "PRVCMD: " + previousCommand.getCommand() + " " +
                    previousCommand.getArg() + " ";
        }
        if (currentCommand != null) {
            mesg += "CMD: " + currentCommand.getCommand() + " " +
                    currentCommand.getArg() + " ";
        }
        if (replyCode != null) {
            mesg += "Reply: " + (answer != null ? answer : replyCode.getMesg()) +
                    " ";
        }
        if (dataConn != null) {
            mesg += dataConn.toString();
        }
        if (ftpDir != null) {
            try {
                mesg += " PWD: " + ftpDir.getPwd();
            } catch (CommandAbstractException e) {
            }
        }
        if (getControlChannel() != null) {
            mesg += " Control: " + getControlChannel();
        }
        try {
            if (getDataConn().getCurrentDataChannel() != null) {
                mesg += " Data: " + getDataConn().getCurrentDataChannel();
            }
        } catch (FtpNoConnectionException e) {
        }
        return mesg + "\n";
    }

    public int getBlockSize() {
        return restart.getMaxSize(configuration.getBLOCKSIZE());
    }

    public FileParameterInterface getFileParameter() {
        return configuration.getFileParameter();
    }

    /**
     * 
     * @param path
     * @return the basename from the given path
     */
    public static String getBasename(String path) {
        File file = new File(path);
        return file.getName();
    }

    /**
     * Reinitialize the authentication to the connection step
     * 
     */
    public void reinitFtpAuth() {
        AbstractCommand connectioncommand = new ConnectionCommand(this);
        setNextCommand(connectioncommand);
        getAuth().clear();
        getDataConn().clear();
        getDataConn().getFtpTransferControl().resetWaitForOpenedDataChannel();
    }

    /**
     * Reinitialize all connection parameters, including authentification
     */
    public void rein() {
        // reset to default
        if (getDataConn().isPassiveMode()) {
            // Previous mode was Passive so remove the current configuration
            InetSocketAddress local = getDataConn().getLocalAddress();
            InetAddress remote = getDataConn().getRemoteAddress().getAddress();
            getConfiguration().delFtpSession(remote, local);
        }
        getDataConn().setMode(FtpArgumentCode.TransferMode.STREAM);
        getDataConn().setStructure(FtpArgumentCode.TransferStructure.FILE);
        getDataConn().setType(FtpArgumentCode.TransferType.ASCII);
        getDataConn().setSubType(TransferSubType.NONPRINT);
        reinitFtpAuth();
    }

    /**
     * Try to open a connection. Do the intermediate reply if any (150) and the final one (125)
     * 
     * @throws Reply425Exception
     *             if the connection cannot be opened
     */
    public void openDataConnection() throws Reply425Exception {
        getDataConn().getFtpTransferControl().openDataConnection();
        getNetworkHandler().writeIntermediateAnswer();
    }

    @Override
    public String getUniqueExtension() {
        return configuration.getUniqueExtension();
    }

    public boolean isSsl() {
        return isSsl;
    }

    public void setSsl(boolean isSsl) {
        this.isSsl = isSsl;
        if (waitForSsl != null) {
            if (isSsl) {
                waitForSsl.setSuccess();
            } else {
                waitForSsl.cancel();
            }
        }
    }

    public void prepareSsl() {
        waitForSsl = new WaarpFuture(true);
    }

    public boolean isSslReady() {
        if (waitForSsl != null) {
            for (int i = 0; i < 10; i++) {
                if (waitForSsl.awaitUninterruptibly(100)) {
                    break;
                }
                Thread.yield();
            }
            logger.debug("DEBUG : " + (waitForSsl != null ? waitForSsl.isDone() : "not Finished") + ":" + isSsl + ":"
                    + getControlChannel());
        }
        return isSsl;
    }

    public boolean isDataSsl() {
        return isDataSsl;
    }

    public void setDataSsl(boolean isDataSsl) {
        this.isDataSsl = isDataSsl;
    }

}
