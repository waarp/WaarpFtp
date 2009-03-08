/**
 * Frederic Bregier LGPL 10 janv. 09 
 * EPRT.java goldengate.ftp.core.command.access GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.rfc2428;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.Reply501Exception;
import goldengate.ftp.core.command.exception.Reply522Exception;
import goldengate.ftp.core.utils.FtpChannelUtils;

import java.net.InetSocketAddress;

/**
 * EPRT command
 * @author frederic
 * goldengate.ftp.core.command.parameter EPRT
 * 
 */
public class EPRT extends AbstractCommand {

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() throws Reply501Exception, Reply522Exception {
		// First Check if any argument
		if (! this.hasArg()) {
			InetSocketAddress inetSocketAddress = 
				this.getFtpSession().getDataConn().getRemoteAddress();
			this.getFtpSession().getDataConn().setActive(inetSocketAddress);
			this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_200_COMMAND_OKAY,
					"EPRT command successful on ("+
					FtpChannelUtils.get2428Address(inetSocketAddress)+")");
			return;
		}
		// Check if Inet Address is OK
		
		InetSocketAddress inetSocketAddress = FtpChannelUtils.get2428InetSocketAddress(this.getArg());
		if (inetSocketAddress == null) {
			// ERROR
			throw new Reply522Exception(null);
		}
		// No Check if the Client address is the same as given
		// OK now try to initialize connection (not open)
		this.getFtpSession().getDataConn().setActive(inetSocketAddress);
		this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_200_COMMAND_OKAY,
				"EPRT command successful on ("+
				FtpChannelUtils.get2428Address(inetSocketAddress)+")");
	}
}
