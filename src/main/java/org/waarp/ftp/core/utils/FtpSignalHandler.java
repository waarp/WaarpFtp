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
package org.waarp.ftp.core.utils;

import java.util.Timer;

import org.waarp.common.utility.DetectionUtils;
import org.waarp.ftp.core.config.FtpConfiguration;
import org.waarp.ftp.core.config.FtpInternalConfiguration;

import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * Signal Handler to allow trapping signals.
 * 
 * @author Frederic Bregier
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
	 * This function is the top function to be called when the process is to be shutdown.
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
		Timer timer = new Timer(true);
		FtpTimerTask timerTask = new FtpTimerTask(FtpTimerTask.TIMER_EXIT);
		timer.schedule(timerTask, configuration.TIMEOUTCON * 2);
		if (shutdown) {
			Thread thread =
					new Thread(new FtpChannelUtils(configuration), "FtpShutownThread");
			thread.setDaemon(true);
			thread.start();
		} else {
			Thread thread =
					new Thread(new FtpChannelUtils(configuration), "FtpShutownThread");
			thread.setDaemon(true);
			thread.start();
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
		if (FtpInternalConfiguration.ISUNIX == null) {
			FtpInternalConfiguration.ISUNIX =
					! DetectionUtils.isWindows();
		}
		if (FtpInternalConfiguration.ISUNIX) {
			if (DetectionUtils.isUnixIBM()) {
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
		System.err.println("Signal: " + signal.getNumber());
		try {
			Thread.sleep(configuration.TIMEOUTCON * 3);
		} catch (InterruptedException e) {
		}
		System.exit(0);
	}
}
