/**
 * Frederic Bregier LGPL 10 janv. 09 
 * BusinessHandler.java goldengate.ftp.core.control GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.control;

import goldengate.ftp.core.auth.FtpAuth;
import goldengate.ftp.core.command.FtpCommandCode;
import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.data.FtpTransfer;
import goldengate.ftp.core.file.FtpDir;
import goldengate.ftp.core.file.FtpOptsMLSx;
import goldengate.ftp.core.file.FtpRestart;
import goldengate.ftp.core.session.FtpSession;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ExceptionEvent;

/**
 * This class is to be implemented in order to allow Business actions according to FTP service
 * @author frederic
 * goldengate.ftp.core.control BusinessHandler
 * 
 */
public abstract class BusinessHandler {
	/**
	 * NettyHandler that holds this BusinessHandler
	 */
	private NetworkHandler networkHandler = null;
	/**
	 * FtpSession
	 */
	private FtpSession session = null;
	/**
	 * Constructor with no argument (mandatory)
	 */
	public BusinessHandler() {
		// nothing to do
	}
	/**
	 * Called when the NetworkHandler is created
	 * @param networkHandler the networkHandler to set
	 */
	public void setNetworkHandler(NetworkHandler networkHandler) {
		this.networkHandler = networkHandler;
		this.session = this.networkHandler.getFtpSession();
	}
	/**
	 * @return the networkHandler
	 */
	public NetworkHandler getNetworkHandler() {
		return networkHandler;
	}
	// Some helpful functions
	/**
	 * 
	 * @return the ftpSession
	 */
	public FtpSession getFtpSession() {
		return session;
	}
	/**
	 * Create a new FtpAuth according to business choice
	 * @return the new FtpAuth
	 */
	public abstract FtpAuth getBusinessNewAuth();
	/**
	 * Create a new FtpDir according to business choice
	 * @return the new FtpDir
	 */
	public abstract FtpDir getBusinessNewFtpDir();
	/**
	 * Create a new FtpRestart according to business choice
	 * @return the new FtpRestart
	 */
	public abstract FtpRestart getBusinessNewFtpRestart();
	/**
	 * 
	 * @param arg the argument from HELP command
	 * @return the string to return to the client for the HELP command
	 */
	public abstract String getHelpMessage(String arg);
	/**
	 * 
	 * @return the string to return to the client for the FEAT command
	 */
	public abstract String getFeatMessage();
	/**
	 * 
	 * @return the string to return to the client for the FEAT command without surrounding by "Extensions supported:\n" and "\nEnd" 
	 */
	protected String getDefaultFeatMessage() {
		StringBuilder builder = new StringBuilder();
		builder.append(FtpCommandCode.MDTM.name());
		builder.append('\n');
		builder.append(FtpCommandCode.MLSD.name());
		builder.append(this.getFtpSession().getFtpDir().getOptsMLSx().getFeat());
		builder.append('\n');
		builder.append(FtpCommandCode.MLST.name());
		builder.append(this.getFtpSession().getFtpDir().getOptsMLSx().getFeat());
		builder.append('\n');
		builder.append(FtpCommandCode.SIZE.name());
		builder.append('\n');
		builder.append(FtpCommandCode.XCUP.name());
		builder.append('\n');
		builder.append(FtpCommandCode.XCWD.name());
		builder.append('\n');
		builder.append(FtpCommandCode.XMKD.name());
		builder.append('\n');
		builder.append(FtpCommandCode.XPWD.name());
		builder.append('\n');
		builder.append(FtpCommandCode.XRMD.name());
		builder.append('\n');
		builder.append(FtpCommandCode.PASV.name());
		builder.append('\n');
		builder.append(FtpCommandCode.ALLO.name());
		builder.append('\n');
		builder.append(FtpCommandCode.EPRT.name());
		builder.append('\n');
		builder.append(FtpCommandCode.EPSV.name());
		builder.append('\n');
		builder.append(FtpCommandCode.XCRC.name());
		builder.append(" \"filename\"");
		builder.append('\n');
		builder.append(FtpCommandCode.XMD5.name());
		builder.append(" \"filename\"");
		builder.append('\n');
		builder.append(FtpCommandCode.XSHA1.name());
		builder.append(" \"filename\"");
		builder.append('\n');
		builder.append(FtpCommandCode.SITE.name());
		builder.append(' ');
		builder.append(FtpCommandCode.XCRC.name());
		//builder.append(" \"filename\"");
		builder.append('\n');
		builder.append(FtpCommandCode.SITE.name());
		builder.append(' ');
		builder.append(FtpCommandCode.XMD5.name());
		//builder.append(" \"filename\"");
		builder.append('\n');
		builder.append(FtpCommandCode.SITE.name());
		builder.append(' ');
		builder.append(FtpCommandCode.XSHA1.name());
		//builder.append(" \"filename\"");
		builder.append('\n');
		builder.append("LAN EN*");
		builder.append('\n');
		builder.append(FtpCommandCode.REST.name());
		builder.append(" STREAM\n");
		builder.append("UTF8");
		return builder.toString();
	}
	/**
	 * @param args
	 * @return the string to return to the client for the FEAT command
	 * @exception FtpCommandAbstractException
	 */
	public abstract String getOptsMessage(String []args) throws FtpCommandAbstractException;
	/**
	 * 
	 * @param args
	 * @return the string to return to the client for the FEAT command for the MLSx argument
	 */
	protected String getMLSxOptsMessage(String []args) {
		FtpOptsMLSx optsMLSx = this.getFtpSession().getFtpDir().getOptsMLSx();
		optsMLSx.setOptsModify((byte)0);
		optsMLSx.setOptsPerm((byte)0);
		optsMLSx.setOptsSize((byte)0);
		optsMLSx.setOptsType((byte)0);
		for (int i = 1; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("modify")) {
				optsMLSx.setOptsModify((byte)1);
			} else if (args[i].equalsIgnoreCase("perm")) {
				optsMLSx.setOptsModify((byte)1);
			} else if (args[i].equalsIgnoreCase("size")) {
				optsMLSx.setOptsModify((byte)1);
			} else if (args[i].equalsIgnoreCase("type")) {
				optsMLSx.setOptsModify((byte)1);
			}
		}
		return args[0]+" "+FtpCommandCode.OPTS.name()+optsMLSx.getFeat();
	}
	/**
	 * Is executed when the channel is closed, just before cleaning and just after.<br>
	 * <I>Note: In some circumstances, it could be a good idea to call the clean operation on
	 * FtpAuth in order to relax contraints on user authenfication. It will be called however
	 * at least when the session will be clean just after this call.</I> 
	 */
	public abstract void executeChannelClosed();
	/**
	 * To Clean the session attached objects
	 */
	protected abstract void cleanSession();
	/**
	 * Clean the BusinessHandler.
	 *
	 */
	public void clean() {
		this.cleanSession();
		this.networkHandler = null;
		this.session = null;
	}
	/**
	 * Is executed when the channel is connected after the handler is on, before answering OK or not on connection,
	 * except if the global service is going to shutdown.
	 * @param channel
	 */
	public abstract void executeChannelConnected(Channel channel);
	/**
	 * Run when an exception is get before the channel is closed. 
	 * This must set a correct answer.
	 * @param e
	 */
	public abstract void exceptionLocalCaught(ExceptionEvent e);
	/**
	 * This method is called for every received message before the execution of the command.
	 * If an exception is raised, the reply is immediate and no action taken.
	 * @exception FtpCommandAbstractException
	 */
	public abstract void beforeRunCommand() throws FtpCommandAbstractException;
	/**
	 * This method is called for every received message after the execution of the command
	 * but before the final reply to the client.
	 * If an exception is raised, the reply is immediate.
	 * @exception FtpCommandAbstractException
	 */
	public abstract void afterRunCommandOk() throws FtpCommandAbstractException;
	/**
	 * Run when a FTP exception is catch (the channel is not necessary closed after). 
	 * This must set a correct answer and a correct code of reply. 
	 * If the code of reply is 421, then the channel will be closed after this call.
	 * @param e
	 */
	public abstract void afterRunCommandKo(FtpCommandAbstractException e);
	/**
	 * Run when a transfer is finished
	 * @param transfer
	 */
	public abstract void afterTransferDone(FtpTransfer transfer);
}
