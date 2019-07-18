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
package org.waarp.ftp.client.transaction;

import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.ftp.client.FtpClientTest;

import java.io.File;

/**
 * FTP Thread used to check multiple FTP clients in parallel with the test scenario
 *
 * @author frederic
 *
 */
public class FtpClientThread implements Runnable {
  /**
   * Internal Logger
   */
  protected static WaarpLogger
      logger = WaarpLoggerFactory.getLogger(FtpClientThread.class);

  private String id;

  private String server;

  private int port = 21;

  private String username;

  private String passwd;

  private String account;

  private String localFilename;

  private String remoteFilename;

  private int numberIteration = 1;

  private int type;

  private int delay;

  private int isSsl;

  /**
   * @param id
   * @param server
   * @param port
   * @param username
   * @param passwd
   * @param account
   * @param localFilename
   * @param nb
   * @param type
   * @param delay
   */
  public FtpClientThread(String id, String server, int port, String username,
                         String passwd, String account, String localFilename,
                         int nb,
                         int type, int delay, int isSsl) {
    this.id = id;
    this.server = server;
    this.port = port;
    this.username = username;
    this.passwd = passwd;
    this.account = account;
    this.localFilename = localFilename;
    File local = new File(this.localFilename);
    remoteFilename = local.getName();
    numberIteration = nb;
    this.type = type;
    this.delay = (delay / 10) * 10;
    this.isSsl = isSsl;
    File dir = new File("/tmp/GGFTP/T" + id + "/" + account);
    dir.mkdirs();
  }

  @Override
  public void run() {
    Ftp4JClientTransactionTest client =
        new Ftp4JClientTransactionTest(server,
                                       port, username, passwd,
                                       account, isSsl);
    // Thread.yield();
    // System.err.println(id+" connect");
    if (!client.connect()) {
      logger.error(id + " Cant connect");
      FtpClientTest.numberKO.incrementAndGet();
      return;
    }
    try {
      if (numberIteration <= 0) {
        FtpClientTest.numberOK.incrementAndGet();
        return;// Only connect
      }
      // client.makeDir(this.id);
      logger.info(id + " change dir");
      client.changeDir(id);
      if (delay >= 10) {
        try {
          Thread.sleep(delay);
        } catch (InterruptedException e) {
        }
      } else {
        Thread.yield();
      }

      logger.info(id + " change type");
      client.changeFileType(true);
      if (type <= 0) {
        logger.warn(id + " change mode passive");
        client.changeMode(true);
        if (type <= -10) {
          for (int i = 0; i < numberIteration; i++) {
            logger.info(id + " transfer passive store " + i);
            if (!client.transferFile(localFilename,
                                     remoteFilename, true)) {
              logger.warn("Cant store file passive mode " + id);
              FtpClientTest.numberKO.incrementAndGet();
              return;
            } else {
              FtpClientTest.numberOK.incrementAndGet();
              if (delay > 0) {
                try {
                  Thread.sleep(delay);
                } catch (InterruptedException e) {
                }
              }
            }
            // System.err.println(id+" end transfer store "+i);
          }
          Thread.yield();
        } else {
          for (int i = 0; i < numberIteration; i++) {
            logger.info(id + " transfer passive store " + i);
            if (!client.transferFile(localFilename,
                                     remoteFilename, true)) {
              logger.warn("Cant store file passive mode " + id);
              FtpClientTest.numberKO.incrementAndGet();
              return;
            } else {
              FtpClientTest.numberOK.incrementAndGet();
              if (delay > 0) {
                try {
                  Thread.sleep(delay);
                } catch (InterruptedException e) {
                }
              }
            }
            if (!client.deleteFile(remoteFilename)) {
              logger.warn(" Cant delete file passive mode " + id);
              FtpClientTest.numberKO.incrementAndGet();
              return;
            } else {
              FtpClientTest.numberOK.incrementAndGet();
              if (delay > 0) {
                try {
                  Thread.sleep(delay);
                } catch (InterruptedException e) {
                }
              }
            }
            // System.err.println(id+" end transfer retr "+i);
          }
          if (!client.transferFile(localFilename,
                                   remoteFilename, true)) {
            logger.warn("Cant store file passive mode " + id);
            FtpClientTest.numberKO.incrementAndGet();
            return;
          } else {
            FtpClientTest.numberOK.incrementAndGet();
            if (delay > 0) {
              try {
                Thread.sleep(delay);
              } catch (InterruptedException e) {
              }
            }
          }
          Thread.yield();
          for (int i = 0; i < numberIteration; i++) {
            logger.info(id + " transfer passive retr " + i);
            if (!client.transferFile(null,
                                     remoteFilename, false)) {
              logger.warn("Cant retrieve file passive mode " + id);
              FtpClientTest.numberKO.incrementAndGet();
              return;
            } else {
              FtpClientTest.numberOK.incrementAndGet();
              if (delay > 0) {
                try {
                  Thread.sleep(delay);
                } catch (InterruptedException e) {
                }
              }
            }
            // System.err.println(id+" end transfer retr "+i);
          }
          Thread.yield();
        }
      }
      if (type >= 0) {
        logger.warn(id + " change mode active");
        client.changeMode(false);
        if (type >= 10) {
          for (int i = 0; i < numberIteration; i++) {
            logger.info(id + " transfer active store " + i);
            if (!client.transferFile(localFilename,
                                     remoteFilename, true)) {
              logger.warn("Cant store file active mode " + id);
              FtpClientTest.numberKO.incrementAndGet();
              return;
            } else {
              FtpClientTest.numberOK.incrementAndGet();
              if (delay > 0) {
                try {
                  Thread.sleep(delay);
                } catch (InterruptedException e) {
                }
              }
            }
            // System.err.println(id+" transfer store end "+i);
          }
          Thread.yield();
        } else {
          for (int i = 0; i < numberIteration; i++) {
            logger.info(id + " transfer active store " + i);
            if (!client.transferFile(localFilename,
                                     remoteFilename, true)) {
              logger.warn("Cant store file active mode " + id);
              FtpClientTest.numberKO.incrementAndGet();
              return;
            } else {
              FtpClientTest.numberOK.incrementAndGet();
              if (delay > 0) {
                try {
                  Thread.sleep(delay);
                } catch (InterruptedException e) {
                }
              }
            }
            if (!client.deleteFile(remoteFilename)) {
              logger.warn("Cant delete file active mode " + id);
              FtpClientTest.numberKO.incrementAndGet();
              return;
            } else {
              FtpClientTest.numberOK.incrementAndGet();
              if (delay > 0) {
                try {
                  Thread.sleep(delay);
                } catch (InterruptedException e) {
                }
              }
            }
            // System.err.println(id+" end transfer retr "+i);
          }
          if (!client.transferFile(localFilename,
                                   remoteFilename, true)) {
            logger.warn("Cant store file active mode " + id);
            FtpClientTest.numberKO.incrementAndGet();
            return;
          } else {
            FtpClientTest.numberOK.incrementAndGet();
            if (delay > 0) {
              try {
                Thread.sleep(delay);
              } catch (InterruptedException e) {
              }
            }
          }
          Thread.yield();
          for (int i = 0; i < numberIteration; i++) {
            logger.info(id + " transfer active retr " + i);
            if (!client.transferFile(null,
                                     remoteFilename, false)) {
              logger.warn("Cant retrieve file active mode " + id);
              FtpClientTest.numberKO.incrementAndGet();
              return;
            } else {
              FtpClientTest.numberOK.incrementAndGet();
              if (delay > 0) {
                try {
                  Thread.sleep(delay);
                } catch (InterruptedException e) {
                }
              }
            }
            // System.err.println(id+" end transfer retr "+i);
          }
        }
      }
      String[] results = client.executeSiteCommand("XCRC " + remoteFilename);
      for (String string : results) {
        logger.warn("XCRC: {}", string);
      }
      results = client.executeSiteCommand("XMD5 " + remoteFilename);
      for (String string : results) {
        logger.warn("XCRC: {}", string);
      }
      results = client.executeSiteCommand("XSHA1 " + remoteFilename);
      for (String string : results) {
        logger.warn("XCRC: {}", string);
      }
      results = client.listFiles();
      for (String string : results) {
        logger.warn("LIST: {}", string);
      }
    } finally {
      logger.warn(id + " disconnect {}:{}", FtpClientTest.numberOK.get(),
                  FtpClientTest.numberKO.get());
      client.logout();
      logger.info(id + " end disconnect");
    }
  }

}
