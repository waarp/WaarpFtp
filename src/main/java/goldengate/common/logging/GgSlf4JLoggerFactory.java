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

import org.jboss.netty.logging.InternalLogger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * Example of logger factory using SLF4J from LOGBACK
 *
 * @author Frederic Bregier
 *
 */
public class GgSlf4JLoggerFactory extends
        org.jboss.netty.logging.Slf4JLoggerFactory {
    /**
     *
     * @param level
     */
    public GgSlf4JLoggerFactory(Level level) {
        super();
        Logger logger = (Logger) LoggerFactory
                .getLogger(LoggerContext.ROOT_NAME);
        logger.setLevel(level);
    }

    @Override
    public InternalLogger newInstance(String name) {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(name);
        return new GgSlf4JLogger(logger);
    }
}
