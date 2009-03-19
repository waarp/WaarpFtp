/**
 * Frederic Bregier LGPL 10 janv. 09 USER.java
 * goldengate.ftp.core.command.access GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.access;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpNextCommandReply;
import goldengate.ftp.core.command.exception.Reply421Exception;
import goldengate.ftp.core.command.exception.Reply501Exception;
import goldengate.ftp.core.command.exception.Reply530Exception;
import goldengate.ftp.core.utils.FtpCommandUtils;

/**
 * USER command
 * 
 * @author frederic goldengate.ftp.core.command.access USER
 * 
 */
public class USER extends AbstractCommand {

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    @Override
    public void exec() throws Reply501Exception, Reply421Exception,
            Reply530Exception {
        if (!this.hasArg()) {
            FtpCommandUtils.reinitFtpAuth(this.getFtpSession());
            throw new Reply501Exception("Need a username as argument");
        }
        String username = this.getArg();
        FtpNextCommandReply nextCommandReply;
        try {
            nextCommandReply = this.getFtpSession().getFtpAuth().setUser(
                    username);
        } catch (Reply530Exception e) {
            FtpCommandUtils.reinitFtpAuth(this.getFtpSession());
            throw e;
        }
        this.setExtraNextCommand(nextCommandReply.command);
        this.getFtpSession().setReplyCode(nextCommandReply.reply,
                nextCommandReply.message);
    }

}
