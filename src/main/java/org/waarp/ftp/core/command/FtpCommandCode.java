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

import org.waarp.ftp.core.command.internal.ConnectionCommand;
import org.waarp.ftp.core.command.internal.IncorrectCommand;
import org.waarp.ftp.core.command.internal.UnimplementedCommand;
import org.waarp.ftp.core.command.internal.UnknownCommand;
import org.waarp.ftp.core.file.FtpFile;
import org.waarp.ftp.core.session.FtpSession;

/**
 * This class must reassemble all the commands that could be implemented. The comment says the
 * object of the command and the kind of returned codes that could follow this command.<br>
 * <br>
 * Command structure:<br>
 * Main class<br>
 * Previous Valid Command (null means all are valid)<br>
 * Next Valid Commands (none means all are valid)<br>
 * 
 * @author Frederic Bregier
 * 
 */
public enum FtpCommandCode {
    // XXX CONNECTION
    /**
     * Command to simulate the beginning of a connection in order to force the authentication step.<br>
     * 
     * 
     * 120->220<br>
     * 220<br>
     * 421<br>
     */
    Connection(
            ConnectionCommand.class,
            null,
            org.waarp.ftp.core.command.access.USER.class, org.waarp.ftp.core.command.rfc2389.FEAT.class),
    // XXX ACCESS CONTROL COMMAND
    /**
     * The argument field is a Telnet string identifying the user. The user identification is that
     * which is required by the server for access to its file system. This command will normally be
     * the first command transmitted by the user after the control connections are made (some
     * servers may require this). Additional identification information in the form of a password
     * and/or an account command may also be required by some servers. Servers may allow a new USER
     * command to be entered at any point in order to change the access control and/or accounting
     * information. This has the effect of flushing any user, password, and account information
     * already supplied and beginning the login sequence again. All transfer parameters are
     * unchanged and any file transfer in progress is completed under the old access control
     * parameters.<br>
     * 
     * 230<br>
     * 530<br>
     * 500, 501, 421<br>
     * 331, 332<br>
     */
    USER(org.waarp.ftp.core.command.access.USER.class, ConnectionCommand.class),
    /**
     * The argument field is a Telnet string specifying the user's password. This command must be
     * immediately preceded by the user name command, and, for some sites, completes the user's
     * identification for access control. Since password information is quite sensitive, it is
     * desirable in general to "mask" it or suppress typeout. It appears that the server has no
     * foolproof way to achieve this. It is therefore the responsibility of the user-FTP process to
     * hide the sensitive password information.<br>
     * 
     * 
     * 230<br>
     * 202<br>
     * 530<br>
     * 500, 501, 503, 421<br>
     * 332<br>
     */
    PASS(org.waarp.ftp.core.command.access.PASS.class, null),
    /**
     * The argument field is a Telnet string identifying the user's account. The command is not
     * necessarily related to the USER command, as some sites may require an account for login and
     * others only for specific access, such as storing files. In the latter case the command may
     * arrive at any time.<br>
     * <br>
     * 
     * There are reply codes to differentiate these cases for the automation: when account
     * information is required for login, the response to a successful PASSword command is reply
     * code 332. On the other hand, if account information is NOT required for login, the reply to a
     * successful PASSword command is 230; and if the account information is needed for a command
     * issued later in the dialogue, the server should return a 332 or 532 reply depending on
     * whether it stores (pending receipt of the ACCounT command) or discards the command,
     * respectively.<br>
     * 
     * 
     * 230<br>
     * 202<br>
     * 530<br>
     * 500, 501, 503, 421<br>
     */
    ACCT(org.waarp.ftp.core.command.access.ACCT.class, null),
    /**
     * This command allows the user to work with a different directory or dataset for file storage
     * or retrieval without altering his login or accounting information. Transfer parameters are
     * similarly unchanged. The argument is a pathname specifying a directory or other system
     * dependent file group designator.<br>
     * 
     * 
     * 250<br>
     * 500, 501, 502, 421, 530, 550<br>
     */
    CWD(org.waarp.ftp.core.command.directory.CWD.class, null),
    /**
     * This command is a special case of CWD, and is included to simplify the implementation of
     * programs for transferring directory trees between operating systems having different syntaxes
     * for naming the parent directory. The reply codes shall be identical to the reply codes of
     * CWD. See Appendix II for further details.<br>
     * 
     * 
     * 200<br>
     * 500, 501, 502, 421, 530, 550<br>
     */
    CDUP(org.waarp.ftp.core.command.directory.CDUP.class, null),
    /**
     * This command allows the user to mount a different file system data structure without altering
     * his login or accounting information. Transfer parameters are similarly unchanged. The
     * argument is a pathname specifying a directory or other system dependent file group
     * designator.<br>
     * <br>
     * As for now, this command will not be implemented, so returns 502.<br>
     * 
     * 
     * 202, 250<br>
     * 500, 501, 502, 421, 530, 550<br>
     */
    // XXX 502
    SMNT(org.waarp.ftp.core.command.directory.SMNT.class, null),
    /**
     * This command terminates a USER, flushing all I/O and account information, except to allow any
     * transfer in progress to be completed. All parameters are reset to the default settings and
     * the control connection is left open. This is identical to the state in which a user finds
     * himself immediately after the control connection is opened. A USER command may be expected to
     * follow.<br>
     * <br>
     * 
     * 120<br>
     * 220<br>
     * 220<br>
     * 421<br>
     * 500, 502<br>
     */
    REIN(org.waarp.ftp.core.command.access.REIN.class, null,
            org.waarp.ftp.core.command.access.USER.class),
    /**
     * This command terminates a USER and if file transfer is not in progress, the server closes the
     * control connection. If file transfer is in progress, the connection will remain open for
     * result response and the server will then close it. If the user-process is transferring files
     * for several USERs but does not wish to close and then reopen connections for each, then the
     * REIN command should be used instead of QUIT.<br>
     * <br>
     * 
     * An unexpected close on the control connection will cause the server to take the effective
     * action of an abort (ABOR) and a logout (QUIT).<br>
     * 
     * 
     * 221<br>
     * 500<br>
     */
    QUIT(org.waarp.ftp.core.command.access.QUIT.class, null),

    // XXX TRANSFER PARAMETER COMMAND
    /**
     * The argument is a HOST-PORT specification for the data port to be used in data connection.
     * There are defaults for both the user and server data ports, and under normal circumstances
     * this command and its reply are not needed. If this command is used, the argument is the
     * concatenation of a 32-bit internet host address and a 16-bit TCP port address. This address
     * information is broken into 8-bit fields and the value of each field is transmitted as a
     * decimal number (in character string representation). The fields are separated by commas. A
     * port command would be:<br>
     * <br>
     * 
     * <pre>
     * PORT h1,h2,h3,h4,p1,p2
     * </pre>
     * 
     * where h1 is the high order 8 bits of the internet host address.<br>
     * 
     * 
     * 
     * 200<br>
     * 500, 501, 421, 530<br>
     */
    PORT(org.waarp.ftp.core.command.parameter.PORT.class, null),
    /**
     * This command requests the server-DTP to "listen" on a data port (which is not its default
     * data port) and to wait for a connection rather than initiate one upon receipt of a transfer
     * command. The response to this command includes the host and port address this server is
     * listening on.<br>
     * 
     * 
     * 
     * 227<br>
     * 500, 501, 502, 421, 530<br>
     */
    PASV(org.waarp.ftp.core.command.parameter.PASV.class, null),
    /**
     * The argument specifies the representation type as described in the Section on Data
     * Representation and Storage. Several types take a second parameter. The first parameter is
     * denoted by a single Telnet character, as is the second Format parameter for ASCII and EBCDIC;
     * the second parameter for local byte is a decimal integer to indicate Bytesize. The parameters
     * are separated by a <code>&lt;SP&gt;</code> (Space, ASCII code 32).<br>
     * <br>
     * 
     * The following codes are assigned for type:<br>
     * 
     * <pre>
     * \    /
     *                A - ASCII |    | N - Non-print
     *                          |-&gt;&lt;-| T - Telnet format effectors
     *                E - EBCDIC|    | C - Carriage Control (ASA)
     *                          /    \
     *                I - Image
     *                L &lt;byte size&gt; - Local byte Byte size
     * </pre>
     * 
     * The default representation type is ASCII Non-print. If the Format parameter is changed, and
     * later just the first argument is changed, Format then returns to the Non-print default.<br>
     * 
     * 200<br>
     * 500, 501, 504, 421, 530<br>
     */
    TYPE(org.waarp.ftp.core.command.parameter.TYPE.class, null),
    /**
     * The argument is a single Telnet character code specifying file structure described in the
     * Section on Data Representation and Storage.<br>
     * <br>
     * 
     * The following codes are assigned for structure:<br>
     * 
     * <pre>
     * F - FtpFile (no record structure)
     *                R - Record structure
     *                P - Page structure
     * </pre>
     * 
     * The default structure is FtpFile.<br>
     * 
     * 
     * 200<br>
     * 500, 501, 504, 421, 530<br>
     */
    STRU(org.waarp.ftp.core.command.parameter.STRU.class, null),
    /**
     * The argument is a single Telnet character code specifying the data transfer modes described
     * in the Section on Transmission Modes.<br>
     * <br>
     * 
     * The following codes are assigned for transfer modes:<br>
     * 
     * <pre>
     * S - Stream
     *                B - Block
     *                C - Compressed
     * </pre>
     * 
     * The default transfer mode is Stream.<br>
     * 
     * 
     * 200<br>
     * 500, 501, 504, 421, 530<br>
     */
    MODE(org.waarp.ftp.core.command.parameter.MODE.class, null),

    // XXX FTP SERVICE COMMAND
    /**
     * This command causes the server-DTP to transfer a copy of the file, specified in the pathname,
     * to the server- or user-DTP at the other end of the data connection. The status and contents
     * of the file at the server site shall be unaffected.<br>
     * 
     * 125, 150<br>
     * (110)<br>
     * 226, 250<br>
     * 425, 426, 451<br>
     * 450, 550<br>
     * 500, 501, 421, 530<br>
     */
    RETR(org.waarp.ftp.core.command.service.RETR.class, null),
    /**
     * This command causes the server-DTP to accept the data transferred via the data connection and
     * to store the data as a file at the server site. If the file specified in the pathname exists
     * at the server site, then its contents shall be replaced by the data being transferred. A new
     * file is created at the server site if the file specified in the pathname does not already
     * exist.<br>
     * 
     * 
     * 125, 150<br>
     * (110)<br>
     * 226, 250<br>
     * 425, 426, 451, 551, 552<br>
     * 532, 450, 452, 553<br>
     * 500, 501, 421, 530<br>
     */
    STOR(org.waarp.ftp.core.command.service.STOR.class, null),
    /**
     * This command behaves like STOR except that the resultant file is to be created in the current
     * directory under a name unique to that directory. The 250 Transfer Started response must
     * include the name generated.<br>
     * 
     * 
     * 125, 150<br>
     * (110)<br>
     * 226, 250<br>
     * 425, 426, 451, 551, 552<br>
     * 532, 450, 452, 553<br>
     * 500, 501, 421, 530<br>
     */
    STOU(org.waarp.ftp.core.command.service.STOU.class, null),
    /**
     * This command causes the server-DTP to accept the data transferred via the data connection and
     * to store the data in a file at the server site. If the file specified in the pathname exists
     * at the server site, then the data shall be appended to that file; otherwise the file
     * specified in the pathname shall be created at the server site.<br>
     * 
     * 
     * 125, 150<br>
     * (110)<br>
     * 226, 250<br>
     * 425, 426, 451, 551, 552<br>
     * 532, 450, 452, 553<br>
     * 500, 501, 421, 530<br>
     */
    APPE(org.waarp.ftp.core.command.service.APPE.class, null),
    /**
     * This command may be required by some servers to reserve sufficient storage to accommodate the
     * new file to be transferred. The argument shall be a decimal integer representing the number
     * of bytes (using the logical byte size) of storage to be reserved for the file. For files sent
     * with record or page structure a maximum record or page size (in logical bytes) might also be
     * necessary; this is indicated by a decimal integer in a second argument field of the command.
     * This second argument is optional, but when present should be separated from the first by the
     * three Telnet characters <code>&lt;SP&gt;</code> R <code>&lt;SP&gt;</code>. This command shall
     * be followed by a STORe or APPEnd command. The ALLO command should be treated as a NOOP (no
     * operation) by those servers which do not require that the maximum size of the file be
     * declared beforehand, and those servers interested in only the maximum record or page size
     * should accept a dummy value in the first argument and ignore it.<br>
     * 
     * 
     * 125, 150<br>
     * 226, 250<br>
     * 425, 426, 451<br>
     * 450<br>
     * 500, 501, 502, 421, 530<br>
     */
    ALLO(org.waarp.ftp.core.command.service.ALLO.class, null),
    /**
     * The argument field represents the server marker at which file transfer is to be restarted.
     * This command does not cause file transfer but skips over the file to the specified data
     * checkpoint. This command shall be immediately followed by the appropriate FTP service command
     * which shall cause file transfer to resume.<br>
     * <br>
     * The current implementation allows restart only on Stream since others would imply to store
     * those informations somewhere (how?).<br>
     * <br>
     * However, it could be changed if necessary by modifying the {@link FtpFile} restartMarker
     * method.<br>
     * <br>
     * This command will accept commands of transfer parameter following since some clients do this.<br>
     * 
     * 500, 501, 502, 421, 530<br>
     * 350<br>
     */
    REST(
            org.waarp.ftp.core.command.service.REST.class,
            null,
            org.waarp.ftp.core.command.service.RETR.class,
            org.waarp.ftp.core.command.service.STOR.class,
            org.waarp.ftp.core.command.service.STOU.class,
            org.waarp.ftp.core.command.service.APPE.class,
            org.waarp.ftp.core.command.parameter.PORT.class,
            org.waarp.ftp.core.command.parameter.PASV.class,
            org.waarp.ftp.core.command.parameter.TYPE.class,
            org.waarp.ftp.core.command.parameter.STRU.class,
            org.waarp.ftp.core.command.parameter.MODE.class),
    /**
     * This command specifies the old pathname of the file which is to be renamed. This command must
     * be immediately followed by a "rename to" RNTO command specifying the new file pathname.<br>
     * 
     * 
     * 450, 550<br>
     * 500, 501, 502, 421, 530<br>
     * 350<br>
     */
    RNFR(
            org.waarp.ftp.core.command.service.RNFR.class,
            null,
            org.waarp.ftp.core.command.service.RNTO.class),
    /**
     * This command specifies the new pathname of the file specified in the immediately preceding
     * "rename from" RNFR command. Together the two commands cause a file to be renamed. <br>
     * 
     * 250<br>
     * 532, 553<br>
     * 500, 501, 502, 503, 421, 530<br>
     */
    RNTO(
            org.waarp.ftp.core.command.service.RNTO.class,
            org.waarp.ftp.core.command.service.RNFR.class),
    /**
     * This command tells the server to abort the previous FTP service command and any associated
     * transfer of data. The abort command may require "special action", as discussed in the Section
     * on FTP Commands, to force recognition by the server. No action is to be taken if the previous
     * command has been completed (including data transfer). The control connection is not to be
     * closed by the server, but the data connection must be closed.<br>
     * <br>
     * 
     * There are two cases for the server upon receipt of this command: (1) the FTP service command
     * was already completed, or (2) the FTP service command is still in progress.<br>
     * <br>
     * 
     * In the first case, the server closes the data connection (if it is open) and responds with a
     * 226 reply, indicating that the abort command was successfully processed.<br>
     * <br>
     * 
     * In the second case, the server aborts the FTP service in progress and closes the data
     * connection, returning a 426 reply to indicate that the service request terminated abnormally.
     * The server then sends a 226 reply, indicating that the abort command was successfully
     * processed.<br>
     * 
     * 
     * 225, 226<br>
     * 500, 501, 502, 421<br>
     */
    ABOR(org.waarp.ftp.core.command.service.ABOR.class, null),
    /**
     * This command causes the file specified in the pathname to be deleted at the server site. If
     * an extra level of protection is desired (such as the query, "Do you really wish to delete?"),
     * it should be provided by the user-FTP process.<br>
     * 
     * 
     * 250<br>
     * 450, 550<br>
     * 500, 501, 502, 421, 530<br>
     */
    DELE(org.waarp.ftp.core.command.service.DELE.class, null),
    /**
     * This command causes the directory specified in the pathname to be removed as a directory (if
     * the pathname is absolute) or as a subdirectory of the current working directory (if the
     * pathname is relative).<br>
     * 
     * 
     * 250<br>
     * 500, 501, 502, 421, 530, 550<br>
     */
    RMD(org.waarp.ftp.core.command.service.RMD.class, null),
    /**
     * This command causes the directory specified in the pathname to be created as a directory (if
     * the pathname is absolute) or as a subdirectory of the current working directory (if the
     * pathname is relative).<br>
     * 
     * 257<br>
     * 500, 501, 502, 421, 530, 550<br>
     */
    MKD(org.waarp.ftp.core.command.service.MKD.class, null),
    /**
     * This command causes the name of the current working directory to be returned in the reply.<br>
     * 
     * 257<br>
     * 500, 501, 502, 421, 550<br>
     */
    PWD(org.waarp.ftp.core.command.service.PWD.class, null),
    /**
     * This command causes a list to be sent from the server to the passive DTP. If the pathname
     * specifies a directory or other group of files, the server should transfer a list of files in
     * the specified directory. If the pathname specifies a file then the server should send current
     * information on the file. A null argument implies the user's current working or default
     * directory. The data transfer is over the data connection in type ASCII or type EBCDIC. (The
     * user must ensure that the TYPE is appropriately ASCII or EBCDIC). Since the information on a
     * file may vary widely from system to system, this information may be hard to use automatically
     * in a program, but may be quite useful to a human user.<br>
     * <br>
     * The option '-a' is accepted but ignored.<br>
     * 
     * 
     * 125, 150<br>
     * 226, 250<br>
     * 425, 426, 451<br>
     * 450<br>
     * 500, 501, 502, 421, 530<br>
     */
    LIST(org.waarp.ftp.core.command.service.LIST.class, null),
    /**
     * This command causes a directory listing to be sent from server to user site. The pathname
     * should specify a directory or other system-specific file group descriptor; a null argument
     * implies the current directory. The server will return a stream of names of files and no other
     * information. The data will be transferred in ASCII or EBCDIC type over the data connection as
     * valid pathname strings separated by <code>&lt;CRLF&gt;</code> or <code>&lt;NL&gt;</code>.
     * (Again the user must ensure that the TYPE is correct.) This command is intended to return
     * information that can be used by a program to further process the files automatically. For
     * example, in the implementation of a "multiple get" function.<br>
     * <br>
     * The option '-l' is accepted and turns to LIST command.<br>
     * 
     * 
     * 125, 150<br>
     * 226, 250<br>
     * 425, 426, 451<br>
     * 450<br>
     * 500, 501, 502, 421, 530<br>
     */
    NLST(org.waarp.ftp.core.command.service.NLST.class, null),
    /**
     * This command is used by the server to provide services specific to his system that are
     * essential to file transfer but not sufficiently universal to be included as commands in the
     * protocol. The nature of these services and the specification of their syntax can be stated in
     * a reply to the HELP SITE command.<br>
     * <br>
     * As for now, this command will not be implemented, so returns 502.<br>
     * <br>
     * 
     * 200<br>
     * 202<br>
     * 500, 501, 530<br>
     */
    SITE(org.waarp.ftp.core.command.info.SITE.class, null),
    /**
     * This command is used to find out the type of operating system at the server. The reply shall
     * have as its first word one of the system names listed in the current version of the Assigned
     * Numbers document.<br>
     * <br>
     * Returns "UNIX Type: L8".<br>
     * 
     * 215<br>
     * 500, 501, 502, 421<br>
     */
    SYST(org.waarp.ftp.core.command.info.SYST.class, null),
    /**
     * This command shall cause a status response to be sent over the control connection in the form
     * of a reply. The command may be sent during a file transfer (along with the Telnet IP and
     * Synch signals--see the Section on FTP Commands) in which case the server will respond with
     * the status of the operation in progress, or it may be sent between file transfers. In the
     * latter case, the command may have an argument field. If the argument is a pathname, the
     * command is analogous to the "list" command except that data shall be transferred over the
     * control connection. If a partial pathname is given, the server may respond with a list of
     * file names or attributes associated with that specification. If no argument is given, the
     * server should return general status information about the server FTP process. This should
     * include current values of all transfer parameters and the status of connections.<br>
     * 
     * 
     * 211, 212, 213<br>
     * 450<br>
     * 500, 501, 502, 421, 530<br>
     */
    STAT(org.waarp.ftp.core.command.info.STAT.class, null),
    /**
     * This command shall cause the server to send helpful information regarding its implementation
     * status over the control connection to the user. The command may take an argument (e.g., any
     * command name) and return more specific information as a response. The reply is type 211 or
     * 214. It is suggested that HELP be allowed before entering a USER command. The server may use
     * this reply to specify site-dependent parameters, e.g., in response to HELP SITE.<br>
     * 
     * 
     * 211, 214<br>
     * 500, 501, 502, 421<br>
     */
    HELP(org.waarp.ftp.core.command.info.HELP.class, null),
    /**
     * This command does not affect any parameters or previously entered commands. It specifies no
     * action other than that the server send an OK reply.<br>
     * 
     * 
     * 200<br>
     * 500 421<br>
     */
    NOOP(org.waarp.ftp.core.command.info.NOOP.class, null),

    // XXX RFC775

    /**
     * Change to a new working directory. Same as CWD<br>
     * 
     * 
     * 250<br>
     * 500, 501, 502, 421, 530, 550<br>
     */
    XCWD(org.waarp.ftp.core.command.rfc775.XCWD.class, null),
    /**
     * Change to the parent of the current working directory. Same as CDUP.<br>
     * 
     * 
     * 200<br>
     * 500, 501, 502, 421, 530, 550<br>
     */
    XCUP(org.waarp.ftp.core.command.rfc775.XCUP.class, null),
    /**
     * Remove the directory. Same as RMD.<br>
     * 
     * 250<br>
     * 500, 501, 502, 421, 530, 550<br>
     */
    XRMD(org.waarp.ftp.core.command.rfc775.XRMD.class, null),
    /**
     * Make a directory. Same as MKD.<br>
     * 
     * 257<br>
     * 500, 501, 502, 421, 530, 550<br>
     */
    XMKD(org.waarp.ftp.core.command.rfc775.XMKD.class, null),
    /**
     * Print the current working directory. Same as PWD.<br>
     * 
     * 257<br>
     * 500, 501, 502, 421, 550<br>
     */
    XPWD(org.waarp.ftp.core.command.rfc775.XPWD.class, null),

    // XXX RFC3659
    /**
     * The FTP command, MODIFICATION TIME (MDTM), can be used to determine when a file in the server
     * NVFS was last modified.<br>
     * <br>
     * 
     * The "pathname" specifies an object in the NVFS that may be the object of a RETR command.
     * Attempts to query the modification time of files that exist but are unable to be retrieved
     * may generate an error- response, or can result in a positive response carrying a time-val
     * with an unspecified value, the choice being made by the server-PI.<br>
     * <br>
     * 
     * The server-PI will respond to the MDTM command with a 213 reply giving the last modification
     * time of the file whose pathname was supplied, or a 550 reply if the file does not exist, the
     * modification time is unavailable, or some other error has occurred.<br>
     * 
     * 
     * 213<br>
     * 500, 501, 550<br>
     */
    MDTM(org.waarp.ftp.core.command.rfc3659.MDTM.class, null),
    /**
     * The FTP command, SIZE OF FILE (SIZE), is used to obtain the transfer size of a file from the
     * server-FTP process. This is the exact number of octets (8 bit bytes) that would be
     * transmitted over the data connection should that file be transmitted. This value will change
     * depending on the current STRUcture, MODE, and TYPE of the data connection or of a data
     * connection that would be created were one created now. Thus, the result of the SIZE command
     * is dependent on the currently established STRU, MODE, and TYPE parameters.<br>
     * <br>
     * 
     * The SIZE command returns how many octets would be transferred if the file were to be
     * transferred using the current transfer structure, mode, and type. This command is normally
     * used in conjunction with the RESTART (REST) command when STORing a file to a remote server in
     * STREAM mode, to determine the restart point. The server-PI might need to read the partially
     * transferred file, do any appropriate conversion, and count the number of octets that would be
     * generated when sending the file in order to correctly respond to this command. Estimates of
     * the file transfer size MUST NOT be returned; only precise information is acceptable.<br>
     * 
     * 
     * 213<br>
     * 500, 501, 550<br>
     */
    SIZE(org.waarp.ftp.core.command.rfc3659.SIZE.class, null),
    /**
     * The MLSD command is intended to standardize the file and directory information returned by
     * the server-FTP process. This command differs from the LIST command in that the format of the
     * replies is strictly defined although extensible.<br>
     * <br>
     * 
     * MLSD lists the contents of a directory if a directory is named, otherwise a 501 reply is
     * returned. If no object is named, the current directory is assumed. That will cause MLSD to
     * list the contents of the current directory.<br>
     * 
     * 
     * 125, 150<br>
     * 226, 250<br>
     * 425, 426, 451<br>
     * 450<br>
     * 500, 501, 502, 421, 530<br>
     */
    MLSD(org.waarp.ftp.core.command.rfc3659.MLSD.class, null),
    /**
     * The MLST command is intended to standardize the file and directory information returned by
     * the server-FTP process. This command differs from the LIST command in that the format of the
     * replies is strictly defined although extensible.<br>
     * <br>
     * 
     * MLST provides data about exactly the object named on its command line, and no others. If no
     * object is named, the current directory is assumed. That will cause MLST to send a one-line
     * response, describing the current directory itself.<br>
     * 
     * 125, 150<br>
     * 226, 250<br>
     * 425, 426, 451<br>
     * 450<br>
     * 500, 501, 502, 421, 530<br>
     */
    MLST(org.waarp.ftp.core.command.rfc3659.MLST.class, null),

    // XXX RFC2389
    /**
     * The FEAT command consists solely of the word "FEAT". It has no parameters or arguments.<br>
     * <br>
     * 
     * Where a server-FTP process does not support the FEAT command, it will respond to the FEAT
     * command with a 500 or 502 reply. This is simply the normal "unrecognized command" reply that
     * any unknown command would elicit. Errors in the command syntax, such as giving parameters,
     * will result in a 501 reply.<br>
     * <br>
     * 
     * Server-FTP processes that recognize the FEAT command, but implement no extended features, and
     * therefore have nothing to report, SHOULD respond with the "no-features" 211 reply. However,
     * as this case is practically indistinguishable from a server-FTP that does not recognize the
     * FEAT command, a 500 or 502 reply MAY also be used. The "no-features" reply MUST NOT use the
     * multi-line response format, exactly one response line is required and permitted.<br>
     * <br>
     * 
     * Replies to the FEAT command MUST comply with the following syntax. Text on the first line of
     * the reply is free form, and not interpreted, and has no practical use, as this text is not
     * expected to be revealed to end users. The syntax of other reply lines is precisely defined,
     * and if present, MUST be exactly as specified.<br>
     * <br>
     * 
     * <pre>
     * feat-response   = error-response / no-features / feature-listing
     *         no-features     = &quot;211&quot; SP *TCHAR CRLF
     *         feature-listing = &quot;211-&quot; *TCHAR CRLF
     *                           1*( SP feature CRLF )
     *                           &quot;211 End&quot; CRLF
     *         feature         = feature-label [ SP feature-parms ]
     *         feature-label   = 1*VCHAR
     *         feature-parms   = 1*TCHAR
     * </pre>
     * 
     * Note that each feature line in the feature-listing begins with a single space. That space is
     * not optional, nor does it indicate general white space. This space guarantees that the
     * feature line can never be misinterpreted as the end of the feature-listing, but is required
     * even where there is no possibility of ambiguity.<br>
     * <br>
     * 
     * Each extension supported must be listed on a separate line to facilitate the possible
     * inclusion of parameters supported by each extension command. The feature-label to be used in
     * the response to the FEAT command will be specified as each new feature is added to the FTP
     * command set. Often it will be the name of a new command added, however this is not required.
     * In fact it is not required that a new feature actually add a new command. Any parameters
     * included are to be specified with the definition of the command concerned. That specification
     * shall also specify how any parameters present are to be interpreted.<br>
     * <br>
     * 
     * The feature-label and feature-parms are nominally case sensitive, however the definitions of
     * specific labels and parameters specify the precise interpretation, and it is to be expected
     * that those definitions will usually specify the label and parameters in a case independent
     * manner. Where this is done, implementations are recommended to use upper case letters when
     * transmitting the feature response.<br>
     * <br>
     * 
     * The FEAT command itself is not included in the list of features supported, support for the
     * FEAT command is indicated by return of a reply other than a 500 or 502 reply.<br>
     * 
     * 
     * 211<br>
     * 500, 501, 550<br>
     */
    FEAT(org.waarp.ftp.core.command.rfc2389.FEAT.class, null),
    /**
     * The OPTS (options) command allows a user-PI to specify the desired behavior of a server-FTP
     * process when another FTP command (the target command) is later issued. The exact behavior,
     * and syntax, will vary with the target command indicated, and will be specified with the
     * definition of that command. Where no OPTS behavior is defined for a particular command there
     * are no options available for that command.<br>
     * 
     * 
     * 200<br>
     * 451, 500, 501, 550<br>
     */
    OPTS(org.waarp.ftp.core.command.rfc2389.OPTS.class, null),

    // XXX RFC2428
    /**
     * The EPRT command allows for the specification of an extended address for the data connection.
     * The extended address MUST consist of the network protocol as well as the network and
     * transport addresses. The format of EPRT is:<br>
     * <br>
     * 
     * <pre>
     * EPRT&lt;space&gt;&lt;d&gt;&lt;net-prt&gt;&lt;d&gt;&lt;net-addr&gt;&lt;d&gt;&lt;tcp-port&gt;&lt;d&gt;
     * </pre>
     * 
     * <br>
     * The EPRT command keyword MUST be followed by a single space (ASCII 32). Following the space,
     * a delimiter character (<code>&lt;d&gt;</code>) MUST be specified. The delimiter character
     * MUST be one of the ASCII characters in range 33-126 inclusive. The character "|" (ASCII 124)
     * is recommended unless it coincides with a character needed to encode the network address.<br>
     * 
     * The <code>&lt;net-prt&gt;</code> argument MUST be an address family number defined by IANA in
     * the latest Assigned Numbers RFC (RFC 1700 [RP94] as of the writing of this document). This
     * number indicates the protocol to be used (and, implicitly, the address length). This document
     * will use two of address family numbers from [RP94] as examples, according to the following
     * table:<br>
     * <br>
     * 
     * <pre>
     * AF Number   Protocol
     *         ---------   --------
     *         1           Internet Protocol, Version 4 [Pos81a]
     *         2           Internet Protocol, Version 6 [DH96]
     * </pre>
     * 
     * <br>
     * The <code>&lt;net-addr&gt;</code> is a protocol specific string representation of the network
     * address. For the two address families specified above (AF Number 1 and 2), addresses MUST be
     * in the following format:<br>
     * <br>
     * 
     * <pre>
     * AF Number   Address Format      Example
     *         ---------   --------------      -------
     *         1           dotted decimal      132.235.1.2
     *         2           IPv6 string         1080::8:800:200C:417A
     *                     representations
     *                     defined in [HD96]
     * </pre>
     * 
     * <br>
     * The <code>&lt;tcp-port&gt;</code> argument must be the string representation of the number of
     * the TCP port on which the host is listening for the data connection.<br>
     * 
     * 
     * 200<br>
     * 500, 501, 522, 421, 530<br>
     */
    EPRT(org.waarp.ftp.core.command.rfc2428.EPRT.class, null),
    /**
     * The EPSV command requests that a server listen on a data port and wait for a connection. The
     * EPSV command takes an optional argument. The response to this command includes only the TCP
     * port number of the listening connection. The format of the response, however, is similar to
     * the argument of the EPRT command. This allows the same parsing routines to be used for both
     * commands. In addition, the format leaves a place holder for the network protocol and/or
     * network address, which may be needed in the EPSV response in the future. The response code
     * for entering passive mode using an extended address MUST be 229. The interpretation of this
     * code, according to [PR85] is:<br>
     * <br>
     * 
     * <pre>
     * 2yz Positive Completion
     *         x2z Connections
     *         xy9 Extended Passive Mode Entered
     * </pre>
     * 
     * <br>
     * The text returned in response to the EPSV command MUST be:<br>
     * <br>
     * 
     * <pre>
     * &lt;text indicating server is entering extended passive mode&gt; (&lt;d&gt;&lt;d&gt;&lt;d&gt;&lt;tcp-port&gt;&lt;d&gt;)
     * </pre>
     * 
     * <br>
     * The portion of the string enclosed in parentheses MUST be the exact string needed by the EPRT
     * command to open the data connection, as specified above.<br>
     * <br>
     * 
     * The first two fields contained in the parenthesis MUST be blank. The third field MUST be the
     * string representation of the TCP port number on which the server is listening for a data
     * connection. The network protocol used by the data connection will be the same network
     * protocol used by the control connection. In addition, the network address used to establish
     * the data connection will be the same network address used for the control connection. An
     * example response string follows:<br>
     * <br>
     * 
     * <pre>
     * Entering Extended Passive Mode (|||6446|)
     * </pre>
     * 
     * <br>
     * The standard negative error codes 500 and 501 are sufficient to handle all errors involving
     * the EPSV command (e.g., syntax errors).<br>
     * <br>
     * 
     * When the EPSV command is issued with no argument, the server will choose the network protocol
     * for the data connection based on the protocol used for the control connection. However, in
     * the case of proxy FTP, this protocol might not be appropriate for communication between the
     * two servers. Therefore, the client needs to be able to request a specific protocol. If the
     * server returns a protocol that is not supported by the host that will be connecting to the
     * port, the client MUST issue an ABOR (abort) command to allow the server to close down the
     * listening connection. The client can then send an EPSV command requesting the use of a
     * specific network protocol, as follows:<br>
     * <br>
     * 
     * <pre>
     * EPSV&lt;space&gt;&lt;net-prt&gt;
     * </pre>
     * 
     * <br>
     * If the requested protocol is supported by the server, it SHOULD use the protocol. If not, the
     * server MUST return the 522 error messages as outlined in section 2.<br>
     * <br>
     * 
     * <b>The following part is not implemented.</b><br>
     * Finally, the EPSV command can be used with the argument "ALL" to inform Network Address
     * Translators that the EPRT command (as well as other data commands) will no longer be used. An
     * example of this command follows:<br>
     * <br>
     * 
     * <pre>
     * EPSV &lt; space &gt; ALL
     * </pre>
     * 
     * <br>
     * Upon receipt of an EPSV ALL command, the server MUST reject all data connection setup
     * commands other than EPSV (i.e., EPRT, PORT, PASV, et al.). This use of the EPSV command is
     * further explained in section 4.<br>
     * 
     * 
     * 229<br>
     * 500, 501, 502, 522, 421, 530<br>
     */
    EPSV(org.waarp.ftp.core.command.rfc2428.EPSV.class, null),

    // XXX EXTENSIONS

    /**
     * Compute CRC on pathname given as argument. Return on control network as 250 "CRC" is CRC of
     * file "pathname"<br>
     * 
     * 250<br>
     * 500, 501, 502, 504, 421, 530<br>
     */
    XCRC(org.waarp.ftp.core.command.extension.XCRC.class, null),
    /**
     * Compute MD5 on pathname given as argument. Return on control network as 250 "MD5" is MD5 of
     * file "pathname"<br>
     * 
     * 250<br>
     * 500, 501, 502, 504, 421, 530<br>
     */
    XMD5(org.waarp.ftp.core.command.extension.XMD5.class, null),
    /**
     * Compute SHA-1 on pathname given as argument. Return on control network as 250 "SHA1" is SHA-1
     * of file "pathname"<br>
     * 
     * 250<br>
     * 500, 501, 502, 504, 421, 530<br>
     */
    XSHA1(org.waarp.ftp.core.command.extension.XSHA1.class, null),

    // XXX GLOBAL OPERATION
    /**
     * Unknown Command from control network<br>
     * Always return 500<br>
     */
    Unknown(UnknownCommand.class, null),
    /**
     * Unimplemented command<br>
     * Always return 502<br>
     */
    Unimplemented(UnimplementedCommand.class, null),
    /**
     * Bad sequence of commands<br>
     * Always return 503<br>
     */
    IncorrectSequence(IncorrectCommand.class, null),

    // XXX INTERNAL FUNCTION

    /**
     * Shutdown command (internal password protected command).<br>
     * Shutdown the FTP service<br>
     */
    INTERNALSHUTDOWN(
            org.waarp.ftp.core.command.internal.INTERNALSHUTDOWN.class,
            null),
    /**
     * Change the Limit of the global bandwidth.<br>
     * No argument reset to default, 1 argument change both write and read to same value, 2
     * arguments stand for write then read limit.<br>
     * Limit is written in byte/s. Example: "LIMITBANDWIDTH 104857600 104857600" stands for 100MB/s
     * limitation globaly.<br>
     * -1 means no limit
     */
    LIMITBANDWIDTH(
            org.waarp.ftp.core.command.internal.LIMITBANDWIDTH.class,
            null),

    // XXX RFC 4217 on SSL/TLS support through commands
    /**
     * After an AUTH => equivalent to REIN so no more authenticated, USER is mandatory,
     * except if PROT is used in between.<br>
     * So the order will be:<br>
     * whatever until authenticated -> AUTH xxx -> USER or [PBSZ 0] / PROT xxx / USER<br>
     * <br>
     * Security-Enhanced login commands (only new replies listed)<br>
     * USER<br>
     * 232<br>
     * 336<br>
     * <br>
     * Data channel commands (only new replies listed)<br>
     * STOR<br>
     * 534, 535<br>
     * STOU<br>
     * 534, 535<br>
     * RETR<br>
     * 534, 535<br>
     * LIST<br>
     * 534, 535<br>
     * NLST<br>
     * 534, 535<br>
     * APPE<br>
     * 534, 535
     */
    /**
     * Security Association Setup AUTH TLS (Control) or AUTH SSL (Control and Data)<br>
     * 234*<br>
     * 502, 504, 534*, 431* 500, 501, 421<br>
     * <br>
     * AUTH TLS -> 234 -> USER or ([PBSZ 0] PROT P then USER) -> 2xy
     */
    AUTH(org.waarp.ftp.core.command.rfc4217.AUTH.class, null,
            org.waarp.ftp.core.command.rfc4217.PROT.class,
            org.waarp.ftp.core.command.rfc4217.PBSZ.class,
            org.waarp.ftp.core.command.access.USER.class),
    /**
     * Security Association Setup<br>
     * CCC (Control SSL Off)<br>
     * 200<br>
     * 500, 533*, 534*
     */
    CCC(org.waarp.ftp.core.command.rfc4217.CCC.class, null),
    /**
     * Data protection negotiation commands<br>
     * PROT P (Data)<br>
     * PROT C (Data SSL Off)<br>
     * 200<br>
     * 504, 536*, 503, 534*, 431* 500, 501, 421, 530
     */
    PROT(org.waarp.ftp.core.command.rfc4217.PROT.class, null),
    /**
     * Data protection negotiation commands<br>
     * PBSZ 0<br>
     * 200<br>
     * 503, 500, 501, 421, 530
     */
    PBSZ(org.waarp.ftp.core.command.rfc4217.PBSZ.class, null);

    /**
     * The Class that implements this command
     */
    public Class<? extends AbstractCommand> command;

    /**
     * Previous positive class that must precede this command (null means any)
     */
    public Class<? extends AbstractCommand> previousValid;

    /**
     * Next valids class that could follow this command (null means any)
     */
    public Class<?>[] nextValids;

    private FtpCommandCode(Class<? extends AbstractCommand> command,
            Class<? extends AbstractCommand> previousValid,
            Class<?>... nextValids) {
        this.command = command;
        this.previousValid = previousValid;
        this.nextValids = nextValids;
    }

    /**
     * Get the corresponding AbstractCommand object from the line received from the client
     * associated with the handler
     * 
     * @param session
     * @param line
     * @return the AbstractCommand from the line received from the client
     */
    public static AbstractCommand getFromLine(FtpSession session, String line) {
        FtpCommandCode ftpCommandCode = null;
        String newline = line;
        if (newline == null) {
            ftpCommandCode = FtpCommandCode.Unknown;
            newline = "";
        }
        String command = null;
        String arg = null;
        if (newline.indexOf(' ') == -1) {
            command = newline;
            arg = null;
        } else {
            command = newline.substring(0, newline.indexOf(' '));
            arg = newline.substring(newline.indexOf(' ') + 1);
            if (arg.length() == 0) {
                arg = null;
            }
        }
        String COMMAND = command.toUpperCase();
        try {
            ftpCommandCode = FtpCommandCode.valueOf(COMMAND);
        } catch (IllegalArgumentException e) {
            ftpCommandCode = FtpCommandCode.Unknown;
        }
        AbstractCommand abstractCommand;
        try {
            abstractCommand = ftpCommandCode.command.newInstance();
        } catch (InstantiationException e) {
            abstractCommand = new UnknownCommand();
            abstractCommand.setArgs(session, COMMAND, arg, Unknown);
            return abstractCommand;
        } catch (IllegalAccessException e) {
            abstractCommand = new UnknownCommand();
            abstractCommand.setArgs(session, COMMAND, arg, Unknown);
            return abstractCommand;
        }
        abstractCommand.setArgs(session, COMMAND, arg, ftpCommandCode);
        return abstractCommand;
    }

    /**
     * True if the command is a Store like operation (APPE, STOR, STOU, ...)
     * 
     * @param command
     * @return True if the command is a Store like operation (APPE, STOR, STOU, ...)
     */
    public static boolean isStoreLikeCommand(FtpCommandCode command) {
        return command == APPE || command == STOR || command == STOU;
    }

    /**
     * True if the command is a Retrieve like operation (RETR, ...)
     * 
     * @param command
     * @return True if the command is a Retrieve like operation (RETR, ...)
     */
    public static boolean isRetrLikeCommand(FtpCommandCode command) {
        return command == RETR;
    }

    /**
     * True if the command is a Retrieve or Store like operation
     * 
     * @param command
     * @return True if the command is a Retrieve or Store like operation
     */
    public static boolean isStorOrRetrLikeCommand(FtpCommandCode command) {
        return isRetrLikeCommand(command) || isStoreLikeCommand(command);
    }

    /**
     * True if the command is a List like operation (LIST, NLST, MLSD, MLST, ...)
     * 
     * @param command
     * @return True if the command is a List like operation (LIST, NLST, MLSD, MLST, ...)
     */
    public static boolean isListLikeCommand(FtpCommandCode command) {
        return command == LIST || command == NLST || command == MLSD ||
                command == MLST;
    }

    /**
     * True if the command is using a Data connection
     * 
     * @param command
     * @return True if the command is using a Data Connection
     */
    public static boolean isDataConnectionUsageCommand(FtpCommandCode command) {
        return isRetrLikeCommand(command) || isStoreLikeCommand(command) || isListLikeCommand(command);
    }

    /**
     * True if the command is a special operation (QUIT, ABOR, NOOP, STAT, ...)
     * 
     * @param command
     * @return True if the command is a special operation (QUIT, ABOR, NOOP, STAT, ...)
     */
    public static boolean isSpecialCommand(FtpCommandCode command) {
        return command == QUIT || command == ABOR || command == NOOP ||
                command == STAT;
    }

    /**
     * True if the command is Ssl related (AUTH, PBSZ, PROT, USER, PASS, ACCT)
     * 
     * @param command
     * @return True if the command is Ssl related (AUTH, PBSZ, PROT, USER, PASS, ACCT)
     */
    public static boolean isSslOrAuthCommand(FtpCommandCode command) {
        return command == AUTH || command == PBSZ || command == PROT
                || command == AUTH || command == PASS || command == ACCT;
    }

    /**
     * True if the command is an extension operation (XMD5, XCRC, XSHA1, ...)
     * 
     * @param command
     * @return True if the command is an extension operation (XMD5, XCRC, XSHA1, ...)
     */
    public static boolean isExtensionCommand(FtpCommandCode command) {
        return command == XMD5 || command == XCRC || command == XSHA1 ||
                command == INTERNALSHUTDOWN || command == LIMITBANDWIDTH;
    }

    @Override
    public String toString() {
        return name();
    }
}
