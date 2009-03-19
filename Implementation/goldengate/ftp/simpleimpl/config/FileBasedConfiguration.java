/**
 * 
 */
package goldengate.ftp.simpleimpl.config;

import goldengate.ftp.core.control.BusinessHandler;
import goldengate.ftp.core.data.handler.DataBusinessHandler;
import goldengate.ftp.core.exception.FtpUnknownFieldException;
import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;
import goldengate.ftp.filesystembased.FilesystemBasedFtpDir;
import goldengate.ftp.filesystembased.config.FilesystemBasedFtpConfiguration;
import goldengate.ftp.filesystembased.digest.MD5;
import goldengate.ftp.simpleimpl.auth.SimpleAuth;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.jboss.netty.handler.trafficshaping.PerformanceCounterFactory;

/**
 * FtpConfiguration based on a XML file
 * 
 * @author fbregier
 * 
 */
public class FileBasedConfiguration extends FilesystemBasedFtpConfiguration {
    /**
     * Internal Logger
     */
    private static final FtpInternalLogger logger = FtpInternalLoggerFactory
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
     * Base Directory
     */
    private static final String XML_SERVER_HOME = "/config/serverhome";

    /**
     * Default number of threads in pool for Server.
     */
    private static final String XML_SERVER_THREAD = "/config/serverthread";

    /**
     * Limit per session
     */
    private static final String XML_LIMITSESSION = "/config/sessionlimit";

    /**
     * Limit global
     */
    private static final String XML_LIMITGLOBAL = "/config/globallimit";

    /**
     * Nb of milliseconds after connexion is in timeout
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
     * If using Fast MD5, should we used the binary JNI library, empty meaning
     * no
     */
    private static final String XML_FASTMD5 = "/config/fastmd5";

    /**
     * Size by default of block size for receive/sending files. Should be a
     * multiple of 8192 (maximum = 64K due to block limitation to 2 bytes)
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
    private static final String RANGE_PORT = "FTP_RANGE_PORT";

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
     */
    public FileBasedConfiguration(Class<?> classtype,
            Class<? extends BusinessHandler> businessHandler,
            Class<? extends DataBusinessHandler> dataBusinessHandler) {
        super(classtype, businessHandler, dataBusinessHandler);
        this.computeNbThreads();
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
        this.setPassword(passwd);
        node = document.selectSingleNode(XML_SERVER_PORT);
        int port = 21;
        if (node != null) {
            port = Integer.parseInt(node.getText());
        }
        this.setServerPort(port);
        node = document.selectSingleNode(XML_SERVER_HOME);
        if (node == null) {
            logger.error("Unable to find Home in Config file: " + filename);
            return false;
        }
        String path = node.getText();
        File file = new File(path);
        try {
            this.setBaseDirectory(FilesystemBasedFtpDir.normalizePath(file
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
            this.SERVER_THREAD = Integer.parseInt(node.getText());
        }
        node = document.selectSingleNode(XML_LIMITGLOBAL);
        if (node != null) {
            this.serverGlobalReadLimit = Long.parseLong(node.getText());
            if (this.serverGlobalReadLimit == -1) {
                this.serverGlobalReadLimit = PerformanceCounterFactory.NO_LIMIT;
            }
            this.serverGlobalWriteLimit = this.serverGlobalReadLimit;
            logger.warn("Global Limit: {}", this.serverGlobalReadLimit);
        }
        node = document.selectSingleNode(XML_LIMITSESSION);
        if (node != null) {
            this.serverChannelReadLimit = Long.parseLong(node.getText());
            if (this.serverChannelWriteLimit == -1) {
                this.serverChannelWriteLimit = PerformanceCounterFactory.NO_LIMIT;
            }
            this.serverChannelWriteLimit = this.serverChannelReadLimit;
            logger.warn("Session Limit: {}", this.serverChannelReadLimit);
        }
        this.delayLimit = PerformanceCounterFactory.DEFAULT_DELAY;
        node = document.selectSingleNode(XML_TIMEOUTCON);
        if (node != null) {
            this.TIMEOUTCON = Integer.parseInt(node.getText());
        }
        node = document.selectSingleNode(XML_DELETEONABORT);
        if (node != null) {
            this.deleteOnAbort = (Integer.parseInt(node.getText()) == 1)? true
                    : false;
        }
        node = document.selectSingleNode(XML_USENIO);
        if (node != null) {
            useNio = (Integer.parseInt(node.getText()) == 1)? true : false;
        }
        node = document.selectSingleNode(XML_USEFASTMD5);
        if (node != null) {
            useFastMd5 = (Integer.parseInt(node.getText()) == 1)? true : false;
            if (useFastMd5) {
                node = document.selectSingleNode(XML_FASTMD5);
                if (node != null) {
                    fastMd5Path = node.getText();
                    if ((fastMd5Path == null) || (fastMd5Path.length() == 0)) {
                        fastMd5Path = null;
                    } else {
                        MD5.initNativeLibrary(fastMd5Path);
                    }
                }
            } else {
                fastMd5Path = null;
            }
        }
        node = document.selectSingleNode(XML_BLOCKSIZE);
        if (node != null) {
            this.BLOCKSIZE = Integer.parseInt(node.getText());
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
        this.setRangePort(rangePort);
        // We use Apache Commons IO
        ueApacheCommonsIo = true;
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
                isAdmin = (node.getText().equals("1"))? true : false;
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
                    logger.debug("User: {} Acct: {}", user, account[i]);
                    i ++;
                }
            }
            SimpleAuth auth = new SimpleAuth(user, userpasswd, account);
            auth.setAdmin(isAdmin);
            this.authentications.put(user, auth);
        }
        document = null;
        return true;
    }

    /**
     * @param user
     * @return the SimpleAuth if any for this user
     */
    public SimpleAuth getSimpleAuth(String user) {
        return this.authentications.get(user);
    }

    /**
     * @see goldengate.ftp.core.config.FtpConfiguration#getNextRangePort()
     */
    @Override
    public int getNextRangePort() {
        try {
            return ((CircularIntValue) this.getProperty(RANGE_PORT)).getNext();
        } catch (FtpUnknownFieldException e) {
            return (-1);
        }
    }

    /**
     * 
     * @param rangePort
     *            the range of available ports for Passive connections
     */
    private void setRangePort(CircularIntValue rangePort) {
        this.setProperty(RANGE_PORT, rangePort);
    }
}
