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
package org.waarp.ftp.core.control;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ExceptionEvent;
import org.waarp.common.command.exception.CommandAbstractException;
import org.waarp.common.file.Restart;
import org.waarp.common.file.filesystembased.FilesystemBasedOptsMLSxImpl;
import org.waarp.ftp.core.command.AbstractCommand;
import org.waarp.ftp.core.command.FtpCommandCode;
import org.waarp.ftp.core.data.FtpTransfer;
import org.waarp.ftp.core.file.FtpAuth;
import org.waarp.ftp.core.file.FtpDir;
import org.waarp.ftp.core.session.FtpSession;

/**
 * This class is to be implemented in order to allow Business actions according to FTP service
 * 
 * @author Frederic Bregier
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
	 * 
	 * @param networkHandler
	 *            the networkHandler to set
	 */
	public void setNetworkHandler(NetworkHandler networkHandler) {
		this.networkHandler = networkHandler;
		session = this.networkHandler.getFtpSession();
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
	 * Create a new AuthInterface according to business choice
	 * 
	 * @return the new FtpAuth
	 */
	public abstract FtpAuth getBusinessNewAuth();

	/**
	 * Create a new FtpDir according to business choice
	 * 
	 * @return the new FtpDir
	 */
	public abstract FtpDir getBusinessNewDir();

	/**
	 * Create a new Restart according to business choice
	 * 
	 * @return the new Restart
	 */
	public abstract Restart getBusinessNewRestart();

	/**
	 * 
	 * @param arg
	 *            the argument from HELP command
	 * @return the string to return to the client for the HELP command
	 */
	public abstract String getHelpMessage(String arg);

	/**
	 * 
	 * @return the string to return to the client for the FEAT command
	 */
	public abstract String getFeatMessage();

	protected String getSslFeatMessage() {
		StringBuilder builder = new StringBuilder();
		builder.append("AUTH TLS\n");
		builder.append("AUTH SSL\n");
		builder.append("CCC\n");
		builder.append("PROT P\n");
		builder.append("PROT C");
		return builder.toString();
	}
	/**
	 * 
	 * @return the string to return to the client for the FEAT command without surrounding by
	 *         "Extensions supported:\n" and "\nEnd"
	 */
	protected String getDefaultFeatMessage() {
		StringBuilder builder = new StringBuilder();
		builder.append(FtpCommandCode.MDTM.name());
		builder.append('\n');
		builder.append(FtpCommandCode.MLSD.name());
		builder.append(getFtpSession().getDir().getOptsMLSx().getFeat());
		builder.append('\n');
		builder.append(FtpCommandCode.MLST.name());
		builder.append(getFtpSession().getDir().getOptsMLSx().getFeat());
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
		// builder.append(" \"filename\"");
		builder.append('\n');
		builder.append(FtpCommandCode.SITE.name());
		builder.append(' ');
		builder.append(FtpCommandCode.XMD5.name());
		// builder.append(" \"filename\"");
		builder.append('\n');
		builder.append(FtpCommandCode.SITE.name());
		builder.append(' ');
		builder.append(FtpCommandCode.XSHA1.name());
		// builder.append(" \"filename\"");
		builder.append('\n');
		builder.append("LAN EN*");
		builder.append('\n');
		builder.append(FtpCommandCode.REST.name());
		builder.append(" STREAM\n");
		//builder.append("UTF8");
		return builder.toString();
	}

	/**
	 * @param args
	 * @return the string to return to the client for the FEAT command
	 * @exception CommandAbstractException
	 */
	public abstract String getOptsMessage(String[] args)
			throws CommandAbstractException;

	/**
	 * Check if a command pass to SITE command is legal
	 * 
	 * @param session
	 * @param line
	 * @return the AbstractCommand to execute if it is a Specialized Command, else Null
	 */
	public abstract AbstractCommand getSpecializedSiteCommand(FtpSession session, String line);

	/**
	 * 
	 * @param args
	 * @return the string to return to the client for the FEAT command for the MLSx argument
	 */
	protected String getMLSxOptsMessage(String[] args) {
		FilesystemBasedOptsMLSxImpl optsMLSx = (FilesystemBasedOptsMLSxImpl) getFtpSession()
				.getDir().getOptsMLSx();
		optsMLSx.setOptsModify((byte) 0);
		optsMLSx.setOptsPerm((byte) 0);
		optsMLSx.setOptsSize((byte) 0);
		optsMLSx.setOptsType((byte) 0);
		for (int i = 1; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("modify")) {
				optsMLSx.setOptsModify((byte) 1);
			} else if (args[i].equalsIgnoreCase("perm")) {
				optsMLSx.setOptsModify((byte) 1);
			} else if (args[i].equalsIgnoreCase("size")) {
				optsMLSx.setOptsModify((byte) 1);
			} else if (args[i].equalsIgnoreCase("type")) {
				optsMLSx.setOptsModify((byte) 1);
			}
		}
		return args[0] + " " + FtpCommandCode.OPTS.name() + optsMLSx.getFeat();
	}

	/**
	 * Is executed when the channel is closed, just before cleaning and just after.<br>
	 * <I>Note: In some circumstances, it could be a good idea to call the clean operation on
	 * FtpAuth in order to relax constraints on user authentication. It will be called however at
	 * least when the session will be clean just after this call.</I>
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
	public void clear() {
		cleanSession();
	}

	/**
	 * Is executed when the channel is connected after the handler is on, before answering OK or not
	 * on connection, except if the global service is going to shutdown.
	 * 
	 * @param channel
	 */
	public abstract void executeChannelConnected(Channel channel);

	/**
	 * Run when an exception is get before the channel is closed. This must set a correct answer.
	 * 
	 * @param e
	 */
	public abstract void exceptionLocalCaught(ExceptionEvent e);

	/**
	 * This method is called for every received message before the execution of the command. If an
	 * exception is raised, the reply is immediate and no action taken.
	 * 
	 * @exception CommandAbstractException
	 */
	public abstract void beforeRunCommand() throws CommandAbstractException;

	/**
	 * This method is called for every received message after the execution of the command but
	 * before the final reply to the client. If an exception is raised, the reply is immediate. This
	 * is the last call before finishing the command.
	 * 
	 * @exception CommandAbstractException
	 */
	public abstract void afterRunCommandOk() throws CommandAbstractException;

	/**
	 * Run when a FTP exception is catch (the channel is not necessary closed after). This must set
	 * a correct answer and a correct code of reply. If the code of reply is 421, then the channel
	 * will be closed after this call. This is the last call before finishing the command.
	 * 
	 * @param e
	 */
	public abstract void afterRunCommandKo(CommandAbstractException e);

	/**
	 * Run when a transfer is finished (eventually in error) but before answering. Note that this is
	 * called only for a Transfer Request (or LIST) but called before afterRunCommandXX is called
	 * (Ok or Ko).
	 * 
	 * @param transfer
	 * @exception CommandAbstractException
	 */
	public abstract void afterTransferDoneBeforeAnswer(FtpTransfer transfer)
			throws CommandAbstractException;
}
