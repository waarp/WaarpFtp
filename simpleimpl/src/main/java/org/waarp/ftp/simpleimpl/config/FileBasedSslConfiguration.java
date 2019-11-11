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

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.waarp.common.crypto.ssl.WaarpSecureKeyStore;
import org.waarp.common.crypto.ssl.WaarpSslContextFactory;
import org.waarp.common.exception.CryptoException;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.common.xml.XmlDecl;
import org.waarp.common.xml.XmlHash;
import org.waarp.common.xml.XmlType;
import org.waarp.common.xml.XmlUtil;
import org.waarp.common.xml.XmlValue;
import org.waarp.ftp.core.config.FtpConfiguration;
import org.waarp.ftp.core.control.ftps.FtpsInitializer;

/**
 * FtpConfiguration based on a XML file
 * 
 * @author Frederic Bregier
 * 
 */
public class FileBasedSslConfiguration {
    /**
     * Internal Logger
     */
    private static final WaarpLogger logger = WaarpLoggerFactory
            .getLogger(FileBasedSslConfiguration.class);

    /**
     * SERVER SSL STOREKEY PATH
     */
    private static final String XML_PATH_KEYPATH = "keypath";

    /**
     * SERVER SSL KEY PASS
     */
    private static final String XML_PATH_KEYPASS = "keypass";

    /**
     * SERVER SSL STOREKEY PASS
     */
    private static final String XML_PATH_KEYSTOREPASS = "keystorepass";

    /**
     * SERVER SSL TRUSTSTOREKEY PATH
     */
    private static final String XML_PATH_TRUSTKEYPATH = "trustkeypath";

    /**
     * SERVER SSL TRUSTSTOREKEY PASS
     */
    private static final String XML_PATH_TRUSTKEYSTOREPASS = "trustkeystorepass";

    /**
     * SERVER SSL Use TrustStore for Client Authentication
     */
    private static final String XML_USECLIENT_AUTHENT = "trustuseclientauthenticate";

    /**
     * Structure of the Configuration file
     * 
     */
    private static final XmlDecl[] configSslDecls = {
            // ssl
            new XmlDecl(XmlType.STRING, XML_PATH_KEYPATH),
            new XmlDecl(XmlType.STRING, XML_PATH_KEYSTOREPASS),
            new XmlDecl(XmlType.STRING, XML_PATH_KEYPASS),
            new XmlDecl(XmlType.STRING, XML_PATH_TRUSTKEYPATH),
            new XmlDecl(XmlType.STRING, XML_PATH_TRUSTKEYSTOREPASS),
            new XmlDecl(XmlType.BOOLEAN, XML_USECLIENT_AUTHENT)
    };
    /**
     * Overall structure of the Configuration file
     */
    private static final String XML_ROOT = "/config/";
    private static final String XML_SSL = "ssl";
    /**
     * Global Structure for Server Configuration
     */
    private static final XmlDecl[] configServer = {
            new XmlDecl(XML_SSL, XmlType.XVAL, XML_ROOT + XML_SSL, configSslDecls, false)
    };
    private static XmlValue[] configuration = null;
    private static XmlHash hashConfig = null;

    protected static boolean loadSsl(FtpConfiguration config) {
        // StoreKey for Server
        XmlValue value = hashConfig.get(XML_PATH_KEYPATH);
        if (value == null || (value.isEmpty())) {
            logger.info("Unable to find Key Path");
            try {
                FtpsInitializer.waarpSecureKeyStore =
                        new WaarpSecureKeyStore("secret", "secret");
            } catch (CryptoException e) {
                logger.error("Bad SecureKeyStore construction");
                return false;
            }
        } else {
            String keypath = value.getString();
            if ((keypath == null) || (keypath.length() == 0)) {
                logger.error("Bad Key Path");
                return false;
            }
            value = hashConfig.get(XML_PATH_KEYSTOREPASS);
            if (value == null || (value.isEmpty())) {
                logger.error("Unable to find KeyStore Passwd");
                return false;
            }
            String keystorepass = value.getString();
            if ((keystorepass == null) || (keystorepass.length() == 0)) {
                logger.error("Bad KeyStore Passwd");
                return false;
            }
            value = hashConfig.get(XML_PATH_KEYPASS);
            if (value == null || (value.isEmpty())) {
                logger.error("Unable to find Key Passwd");
                return false;
            }
            String keypass = value.getString();
            if ((keypass == null) || (keypass.length() == 0)) {
                logger.error("Bad Key Passwd");
                return false;
            }
            try {
                FtpsInitializer.waarpSecureKeyStore =
                        new WaarpSecureKeyStore(keypath, keystorepass,
                                keypass);
            } catch (CryptoException e) {
                logger.error("Bad SecureKeyStore construction");
                return false;
            }

        }
        // TrustedKey for OpenR66 server
        value = hashConfig.get(XML_PATH_TRUSTKEYPATH);
        if (value == null || (value.isEmpty())) {
            logger.info("Unable to find TRUST Key Path");
            FtpsInitializer.waarpSecureKeyStore.initEmptyTrustStore();
        } else {
            String keypath = value.getString();
            if ((keypath == null) || (keypath.length() == 0)) {
                logger.error("Bad TRUST Key Path");
                return false;
            }
            value = hashConfig.get(XML_PATH_TRUSTKEYSTOREPASS);
            if (value == null || (value.isEmpty())) {
                logger.error("Unable to find TRUST KeyStore Passwd");
                return false;
            }
            String keystorepass = value.getString();
            if ((keystorepass == null) || (keystorepass.length() == 0)) {
                logger.error("Bad TRUST KeyStore Passwd");
                return false;
            }
            boolean useClientAuthent = false;
            value = hashConfig.get(XML_USECLIENT_AUTHENT);
            if (value != null && (!value.isEmpty())) {
                useClientAuthent = value.getBoolean();
            }
            try {
                FtpsInitializer.waarpSecureKeyStore.initTrustStore(keypath,
                        keystorepass, useClientAuthent);
            } catch (CryptoException e) {
                logger.error("Bad TrustKeyStore construction");
                return false;
            }
        }
        FtpsInitializer.waarpSslContextFactory =
                new WaarpSslContextFactory(
                        FtpsInitializer.waarpSecureKeyStore);
        return true;
    }

    /**
     * Initiate the configuration from the xml file for server
     * 
     * @param filename
     * @return True if OK
     */
    public static boolean setConfigurationServerFromXml(FtpConfiguration config, String filename) {
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
        configuration = XmlUtil.read(document, configServer);
        hashConfig = new XmlHash(configuration);
        // Now read the configuration
        if (!loadSsl(config)) {
            logger.error("Cannot load SSL configuration");
            return false;
        }
        hashConfig.clear();
        hashConfig = null;
        configuration = null;
        return true;
    }
}
