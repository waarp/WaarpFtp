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
package goldengate.ftp.core.utils;

import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;
import goldengate.ftp.core.config.FtpConfiguration;

import java.util.TimerTask;

/**
 * Timer Task used mainly when the server is going to shutdown in order to be
 * sure the program exit.
 *
 * @author Frederic Bregier
 *
 */
public class FtpTimerTask extends TimerTask {
    /**
     * Internal Logger
     */
    private static final GgInternalLogger logger = GgInternalLoggerFactory
            .getLogger(FtpTimerTask.class);

    /**
     * EXIT type (System.exit(1))
     */
    public static final int TIMER_EXIT = 1;
    /**
     * Finalize Control connection
     */
    public static final int TIMER_CONTROL = 2;

    /**
     * Type of execution in run() method
     */
    private final int type;
    /**
     * Configuration
     */
    public FtpConfiguration configuration = null;

    /**
     * Constructor from type
     *
     * @param type
     */
    public FtpTimerTask(int type) {
        super();
        this.type = type;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.TimerTask#run()
     */
    @Override
    public void run() {
        switch (type) {
            case TIMER_EXIT:
                logger.error("System will force EXIT");
                System.exit(0);
                break;
            case TIMER_CONTROL:
                logger.info("Exit Shutdown Command");
                FtpChannelUtils.terminateCommandChannels(configuration);
                logger.info("Exit end of Command Shutdown");
                break;
            default:
                logger.info("Type unknown in TimerTask");
        }
    }
}
