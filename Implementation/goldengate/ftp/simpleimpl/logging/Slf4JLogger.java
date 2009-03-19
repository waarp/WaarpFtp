/**
 * 
 */
package goldengate.ftp.simpleimpl.logging;

import goldengate.ftp.core.logging.FtpInternalLogger;
import ch.qos.logback.classic.Logger;

/**
 * Example of logger using SLF4J from LOGBACK
 * 
 * @author fbregier
 * 
 */
public class Slf4JLogger extends FtpInternalLogger {
    /**
     * Internal logger
     */
    private final Logger logger;

    /**
     * 
     * @param logger
     */
    public Slf4JLogger(org.slf4j.Logger logger) {
        super();
        this.logger = (Logger) logger;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * goldengate.ftp.core.logging.FtpInternalLogger#debug(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void debug(String format, String arg1, String arg2) {
        this.logger.debug(format, arg1, arg2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * goldengate.ftp.core.logging.FtpInternalLogger#debug(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void debug(String format, String arg1) {
        this.logger.debug(format, arg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * goldengate.ftp.core.logging.FtpInternalLogger#error(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void error(String format, String arg1, String arg2) {
        this.logger.error(format, arg1, arg2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * goldengate.ftp.core.logging.FtpInternalLogger#error(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void error(String format, String arg1) {
        this.logger.error(format, arg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.logging.FtpInternalLogger#info(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void info(String format, String arg1, String arg2) {
        this.logger.info(format, arg1, arg2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.logging.FtpInternalLogger#info(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void info(String format, String arg1) {
        this.logger.info(format, arg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.logging.FtpInternalLogger#warn(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void warn(String format, String arg1, String arg2) {
        this.logger.warn(format, arg1, arg2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see goldengate.ftp.core.logging.FtpInternalLogger#warn(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void warn(String format, String arg1) {
        this.logger.warn(format, arg1);
    }

    // original form
    public void debug(String msg) {
        this.logger.debug(msg);
    }

    public void debug(String msg, Throwable cause) {
        this.logger.debug(msg, cause);
    }

    public void error(String msg) {
        this.logger.error(msg);
    }

    public void error(String msg, Throwable cause) {
        this.logger.error(msg, cause);
    }

    public void info(String msg) {
        this.logger.info(msg);
    }

    public void info(String msg, Throwable cause) {
        this.logger.info(msg, cause);
    }

    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return this.logger.isErrorEnabled();
    }

    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return this.logger.isWarnEnabled();
    }

    public void warn(String msg) {
        this.logger.warn(msg);
    }

    public void warn(String msg, Throwable cause) {
        this.logger.warn(msg, cause);
    }

    @Override
    public String toString() {
        return String.valueOf(this.logger.getName());
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        this.logger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object arg1) {
        this.logger.debug(format, arg1);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        this.logger.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object arg1) {
        this.logger.error(format, arg1);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        this.logger.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object arg1) {
        this.logger.info(format, arg1);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        this.logger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String format, Object arg1) {
        this.logger.warn(format, arg1);
    }
}
