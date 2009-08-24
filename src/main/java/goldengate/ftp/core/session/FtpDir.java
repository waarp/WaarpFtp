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
package goldengate.ftp.core.session;

import goldengate.common.command.exception.CommandAbstractException;
import goldengate.common.file.DirInterface;

/**
 * @author Frederic Bregier
 *
 */
public interface FtpDir extends DirInterface {
    /**
     * Create a new FtpFile
     *
     * @param path
     * @param append
     * @return the new Ftp FileInterface
     * @throws CommandAbstractException
     */
    public abstract FtpFile newFile(String path, boolean append)
            throws CommandAbstractException;

    /**
     * Set a path as the current FileInterface
     *
     * @param path
     * @param append
     *            True if this file is supposed to be in append mode (APPE),
     *            False in any other cases
     * @return the Ftp FileInterface if it is correctly initiate
     * @throws CommandAbstractException
     */
    public abstract FtpFile setFile(String path, boolean append)
            throws CommandAbstractException;

    /**
     * Set a new unique path as the current FileInterface from the current
     * Directory (STOU)
     *
     * @return the Ftp FileInterface if it is correctly initiate
     * @throws CommandAbstractException
     */
    public abstract FtpFile setUniqueFile()
            throws CommandAbstractException;

}
