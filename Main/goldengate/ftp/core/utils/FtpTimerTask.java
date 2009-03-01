/**
 * Frederic Bregier LGPL 10 janv. 09 
 * FtpTimerTask.java goldengate.ftp.core.control GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.utils;

import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;

import java.util.TimerTask;

/**
 * Timer Task used mainly when the server is going to shutdown in order to be sure the program exit.
 * @author frederic
 * goldengate.ftp.core.control FtpTimerTask
 * 
 */
public class FtpTimerTask extends TimerTask {
	/**
	 * Internal Logger
	 */
	private static final FtpInternalLogger logger =
        FtpInternalLoggerFactory.getLogger(FtpTimerTask.class);

	/**
	 * EXIT type (System.exit(1))
	 */
	public static final int TIMER_EXIT = 1;
	/**
	 * Type of execution in run() method
	 */
	private int type = 0;
	/**
	 * Constructor from type
	 * @param type
	 */
	public FtpTimerTask(int type) {
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		switch (type) {
			case TIMER_EXIT:
				logger.error("System will force EXIT");
				System.exit(1);
			default:
				logger.warn("Type unknown in TimerTask");
		}
	}
}
