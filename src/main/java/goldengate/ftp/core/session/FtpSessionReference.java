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
package goldengate.ftp.core.session;

import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;
import goldengate.ftp.core.utils.FtpChannelUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.channel.Channel;

/**
 * Class that allows to retrieve a session when a connection occurs on the Data
 * network based on the {@link InetAddress} of the remote client and the
 * {@link InetSocketAddress} of the server for Passive and reverse for Active
 * connections. This is particularly useful for Passive mode connection since
 * there is no way to pass the session to the connected channel without this
 * reference.
 *
 * @author Frederic Bregier
 *
 */
public class FtpSessionReference {
    /**
     * Internal Logger
     */
    private static final GgInternalLogger logger = GgInternalLoggerFactory
            .getLogger(FtpSessionReference.class);

    /**
     * Index of FtpSession References
     *
     * @author Frederic Bregier
     *
     */
    public class P2PAddress {
        /**
         * Remote Inet Address (no port)
         */
        public InetAddress ipOnly;

        /**
         * Local Inet Socket Address (with port)
         */
        public InetSocketAddress fullIp;

        /**
         * Constructor from Channel
         *
         * @param channel
         */
        public P2PAddress(Channel channel) {
            ipOnly = FtpChannelUtils.getRemoteInetAddress(channel);
            fullIp = (InetSocketAddress) channel.getLocalAddress();
        }

        /**
         * Constructor from addresses
         *
         * @param address
         * @param inetSocketAddress
         */
        public P2PAddress(InetAddress address,
                InetSocketAddress inetSocketAddress) {
            ipOnly = address;
            fullIp = inetSocketAddress;
        }

        /**
         *
         * @return True if the P2Paddress is valid
         */
        public boolean isValid() {
            return ipOnly != null && fullIp != null;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object arg0) {
            if (arg0 == null) {
                return false;
            }
            if (arg0 instanceof P2PAddress) {
                P2PAddress p2paddress = (P2PAddress) arg0;
                if (p2paddress.isValid() && isValid()) {
                    return p2paddress.fullIp.equals(fullIp) &&
                            p2paddress.ipOnly.equals(ipOnly);
                }
            }
            return false;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return fullIp.hashCode() + ipOnly.hashCode();
        }

    }

    /**
     * Reference of FtpSession from InetSocketAddress
     */
    private final ConcurrentHashMap<P2PAddress, FtpSession> hashMap = new ConcurrentHashMap<P2PAddress, FtpSession>();

    /**
     * Constructor
     *
     */
    public FtpSessionReference() {
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
        P2PAddress pAddress = new P2PAddress(ipOnly, fullIp);
        if (!pAddress.isValid()) {
            logger.error("Couple invalid in setNewFtpSession: " + ipOnly +
                    " : " + fullIp);
            return;
        }
        hashMap.put(pAddress, session);
        // logger.debug("Add: {} {}", ipOnly, fullIp);
    }

    /**
     * Return and remove the FtpSession
     *
     * @param channel
     * @return the FtpSession if it exists associated to this channel
     */
    public FtpSession getActiveFtpSession(Channel channel) {
        // First check Active connection
        P2PAddress pAddress = new P2PAddress(((InetSocketAddress) channel
                .getLocalAddress()).getAddress(), (InetSocketAddress) channel
                .getRemoteAddress());
        if (!pAddress.isValid()) {
            logger.error("Couple invalid in getActiveFtpSession: " + channel +
                    channel.getLocalAddress() + channel.getRemoteAddress());
            return null;
        }
        // logger.debug("Get: {} {}", pAddress.ipOnly, pAddress.fullIp);
        return hashMap.remove(pAddress);
    }

    /**
     * Return and remove the FtpSession
     *
     * @param channel
     * @return the FtpSession if it exists associated to this channel
     */
    public FtpSession getPassiveFtpSession(Channel channel) {
        // First check passive connection
        P2PAddress pAddress = new P2PAddress(channel);
        if (!pAddress.isValid()) {
            logger.error("Couple invalid in getPassiveFtpSession: " + channel);
            return null;
        }
        // logger.debug("Get: {} {}", pAddress.ipOnly, pAddress.fullIp);
        return hashMap.remove(pAddress);
    }

    /**
     * Remove the FtpSession from couple of addresses
     *
     * @param ipOnly
     * @param fullIp
     */
    public void delFtpSession(InetAddress ipOnly, InetSocketAddress fullIp) {
        P2PAddress pAddress = new P2PAddress(ipOnly, fullIp);
        if (!pAddress.isValid()) {
            logger.error("Couple invalid in delFtpSession: " + ipOnly + " : " +
                    fullIp);
            return;
        }
        // logger.debug("Del: {} {}", pAddress.ipOnly, pAddress.fullIp);
        hashMap.remove(pAddress);
    }

    /**
     * Test if the couple of addresses is already in the hashmap (only for
     * Active)
     *
     * @param ipOnly
     * @param fullIp
     * @return True if already presents
     */
    public boolean contains(InetAddress ipOnly, InetSocketAddress fullIp) {
        P2PAddress pAddress = new P2PAddress(ipOnly, fullIp);
        if (!pAddress.isValid()) {
            logger.error("Couple invalid in contains: " + ipOnly + " : " +
                    fullIp);
            return false;
        }
        // logger.debug("Contains: {} {}", pAddress.ipOnly, pAddress.fullIp);
        return hashMap.containsKey(pAddress);
    }
    /**
     * 
     * @return the number of active sessions
     */
    public int sessionsNumber() {
        return hashMap.size();
    }
}
