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
package org.waarp.ftp.simpleimpl.data;

import io.netty.channel.Channel;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.ftp.core.data.handler.DataBusinessHandler;

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
    private static final WaarpLogger logger = WaarpLoggerFactory
            .getLogger(FileSystemBasedDataBusinessHandler.class);

    @Override
    protected void cleanSession() {
        // logger.debug("FSDBH Clean session");
    }

    @Override
    public void exceptionLocalCaught(Throwable e) {
        logger.warn("FSDBH Execption", e);
    }

    @Override
    public void executeChannelClosed() {
        // logger.debug("FSDBH Channel closed");
    }

    @Override
    public void executeChannelConnected(Channel channel) {
        // logger.debug("FSDBH Channel connected {}", channel);
    }
}
