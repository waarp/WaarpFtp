/**
 * Copyright 2009, Frederic Bregier, and individual contributors
 * by the @author tags. See the COPYRIGHT.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
 * {@link InetSocketAddress} of the server. This is particularly useful for
 * Passive mode connection since there is no way to pass the session to the
 * connected channel without this reference.
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
        public InetAddress remote;

        /**
         * Local Inet Socket Address (with port)
         */
        public InetSocketAddress local;

        /**
         * Constructor from Channel
         *
         * @param channel
         */
        public P2PAddress(Channel channel) {
            remote = FtpChannelUtils.getRemoteInetAddress(channel);
            local = (InetSocketAddress) channel.getLocalAddress();
        }

        /**
         * Constructor from addresses
         *
         * @param address
         * @param inetSocketAddress
         */
        public P2PAddress(InetAddress address,
                InetSocketAddress inetSocketAddress) {
            remote = address;
            local = inetSocketAddress;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object arg0) {
            if (arg0 instanceof P2PAddress) {
                P2PAddress p2paddress = (P2PAddress) arg0;
                return p2paddress.local.equals(local) && p2paddress.remote
                        .equals(remote);
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
            return local.hashCode() + remote.hashCode();
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
     * @param remote
     * @param local
     * @param session
     */
    public void setNewFtpSession(InetAddress remote, InetSocketAddress local,
            FtpSession session) {
        P2PAddress pAddress = new P2PAddress(remote, local);
        hashMap.put(pAddress, session);
        logger.debug("Add: {} {}", remote, local);
    }

    /**
     * Return and remove the FtpSession
     *
     * @param channel
     * @return the FtpSession if it exists associated to this channel
     */
    public FtpSession getActiveFtpSession(Channel channel) {
        // First check passive connection
        P2PAddress pAddress = new P2PAddress(((InetSocketAddress) channel
                .getLocalAddress()).getAddress(), (InetSocketAddress) channel
                .getRemoteAddress());
        logger.debug("Get: {} {}", pAddress.remote, pAddress.local);
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
        logger.debug("Get: {} {}", pAddress.remote, pAddress.local);
        return hashMap.remove(pAddress);
    }

    /**
     * Remove the FtpSession from couple of addresses
     *
     * @param remote
     * @param local
     */
    public void delFtpSession(InetAddress remote, InetSocketAddress local) {
        P2PAddress pAddress = new P2PAddress(remote, local);
        logger.debug("Del: {} {}", pAddress.remote, pAddress.local);
        hashMap.remove(pAddress);
    }
}
