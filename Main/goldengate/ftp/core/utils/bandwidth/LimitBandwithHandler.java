/**
 * Frederic Bregier LGPL 25 févr. 09 
 * LimitBandwithHandler.java goldengate.ftp.core.utils GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.utils.bandwidth;

import java.io.InvalidClassException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 * Channel Handler that allows to limit the global bandwith or per session bandwith.<br> 
 * One method will specified what is the size of the object to be read or write. 
 * It has to be implemented according to the type of object.<br>
 * Global Monitor must be started and stopped explicitely outside this Handler.<br>
 * Session Monitor will be automaticaly started when the channel
 * is connected, or if given after creation time, it will be started when added.<br> 
 * In the last case, the associated channel must be explicitely set before if the channel 
 * is already connected.<br>
 * Session Monitor will be automaticaly stopped when the connection is closed.
 * @author frederic
 * goldengate.ftp.core.utils LimitBandwithHandler
 * 
 */
@ChannelPipelineCoverage("one")
public abstract class LimitBandwithHandler extends SimpleChannelHandler {
	/**
	 * Session Monitor (set from Control Network Handler)
	 */
	private ThroughputMonitor sessionMonitor = null;
	/**
	 * Global Monitor (set from global configuration)
	 */
	private ThroughputMonitor globalMonitor = null;

	/**
	 * Constructor (both arguments can be null or not)
	 * @param globalMonitor Global Monitor. No startMonitoring() is done. 
	 * The caller must do it (generaly before calling this method).
	 * @param sessionMonitor Session Monitor
	 */
	public LimitBandwithHandler(ThroughputMonitor globalMonitor, ThroughputMonitor sessionMonitor) {
		super();
		this.globalMonitor = globalMonitor;
		this.sessionMonitor = sessionMonitor;
	}
	/**
	 * Change the global Monitor. The previous one (if any) is returned and it is up 
	 * to the caller to call the {@link ThroughputMonitor}.stopMonitoring() method.
	 * @param globalMonitor Global Monitor. No startMonitoring() is done. The caller 
	 * must do it (generaly before calling this method).
	 * @return the previous {@link ThroughputMonitor} if any
	 */
	public ThroughputMonitor setGlobalMonitor(ThroughputMonitor globalMonitor) {
		ThroughputMonitor previous = this.globalMonitor;
		this.globalMonitor = globalMonitor;
		return previous;
	}
	/**
	 * Change the session Monitor and start the new one. The previous one (if any) is stopped and returned.
	 * @param sessionMonitor Session Monitor. If the channel is already connected, 
	 * the caller should set the channel to the monitor prior to this call.
	 * @return the previous {@link ThroughputMonitor} if any
	 */
	public ThroughputMonitor setSessionMonitor(ThroughputMonitor sessionMonitor) {
		ThroughputMonitor previous = this.sessionMonitor;
		this.sessionMonitor = sessionMonitor;
		if (previous != null) {
			previous.stopMonitoring();
		}
		this.sessionMonitor.startMonitoring();
		return previous;
	}
	/**
	 * @return the globalMonitor
	 */
	public ThroughputMonitor getGlobalMonitor() {
		return this.globalMonitor;
	}
	/**
	 * @return the sessionMonitor
	 */
	public ThroughputMonitor getSessionMonitor() {
		return this.sessionMonitor;
	}
	/**
	 * This method has to be implemented. It returns the size in bytes of the message to be read or written.
	 * @param arg1 the MessageEvent to be read or written
	 * @return the size in bytes of the given MessageEvent
	 * @exception Exception An exception can be thrown if the object is not of the expected type
	 */
	protected abstract long getMessageSize(MessageEvent arg1) throws Exception ;
	/**
	 * Example of function (which can be used) for the ChannelBuffer
	 * @param arg1
	 * @return the size in bytes of the given MessageEvent
	 * @throws Exception
	 */
	protected long getChannelBufferMessageSize(MessageEvent arg1) throws Exception {
		Object o = arg1.getMessage();
		if (! (o instanceof ChannelBuffer)) {
			// Type unimplemented
			throw new InvalidClassException("Wrong object received in "+this.getClass().getName()+" codec "+o.getClass().getName());
		}
		ChannelBuffer dataBlock = (ChannelBuffer) o;
		return dataBlock.readableBytes();
	}
	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
	 */
	@Override
	public void messageReceived(ChannelHandlerContext arg0, MessageEvent arg1) throws Exception {
		long size = this.getMessageSize(arg1);
		if (this.sessionMonitor != null) {
			this.sessionMonitor.setReceivedBytes(size);
		}
		if (this.globalMonitor != null) {
			this.globalMonitor.setReceivedBytes(size);
		}
		// The message is then just passed to the next Codec
		super.messageReceived(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelHandler#writeRequested(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
	 */
	@Override
	public void writeRequested(ChannelHandlerContext arg0, MessageEvent arg1) throws Exception {
		long size = this.getMessageSize(arg1);
		if (this.sessionMonitor != null) {
			this.sessionMonitor.setToWriteBytes(size);
		}
		if (this.globalMonitor != null) {
			this.globalMonitor.setToWriteBytes(size);
		}
		// The message is then just passed to the next Codec
		super.writeRequested(arg0, arg1);
	}
	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelHandler#channelClosed(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		if (this.sessionMonitor != null) {
			this.sessionMonitor.stopMonitoring();
		}
		super.channelClosed(ctx, e);
	}
		/* (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		//System.err.println("               setReadable FALSE");
		ctx.getChannel().setReadable(false);
		if (this.sessionMonitor != null) {
			this.sessionMonitor.setMonitoredChannel(ctx.getChannel());
			this.sessionMonitor.startMonitoring();
		}
		super.channelConnected(ctx, e);
		//System.err.println("               setReadable TRUE");
		ctx.getChannel().setReadable(true);
	}
	
}
