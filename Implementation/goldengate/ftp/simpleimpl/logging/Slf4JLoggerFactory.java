/**
 * 
 */
package goldengate.ftp.simpleimpl.logging;

import org.jboss.netty.logging.InternalLogger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * Example of logger factory using SLF4J from LOGBACK
 * @author fbregier
 *
 */
public class Slf4JLoggerFactory extends org.jboss.netty.logging.Slf4JLoggerFactory {
	/**
	 * 
	 * @param level
	 */
	public Slf4JLoggerFactory(Level level) {
		super();
		Logger logger = (Logger) LoggerFactory.getLogger(LoggerContext.ROOT_NAME);
		logger.setLevel(level);
	}
	@Override
    public InternalLogger newInstance(String name) {
        final org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger(name);
        return new Slf4JLogger(logger);
    }
}
