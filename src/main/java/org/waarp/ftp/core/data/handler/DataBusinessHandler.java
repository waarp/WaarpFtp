/**
 * This file is part of Waarp Project.
 * 
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author tags. See the
 * COPYRIGHT.txt in the distribution for a full listing of individual contributors.
 * 
 * All Waarp Project is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Waarp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Waarp . If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.waarp.ftp.core.data.handler;

import io.netty.channel.Channel;
import org.waarp.ftp.core.session.FtpSession;

/**
 * This class is to be implemented in order to allow Business actions according to FTP service
 * 
 * @author Frederic Bregier
 * 
 */
public abstract class DataBusinessHandler {
    /**
     * NettyHandler that holds this DataBusinessHandler
     */
    private DataNetworkHandler dataNetworkHandler = null;

    /**
     * Ftp SessionInterface
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
        return dataNetworkHandler;
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
        return session;
    }

    /**
     * Is executed when the channel is closed, just before the test on the finish status.
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
    public void clear() {
        cleanSession();
    }

    /**
     * Is executed when the channel is connected after the handler is on, before answering OK or not
     * on connection, except if the global service is going to shutdown.
     * 
     * @param channel
     */
    public abstract void executeChannelConnected(Channel channel);

    /**
     * Run when an exception is get before the channel is closed.
     * 
     * @param e
     */
    public abstract void exceptionLocalCaught(Throwable e);
}
