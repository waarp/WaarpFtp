/**
 * Copyright 2009, Frederic Bregier, and individual contributors
 * by the @author tags. See the COPYRIGHT.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
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
package goldengate.common.logging;

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
