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
package goldengate.ftp.core.utils;

import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;

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
     * Type of execution in run() method
     */
    private final int type;

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
            default:
                logger.warn("Type unknown in TimerTask");
        }
    }
}
