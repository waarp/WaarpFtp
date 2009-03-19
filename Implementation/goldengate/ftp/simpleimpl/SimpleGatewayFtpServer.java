/**
 * 
 */
package goldengate.ftp.simpleimpl;

import org.jboss.netty.logging.InternalLoggerFactory;

import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;
import goldengate.ftp.filesystembased.FilesystemBasedFtpDir;
import goldengate.ftp.filesystembased.specific.FilesystemBasedFtpDirJdk6;
import goldengate.ftp.simpleimpl.config.FileBasedConfiguration;
import goldengate.ftp.simpleimpl.control.SimpleBusinessHandler;
import goldengate.ftp.simpleimpl.data.FileSystemBasedDataBusinessHandler;
import goldengate.ftp.simpleimpl.logging.Slf4JLoggerFactory;
import ch.qos.logback.classic.Level;

/**
 * Example of FTP Server using simple authentication (XML File based), and
 * standard Directory and File implementation (Filesystem based).
 * 
 * @author fbregier
 * 
 */
public class SimpleGatewayFtpServer {
    /**
     * Internal Logger
     */
    private static FtpInternalLogger logger = null;

    /**
     * Take a simple XML file as configuration.
     * 
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: " +
                    SimpleGatewayFtpServer.class.getName() + " <config-file>");
            return;
        }
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory(
                Level.WARN));
        logger = FtpInternalLoggerFactory
                .getLogger(SimpleGatewayFtpServer.class);
        String config = args[0];
        FileBasedConfiguration configuration = new FileBasedConfiguration(
                SimpleGatewayFtpServer.class, SimpleBusinessHandler.class,
                FileSystemBasedDataBusinessHandler.class);
        if (!configuration.setConfigurationFromXml(config)) {
            System.err.println("Bad configuration");
            return;
        }
        // Init according JDK
        FilesystemBasedFtpDir.initJdkDependent(new FilesystemBasedFtpDirJdk6());
        // Start server.
        configuration.serverStartup();
        logger.warn("FTP started");
    }

}
