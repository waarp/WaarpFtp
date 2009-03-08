/**
 * 
 */
package goldengate.ftp.simpleimpl.data;

import goldengate.ftp.core.data.handler.DataBusinessHandler;
import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ExceptionEvent;

/**
 * DataBusinessHandler implementation based on Simple Filesystem
 * @author fbregier
 *
 */
public class FileSystemBasedDataBusinessHandler extends DataBusinessHandler {
	/**
	 * Internal Logger
	 */
	private static final FtpInternalLogger logger =
        FtpInternalLoggerFactory.getLogger(FileSystemBasedDataBusinessHandler.class);
	
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.data.handler.DataBusinessHandler#cleanSession(goldengate.ftp.core.session.FtpSession)
	 */
	@Override
	protected void cleanSession() {
		logger.debug("FSDBH Clean session");
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.data.handler.DataBusinessHandler#exceptionLocalCaught(org.jboss.netty.channel.ExceptionEvent)
	 */
	@Override
	public void exceptionLocalCaught(ExceptionEvent e) {
		logger.warn("FSDBH Execption",e.getCause());
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.data.handler.DataBusinessHandler#executeChannelClosed()
	 */
	@Override
	public void executeChannelClosed() {
		logger.debug("FSDBH Channel closed");
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.data.handler.DataBusinessHandler#executeChannelConnected(org.jboss.netty.channel.Channel)
	 */
	@Override
	public void executeChannelConnected(Channel channel) {
		logger.debug("FSDBH Channel connected {}",channel);
	}
}
