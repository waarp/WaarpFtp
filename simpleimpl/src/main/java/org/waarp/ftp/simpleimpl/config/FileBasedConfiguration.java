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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import io.netty.handler.traffic.AbstractTrafficShapingHandler;
import org.waarp.common.digest.FilesystemBasedDigest;
import org.waarp.common.file.FileParameterInterface;
import org.waarp.common.file.filesystembased.FilesystemBasedDirImpl;
import org.waarp.common.file.filesystembased.FilesystemBasedFileParameterImpl;
import org.waarp.common.file.filesystembased.specific.FilesystemBasedDirJdkAbstract;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.ftp.core.config.FtpConfiguration;
import org.waarp.ftp.core.control.BusinessHandler;
import org.waarp.ftp.core.data.handler.DataBusinessHandler;
import org.waarp.ftp.simpleimpl.file.SimpleAuth;

/**
 * FtpConfiguration based on a XML file
 * 
 * @author Frederic Bregier
 * 
 */
public class FileBasedConfiguration extends FtpConfiguration {
    /**
     * Internal Logger
     */
    private static final WaarpLogger logger = WaarpLoggerFactory
            .getLogger(FileBasedConfiguration.class);

    /**
     * SERVER PASSWORD (shutdown)
     */
    private static final String XML_SERVER_PASSWD = "/config/serverpasswd";

    /**
     * SERVER PORT
     */
    private static final String XML_SERVER_PORT = "/config/serverport";
    /**
     * SERVER ADDRESS if any
     */
    private static final String XML_SERVER_ADDRESS = "/config/serveraddress";
    /**
     * Base Directory
     */
    private static final String XML_SERVER_HOME = "/config/serverhome";

    /**
     * Default number of threads in pool for Server.
     */
    private static final String XML_SERVER_THREAD = "/config/serverthread";

    /**
     * Default number of threads in pool for Client.
     */
    private static final String XML_CLIENT_THREAD = "/config/clientthread";

    /**
     * Limit per session
     */
    private static final String XML_LIMITSESSION = "/config/sessionlimit";

    /**
     * Limit global
     */
    private static final String XML_LIMITGLOBAL = "/config/globallimit";

    /**
     * Nb of milliseconds after connection is in timeout
     */
    private static final String XML_TIMEOUTCON = "/config/timeoutcon";

    /**
     * Should a file be deleted when a Store like command is aborted
     */
    private static final String XML_DELETEONABORT = "/config/deleteonabort";

    /**
     * Should a file MD5 SHA1 be computed using NIO
     */
    private static final String XML_USENIO = "/config/usenio";

    /**
     * Should a file MD5 be computed using FastMD5
     */
    private static final String XML_USEFASTMD5 = "/config/usefastmd5";

    /**
     * Size by default of block size for receive/sending files. Should be a multiple of 8192
     * (maximum = 64K due to block limitation to 2 bytes)
     */
    private static final String XML_BLOCKSIZE = "/config/blocksize";

    /**
     * RANGE of PORT for Passive Mode
     */
    private static final String XML_RANGE_PORT_MIN = "/config/rangeport/min";

    /**
     * RANGE of PORT for Passive Mode
     */
    private static final String XML_RANGE_PORT_MAX = "/config/rangeport/max";

    /**
     * Authentication
     */
    private static final String XML_AUTHENTIFICATION_FILE = "/config/authentfile";

    /**
     * Authentication Fields
     */
    private static final String XML_AUTHENTIFICATION_BASED = "/authent/entry";

    /**
     * Authentication Fields
     */
    private static final String XML_AUTHENTIFICATION_USER = "user";

    /**
     * Authentication Fields
     */
    private static final String XML_AUTHENTIFICATION_PASSWD = "passwd";

    /**
     * Authentication Fields
     */
    private static final String XML_AUTHENTIFICATION_ACCOUNT = "account";

    /**
     * Authentication Fields
     */
    private static final String XML_AUTHENTIFICATION_ADMIN = "admin";

    /**
     * RANGE of PORT for Passive Mode
     */
    private CircularIntValue RANGE_PORT = null;

    /**
     * All authentications
     */
    private final ConcurrentHashMap<String, SimpleAuth> authentications = new ConcurrentHashMap<String, SimpleAuth>();

    /**
     * @param classtype
     * @param businessHandler
     *            class that will be used for BusinessHandler
     * @param dataBusinessHandler
     *            class that will be used for DataBusinessHandler
     * @param fileParameter
     *            the FileParameter to use
     */
    public FileBasedConfiguration(Class<?> classtype,
            Class<? extends BusinessHandler> businessHandler,
            Class<? extends DataBusinessHandler> dataBusinessHandler,
            FileParameterInterface fileParameter) {
        super(classtype, businessHandler, dataBusinessHandler, fileParameter);
        computeNbThreads();
    }

    /**
     * Initiate the configuration from the xml file
     * 
     * @param filename
     * @return True if OK
     */
    @SuppressWarnings("unchecked")
    public boolean setConfigurationFromXml(String filename) {
        Document document = null;
        // Open config file
        try {
            document = new SAXReader().read(filename);
        } catch (DocumentException e) {
            logger.error("Unable to read the XML Config file: " + filename, e);
            return false;
        }
        if (document == null) {
            logger.error("Unable to read the XML Config file: " + filename);
            return false;
        }
        Node nodebase, node = null;
        node = document.selectSingleNode(XML_SERVER_PASSWD);
        if (node == null) {
            logger.error("Unable to find Password in Config file: " + filename);
            return false;
        }
        String passwd = node.getText();
        setPassword(passwd);
        node = document.selectSingleNode(XML_SERVER_PORT);
        int port = 21;
        if (node != null) {
            port = Integer.parseInt(node.getText());
        }
        setServerPort(port);
        node = document.selectSingleNode(XML_SERVER_ADDRESS);
        String address = null;
        if (node != null) {
            address = node.getText();
        }
        setServerAddress(address);
        node = document.selectSingleNode(XML_SERVER_HOME);
        if (node == null) {
            logger.error("Unable to find Home in Config file: " + filename);
            return false;
        }
        String path = node.getText();
        File file = new File(path);
        try {
            setBaseDirectory(FilesystemBasedDirImpl.normalizePath(file
                    .getCanonicalPath()));
        } catch (IOException e1) {
            logger.error("Unable to set Home in Config file: " + filename);
            return false;
        }
        if (!file.isDirectory()) {
            logger.error("Home is not a directory in Config file: " + filename);
            return false;
        }
        node = document.selectSingleNode(XML_SERVER_THREAD);
        if (node != null) {
            setSERVER_THREAD(Integer.parseInt(node.getText()));
        }
        node = document.selectSingleNode(XML_CLIENT_THREAD);
        if (node != null) {
            setCLIENT_THREAD(Integer.parseInt(node.getText()));
        }
        if (getSERVER_THREAD() == 0 || getCLIENT_THREAD() == 0) {
            computeNbThreads();
        }
        node = document.selectSingleNode(XML_LIMITGLOBAL);
        if (node != null) {
            serverGlobalReadLimit = Long.parseLong(node.getText());
            if (serverGlobalReadLimit <= 0) {
                serverGlobalReadLimit = 0;
            }
            serverGlobalWriteLimit = serverGlobalReadLimit;
            logger.warn("Global Limit: {}", serverGlobalReadLimit);
        }
        node = document.selectSingleNode(XML_LIMITSESSION);
        if (node != null) {
            serverChannelReadLimit = Long.parseLong(node.getText());
            if (serverChannelReadLimit <= 0) {
                serverChannelReadLimit = 0;
            }
            serverChannelWriteLimit = serverChannelReadLimit;
            logger.warn("SessionInterface Limit: {}", serverChannelReadLimit);
        }
        delayLimit = AbstractTrafficShapingHandler.DEFAULT_CHECK_INTERVAL;
        node = document.selectSingleNode(XML_TIMEOUTCON);
        if (node != null) {
            setTIMEOUTCON(Integer.parseInt(node.getText()));
        }
        node = document.selectSingleNode(XML_DELETEONABORT);
        if (node != null) {
            setDeleteOnAbort(Integer.parseInt(node.getText()) == 1 ? true : false);
        }
        node = document.selectSingleNode(XML_USENIO);
        if (node != null) {
            FilesystemBasedFileParameterImpl.useNio = Integer.parseInt(node
                    .getText()) == 1 ? true : false;
        }
        node = document.selectSingleNode(XML_USEFASTMD5);
        if (node != null) {
            FilesystemBasedDigest.setUseFastMd5(Integer.parseInt(node.getText()) == 1 ? true
                    : false);
        } else {
            FilesystemBasedDigest.setUseFastMd5(false);
        }
        node = document.selectSingleNode(XML_BLOCKSIZE);
        if (node != null) {
            setBLOCKSIZE(Integer.parseInt(node.getText()));
        }
        node = document.selectSingleNode(XML_RANGE_PORT_MIN);
        int min = 100;
        if (node != null) {
            min = Integer.parseInt(node.getText());
        }
        node = document.selectSingleNode(XML_RANGE_PORT_MAX);
        int max = 65535;
        if (node != null) {
            max = Integer.parseInt(node.getText());
        }
        CircularIntValue rangePort = new CircularIntValue(min, max);
        setRangePort(rangePort);
        // We use Apache Commons IO
        FilesystemBasedDirJdkAbstract.ueApacheCommonsIo = true;
        node = document.selectSingleNode(XML_AUTHENTIFICATION_FILE);
        if (node == null) {
            logger.error("Unable to find Authentication file in Config file: " +
                    filename);
            return false;
        }
        String fileauthent = node.getText();
        document = null;
        try {
            document = new SAXReader().read(fileauthent);
        } catch (DocumentException e) {
            logger.error("Unable to read the XML Authentication file: " +
                    fileauthent, e);
            return false;
        }
        if (document == null) {
            logger.error("Unable to read the XML Authentication file: " +
                    fileauthent);
            return false;
        }
        List<Node> list = document.selectNodes(XML_AUTHENTIFICATION_BASED);
        Iterator<Node> iterator = list.iterator();
        while (iterator.hasNext()) {
            nodebase = iterator.next();
            node = nodebase.selectSingleNode(XML_AUTHENTIFICATION_USER);
            if (node == null) {
                continue;
            }
            String user = node.getText();
            node = nodebase.selectSingleNode(XML_AUTHENTIFICATION_PASSWD);
            if (node == null) {
                continue;
            }
            String userpasswd = node.getText();
            node = nodebase.selectSingleNode(XML_AUTHENTIFICATION_ADMIN);
            boolean isAdmin = false;
            if (node != null) {
                isAdmin = node.getText().equals("1") ? true : false;
            }
            List<Node> listaccount = nodebase
                    .selectNodes(XML_AUTHENTIFICATION_ACCOUNT);
            String[] account = null;
            if (!listaccount.isEmpty()) {
                account = new String[listaccount.size()];
                int i = 0;
                Iterator<Node> iteratoraccount = listaccount.iterator();
                while (iteratoraccount.hasNext()) {
                    node = iteratoraccount.next();
                    account[i] = node.getText();
                    // logger.debug("User: {} Acct: {}", user, account[i]);
                    i++;
                }
            }
            SimpleAuth auth = new SimpleAuth(user, userpasswd, account);
            auth.setAdmin(isAdmin);
            authentications.put(user, auth);
        }
        document = null;
        return true;
    }

    /**
     * @param user
     * @return the SimpleAuth if any for this user
     */
    public SimpleAuth getSimpleAuth(String user) {
        return authentications.get(user);
    }

    /**
     * @see org.waarp.ftp.core.config.FtpConfiguration#getNextRangePort()
     */
    @Override
    public int getNextRangePort() {
        return RANGE_PORT.getNext();
    }

    /**
     * 
     * @param rangePort
     *            the range of available ports for Passive connections
     */
    private void setRangePort(CircularIntValue rangePort) {
        RANGE_PORT = rangePort;
    }

    @Override
    public void inShutdownProcess() {
        // nothing to do
    }
}
