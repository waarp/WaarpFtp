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
package goldengate.ftp.simpleimpl.data;

import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;
import goldengate.ftp.core.data.handler.DataBusinessHandler;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ExceptionEvent;

/**
 * DataBusinessHandler implementation based on Simple Filesystem
 *
 * @author Frederic Bregier
 *
 */
public class FileSystemBasedDataBusinessHandler extends DataBusinessHandler {
    /**
     * Internal Logger
     */
    private static final GgInternalLogger logger = GgInternalLoggerFactory
            .getLogger(FileSystemBasedDataBusinessHandler.class);

    /*
     * (non-Javadoc)
     *
     * @see
     * goldengate.ftp.core.data.handler.DataBusinessHandler#cleanSession(goldengate
     * .ftp.core.session.FtpSession)
     */
    @Override
    protected void cleanSession() {
        // logger.debug("FSDBH Clean session");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * goldengate.ftp.core.data.handler.DataBusinessHandler#exceptionLocalCaught
     * (org.jboss.netty.channel.ExceptionEvent)
     */
    @Override
    public void exceptionLocalCaught(ExceptionEvent e) {
        logger.warn("FSDBH Execption", e.getCause());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * goldengate.ftp.core.data.handler.DataBusinessHandler#executeChannelClosed
     * ()
     */
    @Override
    public void executeChannelClosed() {
        // logger.debug("FSDBH Channel closed");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * goldengate.ftp.core.data.handler.DataBusinessHandler#executeChannelConnected
     * (org.jboss.netty.channel.Channel)
     */
    @Override
    public void executeChannelConnected(Channel channel) {
        // logger.debug("FSDBH Channel connected {}", channel);
    }
}
