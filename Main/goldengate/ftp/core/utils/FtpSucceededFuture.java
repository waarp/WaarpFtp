/**
 * 
 */
package goldengate.ftp.core.utils;


/**
 * Future in success
 * @author fbregier
 *
 */
public class FtpSucceededFuture extends FtpCompletedFuture {

	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.ChannelFuture#getCause()
	 */
	public Throwable getCause() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.ChannelFuture#isSuccess()
	 */
	public boolean isSuccess() {
		return true;
	}

}
