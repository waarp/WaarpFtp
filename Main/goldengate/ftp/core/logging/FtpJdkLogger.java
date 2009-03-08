/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @author tags. See the COPYRIGHT.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package goldengate.ftp.core.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <a href="http://java.sun.com/javase/6/docs/technotes/guides/logging/index.html">java.util.logging</a>
 * logger.
 * Based on The Netty Project (netty-dev@lists.jboss.org)
 * @author Trustin Lee (tlee@redhat.com)
 *
 */
public class FtpJdkLogger extends FtpInternalLogger {

    private final Logger logger;
    private final String loggerName;

    FtpJdkLogger(Logger logger, String loggerName) {
    	super();
        this.logger = logger;
        this.loggerName = loggerName;
    }

    public void debug(String msg) {
        logger.logp(Level.FINE, loggerName, null, msg);
    }

    public void debug(String msg, Throwable cause) {
        logger.logp(Level.FINE, loggerName, null, msg, cause);
    }

    public void error(String msg) {
        logger.logp(Level.SEVERE, loggerName, null, msg);
    }

    public void error(String msg, Throwable cause) {
        logger.logp(Level.SEVERE, loggerName, null, msg, cause);
    }

    public void info(String msg) {
        logger.logp(Level.INFO, loggerName, null, msg);
    }

    public void info(String msg, Throwable cause) {
        logger.logp(Level.INFO, loggerName, null, msg, cause);
    }

    public boolean isDebugEnabled() {
        return logger.isLoggable(Level.FINE);
    }

    public boolean isErrorEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

    public boolean isInfoEnabled() {
        return logger.isLoggable(Level.INFO);
    }

    public boolean isWarnEnabled() {
        return logger.isLoggable(Level.WARNING);
    }

    public void warn(String msg) {
        logger.logp(Level.WARNING, loggerName, null, msg);
    }

    public void warn(String msg, Throwable cause) {
        logger.logp(Level.WARNING, loggerName, null, msg, cause);
    }

    @Override
    public String toString() {
        return loggerName;
    }

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.logging.FtpInternalLogger#debug(java.lang.String, java.lang.String)
	 */
	@Override
	public void debug(String format, String arg1) {
		if (logger.isLoggable(Level.FINE)) {
			logger.logp(Level.FINE, loggerName, null, format.replaceFirst("{}", arg1));
		}
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.logging.FtpInternalLogger#debug(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void debug(String format, String arg1, String arg2) {
		if (logger.isLoggable(Level.FINE)) {
			logger.logp(Level.FINE, loggerName, null, format.replaceFirst("{}", arg1).replaceFirst("{}", arg2));
		}
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.logging.FtpInternalLogger#debug(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void debug(String format, Object arg1, Object arg2) {
		if (logger.isLoggable(Level.FINE)) {
			logger.logp(Level.FINE, loggerName, null, format.replaceFirst("{}", arg1.toString()).replaceFirst("{}", arg2.toString()));
		}
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.logging.FtpInternalLogger#debug(java.lang.String, java.lang.Object)
	 */
	@Override
	public void debug(String format, Object arg1) {
		if (logger.isLoggable(Level.FINE)) {
			logger.logp(Level.FINE, loggerName, null, format.replaceFirst("{}", arg1.toString()));
		}
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.logging.FtpInternalLogger#error(java.lang.String, java.lang.String)
	 */
	@Override
	public void error(String format, String arg1) {
		if (logger.isLoggable(Level.SEVERE)) {
			logger.logp(Level.SEVERE, loggerName, null, format.replaceFirst("{}", arg1));
		}
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.logging.FtpInternalLogger#error(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void error(String format, String arg1, String arg2) {
		if (logger.isLoggable(Level.SEVERE)) {
			logger.logp(Level.SEVERE, loggerName, null, format.replaceFirst("{}", arg1).replaceFirst("{}", arg2));
		}
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.logging.FtpInternalLogger#error(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void error(String format, Object arg1, Object arg2) {
		if (logger.isLoggable(Level.SEVERE)) {
			logger.logp(Level.SEVERE, loggerName, null, format.replaceFirst("{}", arg1.toString()).replaceFirst("{}", arg2.toString()));
		}
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.logging.FtpInternalLogger#error(java.lang.String, java.lang.Object)
	 */
	@Override
	public void error(String format, Object arg1) {
		if (logger.isLoggable(Level.SEVERE)) {
			logger.logp(Level.SEVERE, loggerName, null, format.replaceFirst("{}", arg1.toString()));
		}
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.logging.FtpInternalLogger#info(java.lang.String, java.lang.String)
	 */
	@Override
	public void info(String format, String arg1) {
		if (logger.isLoggable(Level.INFO)) {
			logger.logp(Level.INFO, loggerName, null, format.replaceFirst("{}", arg1));
		}
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.logging.FtpInternalLogger#info(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void info(String format, String arg1, String arg2) {
		if (logger.isLoggable(Level.INFO)) {
			logger.logp(Level.INFO, loggerName, null, format.replaceFirst("{}", arg1).replaceFirst("{}", arg2));
		}
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.logging.FtpInternalLogger#info(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void info(String format, Object arg1, Object arg2) {
		if (logger.isLoggable(Level.INFO)) {
			logger.logp(Level.INFO, loggerName, null, format.replaceFirst("{}", arg1.toString()).replaceFirst("{}", arg2.toString()));
		}
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.logging.FtpInternalLogger#info(java.lang.String, java.lang.Object)
	 */
	@Override
	public void info(String format, Object arg1) {
		if (logger.isLoggable(Level.INFO)) {
			logger.logp(Level.INFO, loggerName, null, format.replaceFirst("{}", arg1.toString()));
		}
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.logging.FtpInternalLogger#warn(java.lang.String, java.lang.String)
	 */
	@Override
	public void warn(String format, String arg1) {
		if (logger.isLoggable(Level.WARNING)) {
			logger.logp(Level.WARNING, loggerName, null, format.replaceFirst("{}", arg1));
		}
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.logging.FtpInternalLogger#warn(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void warn(String format, String arg1, String arg2) {
		if (logger.isLoggable(Level.WARNING)) {
			logger.logp(Level.WARNING, loggerName, null, format.replaceFirst("{}", arg1).replaceFirst("{}", arg2));
		}
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.logging.FtpInternalLogger#warn(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void warn(String format, Object arg1, Object arg2) {
		if (logger.isLoggable(Level.WARNING)) {
			logger.logp(Level.WARNING, loggerName, null, format.replaceFirst("{}", arg1.toString()).replaceFirst("{}", arg2.toString()));
		}
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.logging.FtpInternalLogger#warn(java.lang.String, java.lang.Object)
	 */
	@Override
	public void warn(String format, Object arg1) {
		if (logger.isLoggable(Level.WARNING)) {
			logger.logp(Level.WARNING, loggerName, null, format.replaceFirst("{}", arg1.toString()));
		}
	}
}