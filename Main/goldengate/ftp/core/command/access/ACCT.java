/**
 * Frederic Bregier LGPL 10 janv. 09 USER.java
 * goldengate.ftp.core.command.access GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.access;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpNextCommandReply;
import goldengate.ftp.core.command.exception.Reply421Exception;
import goldengate.ftp.core.command.exception.Reply501Exception;
import goldengate.ftp.core.command.exception.Reply502Exception;
import goldengate.ftp.core.command.exception.Reply530Exception;
import goldengate.ftp.core.utils.FtpCommandUtils;

/**
 * ACCT command
 * 
 * @author frederic goldengate.ftp.core.command.access ACCT
 * 
 */
public class ACCT extends AbstractCommand {

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    @Override
    public void exec() throws Reply501Exception, Reply421Exception,
            Reply530Exception, Reply502Exception {
        if (!this.hasArg()) {
            this.invalidCurrentCommand();
            throw new Reply501Exception("Need an account as argument");
        }
        String account = this.getArg();
        FtpNextCommandReply nextCommandReply;
        try {
            nextCommandReply = this.getFtpSession().getFtpAuth().setAccount(
                    account);
        } catch (Reply530Exception e) {
            FtpCommandUtils.reinitFtpAuth(this.getFtpSession());
            throw e;
        }
        this.setExtraNextCommand(nextCommandReply.command);
        this.getFtpSession().setReplyCode(nextCommandReply.reply,
                nextCommandReply.message);
    }

}
