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
package org.waarp.ftp.core.config;

import org.waarp.common.file.DataBlock;

import io.netty.handler.traffic.ChannelTrafficShapingHandler;

/**
 * Channel Traffic Shaping Handler for FTP
 * @author "Frederic Bregier"
 *
 */
public class FtpChannelTrafficShapingHandler extends ChannelTrafficShapingHandler {

    /**
     * @param checkInterval
     */
    public FtpChannelTrafficShapingHandler(long checkInterval) {
        super(checkInterval);
    }

    /**
     * @param writeLimit
     * @param readLimit
     */
    public FtpChannelTrafficShapingHandler(long writeLimit, long readLimit) {
        super(writeLimit, readLimit);
    }

    /**
     * @param writeLimit
     * @param readLimit
     * @param checkInterval
     */
    public FtpChannelTrafficShapingHandler(long writeLimit, long readLimit, long checkInterval) {
        super(writeLimit, readLimit, checkInterval);
    }

    /**
     * @param writeLimit
     * @param readLimit
     * @param checkInterval
     * @param maxTime
     */
    public FtpChannelTrafficShapingHandler(long writeLimit, long readLimit, long checkInterval, long maxTime) {
        super(writeLimit, readLimit, checkInterval, maxTime);
    }

    @Override
    protected long calculateSize(Object msg) {
        if (msg instanceof DataBlock) {
            return ((DataBlock) msg).getByteCount();
        }
        return super.calculateSize(msg);
    }

}
