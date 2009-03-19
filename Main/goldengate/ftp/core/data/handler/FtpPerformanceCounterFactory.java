/**
 * Frederic Bregier LGPL 12 mars 09 FtpPerformanceCounterFactory.java
 * goldengate.ftp.core.data.handler GoldenGateFtp frederic
 */
package goldengate.ftp.core.data.handler;

import java.util.concurrent.ExecutorService;

import org.jboss.netty.handler.trafficshaping.PerformanceCounter;
import org.jboss.netty.handler.trafficshaping.PerformanceCounterFactory;

/**
 * @author frederic goldengate.ftp.core.data.handler
 *         FtpPerformanceCounterFactory
 * 
 */
public class FtpPerformanceCounterFactory extends PerformanceCounterFactory {
    /**
     * Default global limit 512Mbit
     */
    public static long DEFAULT_GLOBAL_LIMIT = 0x4000000L;

    /**
     * Default session limit 64Mbit, so up to 8 full simultaneous clients
     */
    public static long DEFAULT_SESSION_LIMIT = 0x800000L;

    /**
     * Full constructor
     * 
     * @param executorService
     *            created for instance like Executors.newCachedThreadPool
     * @param channelActive
     *            True if each channel will have a PerformanceCounter
     * @param channelLimitWrite
     *            NO_LIMIT or a limit in bytes/s
     * @param channelLimitRead
     *            NO_LIMIT or a limit in bytes/s
     * @param channelDelay
     *            The delay between two computations of performances for
     *            channels or NO_STAT if no stats are to be computed
     * @param globalActive
     *            True if global context will have one unique PerformanceCounter
     * @param globalLimitWrite
     *            NO_LIMIT or a limit in bytes/s
     * @param globalLimitRead
     *            NO_LIMIT or a limit in bytes/s
     * @param globalDelay
     *            The delay between two computations of performances for global
     *            context or NO_STAT if no stats are to be computed
     */
    public FtpPerformanceCounterFactory(ExecutorService executorService,
            boolean channelActive, long channelLimitWrite,
            long channelLimitRead, long channelDelay, boolean globalActive,
            long globalLimitWrite, long globalLimitRead, long globalDelay) {
        super(executorService, channelActive, channelLimitWrite,
                channelLimitRead, channelDelay, globalActive, globalLimitWrite,
                globalLimitRead, globalDelay);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jboss.netty.handler.trafficshaping.PerformanceCounterFactory#accounting
     * (org.jboss.netty.handler.trafficshaping.PerformanceCounter)
     */
    @Override
    protected void accounting(PerformanceCounter arg0) {
        // nothing to do for now

    }

}
