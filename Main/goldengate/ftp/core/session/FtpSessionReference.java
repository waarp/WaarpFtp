/**
 * 
 */
package goldengate.ftp.core.session;


import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;
import goldengate.ftp.core.utils.FtpChannelUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.channel.Channel;

/**
 * Class that allows to retrieve a session when a connection occurs
 * on the Data network based on the {@link InetAddress} of the remote client
 * and the {@link InetSocketAddress} of the server. This is particularly useful
 * for Passive mode connection since there is no way to pass the session
 * to the connected channel without this reference.
 * @author fbregier
 *
 */
public class FtpSessionReference {
	/**
	 * Internal Logger
	 */
	private static final FtpInternalLogger logger =
        FtpInternalLoggerFactory.getLogger(FtpSessionReference.class);
	/**
	 * Index of FtpSession References
	 * @author fbregier
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
		 * @param channel
		 */
		public P2PAddress(Channel channel) {
			this.remote = FtpChannelUtils.getRemoteInetAddress(channel);
			this.local = (InetSocketAddress) channel.getLocalAddress();
		}
		/**
		 * Constructor from addresses
		 * @param address
		 * @param inetSocketAddress
		 */
		public P2PAddress(InetAddress address, InetSocketAddress inetSocketAddress) {
			this.remote = address;
			this.local = inetSocketAddress;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object arg0) {
			if (arg0 instanceof P2PAddress) {
				P2PAddress p2paddress = (P2PAddress) arg0;
				return ((p2paddress.local.equals(this.local)) &&
						(p2paddress.remote.equals(this.remote)));
			}
			return false;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return this.local.hashCode()+this.remote.hashCode();
		}
		
	}
	/**
	 * Reference of FtpSession from InetSocketAddress
	 */
	private final ConcurrentHashMap<P2PAddress, FtpSession> hashMap =
		new ConcurrentHashMap<P2PAddress, FtpSession>();
	/**
	 * Constructor
	 *
	 */
	public FtpSessionReference() {
	}
	/**
	 * Add a session from a couple of addresses
	 * @param remote
	 * @param local
	 * @param session
	 */
	public void setNewFtpSession(InetAddress remote, InetSocketAddress local, FtpSession session) {
		P2PAddress pAddress = new P2PAddress(remote, local);
		this.hashMap.put(pAddress, session);
		logger.debug("Add: {} {}",remote,local);
	}
	/**
	 * Return and remove the FtpSession
	 * @param channel
	 * @return the FtpSession if it exists associated to this channel
	 */
	public FtpSession getFtpSession(Channel channel) {
		P2PAddress pAddress = new P2PAddress(channel);
		logger.debug("Get: {} {}",pAddress.remote,pAddress.local);
		return this.hashMap.remove(pAddress);
	}
	/**
	 * Remove the FtpSession from couple of addresses
	 * @param remote
	 * @param local
	 */
	public void delFtpSession(InetAddress remote, InetSocketAddress local) {
		P2PAddress pAddress = new P2PAddress(remote, local);
		logger.debug("Del: {} {}",pAddress.remote,pAddress.local);
		this.hashMap.remove(pAddress);
	}
}
