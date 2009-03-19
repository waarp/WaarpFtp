/**
 * 
 */
package goldengate.ftp.core.file;

import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.session.FtpSession;

/**
 * Restart object that implements the REST command.<br>
 * Note that if necessary, according to the implementation of {@link FtpDir} and
 * {@link FtpFile}, one could want to implement a way to store or retrieve
 * Marker from/to the client specification.
 * 
 * @author fbregier
 * 
 */
public abstract class FtpRestart {
    /**
     * Ftp Session
     */
    private final FtpSession session;

    /**
     * Is the current Restart object in context set
     */
    private boolean isSet = false;

    /**
     * Default constructor
     * 
     * @param session
     */
    protected FtpRestart(FtpSession session) {
        this.isSet = false;
        this.session = session;
    }

    /**
     * @return the isSet
     */
    protected boolean isSet() {
        return this.isSet;
    }

    /**
     * @param isSet
     *            the isSet to set
     */
    public void setSet(boolean isSet) {
        this.isSet = isSet;
    }

    /**
     * @return the session
     */
    protected FtpSession getFtpSession() {
        return this.session;
    }

    /**
     * Restart from a Marker for the next File
     * 
     * @param marker
     * @return True if the Marker is OK
     * @exception FtpCommandAbstractException
     */
    public abstract boolean restartMarker(String marker)
            throws FtpCommandAbstractException;
    // FIXME Additionally the implementation should implement a way to get the
    // values
}
