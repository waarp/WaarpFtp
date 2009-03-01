/**
 * Frederic Bregier LGPL 10 janv. 09 
 * PORT.java goldengate.ftp.core.command.access GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.command.rfc2428;

import goldengate.ftp.core.command.AbstractCommand;
import goldengate.ftp.core.command.FtpReplyCode;
import goldengate.ftp.core.command.exception.Reply425Exception;
import goldengate.ftp.core.command.exception.Reply501Exception;
import goldengate.ftp.core.config.FtpInternalConfiguration;
import goldengate.ftp.core.data.FtpDataAsyncConn;
import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;
import goldengate.ftp.core.utils.FtpChannelUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * EPSV command
 * @author frederic
 * goldengate.ftp.core.command.parameter EPSV
 * 
 */
public class EPSV extends AbstractCommand {
	/**
	 * Internal Logger
	 */
	private static final FtpInternalLogger logger =
        FtpInternalLoggerFactory.getLogger(EPSV.class);
	
	/**
	 */
	public EPSV() {
		super();
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.command.AbstractCommand#exec()
	 */
	@Override
	public void exec() throws Reply425Exception, Reply501Exception {
		// No Check if any argument
		// Take a new port: 3 attempts
		boolean isInit = false;
		int newport = -1;
		for (int i = 1; i <= FtpInternalConfiguration.RETRYNB ; i++) {
			newport = FtpDataAsyncConn.getNewPassivePort(this.getConfiguration());
			if (newport == -1) {
				throw new Reply425Exception("No port available");
			}
			if (this.getFtpSession().getDataConn().isPassiveMode()) {
				// Previous mode was Passive so remove the current configuration
				InetSocketAddress local = this.getFtpSession().getDataConn().getLocalAddress();
				InetAddress remote =
					this.getFtpSession().getDataConn().getRemoteAddress().getAddress();
				this.getConfiguration().delFtpSession(remote, local);
			}
			logger.info("EPSV: set Passive Port "+newport);
			this.getFtpSession().getDataConn().setLocalPort(newport);
			this.getFtpSession().getDataConn().setPassive();
			// Init the connection
			try {
				if (this.getFtpSession().getDataConn().initPassiveConnection()) {
					isInit = true;
					break;
				}
			} catch (Reply425Exception e) {
				logger.warn("EPSV refused at try: "+i+" with port: "+newport,e);
			}
		}
		if (! isInit) {
			throw new Reply425Exception("Extended Passive mode not started");
		}
		// Return the address in Ftp format
		InetSocketAddress local = this.getFtpSession().getDataConn().getLocalAddress();
		String slocal = "Entering Extended Passive Mode ("+
		FtpChannelUtils.get2428Address(local)+")";
		InetAddress remote =
			this.getFtpSession().getDataConn().getRemoteAddress().getAddress();
		// Add the current FtpSession into the reference of session since the client will open the connection
		this.getConfiguration().setNewFtpSession(remote, local, this.getFtpSession());
		this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_229_ENTERING_PASSIVE_MODE,
				"Entering Extended Passive Mode (|||"+newport+"|)");
		logger.info("EPSV: answer ready on "+slocal);
		/* 
		 * Could be:
		 * this.getFtpSession().setReplyCode(FtpReplyCode.REPLY_229_ENTERING_PASSIVE_MODE,
				slocal);
		 */
	}

}
