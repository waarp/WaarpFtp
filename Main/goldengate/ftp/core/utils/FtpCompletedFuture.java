/**
 * 
 */
package goldengate.ftp.core.utils;

import java.util.concurrent.TimeUnit;

/**
 * Future completed
 * @author fbregier
 *
 */
public abstract class FtpCompletedFuture extends FtpFuture {	
	/**
	 */
	protected FtpCompletedFuture() {
		super(false);
	}

    public FtpFuture await() throws InterruptedException {
        return this;
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return true;
    }

    public boolean await(long timeoutMillis) throws InterruptedException {
        return true;
    }

    public FtpFuture awaitUninterruptibly() {
        return this;
    }

    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
        return true;
    }

    public boolean awaitUninterruptibly(long timeoutMillis) {
        return true;
    }

    public boolean isDone() {
        return true;
    }

    public boolean setFailure(Throwable cause) {
        return false;
    }

    public boolean setSuccess() {
        return false;
    }

    public boolean cancel() {
        return false;
    }

    public boolean isCancelled() {
        return false;
    }
}
