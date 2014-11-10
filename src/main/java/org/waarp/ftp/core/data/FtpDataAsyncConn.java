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
package org.waarp.ftp.core.data;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import io.netty.channel.Channel;

import org.waarp.common.command.exception.Reply425Exception;
import org.waarp.common.crypto.ssl.WaarpSslUtility;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.ftp.core.command.FtpArgumentCode;
import org.waarp.ftp.core.command.FtpArgumentCode.TransferMode;
import org.waarp.ftp.core.command.FtpArgumentCode.TransferStructure;
import org.waarp.ftp.core.command.FtpArgumentCode.TransferType;
import org.waarp.ftp.core.config.FtpConfiguration;
import org.waarp.ftp.core.data.handler.DataNetworkHandler;
import org.waarp.ftp.core.exception.FtpNoConnectionException;
import org.waarp.ftp.core.session.FtpSession;
import org.waarp.ftp.core.utils.FtpChannelUtils;

/**
 * Main class that handles Data connection using asynchronous connection with Netty
 * 
 * @author Frederic Bregier
 * 
 */
public class FtpDataAsyncConn {
    /**
     * Internal Logger
     */
    private static final WaarpLogger logger = WaarpLoggerFactory.getLogger(FtpDataAsyncConn.class);
    /**
     * SessionInterface
     */
    private final FtpSession session;

    /**
     * Current Data Network Handler
     */
    private volatile DataNetworkHandler dataNetworkHandler = null;

    /**
     * Data Channel with the client
     */
    private volatile Channel dataChannel = null;

    /**
     * External address of the client (active)
     */
    private volatile InetSocketAddress remoteAddress = null;

    /**
     * Local listening address for the server (passive)
     */
    private volatile InetSocketAddress localAddress = null;

    /**
     * Active: the connection is done from the Server to the Client on this remotePort Passive: not
     * used
     */
    private volatile int remotePort = -1;

    /**
     * Active: the connection is done from the Server from this localPort to the Client Passive: the
     * connection is done from the Client to the Server on this localPort
     */
    private volatile int localPort = -1;

    /**
     * Is the connection passive
     */
    private volatile boolean passiveMode = false;

    /**
     * Is the server binded (active or passive, but mainly passive)
     */
    private volatile boolean isBind = false;

    /**
     * The FtpTransferControl
     */
    private final FtpTransferControl transferControl;

    /**
     * Current TransferType. Default ASCII
     */
    private volatile FtpArgumentCode.TransferType transferType = FtpArgumentCode.TransferType.ASCII;

    /**
     * Current TransferSubType. Default NONPRINT
     */
    private volatile FtpArgumentCode.TransferSubType transferSubType = FtpArgumentCode.TransferSubType.NONPRINT;

    /**
     * Current TransferStructure. Default FILE
     */
    private volatile FtpArgumentCode.TransferStructure transferStructure = FtpArgumentCode.TransferStructure.FILE;

    /**
     * Current TransferMode. Default Stream
     */
    private volatile FtpArgumentCode.TransferMode transferMode = FtpArgumentCode.TransferMode.STREAM;

    /**
     * Constructor for Active session by default
     * 
     * @param session
     */
    public FtpDataAsyncConn(FtpSession session) {
        this.session = session;
        dataChannel = null;
        remoteAddress = FtpChannelUtils.getRemoteInetSocketAddress(this.session.getControlChannel());
        remotePort = remoteAddress.getPort();
        setDefaultLocalPort();
        resetLocalAddress();
        passiveMode = false;
        isBind = false;
        transferControl = new FtpTransferControl(session);
    }

    /**
     * 
     * @param channel
     * @return True if the given channel is the same as the one currently registered
     */
    public boolean checkCorrectChannel(Channel channel) {
        if (this.dataChannel == null || channel == null) {
            return false;
        }
        return dataChannel.compareTo(channel) == 0;
    }

    /**
     * Clear the Data Connection
     * 
     */
    public void clear() {
        unbindPassive();
        transferControl.clear();
        passiveMode = false;
        remotePort = -1;
        localPort = -1;
    }

    /**
     * Set the local port to the default (20)
     * 
     */
    private void setDefaultLocalPort() {
        setLocalPort(session.getConfiguration().getServerPort() - 1);
        // Default L-1
    }

    /**
     * Set the Local Port (Active or Passive)
     * 
     * @param localPort
     */
    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    /**
     * @return the local address
     */
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    /**
     * @return the remote address
     */
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * @return the remotePort
     */
    public int getRemotePort() {
        return remotePort;
    }

    /**
     * @return the localPort
     */
    public int getLocalPort() {
        return localPort;
    }

    private void resetLocalAddress() {
        localAddress = new InetSocketAddress(FtpChannelUtils
                .getLocalInetAddress(session.getControlChannel()), localPort);
    }

    /**
     * Change to active connection (reset localPort to default)
     * 
     * @param address
     *            remote address
     */
    public void setActive(InetSocketAddress address) {
        unbindPassive();
        setDefaultLocalPort();
        resetLocalAddress();
        remoteAddress = address;
        passiveMode = false;
        isBind = false;
        remotePort = remoteAddress.getPort();
        logger.debug("SetActive: " + this);
    }

    /**
     * Change to passive connection (all necessaries informations like local port should have been
     * set)
     */
    public void setPassive() {
        unbindPassive();
        resetLocalAddress();
        passiveMode = true;
        isBind = false;
        logger.debug("SetPassive: " + this);
    }

    /**
     * @return the passiveMode
     */
    public boolean isPassiveMode() {
        return passiveMode;
    }

    /**
     * 
     * @return True if the connection is bind (active = connected, passive = not necessarily
     *         connected)
     */
    public boolean isBind() {
        return isBind;
    }

    /**
     * Is the Data dataChannel connected
     * 
     * @return True if the dataChannel is connected
     */
    public boolean isActive() {
        return dataChannel != null && dataChannel.isActive();
    }

    /**
     * @return the transferMode
     */
    public FtpArgumentCode.TransferMode getMode() {
        return transferMode;
    }

    /**
     * @param transferMode
     *            the transferMode to set
     */
    public void setMode(FtpArgumentCode.TransferMode transferMode) {
        this.transferMode = transferMode;
        setCorrectCodec();
    }

    /**
     * @return the transferStructure
     */
    public FtpArgumentCode.TransferStructure getStructure() {
        return transferStructure;
    }

    /**
     * @param transferStructure
     *            the transferStructure to set
     */
    public void setStructure(FtpArgumentCode.TransferStructure transferStructure) {
        this.transferStructure = transferStructure;
        setCorrectCodec();
    }

    /**
     * @return the transferSubType
     */
    public FtpArgumentCode.TransferSubType getSubType() {
        return transferSubType;
    }

    /**
     * @param transferSubType
     *            the transferSubType to set
     */
    public void setSubType(FtpArgumentCode.TransferSubType transferSubType) {
        this.transferSubType = transferSubType;
        setCorrectCodec();
    }

    /**
     * @return the transferType
     */
    public FtpArgumentCode.TransferType getType() {
        return transferType;
    }

    /**
     * @param transferType
     *            the transferType to set
     */
    public void setType(FtpArgumentCode.TransferType transferType) {
        this.transferType = transferType;
        setCorrectCodec();
    }

    /**
     * 
     * @return True if the current mode for data connection is FileInterface + (Stream or Block) +
     *         (Ascii or Image)
     */
    public boolean isFileStreamBlockAsciiImage() {
        return transferStructure == TransferStructure.FILE &&
                (transferMode == TransferMode.STREAM || transferMode == TransferMode.BLOCK) &&
                (transferType == TransferType.ASCII || transferType == TransferType.IMAGE);
    }

    /**
     * 
     * @return True if the current mode for data connection is Stream
     */
    public boolean isStreamFile() {
        return transferMode == TransferMode.STREAM &&
                transferStructure == TransferStructure.FILE;
    }

    /**
     * This function must be called after any changes of parameters, ie after MODE, STRU, TYPE
     * 
     */
    private void setCorrectCodec() {
        try {
            getDataNetworkHandler().setCorrectCodec();
        } catch (FtpNoConnectionException e) {
        }
    }

    /**
     * Unbind passive connection when close the Data Channel (from channelInactive())
     * 
     */
    public void unbindPassive() {
        if (isBind && passiveMode) {
            isBind = false;
            InetSocketAddress local = getLocalAddress();
            if (dataChannel != null && dataChannel.isActive()) {
                WaarpSslUtility.closingSslChannel(dataChannel);
            }
            session.getConfiguration().getFtpInternalConfiguration()
                    .unbindPassive(local);
            // Previous mode was Passive so remove the current configuration if
            // any
            InetAddress remote = remoteAddress.getAddress();
            session.getConfiguration().delFtpSession(remote, local);
        }
        dataChannel = null;
        dataNetworkHandler = null;
    }

    /**
     * Initialize the socket from Server side (only used in Passive)
     * 
     * @return True if OK
     * @throws Reply425Exception
     */
    public boolean initPassiveConnection() throws Reply425Exception {
        unbindPassive();
        if (passiveMode) {
            // Connection is enable but the client will do the real connection
            session.getConfiguration().getFtpInternalConfiguration()
                    .bindPassive(getLocalAddress(), session.isDataSsl());
            isBind = true;
            return true;
        }
        // Connection is already prepared
        return true;
    }

    /**
     * Return the current Data Channel
     * 
     * @return the current Data Channel
     * @throws FtpNoConnectionException
     */
    public Channel getCurrentDataChannel() throws FtpNoConnectionException {
        if (dataChannel == null) {
            throw new FtpNoConnectionException("No Data Connection active");
        }
        return dataChannel;
    }

    /**
     * 
     * @return the DataNetworkHandler
     * @throws FtpNoConnectionException
     */
    public DataNetworkHandler getDataNetworkHandler()
            throws FtpNoConnectionException {
        if (dataNetworkHandler == null) {
            throw new FtpNoConnectionException("No Data Connection active");
        }
        return dataNetworkHandler;
    }

    /**
     * 
     * @param dataNetworkHandler
     *            the {@link DataNetworkHandler} to set
     */
    public void setDataNetworkHandler(DataNetworkHandler dataNetworkHandler) {
        this.dataNetworkHandler = dataNetworkHandler;
    }

    /**
     * 
     * @param configuration
     * @return a new Passive Port
     */
    public static int getNewPassivePort(FtpConfiguration configuration) {
        return configuration.getNextRangePort();
    }

    /**
     * @return The current status in String of the different parameters
     */
    public String getStatus() {
        StringBuilder builder = new StringBuilder("Data connection: ")
                .append((isActive() ? "connected " : "not connected "))
                .append((isBind() ? "bind " : "not bind "))
                .append((isPassiveMode() ? "passive mode" : "active mode"))
                .append('\n')
                .append("Mode: ").append(transferMode.name()).append(" localPort: ")
                .append(getLocalPort()).append(" remotePort: ").append(getRemotePort()).append('\n')
                .append("Structure: ").append(transferStructure.name()).append('\n')
                .append("Type: ").append(transferType.name()).append(' ').append(transferSubType.name());
        return builder.toString();
    }

    /**
	 *
	 */
    @Override
    public String toString() {
        return getStatus().replace('\n', ' ');
    }

    /**
     * 
     * @return the FtpTransferControl
     */
    public FtpTransferControl getFtpTransferControl() {
        return transferControl;
    }

    /**
     * Set the new connected Data Channel
     * 
     * @param dataChannel
     *            the new Data Channel
     * @throws InterruptedException
     * @throws Reply425Exception
     */
    public void setNewOpenedDataChannel(Channel dataChannel)
            throws InterruptedException, Reply425Exception {
        this.dataChannel = dataChannel;
        if (dataChannel == null) {
            String curmode = null;
            if (isPassiveMode()) {
                curmode = "passive";
            } else {
                curmode = "active";
            }
            // Cannot open connection
            throw new Reply425Exception("Cannot open " + curmode +
                    " data connection");
        }
        isBind = true;
    }
}
