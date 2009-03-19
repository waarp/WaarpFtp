/**
 * 
 */
package goldengate.ftp.core.utils;

/**
 * Future in success
 * 
 * @author fbregier
 * 
 */
public class FtpSucceededFuture extends FtpCompletedFuture {

    @Override
    public Throwable getCause() {
        return null;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

}
