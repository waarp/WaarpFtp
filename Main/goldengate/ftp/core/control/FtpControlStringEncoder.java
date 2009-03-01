/**
 * Frederic Bregier LGPL 15 févr. 09 
 * FtpControlStringEncoder.java goldengate.ftp.core.control GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.control;

import java.nio.charset.Charset;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.string.StringEncoder;

/**
 * Exactly same as StringEncoder from Netty
 * @author frederic
 * goldengate.ftp.core.control FtpControlStringEncoder
 * 
 */
public class FtpControlStringEncoder extends StringEncoder {
	/**
	 * 
	 */
	public FtpControlStringEncoder() {
	}

	/**
	 * @param arg0
	 */
	public FtpControlStringEncoder(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public FtpControlStringEncoder(Charset arg0) {
		super(arg0);
	}

	/* (non-Javadoc)
	 * @see org.jboss.netty.handler.codec.string.StringEncoder#encode(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.Channel, java.lang.Object)
	 */
	@Override
	protected Object encode(ChannelHandlerContext arg0, Channel arg1, Object arg2) throws Exception {
		return super.encode(arg0, arg1, arg2);
	}

}
