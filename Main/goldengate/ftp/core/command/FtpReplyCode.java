/**
 * Frederic Bregier LGPL 10 janv. 09 
 * FtpReplyCode.java goldengate.ftp.core.command GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command;

import goldengate.ftp.core.config.FtpInternalConfiguration;

/**
 * Reply code references by different RFC.
 * @author frederic
 * goldengate.ftp.core.command FtpReplyCode
 * 
 */
public enum FtpReplyCode {
	/**
     * 110 Restart marker reply. In this case, the text is exact and not left to
     * the particular implementation), it must read: MARK yyyy  (mmmm Where yyyy
     * is User-process data stream marker, and mmmm server's equivalent marker
     * (note the spaces between markers and "=").
     */
    REPLY_110_RESTART_MARKER_REPLY (110),

    /**
     * 120 Service ready in nnn minutes.
     */
    REPLY_120_SERVICE_READY_IN_NNN_MINUTES  (120),

    /**
     * 125 Data connection already open), transfer starting.
     */
    REPLY_125_DATA_CONNECTION_ALREADY_OPEN  (125),

    /**
     * 150 File status okay), about to open data connection.
     */
    REPLY_150_FILE_STATUS_OKAY  (150),

    /**
     * 200 Command okay.
     */
    REPLY_200_COMMAND_OKAY  (200),

    /**
     * 202 Command not implemented, superfluous at this site.
     */
    REPLY_202_COMMAND_NOT_IMPLEMENTED  (202),

    /**
     * 211 System status, or system help reply.
     */
    REPLY_211_SYSTEM_STATUS_REPLY  (211),

    /**
     * 212 Directory status.
     */
    REPLY_212_DIRECTORY_STATUS  (212),

    /**
     * 213 File status.
     */
    REPLY_213_FILE_STATUS  (213),

    /**
     * 214 Help message. On how to use the server or the meaning of a particular
     * non-standard command. This reply is useful only to the human user.
     */
    REPLY_214_HELP_MESSAGE  (214,"This FTP server refers to RFC 959, RFC 775, RFC 2389 and RFC 3659"),

    /**
     * 215 NAME system type. Where NAME is an official system name from the list
     * in the Assigned Numbers document.
     */
    REPLY_215_NAME_SYSTEM_TYPE  (215),

    /**
     * 220 Service ready for new user.
     */
    REPLY_220_SERVICE_READY  (220),

    /**
     * Service closing control connection. Logged out if appropriate.
     */
    REPLY_221_CLOSING_CONTROL_CONNECTION  (221),

    /**
     * 225 Data connection open), no transfer in progress.
     */
    REPLY_225_DATA_CONNECTION_OPEN_NO_TRANSFER_IN_PROGRESS  (225),

    /**
     * Closing data connection. Requested file action successful (for example,
     * file transfer or file abort).
     */
    REPLY_226_CLOSING_DATA_CONNECTION  (226),

    /**
     * 227 Entering Passive Mode (h1,h2,h3,h4,p1,p2).
     */
    REPLY_227_ENTERING_PASSIVE_MODE  (227),

    /**
     * 229 Entering Extended Passive Mode (|n|addr|port|).
     */
    REPLY_229_ENTERING_PASSIVE_MODE  (227),

    /**
     * 230 User logged in, proceed.
     */
    REPLY_230_USER_LOGGED_IN  (230),

    /**
     * 250 Requested file action okay, completed.
     */
    REPLY_250_REQUESTED_FILE_ACTION_OKAY  (250),

    /**
     * 257 "PATHNAME" created.
     */
    REPLY_257_PATHNAME_CREATED  (257),

    /**
     * 331 User name okay, need password.
     */
    REPLY_331_USER_NAME_OKAY_NEED_PASSWORD  (331),

    /**
     * 332 Need account for login.
     */
    REPLY_332_NEED_ACCOUNT_FOR_LOGIN  (332),

    /**
     * 350 Requested file action pending further information.
     */
    REPLY_350_REQUESTED_FILE_ACTION_PENDING_FURTHER_INFORMATION  (350),

    /**
     * 421 Service not available, closing control connection. This may be a
     * reply to any command if the service knows it must shut down.
     */
    REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION  (421),

    /**
     * 425 Can't open data connection.
     */
    REPLY_425_CANT_OPEN_DATA_CONNECTION  (425),

    /**
     * 426 Connection closed), transfer aborted.
     */
    REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED  (426),

    /**
     * 450 Requested file action not taken. File unavailable (e.g., file busy).
     */
    REPLY_450_REQUESTED_FILE_ACTION_NOT_TAKEN  (450),

    /**
     * 451 Requested action aborted: local error in processing.
     */
    REPLY_451_REQUESTED_ACTION_ABORTED  (451),

    /**
     * 452 Requested action not taken. Insufficient storage space in system.
     */
    REPLY_452_REQUESTED_ACTION_NOT_TAKEN  (452),

    /**
     * 500 Syntax error, command unrecognized. This may include errors such as
     * command line too long.
     */
    REPLY_500_SYNTAX_ERROR_COMMAND_UNRECOGNIZED  (500),

    /**
     * 501 Syntax error in parameters or arguments.
     */
    REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS  (501),

    /**
     * 502 Command not implemented.
     */
    REPLY_502_COMMAND_NOT_IMPLEMENTED  (502),

    /**
     * 503 Bad sequence of commands.
     */
    REPLY_503_BAD_SEQUENCE_OF_COMMANDS  (503),

    /**
     * 504 Command not implemented for that parameter.
     */
    REPLY_504_COMMAND_NOT_IMPLEMENTED_FOR_THAT_PARAMETER  (504),
    
    /**
     * 522 Extended Port Failure - unknown network protocol.
     */
    REPLY_522_EXTENDED_PORT_FAILURE_UNKNOWN_NETWORK_PROTOCOL (522),
    
    /**
     * 530 Not logged in.
     */
    REPLY_530_NOT_LOGGED_IN  (530),

    /**
     * 532 Need account for storing files.
     */
    REPLY_532_NEED_ACCOUNT_FOR_STORING_FILES  (532),

    /**
     * 550 Requested action not taken. File unavailable (e.g., file not found,
     * no access).
     */
    REPLY_550_REQUESTED_ACTION_NOT_TAKEN  (550),

    /**
     * 551 Requested action aborted: page type unknown.
     */
    REPLY_551_REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN  (551),

    /**
     * 552 Requested file action aborted. Exceeded storage allocation (for
     * current directory or dataset).
     */
    REPLY_552_REQUESTED_FILE_ACTION_ABORTED_EXCEEDED_STORAGE  (552),

    /**
     * 553 Requested action not taken. File name not allowed.
     */
    REPLY_553_REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED  (553);
    
    /**
     * Numerical code
     */
	private int code;
	/**
	 * Message associated
	 */
	private String mesg;
	/**
	 * Construct a Reply code from specific message
	 * @param code
	 * @param mesg
	 */
	private FtpReplyCode(int code, String mesg) {
		this.code = code;
		this.mesg = getFinalMsg(code, mesg);
	}
	/**
	 * Return the final message formatted as needed from the code and the message
	 * @param code
	 * @param msg
	 * @return the final formatted message
	 */
	public static String getFinalMsg(int code, String msg) {
		StringBuilder builder = new StringBuilder();
		builder.append(code);
		if (msg.indexOf('\n') == -1) {
			builder.append(' ');
			builder.append(msg);
			builder.append(FtpInternalConfiguration.CRLF);
		} else {
			String []lines = msg.split("\n");
			// first line
			builder.append('-');
			builder.append(lines[0]);
			builder.append(FtpInternalConfiguration.CRLF);
			// next lines
			for (int i = 1; i < lines.length-1; i++) {
				int firstBlank = lines[i].indexOf(' ');
				if (firstBlank > 0) {
					String firstParam = lines[i].substring(0, firstBlank);
					boolean isInt = false;
					try {
						Integer.parseInt(firstParam);
						isInt = true;
					} catch (NumberFormatException e) {
						// not a number
					}
					if (isInt) {
						builder.append("  ");
					}
				}
				builder.append(lines[i]);
				builder.append(FtpInternalConfiguration.CRLF);
			}
			// last line
			builder.append(code);
			builder.append(' ');
			builder.append(lines[lines.length-1]);
			builder.append(FtpInternalConfiguration.CRLF);
		}
		return builder.toString();
	}
	/**
	 * Construct a Reply Code from its name in Enum structure
	 * @param code
	 */
	private FtpReplyCode(int code) {
		this.code = code;
		this.mesg = this.name().substring(6).replace('_', ' ')+FtpInternalConfiguration.CRLF;
	}
	/**
	 * @return the code
	 */
	public int getCode() {
		return this.code;
	}

	/**
	 * @return the mesg
	 */
	public String getMesg() {
		return this.mesg;
	}
}
