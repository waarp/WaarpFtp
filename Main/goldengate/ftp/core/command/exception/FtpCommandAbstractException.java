/**
 * Frederic Bregier LGPL 18 janv. 09 
 * FtpCommandAbstractException.java goldengate.ftp.core.command.exception GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.exception;

import goldengate.ftp.core.command.FtpReplyCode;

/**
 * Abstract class for exception in commands
 * @author frederic
 * goldengate.ftp.core.command.exception FtpCommandAbstractException
 * 
 */
public abstract class FtpCommandAbstractException extends Exception {
	/**
	 * Associated code
	 */
	public FtpReplyCode code = null;
	/**
	 * Associated Message if any
	 */
	public String message = null;
	/**
	 * Unique constructor
	 * @param code
	 * @param message
	 */
	public FtpCommandAbstractException(FtpReplyCode code, String message) {
		super(code.getMesg());
		this.code = code;
		this.message = message;
	}
	/**
	 * 
	 */
	public String toString() {
		return "Code: "+this.code.name()+" Mesg: "+(this.message != null ? this.message : "no specific message");
	}
	/* (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return this.toString();
	}
}
