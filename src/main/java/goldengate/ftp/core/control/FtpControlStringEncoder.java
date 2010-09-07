/**
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3.0 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package goldengate.ftp.core.control;

import java.nio.charset.Charset;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.string.StringEncoder;

/**
 * Exactly same as StringEncoder from Netty
 *
 * @author Frederic Bregier
 *
 */
public class FtpControlStringEncoder extends StringEncoder {
    /**
	 *
	 */
    public FtpControlStringEncoder() {
    }

    /**
     * @see StringEncoder
     * @param arg0
     */
    public FtpControlStringEncoder(String arg0) {
        super(Charset.forName(arg0));
    }

    /**
     * @see StringEncoder
     * @param arg0
     */
    public FtpControlStringEncoder(Charset arg0) {
        super(arg0);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.netty.handler.codec.string.StringEncoder#encode(org.jboss.netty
     * .channel.ChannelHandlerContext, org.jboss.netty.channel.Channel,
     * java.lang.Object)
     */
    @Override
    protected Object encode(ChannelHandlerContext arg0, Channel arg1,
            Object arg2) throws Exception {
        return super.encode(arg0, arg1, arg2);
    }

}
