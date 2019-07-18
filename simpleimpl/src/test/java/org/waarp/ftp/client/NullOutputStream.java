/*******************************************************************************
 * This file is part of Waarp Project (named also Waarp or GG).
 *
 *  Copyright (c) 2019, Waarp SAS, and individual contributors by the @author
 *  tags. See the COPYRIGHT.txt in the distribution for a full listing of
 *  individual contributors.
 *
 *  All Waarp Project is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or (at your
 *  option) any later version.
 *
 *  Waarp is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 *  A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  Waarp . If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

/**
 *
 */
package org.waarp.ftp.client;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Simple /dev/null output stream
 *
 * @author frederic
 *
 */
public class NullOutputStream extends OutputStream {

  /**
   *
   */
  public NullOutputStream() {
  }

  @Override
  public void close() throws IOException {
  }

  @Override
  public void flush() throws IOException {
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
  }

  @Override
  public void write(byte[] b) throws IOException {
  }

  @Override
  public void write(int b) throws IOException {
  }

}
