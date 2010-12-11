/**
   This file is part of GoldenGate Project (named also GoldenGate or GG).

   Copyright 2009, Frederic Bregier, and individual contributors by the @author
   tags. See the COPYRIGHT.txt in the distribution for a full listing of
   individual contributors.

   All GoldenGate Project is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   GoldenGate is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with GoldenGate .  If not, see <http://www.gnu.org/licenses/>.
 */
package goldengate.ftp.core.utils;

import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;
import goldengate.ftp.core.config.FtpConfiguration;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Timer;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.ChannelGroupFutureListener;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

/**
 * Some useful functions related to Channel of Netty
 *
 * @author Frederic Bregier
 *
 */
public class FtpChannelUtils implements Runnable {
    /**
     * Internal Logger
     */
    private static final GgInternalLogger logger = GgInternalLoggerFactory
            .getLogger(FtpChannelUtils.class);

    /**
     * Get the Remote InetAddress
     *
     * @param channel
     * @return the remote InetAddress
     */
    public static InetAddress getRemoteInetAddress(Channel channel) {
        InetSocketAddress socketAddress = (InetSocketAddress) channel
                .getRemoteAddress();
        if (socketAddress == null) {
            socketAddress = new InetSocketAddress(20);
        }
        return socketAddress.getAddress();
    }

    /**
     * Get the Local InetAddress
     *
     * @param channel
     * @return the local InetAddress
     */
    public static InetAddress getLocalInetAddress(Channel channel) {
        InetSocketAddress socketAddress = (InetSocketAddress) channel
                .getLocalAddress();
        return socketAddress.getAddress();
    }

    /**
     * Get the Remote InetSocketAddress
     *
     * @param channel
     * @return the remote InetSocketAddress
     */
    public static InetSocketAddress getRemoteInetSocketAddress(Channel channel) {
        return (InetSocketAddress) channel.getRemoteAddress();
    }

    /**
     * Get the Local InetSocketAddress
     *
     * @param channel
     * @return the local InetSocketAddress
     */
    public static InetSocketAddress getLocalInetSocketAddress(Channel channel) {
        return (InetSocketAddress) channel.getLocalAddress();
    }

    /**
     * Get the InetSocketAddress corresponding to the FTP format of address
     *
     * @param arg
     * @return the InetSocketAddress or null if an error occurs
     */
    public static InetSocketAddress getInetSocketAddress(String arg) {
        String[] elements = arg.split(",");
        if (elements.length != 6) {
            return null;
        }
        byte[] address = new byte[4];
        int[] iElements = new int[6];
        for (int i = 0; i < 6; i ++) {
            try {
                iElements[i] = Integer.parseInt(elements[i]);
            } catch (NumberFormatException e) {
                return null;
            }
            if (iElements[i] < 0 || iElements[i] > 255) {
                return null;
            }
        }
        for (int i = 0; i < 4; i ++) {
            address[i] = (byte) iElements[i];
        }
        int port = iElements[4] << 8 | iElements[5];
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByAddress(address);
        } catch (UnknownHostException e) {
            return null;
        }
        return new InetSocketAddress(inetAddress, port);
    }

    /**
     * Return the Address in the format compatible with FTP argument
     *
     * @param address
     * @return the String representation of the address
     */
    public static String getAddress(InetSocketAddress address) {
        InetAddress servAddr = address.getAddress();
        int servPort = address.getPort();
        return servAddr.getHostAddress().replace('.', ',') + ',' +
                (servPort >> 8) + ',' + (servPort & 0xFF);
    }

    /**
     * Get the (RFC2428) InetSocketAddress corresponding to the FTP format of
     * address (RFC2428)
     *
     * @param arg
     * @return the InetSocketAddress or null if an error occurs
     */
    public static InetSocketAddress get2428InetSocketAddress(String arg) {
        // Format: #a#net-addr#tcp-port# where a = 1 IPV4 or 2 IPV6, other will
        // not be supported
        if (arg == null || arg.length() == 0) {
            // bad args
            return null;
        }
        String delim = arg.substring(0, 1);
        String[] infos = arg.split(delim);
        if (infos.length != 3) {
            // bad format
            return null;
        }
        boolean isIPV4 = true;
        if (infos[0].equals("1")) {
            isIPV4 = true;
        } else if (infos[0].equals("2")) {
            isIPV4 = false;
        } else {
            // not supported
            return null;
        }
        byte[] address = null;
        if (isIPV4) {
            // IPV4
            address = new byte[4];
            String[] elements = infos[1].split(".");
            if (elements.length != 4) {
                return null;
            }
            for (int i = 0; i < 4; i ++) {
                try {
                    address[i] = (byte) Integer.parseInt(elements[i]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        } else {
            // IPV6
            address = new byte[16];
            int[] value = new int[8];
            String[] elements = infos[1].split(":");
            if (elements.length != 8) {
                return null;
            }
            for (int i = 0, j = 0; i < 8; i ++) {
                if (elements[i] == null || elements[i].length() == 0) {
                    value[i] = 0;
                } else {
                    try {
                        value[i] = Integer.parseInt(elements[i]);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                address[j ++] = (byte) (value[i] >> 8);
                address[j ++] = (byte) (value[i] & 0xFF);
            }
        }
        int port = 0;
        try {
            port = Integer.parseInt(infos[2]);
        } catch (NumberFormatException e) {
            return null;
        }
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByAddress(address);
        } catch (UnknownHostException e) {
            return null;
        }
        return new InetSocketAddress(inetAddress, port);
    }

    /**
     * Return the (RFC2428) Address in the format compatible with FTP (RFC2428)
     *
     * @param address
     * @return the String representation of the address
     */
    public static String get2428Address(InetSocketAddress address) {
        InetAddress servAddr = address.getAddress();
        int servPort = address.getPort();
        StringBuilder builder = new StringBuilder();
        String hostaddress = servAddr.getHostAddress();
        builder.append('|');
        if (hostaddress.contains(":")) {
            builder.append('2'); // IPV6
        } else {
            builder.append('1'); // IPV4
        }
        builder.append('|');
        builder.append(hostaddress);
        builder.append('|');
        builder.append(servPort);
        builder.append('|');
        return builder.toString();
    }

    /**
     * Finalize resources attached to Control or Data handlers
     *
     * @author Frederic Bregier
     *
     */
    private static class FtpChannelGroupFutureListener implements
            ChannelGroupFutureListener {
        OrderedMemoryAwareThreadPoolExecutor pool;

        ChannelFactory channelFactory;

        ChannelFactory channelFactory2;

        public FtpChannelGroupFutureListener(
                OrderedMemoryAwareThreadPoolExecutor pool,
                ChannelFactory channelFactory, ChannelFactory channelFactory2) {
            this.pool = pool;
            this.channelFactory = channelFactory;
            this.channelFactory2 = channelFactory2;
        }

        public void operationComplete(ChannelGroupFuture future)
                throws Exception {
            pool.shutdownNow();
            channelFactory.releaseExternalResources();
            if (channelFactory2 != null) {
                channelFactory2.releaseExternalResources();
            }
        }
    }

    /**
     * Terminate all registered command channels
     *
     * @param configuration
     * @return the number of previously registered command channels
     */
    static int terminateCommandChannels(FtpConfiguration configuration) {
        int result = configuration.getFtpInternalConfiguration()
                .getCommandChannelGroup().size();
        configuration.getFtpInternalConfiguration().getCommandChannelGroup()
                .close().addListener(
                        new FtpChannelGroupFutureListener(configuration
                                .getFtpInternalConfiguration()
                                .getPipelineExecutor(), configuration
                                .getFtpInternalConfiguration()
                                .getCommandChannelFactory(), null));
        return result;
    }

    /**
     * Terminate all registered data channels
     *
     * @param configuration
     * @return the number of previously registered data channels
     */
    private static int terminateDataChannels(FtpConfiguration configuration) {
        int result = configuration.getFtpInternalConfiguration()
                .getDataChannelGroup().size();
        configuration.getFtpInternalConfiguration().getDataChannelGroup()
                .close().addListener(
                        new FtpChannelGroupFutureListener(configuration
                                .getFtpInternalConfiguration()
                                .getDataPipelineExecutor(), configuration
                                .getFtpInternalConfiguration()
                                .getDataPassiveChannelFactory(), configuration
                                .getFtpInternalConfiguration()
                                .getDataActiveChannelFactory()));
        return result;
    }

    /**
     * Return the current number of command connections
     *
     * @param configuration
     * @return the current number of command connections
     */
    public static int nbCommandChannels(FtpConfiguration configuration) {
        return configuration.getFtpInternalConfiguration()
                .getCommandChannelGroup().size();
    }

    /**
     * Return the current number of data connections
     *
     * @param configuration
     * @return the current number of data connections
     */
    public static int nbDataChannels(FtpConfiguration configuration) {
        return configuration.getFtpInternalConfiguration()
                .getDataChannelGroup().size();
    }

    /**
     * Return the number of still positive command connections
     *
     * @param configuration
     * @return the number of positive command connections
     */
    public static int validCommandChannels(FtpConfiguration configuration) {
        int result = 0;
        Channel channel = null;
        Iterator<Channel> iterator = configuration
                .getFtpInternalConfiguration().getCommandChannelGroup()
                .iterator();
        while (iterator.hasNext()) {
            channel = iterator.next();
            if (channel.getParent() != null) {
                // Child Channel
                if (channel.isConnected()) {
                    // Normal channel
                    result ++;
                } else {
                    Channels.close(channel);
                }
            } else {
                // Parent channel
                result ++;
            }
        }
        return result;
    }

    /**
     * Exit global ChannelFactory
     *
     * @param configuration
     */
    private static void exit(FtpConfiguration configuration) {
        configuration.isShutdown = true;
        long delay = configuration.TIMEOUTCON;
        logger.warn("Exit: Give a delay of " + delay + " ms");
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
        }
        configuration.getFtpInternalConfiguration()
                .getGlobalTrafficShapingHandler().releaseExternalResources();
        Timer timer = new Timer(true);
        FtpTimerTask timerTask = new FtpTimerTask(FtpTimerTask.TIMER_CONTROL);
        timerTask.configuration = configuration;
        timer.schedule(timerTask, configuration.TIMEOUTCON/2);
        logger.warn("Exit Shutdown Data");
        terminateDataChannels(configuration);
        logger.warn("Exit end of Data Shutdown");
    }

    /**
     * This function is the top function to be called when the server is to be
     * shutdown.
     *
     * @param configuration
     */
    public static void teminateServer(FtpConfiguration configuration) {
        FtpSignalHandler.terminate(true, configuration);
    }

    /**
     * Add a command channel into the list
     *
     * @param channel
     * @param configuration
     */
    public static void addCommandChannel(Channel channel,
            FtpConfiguration configuration) {
        // logger.info("Add Command Channel {}", channel);
        configuration.getFtpInternalConfiguration().getCommandChannelGroup()
                .add(channel);
    }

    /**
     * Add a data channel into the list
     *
     * @param channel
     * @param configuration
     */
    public static void addDataChannel(Channel channel,
            FtpConfiguration configuration) {
        // logger.info("Add Data Channel {}", channel);
        configuration.getFtpInternalConfiguration().getDataChannelGroup().add(
                channel);
    }

    /**
     * Used to run Exit command
     */
    private FtpConfiguration configuration;

    public FtpChannelUtils(FtpConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void run() {
        exit(configuration);
    }

}
