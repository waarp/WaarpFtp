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
package org.waarp.ftp.core.exception;

/**
 * No transfer exception
 * 
 * @author Frederic Bregier
 * 
 */
public class FtpNoTransferException extends Exception {

    /**
	 *
	 */
    private static final long serialVersionUID = 977343700748516315L;

    /**
     * @param message
     */
    public FtpNoTransferException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public FtpNoTransferException(String message, Throwable cause) {
        super(message, cause);
    }

}
