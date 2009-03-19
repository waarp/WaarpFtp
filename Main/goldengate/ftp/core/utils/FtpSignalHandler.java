/**
 * Frederic Bregier LGPL 10 janv. 09 FtpSignalHandler.java
 * goldengate.ftp.core.control GoldenGateFtp frederic
 */
package goldengate.ftp.core.utils;

import goldengate.ftp.core.config.FtpConfiguration;
import goldengate.ftp.core.config.FtpInternalConfiguration;

import java.util.Timer;

import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * Signal Handler to allow trapping signals.
 * 
 * @author frederic goldengate.ftp.core.control FtpSignalHandler
 * 
 */
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
            FtpChannelUtils.exit(configuration);
            // shouldn't be System.exit(2);
        } else {
            FtpChannelUtils.exit(configuration);
            shutdown = true;
        }
    }

    /**
     * Function to initialized the SignalHandler
     * 
     * @param configuration
     */
    public static void initSignalHandler(FtpConfiguration configuration) {
        if (initialized) return;
        Signal diagSignal = new Signal("TERM");
        FtpSignalHandler diagHandler = new FtpSignalHandler(configuration);
        diagHandler.oldHandler = Signal.handle(diagSignal, diagHandler);
        // Not on WINDOWS
        if (FtpInternalConfiguration.ISUNIX) {
            diagSignal = new Signal("USR1");
            diagHandler = new FtpSignalHandler(configuration);
            diagHandler.oldHandler = Signal.handle(diagSignal, diagHandler);
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
            terminate(this.configuration);
            // Chain back to previous handler, if one exists
            if (this.oldHandler != SIG_DFL && this.oldHandler != SIG_IGN) {
                this.oldHandler.handle(signal);
            }
        } catch (Exception e) {
        }
        System.exit(signal.getNumber());
    }
}
