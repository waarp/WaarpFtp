/**
 * Frederic Bregier LGPL 10 janv. 09 PORT.java
 * goldengate.ftp.core.command.access GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.info;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpCommandCode;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.session.FtpSession;

/**
 * NOOP command
 * 
 * @author frederic goldengate.ftp.core.command.service NOOP
 * 
 */
public class NOOP extends AbstractCommand {
    /**
     * Constructor for empty NOOP
     * 
     * @param session
     */
    public NOOP(FtpSession session) {
        super();
        this.setArgs(session, FtpCommandCode.NOOP.name(), null,
                FtpCommandCode.NOOP);
    }

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    @Override
    public void exec() {
        this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_200_COMMAND_OKAY,
                null);
    }

}
