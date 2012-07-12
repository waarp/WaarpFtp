/**
   This file is part of Waarp Project.

   Copyright 2009, Frederic Bregier, and individual contributors by the @author
   tags. See the COPYRIGHT.txt in the distribution for a full listing of
   individual contributors.

   All Waarp Project is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Waarp is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Waarp .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.waarp.ftp.core.control.ftps;

import java.util.concurrent.ExecutorService;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.waarp.common.command.ReplyCode;
import org.waarp.common.crypto.ssl.WaarpSecureKeyStore;
import org.waarp.common.crypto.ssl.WaarpSslContextFactory;
import org.waarp.ftp.core.config.FtpConfiguration;
import org.waarp.ftp.core.control.BusinessHandler;
import org.waarp.ftp.core.control.FtpControlStringDecoder;
import org.waarp.ftp.core.control.FtpControlStringEncoder;
import org.waarp.ftp.core.session.FtpSession;

/**
 * @author "Frederic Bregier"
 *
 */
public class FtpsPipelineFactory implements ChannelPipelineFactory {
	/**
	 * CRLF, CRNUL, LF delimiters
	 */
	private static final ChannelBuffer[] delimiter = new ChannelBuffer[] {
			ChannelBuffers.wrappedBuffer(ReplyCode.CRLF.getBytes()),
			ChannelBuffers.wrappedBuffer(ReplyCode.CRNUL.getBytes()),
			ChannelBuffers.wrappedBuffer(ReplyCode.LF.getBytes()) };

	private static final FtpControlStringDecoder ftpControlStringDecoder = new FtpControlStringDecoder();

	private static final FtpControlStringEncoder ftpControlStringEncoder = new FtpControlStringEncoder();

	public static WaarpSslContextFactory waarpSslContextFactory;
	public static WaarpSecureKeyStore WaarpSecureKeyStore;
	private final ExecutorService executorService;

	/**
	 * Business Handler Class if any (Target Mode only)
	 */
	private final Class<? extends BusinessHandler> businessHandler;

	/**
	 * Configuration
	 */
	private final FtpConfiguration configuration;

	/**
	 * Constructor which Initializes some data for Server only
	 * 
	 * @param businessHandler
	 * @param configuration
	 * @param executor
	 */
	public FtpsPipelineFactory(Class<? extends BusinessHandler> businessHandler,
			FtpConfiguration configuration, ExecutorService executor) {
		this.businessHandler = businessHandler;
		this.configuration = configuration;
		this.executorService = executor;
	}

	/**
	 * Create the pipeline with Handler, ObjectDecoder, ObjectEncoder.
	 * 
	 * @see org.jboss.netty.channel.ChannelPipelineFactory#getPipeline()
	 */
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		// Server: no renegotiation still, but possible clientAuthent
		pipeline.addLast("ssl",
				waarpSslContextFactory.initPipelineFactory(true,
						waarpSslContextFactory.needClientAuthentication(),
						true, executorService));

		// Add the text line codec combination first,
		pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192,
				delimiter));
		pipeline.addLast("decoder", ftpControlStringDecoder);
		pipeline.addLast("encoder", ftpControlStringEncoder);
		// Threaded execution for business logic
		pipeline.addLast("pipelineExecutor", new ExecutionHandler(configuration
				.getFtpInternalConfiguration().getPipelineExecutor()));
		// and then business logic. New one on every connection
		BusinessHandler newbusiness = businessHandler.newInstance();
		SslNetworkHandler newNetworkHandler = new SslNetworkHandler(new FtpSession(
				configuration, newbusiness));
		pipeline.addLast("handler", newNetworkHandler);
		return pipeline;
	}
}
