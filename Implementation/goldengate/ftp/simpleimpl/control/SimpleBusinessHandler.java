/**
 * 
 */
package goldengate.ftp.simpleimpl.control;

import goldengate.ftp.core.auth.FtpAuth;
import goldengate.ftp.core.command.FtpCommandCode;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.command.exception.Reply502Exception;
import goldengate.ftp.core.control.BusinessHandler;
import goldengate.ftp.core.data.FtpTransfer;
import goldengate.ftp.core.file.FtpDir;
import goldengate.ftp.core.file.FtpRestart;
import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;
import goldengate.ftp.filesystembased.FilesystemBasedFtpRestart;
import goldengate.ftp.simpleimpl.auth.FileBasedAuth;
import goldengate.ftp.simpleimpl.file.FileBasedDir;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ExceptionEvent;

/**
 * BusinessHandler implementation that allows pre and post actions on any
 * operations and specifically on transfer operations
 * 
 * @author fbregier
 * 
 */
public class SimpleBusinessHandler extends BusinessHandler {
    /**
     * Internal Logger
     */
    private static final FtpInternalLogger logger = FtpInternalLoggerFactory
            .getLogger(SimpleBusinessHandler.class);

    /*
     * (non-Javadoc)
     * 
     * @see
     * goldengate.ftp.core.control.BusinessHandler#afterRunCommandKo(goldengate
     * .ftp.core.command.exception.FtpCommandAbstractException)
     */
    @Override
    public void afterRunCommandKo(FtpCommandAbstractException e) {
        // TODO Auto-generated method stub
        logger.warn("GBBH: AFTKO: {} {}", this.getFtpSession(), e.getMessage());
    }

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.control.BusinessHandler#afterRunCommandOk()
     */
    @Override
    public void afterRunCommandOk() throws FtpCommandAbstractException {
        // TODO Auto-generated method stub
        logger.info("GBBH: AFTOK: {}", this.getFtpSession());
    }

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.control.BusinessHandler#beforeRunCommand()
     */
    @Override
    public void beforeRunCommand() throws FtpCommandAbstractException {
        // TODO Auto-generated method stub
        logger.info("GBBH: BEFCD: {}", this.getFtpSession());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * goldengate.ftp.core.control.BusinessHandler#cleanSession(goldengate.ftp
     * .core.session.FtpSession)
     */
    @Override
    protected void cleanSession() {
        // TODO Auto-generated method stub
        logger.info("GBBH: CLNSE: {}", this.getFtpSession());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * goldengate.ftp.core.control.BusinessHandler#exceptionLocalCaught(org.
     * jboss.netty.channel.ExceptionEvent)
     */
    @Override
    public void exceptionLocalCaught(ExceptionEvent e) {
        // TODO Auto-generated method stub
        logger.warn("GBBH: EXCEP: {} {}", this.getFtpSession(), e.getCause()
                .getMessage());
    }

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.control.BusinessHandler#executeChannelClosed()
     */
    @Override
    public void executeChannelClosed() {
        // TODO Auto-generated method stub
        logger.warn("GBBH: CLOSED: for user {} with session {} ", this
                .getFtpSession().getFtpAuth().getUser(), this.getFtpSession());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * goldengate.ftp.core.control.BusinessHandler#executeChannelConnected(org
     * .jboss.netty.channel.Channel)
     */
    @Override
    public void executeChannelConnected(Channel channel) {
        // TODO Auto-generated method stub
        logger.info("GBBH: CONNEC: {}", this.getFtpSession());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * goldengate.ftp.core.control.BusinessHandler#getBusinessNewAuth(goldengate
     * .ftp.core.config.FtpConfiguration)
     */
    @Override
    public FtpAuth getBusinessNewAuth() {
        return new FileBasedAuth(this.getFtpSession());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * goldengate.ftp.core.control.BusinessHandler#getBusinessNewFtpDir(goldengate
     * .ftp.core.auth.FtpAuth)
     */
    @Override
    public FtpDir getBusinessNewFtpDir() {
        return new FileBasedDir(this.getFtpSession());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * goldengate.ftp.core.control.BusinessHandler#getBusinessNewFtpRestart(
     * goldengate.ftp.core.session.FtpSession)
     */
    @Override
    public FtpRestart getBusinessNewFtpRestart() {
        return new FilesystemBasedFtpRestart(this.getFtpSession());
    }

    @Override
    public void afterTransferDone(FtpTransfer transfer) {
        if (transfer.getCommand() == FtpCommandCode.APPE) {
            logger.info("GBBH: Transfer: {} " + transfer.getStatus() + " {}",
                    transfer.getCommand(), transfer.getPath());
        } else if (transfer.getCommand() == FtpCommandCode.RETR) {
            logger.info("GBBH: Transfer: {} " + transfer.getStatus() + " {}",
                    transfer.getCommand(), transfer.getPath());
        } else if (transfer.getCommand() == FtpCommandCode.STOR) {
            logger.info("GBBH: Transfer: {} " + transfer.getStatus() + " {}",
                    transfer.getCommand(), transfer.getPath());
        } else if (transfer.getCommand() == FtpCommandCode.STOU) {
            logger.info("GBBH: Transfer: {} " + transfer.getStatus() + " {}",
                    transfer.getCommand(), transfer.getPath());
        } else {
            logger.warn("GBBH: Transfer unknown: {} " + transfer.getStatus() +
                    " {}", transfer.getCommand(), transfer.getPath());
            // Nothing to do
        }
    }

    @Override
    public String getHelpMessage(String arg) {
        return "This FTP server is only intend as a Gateway.\n"
                + "This FTP server refers to RFC 959, 775, 2389, 2428, 3659 and supports XCRC, XMD5 and XSHA1 commands.\n"
                + "XCRC, XMD5 and XSHA1 take a simple filename as argument and return \"250 digest-value is the digest of filename\".";
    }

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.control.BusinessHandler#getFeatMessage()
     */
    @Override
    public String getFeatMessage() {
        StringBuilder builder = new StringBuilder("Extensions supported:");
        builder.append('\n');
        builder.append(this.getDefaultFeatMessage());
        builder.append("\nEnd");
        return builder.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * goldengate.ftp.core.control.BusinessHandler#getOptsMessage(java.lang.
     * String[])
     */
    @Override
    public String getOptsMessage(String[] args)
            throws FtpCommandAbstractException {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase(FtpCommandCode.MLST.name()) ||
                    args[0].equalsIgnoreCase(FtpCommandCode.MLSD.name())) {
                return this.getMLSxOptsMessage(args);
            }
            throw new Reply502Exception("OPTS not implemented for " + args[0]);
        }
        throw new Reply502Exception("OPTS not implemented");
    }
}
