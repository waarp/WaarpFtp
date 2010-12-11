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
