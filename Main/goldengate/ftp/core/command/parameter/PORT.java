/**
 * Frederic Bregier LGPL 10 janv. 09 
 * PORT.java goldengate.ftp.core.command.access GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.parameter;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.Reply501Exception;
import goldengate.ftp.core.utils.FtpChannelUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * PORT command
 * @author frederic
 * goldengate.ftp.core.command.parameter PORT
 * 
 */
public class PORT extends AbstractCommand {

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() throws Reply501Exception {
		// First Check if any argument
		if (! this.hasArg()) {
			InetSocketAddress inetSocketAddress = 
				this.getFtpSession().getDataConn().getRemoteAddress();
			this.getFtpSession().getDataConn().setActive(inetSocketAddress);
			this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_200_COMMAND_OKAY,
					"PORT command successful on ("+inetSocketAddress.toString()+")");
			return;
		}
		// Check if Inet Address is OK
		InetSocketAddress inetSocketAddress = FtpChannelUtils.getInetSocketAddress(this.getArg());
		if (inetSocketAddress == null) {
			// ERROR
			throw new Reply501Exception("Need correct Inet Address as argument");
		}
		// Check if the Client address is the same as given
		InetAddress remoteAddress = inetSocketAddress.getAddress();
		InetAddress trueRemoteAddress =
			this.getFtpSession().getDataConn().getRemoteAddress().getAddress();
		if (! remoteAddress.equals(trueRemoteAddress)) {
			// ERROR
			throw new Reply501Exception("Given Inet Address mismatchs actual client Address");
		}
		// OK now try to initialize connection (not open)
		this.getFtpSession().getDataConn().setActive(inetSocketAddress);
		this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_200_COMMAND_OKAY,
				"PORT command successful on ("+inetSocketAddress.toString()+")");
	}
}
