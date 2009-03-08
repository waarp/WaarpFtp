/**
 * Frederic Bregier LGPL 15 févr. 09 
 * FtpControlStringEncoder.java goldengate.ftp.core.control GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.control;

import java.nio.charset.Charset;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.string.StringDecoder;

/**
 * Exactly same as StringDecoder from Netty
 * @author frederic
 * goldengate.ftp.core.control FtpControlStringDecoder
 * 
 */
public class FtpControlStringDecoder extends StringDecoder {
	/**
	 * 
	 */
	public FtpControlStringDecoder() {
	}

	/**
	 * @see StringDecoder
	 * @param arg0
	 */
	public FtpControlStringDecoder(String arg0) {
		super(arg0);
	}

	/**
	 * @see StringDecoder
	 * @param arg0
	 */
	public FtpControlStringDecoder(Charset arg0) {
		super(arg0);
	}

	/* (non-Javadoc)
	 * @see org.jboss.netty.handler.codec.string.StringDecoder#decode(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.Channel, java.lang.Object)
	 */
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		// TODO Auto-generated method stub
		return super.decode(ctx, channel, msg);
	}
}
