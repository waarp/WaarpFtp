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
package org.waarp.ftp.client;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Timeout listener for Data connection for FTP4J model
 *
 * @author "Frederic Bregier"
 */
public class DataTimeOutListener implements FTPDataTransferListener {
  /**
   * Internal Logger
   */
  private static final WaarpLogger logger =
      WaarpLoggerFactory.getLogger(DataTimeOutListener.class);

  private final FTPClient client;
  private final Timer timer;
  private final String command;
  private final String file;
  private long timeout = 10000;
  private long last = System.currentTimeMillis();
  private boolean finished;

  public DataTimeOutListener(FTPClient client, long timeout, String command,
                             String file) {
    this.client = client;
    timer = new Timer(true);
    this.timeout = timeout;
    this.command = command;
    this.file = file;
  }

  private void renewTask() {
    TimerTask task = new TimerTask() {
      @Override
      public void run() {
        if (finished) {
          return;
        }
        long now = System.currentTimeMillis();
        if (now - last - timeout > 0) {
          try {
            logger
                .warn("Timeout during file transfer: " + command + " " + file);
            client.abortCurrentDataTransfer(true);
          } catch (IOException e) {
          } catch (FTPIllegalReplyException e) {
          }
        } else {
          renewTask();
        }
      }
    };
    timer.schedule(task, timeout);
  }

  @Override
  public void started() {
    renewTask();
    last = System.currentTimeMillis();
  }

  @Override
  public void transferred(int length) {
    last = System.currentTimeMillis();
  }

  @Override
  public void completed() {
    finished = true;
    last = System.currentTimeMillis();
    timer.cancel();
  }

  @Override
  public void aborted() {
    finished = true;
    last = System.currentTimeMillis();
    timer.cancel();
  }

  @Override
  public void failed() {
    finished = true;
    last = System.currentTimeMillis();
    timer.cancel();
  }

}
