/**
 * 
 */
package goldengate.ftp.core.data;

import goldengate.ftp.core.command.FtpCommandCode;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.exception.FtpNoFileException;
import goldengate.ftp.core.file.FtpFile;

import java.util.List;



/**
 * Class that owns one transfer to be run
 * @author fbregier
 *
 */
public class FtpTransfer {
	/**
	 * The command to execute
	 */
	private FtpCommandCode command = null;
	/**
	 * The information (list) on which the command was executed
	 */
	private List<String> info = null;
	/**
	 * The original path on which the command was executed
	 */
	private String path = null;
	/**
	 * Current Ftp File
	 */
	private FtpFile currentFile = null;
	/**
	 * The status
	 */
	private boolean status = false;
	/**
	 * @param command
	 * @param fileOrInfo
	 * @param path
	 */
	public FtpTransfer(FtpCommandCode command, List<String> fileOrInfo, String path) {
		this.command = command;
		this.info = fileOrInfo;
		this.path = path;
	}
	/**
	 * @param command
	 * @param file
	 */
	public FtpTransfer(FtpCommandCode command, FtpFile file) {
		this.command = command;
		this.currentFile = file;
		try {
			this.path = file.getFile();
		} catch (FtpCommandAbstractException e) {
		}
	}
	/**
	 * @return the command
	 */
	public FtpCommandCode getCommand() {
		return command;
	}
	/**
	 * @return the file
	 * @throws FtpNoFileException 
	 */
	public FtpFile getFtpFile() throws FtpNoFileException {
		if (this.currentFile == null) {
			throw new FtpNoFileException("No file associated with the transfer");
		}
		return currentFile;
	}	
	/**
	 * @return the Info
	 */
	public List<String> getInfo() {
		return info;
	}
	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
	/**
	 * @return the status
	 */
	public boolean getStatus() {
		return status;
	}
	/**
	 * @param status
	 */
	public void setStatus(boolean status) {
		this.status = status;
	}
	/**
	 * 
	 */
	public String toString() {
		return this.command.name()+" "+this.path;
	}
}
