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
package org.waarp.ftp.core.file;

import org.waarp.common.command.exception.CommandAbstractException;
import org.waarp.common.file.DirInterface;

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
     *            True if this file is supposed to be in append mode (APPE), False in any other
     *            cases
     * @return the Ftp FileInterface if it is correctly initiate
     * @throws CommandAbstractException
     */
    public abstract FtpFile setFile(String path, boolean append)
            throws CommandAbstractException;

    /**
     * Set a new unique path as the current FileInterface from the current Directory (STOU)
     * 
     * @return the Ftp FileInterface if it is correctly initiate
     * @throws CommandAbstractException
     */
    public abstract FtpFile setUniqueFile()
            throws CommandAbstractException;

}
