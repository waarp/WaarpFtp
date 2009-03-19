/**
 * 
 */
package goldengate.ftp.core.utils;

/**
 * Future in failure
 * 
 * @author fbregier
 * 
 */
public class FtpFailedFuture extends FtpCompletedFuture {

    private final Throwable cause;

    /**
     * Creates a new instance.
     * 
     * @param cause
     *            the cause of failure
     */
    public FtpFailedFuture(Throwable cause) {
        super();
        if (cause == null) {
            throw new NullPointerException("cause");
        }
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

}
