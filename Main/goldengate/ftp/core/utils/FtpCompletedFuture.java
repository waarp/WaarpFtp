/**
 * 
 */
package goldengate.ftp.core.utils;

import java.util.concurrent.TimeUnit;

/**
 * Future completed
 * 
 * @author fbregier
 * 
 */
public abstract class FtpCompletedFuture extends FtpFuture {
    /**
	 */
    protected FtpCompletedFuture() {
        super(false);
    }

    @Override
    public FtpFuture await() throws InterruptedException {
        return this;
    }

    @Override
    public boolean await(long timeout, TimeUnit unit)
            throws InterruptedException {
        return true;
    }

    @Override
    public boolean await(long timeoutMillis) throws InterruptedException {
        return true;
    }

    @Override
    public FtpFuture awaitUninterruptibly() {
        return this;
    }

    @Override
    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
        return true;
    }

    @Override
    public boolean awaitUninterruptibly(long timeoutMillis) {
        return true;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public boolean setFailure(Throwable cause) {
        return false;
    }

    @Override
    public boolean setSuccess() {
        return false;
    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
