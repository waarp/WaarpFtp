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
package org.waarp.ftp.core.command;

import java.nio.charset.Charset;
import java.util.Set;
import java.util.SortedMap;

import org.waarp.common.exception.InvalidArgumentException;

/**
 * Definition of all Argument of Parameter commands (MODE, STRU, TYPE)
 * 
 * @author Frederic Bregier
 * 
 */
public class FtpArgumentCode {

    /**
     * Type of transmission
     * 
     * @author Frederic Bregier org.waarp.ftp.core.data TransferType
     * 
     */
    public static enum TransferType {
        /**
         * Ascii TransferType
         */
        ASCII('A', "ASCII"),
        /**
         * Ebcdic TransferType
         */
        EBCDIC('E', "ebcdic-cp-us"), // could be ebcdic-cp-LG where LG is
        // language like fr, gb, ...
        /**
         * Image TransferType
         */
        IMAGE('I'),
        /**
         * Specific Length TransferType
         */
        LENGTH('L');
        /**
         * TransferType
         */
        public char type;

        /**
         * Charset Name if any
         */
        public Charset charset;

        private TransferType(char type) {
            this.type = type;
            charset = Charset.defaultCharset();
        }

        private TransferType(char type, String charsetName) {
            this.type = type;
            this.charset = Charset.forName(charsetName);
        }
    }

    /**
     * SubType of transmission
     * 
     * @author Frederic Bregier org.waarp.ftp.core.data TransferSubType
     * 
     */
    public static enum TransferSubType {
        /**
         * Non-print TransferSubType
         */
        NONPRINT('N'),
        /**
         * Telnet format effectors TransferSubType
         */
        TELNET('T'),
        /**
         * Carriage Control ASA TransferSubType
         */
        CARRIAGE('C');
        /**
         * TransferSubType
         */
        public char subtype;

        private TransferSubType(char subtype) {
            this.subtype = subtype;
        }
    }

    /**
     * Structure of transmission
     * 
     * @author Frederic Bregier org.waarp.ftp.core.data TransferStructure
     * 
     */
    public static enum TransferStructure {
        /**
         * FileInterface TransferStructure
         */
        FILE('F'),
        /**
         * Record TransferStructure
         */
        RECORD('R'),
        /**
         * Page TransferStructure
         */
        PAGE('P');
        /**
         * TransferStructure
         */
        public char structure;

        private TransferStructure(char structure) {
            this.structure = structure;
        }
    }

    /**
     * Mode of transmission
     * 
     * @author Frederic Bregier org.waarp.ftp.core.data TransferMode
     * 
     */
    public static enum TransferMode {
        /**
         * Stream TransferMode
         */
        STREAM('S'),
        /**
         * Block TransferMode
         */
        BLOCK('B'),
        /**
         * Compressed TransferMode
         */
        COMPRESSED('C');
        /**
         * TransferMode
         */
        public char mode;

        private TransferMode(char mode) {
            this.mode = mode;
        }
    }

    /**
     * Get the TransferType according to the char
     * 
     * @param type
     * @return the corresponding TransferType
     * @exception InvalidArgumentException
     *                if the type is unknown
     */
    public static FtpArgumentCode.TransferType getTransferType(char type)
            throws InvalidArgumentException {
        switch (type) {
            case 'A':
            case 'a':
                return FtpArgumentCode.TransferType.ASCII;
            case 'E':
            case 'e':
                return FtpArgumentCode.TransferType.EBCDIC;
            case 'I':
            case 'i':
                return FtpArgumentCode.TransferType.IMAGE;
            case 'L':
            case 'l':
                return FtpArgumentCode.TransferType.LENGTH;
            default:
                throw new InvalidArgumentException(
                        "Argument for TransferType is not allowed: " + type);
        }
    }

    /**
     * Get the TransferSubType according to the char
     * 
     * @param subType
     * @return the corresponding TransferSubType
     * @exception InvalidArgumentException
     *                if the TransferSubType is unknown
     */
    public static FtpArgumentCode.TransferSubType getTransferSubType(
            char subType) throws InvalidArgumentException {
        switch (subType) {
            case 'C':
            case 'c':
                return FtpArgumentCode.TransferSubType.CARRIAGE;
            case 'N':
            case 'n':
                return FtpArgumentCode.TransferSubType.NONPRINT;
            case 'T':
            case 't':
                return FtpArgumentCode.TransferSubType.TELNET;
            default:
                throw new InvalidArgumentException(
                        "Argument for TransferSubType is not allowed: " +
                                subType);
        }
    }

    /**
     * Get the TransferStructure according to the char
     * 
     * @param structure
     * @return the corresponding TransferStructure
     * @exception InvalidArgumentException
     *                if the TransferStructure is unknown
     */
    public static FtpArgumentCode.TransferStructure getTransferStructure(
            char structure) throws InvalidArgumentException {
        switch (structure) {
            case 'P':
            case 'p':
                return FtpArgumentCode.TransferStructure.PAGE;
            case 'F':
            case 'f':
                return FtpArgumentCode.TransferStructure.FILE;
            case 'R':
            case 'r':
                return FtpArgumentCode.TransferStructure.RECORD;
            default:
                throw new InvalidArgumentException(
                        "Argument for TransferStructure is not allowed: " +
                                structure);
        }
    }

    /**
     * Get the TransferMode according to the char
     * 
     * @param mode
     * @return the corresponding TransferMode
     * @exception InvalidArgumentException
     *                if the TransferMode is unknown
     */
    public static FtpArgumentCode.TransferMode getTransferMode(char mode)
            throws InvalidArgumentException {
        switch (mode) {
            case 'B':
            case 'b':
                return FtpArgumentCode.TransferMode.BLOCK;
            case 'C':
            case 'c':
                return FtpArgumentCode.TransferMode.COMPRESSED;
            case 'S':
            case 's':
                return FtpArgumentCode.TransferMode.STREAM;
            default:
                throw new InvalidArgumentException(
                        "Argument for TransferMode is not allowed: " + mode);
        }
    }

    /**
     * List all charsets supported by the current platform
     * 
     * @param args
     */
    public static void main(String args[]) {
        SortedMap<String, Charset> charsets = Charset.availableCharsets();
        Set<String> names = charsets.keySet();
        for (String name : names) {
            Charset charset = charsets.get(name);
            System.out.println(charset);
            Set<String> aliases = charset.aliases();
            for (String alias : aliases) {
                System.out.println("   " + alias);
            }
        }
    }
}
