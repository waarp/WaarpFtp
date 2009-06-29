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

import goldengate.ftp.core.config.FtpConfiguration;
import goldengate.ftp.core.config.FtpInternalConfiguration;

import java.util.Timer;

import org.jboss.netty.util.internal.SystemPropertyUtil;

import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * Signal Handler to allow trapping signals.
 *
 * @author Frederic Bregier
 *
 */
@SuppressWarnings("restriction")
public final class FtpSignalHandler implements SignalHandler {
    /**
     * Set if the program is in shutdown
     */
    private static boolean shutdown = false;

    /**
     * Set if the Handler is initialized
     */
    private static boolean initialized = false;

    /**
     * Previous Handler
     */
    private SignalHandler oldHandler = null;

    /**
     * Configuration
     */
    private final FtpConfiguration configuration;

    /**
     *
     * @param configuration
     */
    private FtpSignalHandler(FtpConfiguration configuration) {
        super();
        this.configuration = configuration;
    }

    /**
     * Says if the Process is currently in shutdown
     *
     * @return True if already in shutdown
     */
    public static boolean isInShutdown() {
        return shutdown;
    }

    /**
     * This function is the top function to be called when the process is to be
     * shutdown.
     *
     * @param immediate
     * @param configuration
     */
    public static void terminate(boolean immediate,
            FtpConfiguration configuration) {
        if (immediate) {
            shutdown = immediate;
        }
        terminate(configuration);
    }

    /**
     * Function to terminate IoSession and Connection.
     *
     * @param configuration
     */
    private static void terminate(FtpConfiguration configuration) {
        Timer timer = null;
        timer = new Timer(true);
        FtpTimerTask timerTask = new FtpTimerTask(FtpTimerTask.TIMER_EXIT);
        timer.schedule(timerTask, configuration.TIMEOUTCON * 4);
        if (shutdown) {
        	new Thread(new FtpChannelUtils(configuration)).start();
            //FtpChannelUtils.exit(configuration);
            // shouldn't be System.exit(2);
        } else {
        	new Thread(new FtpChannelUtils(configuration)).start();
            //FtpChannelUtils.exit(configuration);
            shutdown = true;
        }
    }

    /**
     * Function to initialized the SignalHandler
     *
     * @param configuration
     */
    public static void initSignalHandler(FtpConfiguration configuration) {
        if (initialized) {
            return;
        }
        Signal diagSignal = new Signal("TERM");
        FtpSignalHandler diagHandler = new FtpSignalHandler(configuration);
        diagHandler.oldHandler = Signal.handle(diagSignal, diagHandler);
        // Not on WINDOWS
        if (FtpInternalConfiguration.ISUNIX) {
            String vendor = SystemPropertyUtil.get("java.vm.vendor");
            vendor = vendor.toLowerCase();
            if (vendor.indexOf("ibm") >= 0) {
                diagSignal = new Signal("USR1");
                diagHandler = new FtpSignalHandler(configuration);
                diagHandler.oldHandler = Signal.handle(diagSignal, diagHandler);
            }
        }
        initialized = true;
    }

    /**
     * Handle signal
     *
     * @param signal
     */
    public void handle(Signal signal) {
        try {
            terminate(configuration);
            // Chain back to previous handler, if one exists
            if (oldHandler != SIG_DFL && oldHandler != SIG_IGN) {
                oldHandler.handle(signal);
            }
        } catch (Exception e) {
        }
        System.exit(signal.getNumber());
    }
}
