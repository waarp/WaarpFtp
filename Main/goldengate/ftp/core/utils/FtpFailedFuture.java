/**
 * 
 */
package goldengate.ftp.core.utils;


/**
 * Future in failure
 * @author fbregier
 *
 */
public class FtpFailedFuture extends FtpCompletedFuture {
	
	private final Throwable cause;

    /**
     * Creates a new instance.
     *
     * @param cause   the cause of failure
     */
    public FtpFailedFuture(Throwable cause) {
        super();
        if (cause == null) {
            throw new NullPointerException("cause");
        }
        this.cause = cause;
    }

	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.ChannelFuture#getCause()
	 */
    public Throwable getCause() {
        return cause;
    }

	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.ChannelFuture#isSuccess()
	 */
	public boolean isSuccess() {
		return false;
	}

}
