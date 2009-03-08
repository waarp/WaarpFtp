/**
 * Frederic Bregier LGPL 10 janv. 09 
 * USER.java goldengate.ftp.core.command.access GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.internal;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.Reply500Exception;
import goldengate.ftp.core.command.exception.Reply501Exception;

/**
 * Internal limit bandwidth command that will change the global limit bandwidth
 * @author frederic
 * goldengate.ftp.core.command LIMITBANDWIDTH
 * 
 */
public class LIMITBANDWIDTH extends AbstractCommand {
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() throws Reply501Exception, Reply500Exception {
		if (! this.getFtpSession().getFtpAuth().isAdmin()) {
			// not admin
			throw new Reply500Exception("Command Not Allowed");
		}
		if (! this.hasArg()) {
			// reset to default
			this.getConfiguration().resetGlobalMonitor(0,0);
			this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_200_COMMAND_OKAY, "Limit reset to default");
			return;
		}
		String [] limits = this.getArgs();
		long writeLimit = 0;
		long readLimit = 0;
		try {
			if (limits.length == 1) {
				writeLimit = Long.parseLong(limits[0]);
				if (writeLimit == 0) {
					throw new Reply501Exception("Limit cannot be 0");
				}
				readLimit = writeLimit;
			} else {
				writeLimit = Long.parseLong(limits[0]);
				if (writeLimit == 0) {
					throw new Reply501Exception("Limit cannot be 0");
				}
				readLimit = Long.parseLong(limits[1]);
				if (readLimit == 0) {
					throw new Reply501Exception("Limit cannot be 0");
				}
			}
		} catch (NumberFormatException e) {
			throw new Reply501Exception(this.getCommand()+" ([write and read limits in b/s] | [write limit in b/s] [read limit in b/s]");
		}
		this.getConfiguration().resetGlobalMonitor(writeLimit, readLimit);
		this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_200_COMMAND_OKAY, "Limit set to new values");
	}
	
}
