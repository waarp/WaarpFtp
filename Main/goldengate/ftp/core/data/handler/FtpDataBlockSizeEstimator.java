/**
 * LGPL V3.0
 * FtpDataBlockSizeEstimator.java goldengate.ftp.core.data.handler GoldenGateFtp
 * 21 mars 2009 frederic
 */
package goldengate.ftp.core.data.handler;

import org.jboss.netty.util.ObjectSizeEstimator;


/**
 * @author frederic
 *
 */
public class FtpDataBlockSizeEstimator implements ObjectSizeEstimator {

    /* (non-Javadoc)
     * @see org.jboss.netty.handler.execution.ObjectSizeEstimator#estimateSize(java.lang.Object)
     */
    @Override
    public int estimateSize(Object o) {
        if (!(o instanceof FtpDataBlock)) {
            // Type unimplemented
            return 8;
        }
        FtpDataBlock dataBlock = (FtpDataBlock) o;
        return dataBlock.getByteCount();
    }

}
