/**
 * 
 */
package goldengate.ftp.core.command;

/**
 * Used by Authentification step in order to allow a specific command to be accepted after the current command.
 * If NOOP is specified, any command is valid. Specify also the reply code and the associated message.
 * @author fbregier
 *
 */
public class FtpNextCommandReply {
	/**
	 * Command to be accepted next time
	 */
	public FtpCommandCode command = null;
	/**
	 * Reply to do to the Ftp client
	 */
	public FtpReplyCode reply = null;
	/**
	 * Message
	 */
	public String message = null;
	/**
	 * @param command
	 * @param reply
	 * @param message
	 */
	public FtpNextCommandReply(FtpCommandCode command, FtpReplyCode reply, String message) {
		this.command = command;
		this.reply = reply;
		this.message = message;
	}
}
