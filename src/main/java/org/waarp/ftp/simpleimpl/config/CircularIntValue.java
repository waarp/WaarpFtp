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
package org.waarp.ftp.simpleimpl.config;

/**
 * Circular Value used by passive connections to find the next valid port to propose to the client.
 * 
 * @author Frederic Bregier
 * 
 */
public class CircularIntValue {
	/**
	 * Min value
	 */
	private final int min;

	/**
	 * Max value
	 */
	private final int max;

	/**
	 * Current Value
	 */
	private Integer current;

	/**
	 * Create a circular range of values
	 * 
	 * @param min
	 * @param max
	 */
	public CircularIntValue(int min, int max) {
		this.min = min;
		this.max = max;
		current = new Integer(this.min - 1);
	}

	/**
	 * Get the next value
	 * 
	 * @return the next value
	 */
	public int getNext() {
		synchronized (current) {
			if (current >= max) {
				current = min;
			} else {
				current++;
			}
			return current;
		}
	}
}
