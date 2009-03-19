/**
 * Frederic Bregier LGPL 10 janv. 09 AbstractCommand.java
 * goldengate.ftp.core.command GoldenGateFtp frederic
 */
package goldengate.ftp.core.command;

import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.config.FtpConfiguration;
import goldengate.ftp.core.exception.FtpInvalidArgumentException;
import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;
import goldengate.ftp.core.session.FtpSession;

/**
 * Abstract definition of an FTP Command
 * 
 * @author frederic goldengate.ftp.core.command AbstractCommand
 * 
 */
public abstract class AbstractCommand {
    /**
     * Internal Logger
     */
    private static final FtpInternalLogger logger = FtpInternalLoggerFactory
            .getLogger(AbstractCommand.class);

    /**
     * Code of Command
     */
    private FtpCommandCode code;

    /**
     * String attached to the command
     */
    private String command;

    /**
     * Argument attached to this command
     */
    private String arg;

    /**
     * The Ftp Session
     */
    private FtpSession session;

    /**
     * Internal Object (whatever the used). This has to be clean by Business
     * Handler cleanSession.
     */
    private Object object;

    /**
     * Extra allowed nextCommand
     */
    private FtpCommandCode extraNextCommand = null;

    /**
     * Set the AbstractCommand from the args
     * 
     * @param session
     * @param command
     * @param arg
     * @param code
     */
    public void setArgs(FtpSession session, String command, String arg,
            FtpCommandCode code) {
        this.session = session;
        this.command = command;
        this.arg = arg;
        this.code = code;
    }

    /**
     * Execute the command. This execution must set the replyCode in the session
     * to a correct value before returning.
     * 
     * @exception FtpCommandAbstractException
     *                in case of an FTP Error occurs
     */
    abstract public void exec() throws FtpCommandAbstractException;

    /**
     * This function is intend to allow to force USER->PASS->ACCT->CDW for
     * instance
     * 
     * @param extraNextCommand
     *            the extraNextCommand to set
     */
    public void setExtraNextCommand(FtpCommandCode extraNextCommand) {
        if (extraNextCommand != FtpCommandCode.NOOP) {
            this.extraNextCommand = extraNextCommand;
        } else {
            this.extraNextCommand = null;
        }
    }

    /**
     * This function is called when a new command is received to check if this
     * new command is positive according to the previous command and status.
     * 
     * @param newCommand
     * @return True if this new command is OK, else False
     */
    public boolean isNextCommandValid(AbstractCommand newCommand) {
        Class<? extends AbstractCommand> newClass = newCommand.getClass();
        // Special commands: QUIT ABORT STAT NOP
        if (FtpCommandCode.isSpecialCommand(newCommand.getCode())) {
            logger.debug("VALID since {}", newCommand.command);
            return true;
        }
        if (this.extraNextCommand != null) {
            if (this.extraNextCommand.command == newClass) {
                logger.debug("VALID {} after {} since extra next command",
                        newCommand.command, this.command);
                return true;
            }
            if ((this.code.nextValids != null) &&
                    (this.code.nextValids.length > 0)) {
                for (int i = 0; i < this.code.nextValids.length; i ++) {
                    if (this.code.nextValids[i] == newClass) {
                        logger.debug("VALID {} after {} since next command",
                                newCommand.command, this.command);
                        return true;
                    }
                }
            }
            logger.debug("NOT VALID {} after {}", newCommand.command,
                    this.command);
            return false;
        }
        if ((this.code.nextValids == null) ||
                (this.code.nextValids.length == 0)) {
            // Any command is allowed
            logger.debug("VALID {} after {} since all valid",
                    newCommand.command, this.command);
            return true;
        }
        for (int i = 0; i < this.code.nextValids.length; i ++) {
            if (this.code.nextValids[i] == newClass) {
                logger.debug("VALID {} since next command {}",
                        newCommand.command, this.command);
                return true;
            }
        }
        logger.debug("DEFAULT NOT VALID {} after {}", newCommand.command,
                this.command);
        return false;
    }

    /**
     * @return the object
     */
    public Object getObject() {
        return this.object;
    }

    /**
     * @param object
     *            the object to set
     */
    public void setObject(Object object) {
        this.object = object;
    }

    /**
     * @return the arg
     */
    public String getArg() {
        return this.arg;
    }

    /**
     * 
     * @return the list of arguments
     */
    public String[] getArgs() {
        return this.arg.split(" ");
    }

    /**
     * Get an integer value from argument
     * 
     * @param argx
     * @return the integer
     * @throws FtpInvalidArgumentException
     *             if the argument is not an integer
     */
    public int getValue(String argx) throws FtpInvalidArgumentException {
        int i = 0;
        try {
            i = Integer.parseInt(argx);
        } catch (NumberFormatException e) {
            throw new FtpInvalidArgumentException("Not an integer");
        }
        return i;
    }

    /**
     * @return the command
     */
    public String getCommand() {
        return this.command;
    }

    /**
     * Does this command has an argument
     * 
     * @return True if it has an argument
     */
    public boolean hasArg() {
        return ((this.arg != null) && (this.arg.length() != 0));
    }

    /**
     * 
     * @return the current FtpSession
     */
    public FtpSession getFtpSession() {
        return this.session;
    }

    // some helpful functions
    /**
     * 
     * @return The current configuration object
     */
    public FtpConfiguration getConfiguration() {
        return this.session.getConfiguration();
    }

    /**
     * Set the previous command as the new current command (used after a
     * incorrect sequence of commands or unknown command). Also clear the
     * Restart object.
     * 
     */
    public void invalidCurrentCommand() {
        this.session.getFtpRestart().setSet(false);
        this.session.setPreviousAsCurrentCommand();
    }

    /**
     * 
     * @return The FtpCommandCode associated with this command
     */
    public FtpCommandCode getCode() {
        return this.code;
    }
}
