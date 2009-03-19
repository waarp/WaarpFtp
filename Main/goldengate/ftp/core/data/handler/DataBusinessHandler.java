/**
 * Frederic Bregier LGPL 10 janv. 09 DataBusinessHandler.java
 * goldengate.ftp.core.control GoldenGateFtp frederic
 */
package goldengate.ftp.core.data.handler;

import goldengate.ftp.core.session.FtpSession;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ExceptionEvent;

/**
 * This class is to be implemented in order to allow Business actions according
 * to FTP service
 * 
 * @author frederic goldengate.ftp.core.control DataBusinessHandler
 * 
 */
public abstract class DataBusinessHandler {
    /**
     * NettyHandler that holds this DataBusinessHandler
     */
    private DataNetworkHandler dataNetworkHandler = null;

    /**
     * Ftp Session
     */
    private FtpSession session = null;

    /**
     * Constructor with no argument (mandatory)
     * 
     */
    public DataBusinessHandler() {
        // nothing to do
    }

    /**
     * Call when the DataNetworkHandler is created
     * 
     * @param dataNetworkHandler
     *            the dataNetworkHandler to set
     */
    public void setDataNetworkHandler(DataNetworkHandler dataNetworkHandler) {
        this.dataNetworkHandler = dataNetworkHandler;
    }

    /**
     * @return the dataNetworkHandler
     */
    public DataNetworkHandler getDataNetworkHandler() {
        return this.dataNetworkHandler;
    }

    /**
     * Called when the connection is opened
     * 
     * @param session
     *            the session to set
     */
    public void setFtpSession(FtpSession session) {
        this.session = session;
    }

    // Some helpful functions
    /**
     * 
     * @return the ftpSession
     */
    public FtpSession getFtpSession() {
        return this.session;
    }

    /**
     * Is executed when the channel is closed, just before the test on the
     * finish status.
     */
    public abstract void executeChannelClosed();

    /**
     * To Clean the session attached objects for Data Network
     */
    protected abstract void cleanSession();

    /**
     * Clean the DataBusinessHandler
     * 
     */
    public void clean() {
        this.cleanSession();
        this.dataNetworkHandler = null;
    }

    /**
     * Is executed when the channel is connected after the handler is on, before
     * answering OK or not on connection, except if the global service is going
     * to shutdown.
     * 
     * @param channel
     */
    public abstract void executeChannelConnected(Channel channel);

    /**
     * Run when an exception is get before the channel is closed.
     * 
     * @param e
     */
    public abstract void exceptionLocalCaught(ExceptionEvent e);
}
