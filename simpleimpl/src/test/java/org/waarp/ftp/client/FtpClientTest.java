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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.waarp.common.logging.WaarpLogLevel;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.common.logging.WaarpSlf4JLoggerFactory;
import org.waarp.common.utility.DetectionUtils;
import org.waarp.ftp.FtpServer;
import org.waarp.ftp.client.transaction.Ftp4JClientTransactionTest;
import org.waarp.ftp.client.transaction.FtpClientThread;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static junit.framework.TestCase.*;

/**
 * Simple test example using predefined scenario (Note: this uses the configuration example for user shutdown
 * command)
 *
 * @author frederic
 *
 */
public class FtpClientTest {
  public static AtomicLong numberOK = new AtomicLong(0);
  public static AtomicLong numberKO = new AtomicLong(0);
  /**
   * Internal Logger
   */
  protected static WaarpLogger logger =
      WaarpLoggerFactory.getLogger(FtpClientTest.class);

  /**
   * @param args
   */
  public static void main(String[] args) {
    WaarpLoggerFactory.setDefaultFactory(new WaarpSlf4JLoggerFactory(null));
    System.setProperty("javax.net.debug", "false");

    String server = null;
    int port = 21;
    String username = null;
    String passwd = null;
    String account = null;
    String localFilename = null;
    int numberThread = 1;
    int numberIteration = 1;
    if (args.length < 8) {
      System.err.println("Usage: " + FtpClientTest.class.getSimpleName()
                         +
                         " server port user pwd acct localfilename nbThread nbIter");
      DetectionUtils.SystemExit(1);
      return;
    }
    server = args[0];
    port = Integer.parseInt(args[1]);
    username = args[2];
    passwd = args[3];
    account = args[4];
    localFilename = args[5];
    numberThread = Integer.parseInt(args[6]);
    numberIteration = Integer.parseInt(args[7]);
    int type = 0;
    if (args.length > 8) {
      type = Integer.parseInt(args[8]);
    } else {
      System.out.println("Both ways");
    }
    int delay = 0;
    if (args.length > 9) {
      delay = Integer.parseInt(args[9]);
    }
    int isSSL = 0;
    if (args.length > 10) {
      isSSL = Integer.parseInt(args[10]);
    }
    boolean shutdown = false;
    if (args.length > 11) {
      shutdown = Integer.parseInt(args[11]) > 0;
    }
    FtpClientTest ftpClient = new FtpClientTest();
    ftpClient.testFtp4J(server, port, username, passwd, account, isSSL,
                        localFilename, type, delay, shutdown,
                        numberThread, numberIteration);
  }

  public void testFtp4J(String server, int port, String username, String passwd,
                        String account, int isSSL,
                        String localFilename, int type, int delay,
                        boolean shutdown, int numberThread,
                        int numberIteration) {
    // initiate Directories
    Ftp4JClientTransactionTest client =
        new Ftp4JClientTransactionTest(server, port, username, passwd, account,
                                       isSSL);

    logger.warn("First connexion");
    if (!client.connect()) {
      logger.error("Can't connect");
      FtpClientTest.numberKO.incrementAndGet();
      return;
    }
    try {
      logger.warn("Create Dirs");
      for (int i = 0; i < numberThread; i++) {
        client.makeDir("T" + i);
      }
      logger.warn("Feature commands");
      System.err.println("SITE: " + client.featureEnabled("SITE"));
      System.err.println("SITE CRC: " + client.featureEnabled("SITE XCRC"));
      System.err.println("CRC: " + client.featureEnabled("XCRC"));
      System.err.println("MD5: " + client.featureEnabled("XMD5"));
      System.err.println("SHA1: " + client.featureEnabled("XSHA1"));
    } finally {
      logger.warn("Logout");
      client.logout();
    }
    if (isSSL > 0) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
      }
    }
    ExecutorService executorService = Executors.newCachedThreadPool();
    logger.warn("Will start {} Threads", numberThread);
    long date1 = System.currentTimeMillis();
    for (int i = 0; i < numberThread; i++) {
      executorService.execute(
          new FtpClientThread("T" + i, server, port, username, passwd, account,
                              localFilename,
                              numberIteration, type, delay, isSSL));
      if (delay > 0) {
        try {
          long newdel = ((delay / 3) / 10) * 10;
          if (newdel == 0) {
            Thread.yield();
          } else {
            Thread.sleep(newdel);
          }
        } catch (InterruptedException e) {
        }
      } else {
        Thread.yield();
      }
    }
    try {
      Thread.sleep(100);
    } catch (InterruptedException e1) {
      e1.printStackTrace();
      executorService.shutdownNow();
      // Thread.currentThread().interrupt();
    }
    executorService.shutdown();
    long date2 = 0;
    try {
      if (!executorService.awaitTermination(12000, TimeUnit.SECONDS)) {
        date2 = System.currentTimeMillis() - 120000 * 60;
        executorService.shutdownNow();
        if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
          System.err.println("Really not shutdown normally");
        }
      } else {
        date2 = System.currentTimeMillis();
      }
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      executorService.shutdownNow();
      date2 = System.currentTimeMillis();
      // Thread.currentThread().interrupt();
    }

    logger.warn(
        localFilename + " " + numberThread + " " + numberIteration + " " +
        type + " Real: "
        + (date2 - date1) + " OK: " + numberOK.get() + " KO: " +
        numberKO.get() + " Trf/s: "
        + numberOK.get() * 1000 / (date2 - date1));
    assertTrue("No KO", numberKO.get() == 0);
  }

  @BeforeClass
  public static void startServer() throws IOException {
    WaarpLoggerFactory
        .setDefaultFactory(new WaarpSlf4JLoggerFactory(WaarpLogLevel.WARN));

    FtpServer.startFtpServer("config.xml");
    File localFilename = new File("/tmp/ftpfile.bin");
    FileWriter fileWriterBig = new FileWriter(localFilename);
    for (int i = 0; i < 100; i++) {
      fileWriterBig.write("0123456789");
    }
    fileWriterBig.flush();
    fileWriterBig.close();
    logger.warn("Will start server");
  }

  @AfterClass
  public static void stopServer() {
    logger.warn("Will shutdown from client");
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
    }
    Ftp4JClientTransactionTest client =
        new Ftp4JClientTransactionTest("127.0.0.1", 2021, "fredo", "fred1", "a",
                                       0);
    if (!client.connect()) {
      logger.warn("Cant connect");
      numberKO.incrementAndGet();
      return;
    }
    try {
      String[] results = client.executeSiteCommand("internalshutdown abcdef");
      System.err.print("SHUTDOWN: ");
      for (String string : results) {
        System.err.println(string);
      }
    } finally {
      client.disconnect();
    }
    logger.warn("Will stop server");
    FtpServer.stopFtpServer();
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
    }
  }

  @Test
  public void testFtp4JSimple() throws IOException {
    numberKO.set(0);
    numberOK.set(0);
    File localFilename = new File("/tmp/ftpfile.bin");
    testFtp4J("127.0.0.1", 2021, "fred", "fred2", "a", 0,
              localFilename.getAbsolutePath(), 0, 50, true, 1, 1);
  }

}
