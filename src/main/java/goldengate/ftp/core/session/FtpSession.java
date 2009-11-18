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
package goldengate.ftp.core.session;

import java.io.File;

import goldengate.common.command.CommandInterface;
import goldengate.common.command.ReplyCode;
import goldengate.common.command.exception.CommandAbstractException;
import goldengate.common.command.exception.Reply425Exception;
import goldengate.common.file.FileParameterInterface;
import goldengate.common.file.Restart;
import goldengate.common.file.SessionInterface;
import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.internal.ConnectionCommand;
import goldengate.ftp.core.config.FtpConfiguration;
import goldengate.ftp.core.control.BusinessHandler;
import goldengate.ftp.core.control.NetworkHandler;
import goldengate.ftp.core.data.FtpDataAsyncConn;
import goldengate.ftp.core.file.FtpAuth;
import goldengate.ftp.core.file.FtpDir;

import org.jboss.netty.channel.Channel;

/**
 * Main class that stores any information that must be accessible from anywhere
 * during the connection of one user.
 *
 * @author Frederic Bregier
 *
 */
public class FtpSession implements SessionInterface {
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
    private volatile boolean isReady = false;

    /**
     * Constructor
     *
     * @param configuration
     * @param handler
     */
    public FtpSession(FtpConfiguration configuration, BusinessHandler handler) {
        this.configuration = configuration;
        businessHandler = handler;
        isReady = false;
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
     * This function is called when the Command Channel is connected (from
     * channelConnected of the NetworkHandler)
     */
    public void setControlConnected() {
        dataConn = new FtpDataAsyncConn(this);
        // AuthInterface must be done before FtpFile
        ftpAuth = businessHandler.getBusinessNewAuth();
        ftpDir = businessHandler.getBusinessNewDir();
        restart = businessHandler.getBusinessNewRestart();
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
     * Set the previous command as the new current command (used after a
     * incorrect sequence of commands or unknown command)
     *
     */
    public void setPreviousAsCurrentCommand() {
        currentCommand = previousCommand;
    }

    /**
     * @return the answer
     */
    public String getAnswer() {
        if (answer == null) {
            answer = replyCode.getMesg();
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
        this
                .setReplyCode(
                        ReplyCode.REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
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
        isReady = false;
    }

    /**
     * @return True if the Control is ready to accept command
     */
    public boolean isReady() {
        return isReady;
    }

    /**
     * @param isReady
     *            the isReady to set
     */
    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    @Override
    public String toString() {
        String mesg = "FtpSession: ";
        if (currentCommand != null) {
            mesg += "CMD: " + currentCommand.getCommand() + " " +
                    currentCommand.getArg() + " ";
        }
        if (replyCode != null) {
            mesg += "Reply: " + (answer != null? answer : replyCode.getMesg()) +
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
        return mesg + "\n";
    }

    public int getBlockSize() {
        return configuration.BLOCKSIZE;
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
    }

    /**
     * Try to open a connection. Do the intermediate reply if any (150) and the
     * final one (125)
     *
     * @throws Reply425Exception
     *             if the connection cannot be opened
     */
    public void openDataConnection() throws Reply425Exception {
        getDataConn().getFtpTransferControl().openDataConnection();
    }

    /* (non-Javadoc)
     * @see goldengate.common.file.SessionInterface#getUniqueExtension()
     */
    @Override
    public String getUniqueExtension() {
        return configuration.getUniqueExtension();
    }
}
