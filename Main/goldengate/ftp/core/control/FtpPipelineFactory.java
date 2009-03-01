/**
 * Frederic Bregier LGPL 10 janv. 09 
 * FtpPipelineFactory.java goldengate.ftp.core.control GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.control;

import goldengate.ftp.core.config.FtpConfiguration;
import goldengate.ftp.core.config.FtpInternalConfiguration;
import goldengate.ftp.core.session.FtpSession;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.execution.ExecutionHandler;

/**
 * Pipeline factory for Control command connection
 * @author frederic
 * goldengate.ftp.core.control FtpPipelineFactory
 * 
 */
public class FtpPipelineFactory implements ChannelPipelineFactory {
	/**
	 * CRLF, CRNUL, LF delimiters
	 */
	private static final ChannelBuffer[] delimiter = 
		new ChannelBuffer[] {ChannelBuffers.wrappedBuffer(FtpInternalConfiguration.CRLF.getBytes()),
			ChannelBuffers.wrappedBuffer(FtpInternalConfiguration.CRNUL.getBytes()),
			ChannelBuffers.wrappedBuffer(FtpInternalConfiguration.LF.getBytes())};
	/**
	 * Business Handler Class if any (Target Mode only)
	 */
	private Class businessHandler = null;
	/**
	 * Configuration
	 */
	private FtpConfiguration configuration = null;
	/**
	 * Constructor wich Initializes some data for Server only
	 * @param businessHandler
	 * @param configuration 
	 */
	public FtpPipelineFactory(Class businessHandler, FtpConfiguration configuration) {
		this.businessHandler = businessHandler;
		this.configuration = configuration;
	}
	/**
	 * Create the pipeline with Handler, ObjectDecoder, ObjectEncoder.
	 * @see org.jboss.netty.channel.ChannelPipelineFactory#getPipeline()
	 */
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		// Add the text line codec combination first,
		pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, delimiter));
		pipeline.addLast("decoder", new FtpControlStringDecoder());
		pipeline.addLast("encoder", new FtpControlStringEncoder());
		// Threaded execution for business logic
		pipeline.addLast("pipelineExecutor", 
				new ExecutionHandler(configuration.getFtpInternalConfiguration().getPipelineExecutor()));
		// and then business logic. New one on every connection
		BusinessHandler newbusiness = (BusinessHandler) businessHandler.newInstance();
		NetworkHandler newNetworkHandler = 
			new NetworkHandler(new FtpSession(configuration,newbusiness));
		pipeline.addLast("handler", newNetworkHandler);
		return pipeline;
	}
}
