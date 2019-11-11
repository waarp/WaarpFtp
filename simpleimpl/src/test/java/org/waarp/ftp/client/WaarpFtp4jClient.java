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

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPCommunicationListener;
import it.sauronsoftware.ftp4j.FTPConnector;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;
import it.sauronsoftware.ftp4j.FTPReply;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * FTP client using FTP4J model (working in all modes)
 *
 * @author "Frederic Bregier"
 */
public class WaarpFtp4jClient {
  /**
   * Internal Logger
   */
  private static final WaarpLogger logger =
      WaarpLoggerFactory.getLogger(WaarpFtp4jClient.class);
  protected FTPClient ftpClient;
  protected String result;
  String server;
  int port = 21;
  String user;
  String pwd;
  String acct;
  int timeout;
  int keepalive;
  boolean isPassive;
  int ssl; // -1 native, 1 auth
  private boolean binaryTransfer = true;

  /**
   * @param server
   * @param port
   * @param user
   * @param pwd
   * @param acct
   * @param isPassive
   * @param ssl
   * @param timeout
   */
  public WaarpFtp4jClient(String server, int port,
                          String user, String pwd, String acct,
                          boolean isPassive, int ssl, int keepalive,
                          int timeout) {
    this.server = server;
    this.port = port;
    this.user = user;
    this.pwd = pwd;
    this.acct = acct;
    this.isPassive = isPassive;
    this.ssl = ssl;
    this.keepalive = keepalive;
    this.timeout = timeout;
    ftpClient = new FTPClient();
    if (this.ssl != 0) {
      // implicit or explicit
      TrustManager[] trustManager = {
          new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
              return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs,
                                           String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs,
                                           String authType) {
            }
          }
      };
      SSLContext sslContext = null;
      try {
        sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustManager, new SecureRandom());
      } catch (NoSuchAlgorithmException e) {
        throw new IllegalArgumentException("Bad algorithm", e);
      } catch (KeyManagementException e) {
        throw new IllegalArgumentException("Bad KeyManagement", e);
      }
      SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
      ftpClient.setSSLSocketFactory(sslSocketFactory);
      if (this.ssl < 0) {
        ftpClient.setSecurity(FTPClient.SECURITY_FTPS);
      } else {
        ftpClient.setSecurity(FTPClient.SECURITY_FTPES);
      }
    } else {
      ftpClient = new FTPClient();
    }
    if (timeout > 0) {
      System
          .setProperty("ftp4j.activeDataTransfer.acceptTimeout", "" + timeout);
    }
    System.setProperty("ftp4j.activeDataTransfer.hostAddress", "127.0.0.1");

    ftpClient.addCommunicationListener(new FTPCommunicationListener() {
      @Override
      public void sent(String arg0) {
        logger.debug("Command: " + arg0);
      }

      @Override
      public void received(String arg0) {
        logger.debug("Answer: " + arg0);
      }
    });
    FTPConnector connector = ftpClient.getConnector();
    connector.setCloseTimeout(timeout);
    connector.setReadTimeout(timeout);
    connector.setUseSuggestedAddressForDataConnections(true);
  }

  /**
   * @return the result associated with the last command
   */
  public String getResult() {
    return result;
  }

  /**
   * Try to connect to the server and goes with the authentication
   *
   * @return True if connected and authenticated, else False
   */
  public boolean connect() {
    result = null;
    boolean isActive = false;
    try {
      try {
        ftpClient.connect(server, port);
      } catch (SocketException e) {
        result = "Connection in error";
        logger.error(result, e);
        return false;
      } catch (IOException e) {
        result = "Connection in error";
        logger.error(result, e);
        return false;
      } catch (IllegalStateException e) {
        result = "Connection in error";
        logger.error(result, e);
        return false;
      } catch (FTPIllegalReplyException e) {
        result = "Connection in error";
        logger.error(result, e);
        return false;
      } catch (FTPException e) {
        result = "Connection in error";
        logger.error(result, e);
        return false;
      }
      try {
        if (acct == null) {
          // no account
          ftpClient.login(user, pwd);
        } else {
          ftpClient.login(user, pwd, acct);
        }
      } catch (IOException e) {
        result = "Login in error";
        logger.error(result, e);
        return false;
      } catch (IllegalStateException e) {
        logout();
        result = "Login in error";
        logger.error(result);
        return false;
      } catch (FTPIllegalReplyException e) {
        logout();
        result = "Login in error";
        logger.error(result);
        return false;
      } catch (FTPException e) {
        logout();
        result = "Login in error";
        logger.error(result);
        return false;
      }
      try {
        ftpClient.setType(FTPClient.TYPE_BINARY);
      } catch (IllegalArgumentException e1) {
        result = "Set BINARY in error";
        logger.error(result, e1);
        return false;
      }
      changeMode(isPassive);
      if (keepalive > 0) {
        ftpClient.setAutoNoopTimeout(keepalive);
      }
      isActive = true;
      return true;
    } finally {
      if (!isActive && !ftpClient.isPassive()) {
        disconnect();
      }
    }
  }

  /**
   * QUIT the control connection
   */
  public void logout() {
    ftpClient.setAutoNoopTimeout(0);
    logger.debug("QUIT");
    if (executeCommand("QUIT") == null) {
      try {
        ftpClient.logout();
      } catch (IOException e) {
        // do nothing
      } catch (IllegalStateException e) {
        // do nothing
      } catch (FTPIllegalReplyException e) {
        // do nothing
      } catch (FTPException e) {
        // do nothing
      } finally {
        if (!ftpClient.isPassive()) {
          disconnect();
        }
      }
    }
  }

  /**
   * Change to passive (true) or active (false) mode
   *
   * @param passive
   */
  public void changeMode(boolean passive) {
    isPassive = passive;
    ftpClient.setPassive(passive);
  }

  /**
   * Disconnect the Ftp Client
   */
  public void disconnect() {
    ftpClient.setAutoNoopTimeout(0);
    try {
      ftpClient.disconnect(false);
    } catch (IOException e) {
      logger.debug("Disconnection in error", e);
    } catch (IllegalStateException e) {
      logger.debug("Disconnection in error", e);
    } catch (FTPIllegalReplyException e) {
      logger.debug("Disconnection in error", e);
    } catch (FTPException e) {
      logger.debug("Disconnection in error", e);
    }
  }

  /**
   * @param params
   *
   * @return the string lines result for the command params
   */
  public String[] executeCommand(String params) {
    result = null;
    try {
      logger.debug(params);
      FTPReply reply = ftpClient.sendCustomCommand(params);
      if (!reply.isSuccessCode()) {
        result = reply.toString();
        return null;
      }
      return reply.getMessages();
    } catch (IOException e) {
      result = "Cannot execute operation Site";
      logger.error(result, e);
      return null;
    } catch (IllegalStateException e) {
      result = "Cannot execute operation Site";
      logger.error(result, e);
      return null;
    } catch (FTPIllegalReplyException e) {
      result = "Cannot execute operation Site";
      logger.error(result, e);
      return null;
    }
  }

  /**
   * Create a new directory
   *
   * @param newDir
   *
   * @return True if created
   */
  public boolean makeDir(String newDir) {
    result = null;
    try {
      ftpClient.createDirectory(newDir);
      return true;
    } catch (IOException e) {
      result = "MKDIR in error";
      logger.info(result, e);
      return false;
    } catch (IllegalStateException e) {
      result = "MKDIR in error";
      logger.info(result, e);
      return false;
    } catch (FTPIllegalReplyException e) {
      result = "MKDIR in error";
      logger.info(result, e);
      return false;
    } catch (FTPException e) {
      result = "MKDIR in error";
      logger.info(result, e);
      return false;
    }
  }

  /**
   * Change remote directory
   *
   * @param newDir
   *
   * @return True if the change is OK
   */
  public boolean changeDir(String newDir) {
    result = null;
    try {
      ftpClient.changeDirectory(newDir);
      return true;
    } catch (IOException e) {
      result = "CHDIR in error";
      logger.info(result, e);
      return false;
    } catch (IllegalStateException e) {
      result = "CHDIR in error";
      logger.info(result, e);
      return false;
    } catch (FTPIllegalReplyException e) {
      result = "CHDIR in error";
      logger.info(result, e);
      return false;
    } catch (FTPException e) {
      result = "CHDIR in error";
      logger.info(result, e);
      return false;
    }
  }

  /**
   * Change the FileType of Transfer (Binary true, ASCII false)
   *
   * @param binaryTransfer1
   *
   * @return True if the change is OK
   */
  public boolean changeFileType(boolean binaryTransfer1) {
    result = null;
    binaryTransfer = binaryTransfer1;
    try {
      if (binaryTransfer) {
        ftpClient.setType(FTPClient.TYPE_BINARY);
      } else {
        ftpClient.setType(FTPClient.TYPE_TEXTUAL);
      }
      return true;
    } catch (IllegalArgumentException e) {
      result = "FileType in error";
      logger.warn(result, e);
      return false;
    }
  }

  /**
   * Ask to transfer a file
   *
   * @param local local filepath (full path)
   * @param remote filename (basename)
   * @param getStoreOrAppend -1 = get, 1 = store, 2 = append
   *
   * @return True if the file is correctly transfered
   */
  public boolean transferFile(String local, String remote,
                              int getStoreOrAppend) {
    result = null;
    try {
      if (getStoreOrAppend > 0) {
        File from = new File(local);
        result = "Cannot finalize store like operation";
        logger.debug("Will STOR: " + from);
        try {
          if (getStoreOrAppend == 1) {
            ftpClient
                .upload(from, new DataTimeOutListener(ftpClient, timeout,
                                                      "STOR", local));
          } else {
            // append
            ftpClient
                .append(from, new DataTimeOutListener(ftpClient, timeout,
                                                      "APPE", local));
          }
          result = null;
        } catch (IllegalStateException e) {
          logger.error(result, e);
          return false;
        } catch (FTPIllegalReplyException e) {
          logger.error(result, e);
          return false;
        } catch (FTPException e) {
          logger.error(result, e);
          return false;
        } catch (FTPDataTransferException e) {
          logger.error(result, e);
          return false;
        } catch (FTPAbortedException e) {
          logger.error(result, e);
          return false;
        }
        return true;
      } else {
        result = "Cannot finalize retrieve like operation";
        if (local == null) {
          // test
          NullOutputStream nullOutputStream = new NullOutputStream();
          logger.debug("Will DLD nullStream: " + remote);
          try {
            ftpClient.download(remote, nullOutputStream, 0,
                               new DataTimeOutListener(ftpClient, timeout,
                                                       "RETR", remote));
            result = null;
          } catch (IllegalStateException e) {
            logger.error(result, e);
            return false;
          } catch (FTPIllegalReplyException e) {
            logger.error(result, e);
            return false;
          } catch (FTPException e) {
            logger.error(result, e);
            return false;
          } catch (FTPDataTransferException e) {
            logger.error(result, e);
            return false;
          } catch (FTPAbortedException e) {
            logger.error(result, e);
            return false;
          }
        } else {
          logger.debug("Will DLD to local: " + remote + " into " + local);
          File to = new File(local);
          try {
            ftpClient
                .download(remote, to, new DataTimeOutListener(ftpClient,
                                                              timeout, "RETR",
                                                              local));
            result = null;
          } catch (IllegalStateException e) {
            logger.error(result, e);
            return false;
          } catch (FTPIllegalReplyException e) {
            logger.error(result, e);
            return false;
          } catch (FTPException e) {
            logger.error(result, e);
            return false;
          } catch (FTPDataTransferException e) {
            logger.error(result, e);
            return false;
          } catch (FTPAbortedException e) {
            logger.error(result, e);
            return false;
          }
        }
        return true;
      }
    } catch (IOException e) {
      result = "Cannot finalize operation";
      logger.error(result, e);
      return false;
    }
  }

  /**
   * @return the list of Files as given by FTP
   */
  public String[] listFiles() {
    try {
      FTPFile[] list = ftpClient.list();
      String[] results = new String[list.length];
      int i = 0;
      for (FTPFile file : list) {
        results[i] = file.toString();
        i++;
      }
      return results;
    } catch (IOException e) {
      result = "Cannot finalize transfer operation";
      logger.error(result, e);
      return null;
    } catch (IllegalStateException e) {
      result = "Cannot finalize transfer operation";
      logger.error(result, e);
      return null;
    } catch (FTPIllegalReplyException e) {
      result = "Cannot finalize transfer operation";
      logger.error(result, e);
      return null;
    } catch (FTPException e) {
      result = "Cannot finalize transfer operation";
      logger.error(result, e);
      return null;
    } catch (FTPDataTransferException e) {
      result = "Cannot finalize transfer operation";
      logger.error(result, e);
      return null;
    } catch (FTPAbortedException e) {
      result = "Cannot finalize transfer operation";
      logger.error(result, e);
      return null;
    } catch (FTPListParseException e) {
      result = "Cannot finalize transfer operation";
      logger.error(result, e);
      return null;
    }
  }

  /**
   * @param feature
   *
   * @return True if the given feature is listed
   */
  public boolean featureEnabled(String feature) {
    try {
      FTPReply reply = ftpClient.sendCustomCommand("FEAT");
      String[] msg = reply.getMessages();
      for (String string : msg) {
        if (string.contains(feature.toUpperCase())) {
          return true;
        }
      }
      return false;
    } catch (IOException e) {
      result = "Cannot execute operation Feature";
      logger.error(result, e);
      return false;
    } catch (IllegalStateException e) {
      result = "Cannot execute operation Feature";
      logger.error(result, e);
      return false;
    } catch (FTPIllegalReplyException e) {
      result = "Cannot execute operation Feature";
      logger.error(result, e);
      return false;
    }
  }

  /**
   * @param remote
   *
   * @return True if deleted
   */
  public boolean deleteFile(String remote) {
    try {
      logger.debug("DELE {}", remote);
      ftpClient.deleteFile(remote);
      return true;
    } catch (IOException e) {
      result = "Cannot execute operation Site";
      logger.error(result, e);
      return false;
    } catch (IllegalStateException e) {
      result = "Cannot execute operation Site";
      logger.error(result, e);
      return false;
    } catch (FTPIllegalReplyException e) {
      result = "Cannot execute operation Site";
      logger.error(result, e);
      return false;
    } catch (FTPException e) {
      result = "Cannot execute operation Site";
      logger.error(result, e);
      return false;
    }
  }

  /**
   * @param params command without SITE in front
   *
   * @return the string lines result for the SITE command params
   */
  public String[] executeSiteCommand(String params) {
    result = null;
    try {
      logger.debug("SITE " + params);
      FTPReply reply = ftpClient.sendSiteCommand(params);
      if (!reply.isSuccessCode()) {
        result = reply.toString();
        return null;
      }
      return reply.getMessages();
    } catch (IOException e) {
      result = "Cannot execute operation Site";
      logger.error(result, e);
      return null;
    } catch (IllegalStateException e) {
      result = "Cannot execute operation Site";
      logger.error(result, e);
      return null;
    } catch (FTPIllegalReplyException e) {
      result = "Cannot execute operation Site";
      logger.error(result, e);
      return null;
    }
  }

}
