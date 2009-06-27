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
package goldengate.common.file;

import goldengate.common.command.exception.CommandAbstractException;
import goldengate.common.exception.NoRestartException;

/**
 * Restart object that implements the REST command.<br>
 * Note that if necessary, according to the implementation of
 * {@link DirInterface} and {@link FileInterface}, one could want to implement a
 * way to store or retrieve Marker from/to the client specification.
 *
 * @author Frederic Bregier
 *
 */
public abstract class Restart {
    /**
     * SessionInterface
     */
    private final SessionInterface session;

    /**
     * Is the current Restart object in context set
     */
    private boolean isSet = false;

    /**
     * Default constructor
     *
     * @param session
     */
    protected Restart(SessionInterface session) {
        isSet = false;
        this.session = session;
    }

    /**
     * @return the isSet
     */
    protected boolean isSet() {
        return isSet;
    }

    /**
     * @param isSet
     *            the isSet to set
     */
    public void setSet(boolean isSet) {
        this.isSet = isSet;
    }

    /**
     * @return the session
     */
    protected SessionInterface getSession() {
        return session;
    }

    /**
     * Restart from a Marker for the next FileInterface
     *
     * @param marker
     * @return True if the Marker is OK
     * @exception CommandAbstractException
     */
    public abstract boolean restartMarker(String marker)
            throws CommandAbstractException;

    /**
     *
     * @return the position from a previous REST command
     * @throws NoRestartException
     *             if no REST command was issued before
     */
    public abstract long getPosition() throws NoRestartException;
    // FIXME Additionally the implementation should implement a way to get the
    // values
}
