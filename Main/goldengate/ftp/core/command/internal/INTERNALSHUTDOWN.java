/**
 * Frederic Bregier LGPL 10 janv. 09 USER.java
 * goldengate.ftp.core.command.access GoldenGateFtp frederic
 */
package goldengate.ftp.core.command.internal;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.Reply500Exception;
import goldengate.ftp.core.command.exception.Reply501Exception;
import goldengate.ftp.core.config.FtpConfiguration;
import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;
import goldengate.ftp.core.utils.FtpChannelUtils;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.Channels;

/**
 * Internal shutdown command that will shutdown the FTP service with a password
 * 
 * @author frederic goldengate.ftp.core.command INTERNALSHUTDOWN
 * 
 */
public class INTERNALSHUTDOWN extends AbstractCommand {
    /**
     * Internal Logger
     */
    private static final FtpInternalLogger logger = FtpInternalLoggerFactory
            .getLogger(INTERNALSHUTDOWN.class);
    /**
     * 
     * @author frederic
     *
     */
    private class ShutdownChannelFutureListener implements ChannelFutureListener {

        private final FtpConfiguration configuration;
        protected ShutdownChannelFutureListener(FtpConfiguration configuration) {
            this.configuration = configuration;
        }
        /* (non-Javadoc)
         * @see org.jboss.netty.channel.ChannelFutureListener#operationComplete(org.jboss.netty.channel.ChannelFuture)
         */
        @Override
        public void operationComplete(ChannelFuture arg0) throws Exception {
            Channels.close(arg0.getChannel());
            FtpChannelUtils.teminateServer(this.configuration);
            
        }
        
    }
    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.command.AbstractCommand#exec()
     */
    @Override
    public void exec() throws Reply501Exception, Reply500Exception {
        if (!this.getFtpSession().getFtpAuth().isAdmin()) {
            // not admin
            throw new Reply500Exception("Command Not Allowed");
        }
        if (!this.hasArg()) {
            throw new Reply501Exception("Shutdown Need password");
        }
        String password = this.getArg();
        if (!this.getConfiguration().checkPassword(password)) {
            throw new Reply501Exception("Shutdown Need a correct password");
        }
        logger.warn("Shutdown...");
        this.getFtpSession().setReplyCode(
                FtpReplyCode.REPLY_221_CLOSING_CONTROL_CONNECTION,
                "System shutdown");
        this.getFtpSession().getNetworkHandler().writeIntermediateAnswer()
            .addListener(new ShutdownChannelFutureListener(this.getConfiguration()));
    }

}
