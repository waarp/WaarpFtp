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

import java.util.concurrent.ScheduledExecutorService;

import org.waarp.common.file.DataBlock;

import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;

/**
 * Global Traffic Shaping Handler for FTP
 * @author "Frederic Bregier"
 *
 */
public class FtpGlobalTrafficShapingHandler extends GlobalChannelTrafficShapingHandler {

    public FtpGlobalTrafficShapingHandler(ScheduledExecutorService executor, long writeGlobalLimit,
            long readGlobalLimit, long writeChannelLimit, long readChannelLimit, long checkInterval, long maxTime) {
        super(executor, writeGlobalLimit, readGlobalLimit, writeChannelLimit, readChannelLimit, checkInterval, maxTime);
    }

    public FtpGlobalTrafficShapingHandler(ScheduledExecutorService executor, long writeGlobalLimit,
            long readGlobalLimit, long writeChannelLimit, long readChannelLimit, long checkInterval) {
        super(executor, writeGlobalLimit, readGlobalLimit, writeChannelLimit, readChannelLimit, checkInterval);
    }

    public FtpGlobalTrafficShapingHandler(ScheduledExecutorService executor, long writeGlobalLimit,
            long readGlobalLimit, long writeChannelLimit, long readChannelLimit) {
        super(executor, writeGlobalLimit, readGlobalLimit, writeChannelLimit, readChannelLimit);
    }

    public FtpGlobalTrafficShapingHandler(ScheduledExecutorService executor, long checkInterval) {
        super(executor, checkInterval);
    }

    public FtpGlobalTrafficShapingHandler(ScheduledExecutorService executor) {
        super(executor);
    }

    @Override
    protected long calculateSize(Object msg) {
        if (msg instanceof DataBlock) {
            return ((DataBlock) msg).getByteCount();
        }
        return super.calculateSize(msg);
    }

}
