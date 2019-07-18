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
package org.waarp.ftp.core.config;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.netty.channel.Channel;
import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;

import org.waarp.common.file.FileParameterInterface;
import org.waarp.common.utility.WaarpShutdownHook.ShutdownConfiguration;
import org.waarp.ftp.core.control.BusinessHandler;
import org.waarp.ftp.core.data.handler.DataBusinessHandler;
import org.waarp.ftp.core.exception.FtpNoConnectionException;
import org.waarp.ftp.core.exception.FtpUnknownFieldException;
import org.waarp.ftp.core.session.FtpSession;

/**
 * Abstract class for configuration
 * 
 * @author Frederic Bregier
 * 
 */
public abstract class FtpConfiguration {
    // FTP Configuration: Externals
    /**
     * Default session limit 64Mbit, so up to 8 full simultaneous clients
     */
    static long DEFAULT_SESSION_LIMIT = 0x800000L;

    /**
     * Default global limit 512Mbit
     */
    static long DEFAULT_GLOBAL_LIMIT = 0x4000000L;

    /**
     * Nb of milliseconds after pending data transfer is in timeout
     */
    private static long DATATIMEOUTCON = 5000;

    /**
     * PASSWORD for SHUTDOWN
     */
    private static final String FTP_PASSWORD = "FTP_PASSWORD";

    // END OF STATIC VALUES
    /**
     * Internal configuration
     */
    private final FtpInternalConfiguration internalConfiguration;

    /**
     * SERVER PORT
     */
    private int SERVER_PORT = 21;
    /**
     * Default Address if any
     */
    private String SERVER_ADDRESS = null;

    /**
     * Base Directory
     */
    private String BASE_DIRECTORY = null;

    /**
     * Associated FileParameterInterface
     */
    private final FileParameterInterface fileParameter;

    /**
     * True if the service is going to shutdown
     */
    private volatile boolean isShutdown = false;

    /**
     * Default number of threads in pool for Server. The default value is for client for Executor in
     * the Pipeline for Business logic. Server will change this value on startup if not set.
     * Default 0 means in proportion of real core number.
     */
    private int SERVER_THREAD = 0;

    /**
     * Default number of threads in pool for Client part.
     */
    private int CLIENT_THREAD = 80;

    /**
     * Which class owns this configuration
     */
    Class<?> fromClass = null;

    /**
     * Which class will be used for DataBusinessHandler
     */
    Class<? extends DataBusinessHandler> dataBusinessHandler = null;

    /**
     * Which class will be used for BusinessHandler
     */
    Class<? extends BusinessHandler> businessHandler = null;

    /**
     * Internal Lock
     */
    private ReentrantLock lock = new ReentrantLock();

    /**
     * Nb of milliseconds after connection is in timeout
     */
    private long TIMEOUTCON = 30000;

    /**
     * Size by default of block size for receive/sending files. Should be a multiple of 8192
     * (maximum = 64K due to block limitation to 2 bytes)
     */
    private int BLOCKSIZE = 0x10000; // 64K

    /**
     * Limit in Write byte/s to apply globally to the FTP Server
     */
    protected long serverGlobalWriteLimit = DEFAULT_GLOBAL_LIMIT;

    /**
     * Limit in Read byte/s to apply globally to the FTP Server
     */
    protected long serverGlobalReadLimit = DEFAULT_GLOBAL_LIMIT;

    /**
     * Limit in Write byte/s to apply by session to the FTP Server
     */
    protected long serverChannelWriteLimit = DEFAULT_SESSION_LIMIT;

    /**
     * Limit in Read byte/s to apply by session to the FTP Server
     */
    protected long serverChannelReadLimit = DEFAULT_SESSION_LIMIT;

    /**
     * Delay in ms between two checks
     */
    protected long delayLimit = 1000;

    /**
     * Should the file be deleted when the transfer is aborted on STOR like commands
     */
    private boolean deleteOnAbort = false;

    /**
     * Max global memory limit: default is 4GB
     */
    private long maxGlobalMemory = 0x100000000L;

    /**
     * General Configuration Object
     */
    private final HashMap<String, Object> properties = new HashMap<String, Object>();

    /**
     * Use by ShutdownHook
     */
    private final ShutdownConfiguration shutdownConfiguration = new ShutdownConfiguration();

    /**
     * Simple constructor
     * 
     * @param classtype
     *            Owner
     * @param businessHandler
     *            class that will be used for BusinessHandler
     * @param dataBusinessHandler
     *            class that will be used for DataBusinessHandler
     * @param fileParameter
     *            the FileParameterInterface to used
     */
    public FtpConfiguration(Class<?> classtype,
            Class<? extends BusinessHandler> businessHandler,
            Class<? extends DataBusinessHandler> dataBusinessHandler,
            FileParameterInterface fileParameter) {
        fromClass = classtype;
        this.dataBusinessHandler = dataBusinessHandler;
        this.businessHandler = businessHandler;
        internalConfiguration = new FtpInternalConfiguration(this);
        this.fileParameter = fileParameter;
    }

    /**
     * 
     * @param key
     * @return The String property associated to the key
     * @throws FtpUnknownFieldException
     */
    public String getStringProperty(String key) throws FtpUnknownFieldException {
        String s = (String) properties.get(key);
        if (s == null) {
            throw new FtpUnknownFieldException("Property has no value: " + key);
        }
        return s;
    }

    /**
     * 
     * @param key
     * @return The Integer property associated to the key
     * @throws FtpUnknownFieldException
     */
    public int getIntProperty(String key) throws FtpUnknownFieldException {
        Integer i = (Integer) properties.get(key);
        if (i == null) {
            throw new FtpUnknownFieldException("Property has no value: " + key);
        }
        return i;
    }

    /**
     * 
     * @param key
     * @return The File associated to the key
     * @throws FtpUnknownFieldException
     */
    public File getFileProperty(String key) throws FtpUnknownFieldException {
        File f = (File) properties.get(key);
        if (f == null) {
            throw new FtpUnknownFieldException("Property has no value: " + key);
        }
        return f;
    }

    /**
     * 
     * @param key
     * @return The Object property associated to the key
     * @throws FtpUnknownFieldException
     */
    public Object getProperty(String key) throws FtpUnknownFieldException {
        Object o = properties.get(key);
        if (o == null) {
            throw new FtpUnknownFieldException("Property has no value: " + key);
        }
        return o;
    }

    /**
     * 
     * @return the TCP Port to listen in the Ftp Server
     */
    public int getServerPort() {
        return SERVER_PORT;
    }

    /**
     * 
     * @return the Address of the Ftp Server if any (may be null)
     */
    public String getServerAddress() {
        return SERVER_ADDRESS;
    }

    /**
     * 
     * @return the limit in Write byte/s to apply globally to the Ftp Server
     */
    public long getServerGlobalWriteLimit() {
        return serverGlobalWriteLimit;
    }

    /**
     * 
     * @return the limit in Write byte/s to apply for each session to the Ftp Server
     */
    public long getServerChannelWriteLimit() {
        return serverChannelWriteLimit;
    }

    /**
     * 
     * @return the limit in Read byte/s to apply globally to the Ftp Server
     */
    public long getServerGlobalReadLimit() {
        return serverGlobalReadLimit;
    }

    /**
     * 
     * @return the limit in Read byte/s to apply for each session to the Ftp Server
     */
    public long getServerChannelReadLimit() {
        return serverChannelReadLimit;
    }

    /**
     * @return the delayLimit to apply between two check
     */
    public long getDelayLimit() {
        return delayLimit;
    }

    /**
     * Check the password for Shutdown
     * 
     * @param password
     * @return True if the password is OK
     */
    public boolean checkPassword(String password) {
        String serverpassword;
        try {
            serverpassword = getStringProperty(FTP_PASSWORD);
        } catch (FtpUnknownFieldException e) {
            return false;
        }
        return serverpassword.equals(password);
    }

    /**
     * Return the next available port for passive connections.
     * 
     * @return the next available Port for Passive connections
     */
    public abstract int getNextRangePort();

    /**
     * 
     * @return the Base Directory of this Ftp Server
     */
    public String getBaseDirectory() {
        return BASE_DIRECTORY;
    }

    /**
     * 
     * @param key
     * @param s
     */
    public void setStringProperty(String key, String s) {
        properties.put(key, s);
    }

    /**
     * 
     * @param key
     * @param i
     */
    public void setIntProperty(String key, int i) {
        properties.put(key, Integer.valueOf(i));
    }

    /**
     * 
     * @param key
     * @param f
     */
    public void setFileProperty(String key, File f) {
        properties.put(key, f);
    }

    /**
     * 
     * @param key
     * @param o
     */
    public void setProperty(String key, Object o) {
        properties.put(key, o);
    }

    /**
     * @param port
     *            the new port
     */
    public void setServerPort(int port) {
        SERVER_PORT = port;
    }

    /**
     * @param address
     *            the address to use while answering for address
     */
    public void setServerAddress(String address) {
        SERVER_ADDRESS = address;
    }

    /**
     * @param dir
     *            the new base directory
     */
    public void setBaseDirectory(String dir) {
        BASE_DIRECTORY = dir;
    }

    /**
     * @param password
     *            the new password for shutdown
     */
    public void setPassword(String password) {
        setStringProperty(FTP_PASSWORD, password);
    }

    /**
     * @return the dataBusinessHandler
     */
    public Class<? extends DataBusinessHandler> getDataBusinessHandler() {
        return dataBusinessHandler;
    }

    /**
     * Init internal configuration
     * 
     * @throws FtpNoConnectionException
     * 
     */
    public void serverStartup() throws FtpNoConnectionException {
        internalConfiguration.serverStartup();
    }

    /**
     * Reset the global monitor for bandwidth limitation and change future channel monitors with
     * values divided by 10 (channel = global / 10)
     * 
     * @param writeLimit
     * @param readLimit
     */
    public void changeNetworkLimit(long writeLimit, long readLimit) {
        long newWriteLimit = writeLimit > 1024 ? writeLimit
                : serverGlobalWriteLimit;
        if (writeLimit <= 0) {
            newWriteLimit = 0;
        }
        long newReadLimit = readLimit > 1024 ? readLimit : serverGlobalReadLimit;
        if (readLimit <= 0) {
            newReadLimit = 0;
        }
        FtpGlobalTrafficShapingHandler fgts = internalConfiguration.getGlobalTrafficShapingHandler();
        fgts.configure(
                newWriteLimit, newReadLimit);
        serverChannelReadLimit = newReadLimit / 10;
        serverChannelWriteLimit = newWriteLimit / 10;
        if (fgts instanceof GlobalChannelTrafficShapingHandler) {
            ((GlobalChannelTrafficShapingHandler) fgts)
                .configureChannel(serverChannelWriteLimit, serverChannelReadLimit);
        }
    }

    /**
     * Compute number of threads for both client and server from the real number of available
     * processors (double + 1) if the value is less than 64 threads.
     * 
     */
    public void computeNbThreads() {
        int nb = Runtime.getRuntime().availableProcessors() * 2 + 1;
        if (nb > 32) {
            nb = Runtime.getRuntime().availableProcessors() + 1;
        }
        if (getSERVER_THREAD() < nb) {
            setSERVER_THREAD(nb);
            setCLIENT_THREAD(getSERVER_THREAD() * 10);
        } else if (getCLIENT_THREAD() < nb) {
            setCLIENT_THREAD(nb * 10);
        }
    }

    /**
     * 
     * @return the lock on configuration
     */
    public Lock getLock() {
        return lock;
    }

    /**
     * In bind/unbind operation, lock
     */
    public void bindLock() {
        lock.lock();
    }

    /**
     * In bind/unbind operation, unlock
     */
    public void bindUnlock() {
        lock.unlock();
    }

    /**
     * 
     * @return the FtpInternalConfiguration
     */
    public FtpInternalConfiguration getFtpInternalConfiguration() {
        return internalConfiguration;
    }

    /**
     * Add a session from a couple of addresses
     * 
     * @param ipOnly
     * @param fullIp
     * @param session
     */
    public void setNewFtpSession(InetAddress ipOnly, InetSocketAddress fullIp,
            FtpSession session) {
        internalConfiguration.setNewFtpSession(ipOnly, fullIp, session);
    }

    /**
     * Return and remove the FtpSession
     * 
     * @param channel
     * @param active
     * @return the FtpSession if it exists associated to this channel
     */
    public FtpSession getFtpSession(Channel channel, boolean active) {
        return internalConfiguration.getFtpSession(channel, active, true);
    }

    /**
     * Return the FtpSession
     * 
     * @param channel
     * @param active
     * @return the FtpSession if it exists associated to this channel
     */
    public FtpSession getFtpSessionNoRemove(Channel channel, boolean active) {
        return internalConfiguration.getFtpSession(channel, active, false);
    }

    /**
     * Remove the FtpSession
     * 
     * @param ipOnly
     * @param fullIp
     */
    public void delFtpSession(InetAddress ipOnly, InetSocketAddress fullIp) {
        internalConfiguration.delFtpSession(ipOnly, fullIp);
    }

    /**
     * Test if the couple of addresses is already in the context
     * 
     * @param ipOnly
     * @param fullIp
     * @return True if the couple is present
     */
    public boolean hasFtpSession(InetAddress ipOnly, InetSocketAddress fullIp) {
        return internalConfiguration.hasFtpSession(ipOnly, fullIp);
    }

    /**
     * @return the fileParameter
     */
    public FileParameterInterface getFileParameter() {
        return fileParameter;
    }

    public String getUniqueExtension() {
        // Can be overridden if necessary
        return ".stou";
    }

    /**
     * To use if any external resources are to be released when shutting down
     */
    public void releaseResources() {
        internalConfiguration.releaseResources();
    }

    /**
     * Shutdown process is on going
     */
    public abstract void inShutdownProcess();

    /**
     * @return the isShutdown
     */
    public boolean isShutdown() {
        return isShutdown;
    }

    /**
     * @param isShutdown the isShutdown to set
     */
    public void setShutdown(boolean isShutdown) {
        this.isShutdown = isShutdown;
    }

    /**
     * @return the sERVER_THREAD
     */
    public int getSERVER_THREAD() {
        return SERVER_THREAD;
    }

    /**
     * @param sERVER_THREAD the sERVER_THREAD to set
     */
    public void setSERVER_THREAD(int sERVER_THREAD) {
        SERVER_THREAD = sERVER_THREAD;
    }

    /**
     * @return the cLIENT_THREAD
     */
    public int getCLIENT_THREAD() {
        return CLIENT_THREAD;
    }

    /**
     * @param cLIENT_THREAD the cLIENT_THREAD to set
     */
    public void setCLIENT_THREAD(int cLIENT_THREAD) {
        CLIENT_THREAD = cLIENT_THREAD;
    }

    /**
     * @return the tIMEOUTCON
     */
    public long getTIMEOUTCON() {
        return TIMEOUTCON;
    }

    /**
     * @param tIMEOUTCON the tIMEOUTCON to set
     */
    public void setTIMEOUTCON(long tIMEOUTCON) {
        TIMEOUTCON = tIMEOUTCON;
    }

    /**
     * @return the bLOCKSIZE
     */
    public int getBLOCKSIZE() {
        return BLOCKSIZE;
    }

    /**
     * @param bLOCKSIZE the bLOCKSIZE to set
     */
    public void setBLOCKSIZE(int bLOCKSIZE) {
        BLOCKSIZE = bLOCKSIZE;
    }

    /**
     * @return the deleteOnAbort
     */
    public boolean isDeleteOnAbort() {
        return deleteOnAbort;
    }

    /**
     * @param deleteOnAbort the deleteOnAbort to set
     */
    public void setDeleteOnAbort(boolean deleteOnAbort) {
        this.deleteOnAbort = deleteOnAbort;
    }

    /**
     * @return the dATATIMEOUTCON
     */
    public static long getDATATIMEOUTCON() {
        return DATATIMEOUTCON;
    }

    /**
     * @param dATATIMEOUTCON the dATATIMEOUTCON to set
     */
    public static void setDATATIMEOUTCON(long dATATIMEOUTCON) {
        DATATIMEOUTCON = dATATIMEOUTCON;
    }

    /**
     * @return the maxGlobalMemory
     */
    public long getMaxGlobalMemory() {
        return maxGlobalMemory;
    }

    /**
     * @param maxGlobalMemory the maxGlobalMemory to set
     */
    public void setMaxGlobalMemory(long maxGlobalMemory) {
        this.maxGlobalMemory = maxGlobalMemory;
    }

    /**
     * @return the shutdownConfiguration
     */
    public ShutdownConfiguration getShutdownConfiguration() {
        return shutdownConfiguration;
    }
}
