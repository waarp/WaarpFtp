/**
   This file is part of GoldenGate Project (named also GoldenGate or GG).

   Copyright 2009, Frederic Bregier, and individual contributors by the @author
   tags. See the COPYRIGHT.txt in the distribution for a full listing of
   individual contributors.

   All GoldenGate Project is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   GoldenGate is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with GoldenGate .  If not, see <http://www.gnu.org/licenses/>.
 */
package goldengate.ftp.simpleimpl;

import goldengate.common.file.filesystembased.FilesystemBasedDirImpl;
import goldengate.common.file.filesystembased.FilesystemBasedFileParameterImpl;
import goldengate.common.file.filesystembased.specific.FilesystemBasedDirJdk5;
import goldengate.common.file.filesystembased.specific.FilesystemBasedDirJdk6;
import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;
import goldengate.common.logging.GgSlf4JLoggerFactory;
import goldengate.ftp.core.config.FtpConfiguration;
import goldengate.ftp.simpleimpl.config.FileBasedConfiguration;
import goldengate.ftp.simpleimpl.control.SimpleBusinessHandler;
import goldengate.ftp.simpleimpl.data.FileSystemBasedDataBusinessHandler;

import org.jboss.netty.logging.InternalLoggerFactory;

/**
 * Example of FTP Server using simple authentication (XML FileInterface based),
 * and standard Directory and FileInterface implementation (Filesystem based).
 *
 * @author Frederic Bregier
 *
 */
public class SimpleGatewayFtpServer {
    /**
     * Internal Logger
     */
    private static GgInternalLogger logger = null;

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
        InternalLoggerFactory.setDefaultFactory(new GgSlf4JLoggerFactory(null));
        logger = GgInternalLoggerFactory
                .getLogger(SimpleGatewayFtpServer.class);
        String config = args[0];
        FileBasedConfiguration configuration = new FileBasedConfiguration(
                SimpleGatewayFtpServer.class, SimpleBusinessHandler.class,
                FileSystemBasedDataBusinessHandler.class,
                new FilesystemBasedFileParameterImpl());
        if (!configuration.setConfigurationFromXml(config)) {
            System.err.println("Bad configuration");
            return;
        }
        // Init according JDK
        if (FtpConfiguration.USEJDK6) {
            FilesystemBasedDirImpl.initJdkDependent(new FilesystemBasedDirJdk6());
        } else {
            FilesystemBasedDirImpl.initJdkDependent(new FilesystemBasedDirJdk5());
        }
        // Start server.
        configuration.serverStartup();
        logger.warn("FTP started");
    }

}
