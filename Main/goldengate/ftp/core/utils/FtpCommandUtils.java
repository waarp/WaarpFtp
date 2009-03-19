/**
 * 
 */
package goldengate.ftp.core.utils;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.exception.Reply425Exception;
import goldengate.ftp.core.command.internal.ConnectionCommand;
import goldengate.ftp.core.session.FtpSession;

/**
 * Some useful commands to simplify call from several Commands.
 * 
 * @author fbregier
 * 
 */
public class FtpCommandUtils {
    /**
     * Reinitialize the authentication to the connection step
     * 
     * @param session
     */
    public static void reinitFtpAuth(FtpSession session) {
        AbstractCommand connectioncommand = new ConnectionCommand(session);
        session.setNextCommand(connectioncommand);
        session.getFtpAuth().clean();
        session.getDataConn().clear();
    }

    /**
     * Try to open a connection. Do the intermediate reply if any (150) and the
     * final one (125)
     * 
     * @param session
     * @throws Reply425Exception
     *             if the connection cannot be opened
     */
    public static void openDataConnection(FtpSession session)
            throws Reply425Exception {
        session.getDataConn().getFtpTransferControl().openDataConnection();
    }
}
