/**
 * 
 */
package goldengate.ftp.core.logging;

import org.jboss.netty.logging.InternalLogger;

/**
 * Logger inspired from Netty implementation, adding some extra
 * commands that allow to limit the overhead of some ignored
 * logger calls (toString or string construction is called only if necessary).
 * 
 * Based on The Netty Project (netty-dev@lists.jboss.org)
 * @author Trustin Lee (tlee@redhat.com)

 * @author fbregier
 *
 */
public abstract class FtpInternalLogger implements InternalLogger {
	/**
	 * 
	 * @param format
	 * @param arg1
	 */
	public abstract void debug(String format, String arg1);
	/**
	 * 
	 * @param format
	 * @param arg1
	 */
	public abstract void info(String format, String arg1);
	/**
	 * 
	 * @param format
	 * @param arg1
	 */
	public abstract void warn(String format, String arg1);
	/**
	 * 
	 * @param format
	 * @param arg1
	 */
	public abstract void error(String format, String arg1);
	/**
	 * 
	 * @param format
	 * @param arg1
	 * @param arg2
	 */
	public abstract void debug(String format, String arg1, String arg2);
	/**
	 * 
	 * @param format
	 * @param arg1
	 * @param arg2
	 */
	public abstract void info(String format, String arg1, String arg2);
	/**
	 * 
	 * @param format
	 * @param arg1
	 * @param arg2
	 */
	public abstract void warn(String format, String arg1, String arg2);
	/**
	 * 
	 * @param format
	 * @param arg1
	 * @param arg2
	 */
	public abstract void error(String format, String arg1, String arg2);
	/**
	 * 
	 * @param format
	 * @param arg1
	 * @param arg2
	 */
	public abstract void debug(String format, Object arg1, Object arg2);
	/**
	 * 
	 * @param format
	 * @param arg1
	 * @param arg2
	 */
	public abstract void info(String format, Object arg1, Object arg2);
	/**
	 * 
	 * @param format
	 * @param arg1
	 * @param arg2
	 */
	public abstract void warn(String format, Object arg1, Object arg2);
	/**
	 * 
	 * @param format
	 * @param arg1
	 * @param arg2
	 */
	public abstract void error(String format, Object arg1, Object arg2);
	/**
	 * 
	 * @param format
	 * @param arg1
	 */
	public abstract void debug(String format, Object arg1);
	/**
	 * 
	 * @param format
	 * @param arg1
	 */
	public abstract void info(String format, Object arg1);
	/**
	 * 
	 * @param format
	 * @param arg1
	 */
	public abstract void warn(String format, Object arg1);
	/**
	 * 
	 * @param format
	 * @param arg1
	 */
	public abstract void error(String format, Object arg1);
}
