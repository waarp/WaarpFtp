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
package goldengate.common.logging;

import org.jboss.netty.logging.InternalLogLevel;
import org.jboss.netty.logging.InternalLogger;

/**
 * Logger inspired from Netty implementation, adding some extra commands that
 * allow to limit the overhead of some ignored logger calls (toString or string
 * construction is called only if necessary).
 *
 * Based on The Netty Project (netty-dev@lists.jboss.org)
 *
 * @author Trustin Lee (tlee@redhat.com)
 *
 * @author Frederic Bregier
 *
 */
public abstract class GgInternalLogger implements InternalLogger {
    /**
     * To be used in message for logger (rank 2) like
     * logger.warn(code,"message:"+getImmediateMethodAndLine(),null);
     * 
     * @return "ClassAndMethodName(FileName:LineNumber)"
     */
    public static String getImmediateMethodAndLine() {
        StackTraceElement elt = Thread.currentThread().getStackTrace()[2];
        return getMethodAndLine(elt);
    }

    /**
     * To be used only by Logger (rank 5)
     * 
     * @return "MethodName(FileName:LineNumber)"
     */
    protected static String getLoggerMethodAndLine() {
        StackTraceElement elt = Thread.currentThread().getStackTrace()[3];
        return getMethodAndLine(elt);
    }

    /**
     * @param rank
     *            is the current depth of call+1 (immediate = 1+1=2)
     * @return "ClassAndMethodName(FileName:LineNumber)"
     */
    public static String getRankMethodAndLine(int rank) {
        StackTraceElement elt = Thread.currentThread().getStackTrace()[rank];
        return getMethodAndLine(elt);
    }
    /**
     * 
     * @param elt
     * @return "MethodName(FileName:LineNumber) " from elt
     */
    private static String getMethodAndLine(StackTraceElement elt) {
        StringBuilder builder = new StringBuilder(elt.getClassName());
        builder.append(".");
        builder.append(elt.getMethodName());
        builder.append("(");
        builder.append(elt.getFileName());
        builder.append(":");
        builder.append(elt.getLineNumber());
        builder.append(") : ");
        return builder.toString();
    }
    /**
     * @param level
     * @return True if the level is enabled
     */
    public boolean isEnabled(InternalLogLevel level) {
        switch (level) {
            case DEBUG:
                return isDebugEnabled();
            case INFO:
                return isInfoEnabled();
            case WARN:
                return isWarnEnabled();
            case ERROR:
                return isErrorEnabled();
            default:
                throw new Error();
        }
    }

    public void log(InternalLogLevel level, String msg, Throwable cause) {
        switch (level) {
            case DEBUG:
                debug(msg, cause);
                break;
            case INFO:
                info(msg, cause);
                break;
            case WARN:
                warn(msg, cause);
                break;
            case ERROR:
                error(msg, cause);
                break;
            default:
                throw new Error();
        }
    }

    public void log(InternalLogLevel level, String msg) {
        switch (level) {
            case DEBUG:
                debug(msg);
                break;
            case INFO:
                info(msg);
                break;
            case WARN:
                warn(msg);
                break;
            case ERROR:
                error(msg);
                break;
            default:
                throw new Error();
        }
    }

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
