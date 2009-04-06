/**
 * 
 */
package goldengate.ftp.core.data.handler;

import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.handler.traffic.TrafficCounterFactory;
import org.jboss.netty.handler.traffic.TrafficShapingHandler;
import org.jboss.netty.util.ObjectSizeEstimator;

/**
 * Channel Handler that allows to limit the global bandwidth or per session
 * bandwidth. This is to be placed between the {@link FtpDataModeCodec} and the
 * {@link FtpDataTypeCodec} in order to have {@link FtpDataBlock} object.
 * 
 * @author fbregier
 * 
 */
@ChannelPipelineCoverage("one")
public class FtpDataLimitBandwidth extends TrafficShapingHandler {

    /**
     * @param factory
     * @param objectSizeEstimator
     */
    public FtpDataLimitBandwidth(TrafficCounterFactory factory, ObjectSizeEstimator objectSizeEstimator) {
        super(factory, objectSizeEstimator);
    }

}
