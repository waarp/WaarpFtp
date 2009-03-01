/**
 * Frederic Bregier LGPL 24 janv. 09 
 * FtpArgumentCode.java goldengate.ftp.core.command GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command;

import goldengate.ftp.core.exception.FtpInvalidArgumentException;

import java.nio.charset.Charset;
import java.util.Set;
import java.util.SortedMap;

/**
 * Definition of all Argument of Parameter commands (MODE, STRU, TYPE)
 * @author frederic
 * goldengate.ftp.core.command FtpArgumentCode
 * 
 */
public class FtpArgumentCode {

	/**
	 * Type of transmission
	 * @author frederic
	 * goldengate.ftp.core.data TransferType
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
		EBCDIC('E', "ebcdic-cp-us"),// could be ebcdic-cp-LG where LG is language like fr, gb, ...
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
		public String charsetName;
		
		private TransferType(char type) {
			this.type = type;
			this.charsetName = null;
		}
		private TransferType(char type, String charsetName) {
			this.type = type;
			this.charsetName = charsetName;
		}
	}

	/**
	 * SubType of transmission
	 * @author frederic
	 * goldengate.ftp.core.data TransferSubType
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
	 * @author frederic
	 * goldengate.ftp.core.data TransferStructure
	 *
	 */
	public static enum TransferStructure {
		/**
		 * File TransferStructure
		 */
		FILE ('F'),
		/**
		 * Record TransferStructure
		 */
		RECORD ('R'),
		/**
		 * Page TransferStructure
		 */
		PAGE ('P');
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
	 * @author frederic
	 * goldengate.ftp.core.data TransferMode
	 *
	 */
	public static enum TransferMode {
		/**
		 * Stream TransferMode
		 */
		STREAM ('S'),
		/**
		 * Block TransferMode
		 */
		BLOCK ('B'),
		/**
		 * Compressed TransferMode
		 */
		COMPRESSED ('C');
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
	 * @param type
	 * @return the corresponding TransferType
	 * @exception FtpInvalidArgumentException if the type is unknown
	 */
	public static FtpArgumentCode.TransferType getTransferType(char type) throws FtpInvalidArgumentException {
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
		}
		throw new FtpInvalidArgumentException("Argument for TransferType is not allowed: "+type);
	}
	/**
	 * Get the TransferSubType according to the char
	 * @param subType
	 * @return the corresponding TransferSubType
	 * @exception FtpInvalidArgumentException if the TransferSubType is unknown
	 */
	public static FtpArgumentCode.TransferSubType getTransferSubType(char subType) throws FtpInvalidArgumentException {
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
		}
		throw new FtpInvalidArgumentException("Argument for TransferSubType is not allowed: "+subType);
	}
	/**
	 * Get the TransferStructure according to the char
	 * @param structure
	 * @return the corresponding TransferStructure
	 * @exception FtpInvalidArgumentException if the TransferStructure is unknown
	 */
	public static FtpArgumentCode.TransferStructure getTransferStructure(char structure) throws FtpInvalidArgumentException {
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
		}
		throw new FtpInvalidArgumentException("Argument for TransferStructure is not allowed: "+structure);
	}
	/**
	 * Get the TransferMode according to the char
	 * @param mode
	 * @return the corresponding TransferMode
	 * @exception FtpInvalidArgumentException if the TransferMode is unknown
	 */
	public static FtpArgumentCode.TransferMode getTransferMode(char mode) throws FtpInvalidArgumentException {
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
		}
		throw new FtpInvalidArgumentException("Argument for TransferMode is not allowed: "+mode);
	}
	
	/**
	 * List all charsets supported by the current platform
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
				System.out.println("   "+alias);
			}
		}
	}
}
