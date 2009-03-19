/**
 * Frederic Bregier LGPL 10 janv. 09 FtpDataPipelineFactory.java
 * goldengate.ftp.core.control GoldenGateFtp frederic
 */
package goldengate.ftp.core.data.handler;

import goldengate.ftp.core.command.FtpArgumentCode.TransferMode;
import goldengate.ftp.core.command.FtpArgumentCode.TransferStructure;
import goldengate.ftp.core.command.FtpArgumentCode.TransferSubType;
import goldengate.ftp.core.command.FtpArgumentCode.TransferType;
import goldengate.ftp.core.config.FtpConfiguration;
import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.execution.ExecutionHandler;

/**
 * Pipeline Factory for Data Network.
 * 
 * @author frederic goldengate.ftp.core.control FtpDataPipelineFactory
 * 
 */
public class FtpDataPipelineFactory implements ChannelPipelineFactory {
    /**
     * Internal Logger
     */
    private static FtpInternalLogger logger = FtpInternalLoggerFactory
            .getLogger(FtpDataPipelineFactory.class);

    /**
     * Mode Codec
     */
    public static final String CODEC_MODE = "MODE";

    /**
     * Limit Codec
     */
    public static final String CODEC_LIMIT = "LIMITATION";

    /**
     * Type Codec
     */
    public static final String CODEC_TYPE = "TYPE";

    /**
     * Structure Codec
     */
    public static final String CODEC_STRUCTURE = "STRUCTURE";

    /**
     * Pipeline Executor Codec
     */
    public static final String PIPELINE_EXECUTOR = "pipelineExecutor";

    /**
     * Handler Codec
     */
    public static final String HANDLER = "handler";

    /**
     * Business Handler Class
     */
    private final Class<? extends DataBusinessHandler> dataBusinessHandler;

    /**
     * Configuration
     */
    private final FtpConfiguration configuration;

    /**
     * Constructor which Initializes some data
     * 
     * @param dataBusinessHandler
     * @param configuration
     */
    public FtpDataPipelineFactory(
            Class<? extends DataBusinessHandler> dataBusinessHandler,
            FtpConfiguration configuration) {
        this.dataBusinessHandler = dataBusinessHandler;
        this.configuration = configuration;
    }

    /**
     * Create the pipeline with Handler, ObjectDecoder, ObjectEncoder.
     * 
     * @see org.jboss.netty.channel.ChannelPipelineFactory#getPipeline()
     */
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        // Add default codec but they will change by the channelConnected
        logger.debug("Set Default Codec");
        pipeline.addFirst(CODEC_MODE, new FtpDataModeCodec(TransferMode.STREAM,
                TransferStructure.FILE));
        pipeline.addLast(CODEC_LIMIT, new FtpDataLimitBandwidth(
                this.configuration.getFtpInternalConfiguration()
                        .getPerformanceCounterFactory()));
        pipeline.addLast(CODEC_TYPE, new FtpDataTypeCodec(TransferType.ASCII,
                TransferSubType.NONPRINT));
        pipeline.addLast(CODEC_STRUCTURE, new FtpDataStructureCodec(
                TransferStructure.FILE));
        // Threaded execution for business logic
        pipeline.addLast(PIPELINE_EXECUTOR, new ExecutionHandler(
                this.configuration.getFtpInternalConfiguration()
                        .getDataPipelineExecutor()));
        // and then business logic. New one on every connection
        DataBusinessHandler newbusiness = this.dataBusinessHandler
                .newInstance();
        DataNetworkHandler newNetworkHandler = new DataNetworkHandler(
                this.configuration, newbusiness);
        pipeline.addLast(HANDLER, newNetworkHandler);
        return pipeline;
    }
}
