/**
 * Frederic Bregier LGPL 10 janv. 09 USER.java
 * goldengate.ftp.core.command.access GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.internal;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpCommandCode;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.info.NOOP;
import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;

/**
 * Incorrect command
 * 
 * @author frederic goldengate.ftp.core.command IncorrectCommand (Bad Sequence)
 * 
 */
public class IncorrectCommand extends AbstractCommand {
    /**
     * Internal Logger
     */
    private static final FtpInternalLogger logger = FtpInternalLoggerFactory
            .getLogger(IncorrectCommand.class);

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    @Override
    public void exec() {
        this.getFtpSession().setReplyCode(
                FtpReplyCode.REPLY_503_BAD_SEQUENCE_OF_COMMANDS,
                "Bas sequence of commands: " + this.getCommand() +
                        " following " +
                        this.getFtpSession().getPreviousCommand().getCommand());
        logger.warn(this.getFtpSession().getAnswer());
        if ((this.getFtpSession().getPreviousCommand().getCode() != FtpCommandCode.Connection) &&
                (this.getFtpSession().getPreviousCommand().getCode() != FtpCommandCode.PASS) &&
                (this.getFtpSession().getPreviousCommand().getCode() != FtpCommandCode.USER)) {
            this.getFtpSession().setNextCommand(new NOOP(this.getFtpSession()));
        } else {
            this.invalidCurrentCommand();
        }
    }
}
