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
package org.waarp.ftp.simpleimpl;

import org.waarp.common.file.filesystembased.FilesystemBasedFileParameterImpl;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.common.logging.WaarpSlf4JLoggerFactory;
import org.waarp.ftp.core.exception.FtpNoConnectionException;
import org.waarp.ftp.simpleimpl.config.FileBasedConfiguration;
import org.waarp.ftp.simpleimpl.control.SimpleBusinessHandler;
import org.waarp.ftp.simpleimpl.data.FileSystemBasedDataBusinessHandler;

/**
 * Example of FTP Server using simple authentication (XML FileInterface based), and standard
 * Directory and FileInterface implementation (Filesystem based).
 * 
 * @author Frederic Bregier
 * 
 */
public class SimpleGatewayFtpServer {
    /**
     * Internal Logger
     */
    private static WaarpLogger logger = null;

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
        WaarpLoggerFactory.setDefaultFactory(new WaarpSlf4JLoggerFactory(null));
        logger = WaarpLoggerFactory.getLogger(SimpleGatewayFtpServer.class);
        String config = args[0];
        FileBasedConfiguration configuration = new FileBasedConfiguration(
                SimpleGatewayFtpServer.class, SimpleBusinessHandler.class,
                FileSystemBasedDataBusinessHandler.class,
                new FilesystemBasedFileParameterImpl());
        if (!configuration.setConfigurationFromXml(config)) {
            System.err.println("Bad configuration");
            return;
        }
        // Start server.
        try {
            configuration.serverStartup();
        } catch (FtpNoConnectionException e) {
            logger.error("FTP not started", e);
        }
        logger.info("FTP started");
    }

}
