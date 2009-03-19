/**
 * Frederic Bregier LGPL 10 janv. 09 FtpSession.java goldengate.ftp.core.session
 * GoldenGateFtp frederic
 */
package goldengate.ftp.core.session;

import goldengate.ftp.core.auth.FtpAuth;
import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.config.FtpConfiguration;
import goldengate.ftp.core.control.BusinessHandler;
import goldengate.ftp.core.control.NetworkHandler;
import goldengate.ftp.core.data.FtpDataAsyncConn;
import goldengate.ftp.core.file.FtpDir;
import goldengate.ftp.core.file.FtpRestart;

import org.jboss.netty.channel.Channel;

/**
 * Main class that stores any information that must be accessible from anywhere
 * during the connection of one user.
 * 
 * @author frederic goldengate.ftp.core.session FtpSession
 * 
 */
public class FtpSession {
    /**
     * Business Handler
     */
    private BusinessHandler businessHandler = null;

    /**
     * Associated global configuration
     */
    private FtpConfiguration configuration = null;

    /**
     * Associated Binary connection
     */
    private FtpDataAsyncConn dataConn = null;

    /**
     * Ftp Authentication
     */
    private FtpAuth ftpAuth = null;

    /**
     * Ftp Dir configuration and access
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
    private FtpReplyCode replyCode = null;

    /**
     * Real text for answer
     */
    private String answer = null;

    /**
     * Current Restart information
     */
    private FtpRestart restart = null;

    /**
     * Is the control ready to accept command
     */
    private boolean isReady = false;

    /**
     * Constructor
     * 
     * @param configuration
     * @param handler
     */
    public FtpSession(FtpConfiguration configuration, BusinessHandler handler) {
        this.configuration = configuration;
        this.businessHandler = handler;
        this.isReady = false;
    }

    /**
     * @return the businessHandler
     */
    public BusinessHandler getBusinessHandler() {
        return this.businessHandler;
    }

    /**
     * Get the configuration
     * 
     * @return the configuration
     */
    public FtpConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * @return the ftpDir
     */
    public FtpDir getFtpDir() {
        return this.ftpDir;
    }

    /**
     * @return the Data Connection
     */
    public FtpDataAsyncConn getDataConn() {
        return this.dataConn;
    }

    /**
     * @return the ftpAuth
     */
    public FtpAuth getFtpAuth() {
        return this.ftpAuth;
    }

    /**
     * @return the restart
     */
    public FtpRestart getFtpRestart() {
        return this.restart;
    }

    /**
     * This function is called when the Command Channel is connected (from
     * channelConnected of the NetworkHandler)
     */
    public void setControlConnected() {
        this.dataConn = new FtpDataAsyncConn(this);
        // Auth must be done before FtpFile
        this.ftpAuth = this.businessHandler.getBusinessNewAuth();
        this.ftpDir = this.businessHandler.getBusinessNewFtpDir();
        this.restart = this.businessHandler.getBusinessNewFtpRestart();
    }

    /**
     * @return the Control channel
     */
    public Channel getControlChannel() {
        return this.getNetworkHandler().getControlChannel();
    }

    /**
     * 
     * @return The network handler associated with control
     */
    public NetworkHandler getNetworkHandler() {
        if (this.businessHandler != null) {
            return this.businessHandler.getNetworkHandler();
        }
        return null;
    }

    /**
     * Set the new current command
     * 
     * @param command
     */
    public void setNextCommand(AbstractCommand command) {
        this.previousCommand = this.currentCommand;
        this.currentCommand = command;
    }

    /**
     * @return the currentCommand
     */
    public AbstractCommand getCurrentCommand() {
        return this.currentCommand;
    }

    /**
     * @return the previousCommand
     */
    public AbstractCommand getPreviousCommand() {
        return this.previousCommand;
    }

    /**
     * Set the previous command as the new current command (used after a
     * incorrect sequence of commands or unknown command)
     * 
     */
    public void setPreviousAsCurrentCommand() {
        this.currentCommand = this.previousCommand;
    }

    /**
     * @return the answer
     */
    public String getAnswer() {
        if (this.answer == null) {
            this.answer = this.replyCode.getMesg();
        }
        return this.answer;
    }

    /**
     * @param replyCode
     *            the replyCode to set
     * @param answer
     */
    public void setReplyCode(FtpReplyCode replyCode, String answer) {
        this.replyCode = replyCode;
        if (answer != null) {
            this.answer = FtpReplyCode.getFinalMsg(replyCode.getCode(), answer);
        } else {
            this.answer = replyCode.getMesg();
        }
    }

    /**
     * @param exception
     */
    public void setReplyCode(FtpCommandAbstractException exception) {
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
                        FtpReplyCode.REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
                        answer);
    }

    /**
     * Set Exit normal code
     * 
     * @param answer
     */
    public void setExitNormalCode(String answer) {
        this.setReplyCode(FtpReplyCode.REPLY_221_CLOSING_CONTROL_CONNECTION,
                answer);
    }

    /**
     * @return the replyCode
     */
    public FtpReplyCode getReplyCode() {
        return this.replyCode;
    }

    /**
     * Clean the session
     * 
     */
    public void clean() {
        if (this.dataConn != null) {
            this.dataConn.clear();
            this.dataConn = null;
        }
        if (this.ftpDir != null) {
            this.ftpDir.clear();
            this.ftpDir = null;
        }
        if (this.ftpAuth != null) {
            this.ftpAuth.clean();
            this.ftpAuth = null;
        }
        this.businessHandler = null;
        this.configuration = null;
        this.previousCommand = null;
        this.currentCommand = null;
        this.replyCode = null;
        this.answer = null;
        this.isReady = false;
    }

    /**
     * @return True if the Control is ready to accept command
     */
    public boolean isReady() {
        return this.isReady;
    }

    /**
     * @param isReady
     *            the isReady to set
     */
    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    /**
	 * 
	 */
    @Override
    public String toString() {
        String mesg = "FtpSession: ";
        if (this.currentCommand != null) {
            mesg += "CMD: " + this.currentCommand.getCommand() + " " +
                    this.currentCommand.getArg() + " ";
        }
        if (this.replyCode != null) {
            mesg += "Reply: " +
                    (this.answer != null? this.answer : this.replyCode
                            .getMesg()) + " ";
        }
        if (this.dataConn != null) {
            mesg += this.dataConn.toString();
        }
        if (this.ftpDir != null) {
            try {
                mesg += "PWD: " + this.ftpDir.getPwd();
            } catch (FtpCommandAbstractException e) {
            }
        }
        return mesg + "\n";
    }
}
