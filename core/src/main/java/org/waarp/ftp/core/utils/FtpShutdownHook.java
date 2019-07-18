/**
   This file is part of Waarp Project.

   Copyright 2009, Frederic Bregier, and individual contributors by the @author
   tags. See the COPYRIGHT.txt in the distribution for a full listing of
   individual contributors.

   All Waarp Project is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Waarp is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Waarp .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.waarp.ftp.core.utils;

import org.waarp.common.utility.WaarpShutdownHook;
import org.waarp.ftp.core.config.FtpConfiguration;

/**
 * @author "Frederic Bregier"
 *
 */
public class FtpShutdownHook extends WaarpShutdownHook {

    protected static FtpConfiguration configuration;

    /**
     * @param configuration
     */
    public FtpShutdownHook(ShutdownConfiguration configuration, FtpConfiguration ftpconfiguration) {
        super(configuration);
        FtpShutdownHook.configuration = ftpconfiguration;
    }

    @Override
    protected void exit() {
        FtpChannelUtils.exit(FtpShutdownHook.configuration);
    }

}
