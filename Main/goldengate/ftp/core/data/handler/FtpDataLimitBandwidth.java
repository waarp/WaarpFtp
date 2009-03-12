/**
 * 
 */
package goldengate.ftp.core.data.handler;

import goldengate.ftp.core.exception.FtpInvalidArgumentException;

import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.trafficshaping.PerformanceCounterFactory;
import org.jboss.netty.handler.trafficshaping.TrafficShapingHandler;

/**
 * Channel Handler that allows to limit the global bandwidth
 * or per session bandwidth. This is to be placed between
 * the {@link FtpDataModeCodec} and the {@link FtpDataTypeCodec}
 * in order to have {@link FtpDataBlock} object.
 * @author fbregier
 *
 */
@ChannelPipelineCoverage("one")
public class FtpDataLimitBandwidth extends TrafficShapingHandler {
	
	/**
	 * @param factory
	 */
	public FtpDataLimitBandwidth(PerformanceCounterFactory factory) {
		super(factory);
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.utils.LimitBandwithHandler#getMessageSize(org.jboss.netty.channel.MessageEvent)
	 */
	@Override
	protected long getMessageSize(MessageEvent arg1) throws Exception {
		Object o = arg1.getMessage();
		if (! (o instanceof FtpDataBlock)) {
			// Type unimplemented
			throw new FtpInvalidArgumentException("Wrong object received in "+this.getClass().getName()+" codec "+o.getClass().getName());
		}
		FtpDataBlock dataBlock = (FtpDataBlock) o;
		return dataBlock.getByteCount();
	}
}
