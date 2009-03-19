/**
 * 
 */
package goldengate.ftp.core.data.handler;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Main object implementing Data Block whaveter the mode, type, structure used.
 * 
 * @author fbregier
 * 
 */
public class FtpDataBlock {
    private static final int EOR = 128;

    private static final int EOF = 64;

    private static final int ERROR = 32;

    private static final int RESTART = 16;

    /**
     * Descriptor
     */
    private int descriptor = 0;

    /**
     * Byte Count
     */
    private int byteCount = -1;

    /**
     * Markers
     */
    private int[] markers = null;

    /**
     * Byte Array
     */
    private ChannelBuffer block = null;

    /**
     * is EOF
     */
    private boolean isEOF = false;

    /**
     * is EOR
     */
    private boolean isEOR = false;

    /**
     * is in ERROR (should not be used)
     */
    private boolean isERROR = false;

    /**
     * is a MARKER RESTART
     */
    private boolean isRESTART = false;

    /**
     * Create a simple and empty DataBlock
     */
    public FtpDataBlock() {
    }

    /**
     * @return the block
     */
    public ChannelBuffer getBlock() {
        return this.block;
    }

    /**
     * Set the block and the byte count according to the block
     * 
     * @param block
     *            the block to set
     */
    public void setBlock(ChannelBuffer block) {
        if (this.isRESTART) {
            this.block = null;
            this.markers = new int[6];
            for (int i = 0; i < 6; i ++) {
                this.markers[i] = block.readByte();
            }
            this.byteCount = 6;
            return;
        }
        this.block = block;
        if (this.block == null) {
            this.byteCount = 0;
        } else {
            this.byteCount = this.block.readableBytes();
        }
    }

    /**
     * @return the byteCount
     */
    public int getByteCount() {
        return this.byteCount;
    }

    /**
     * @param byteCount
     *            the byteCount to set
     */
    public void setByteCount(int byteCount) {
        this.byteCount = byteCount;
    }

    /**
     * @param upper
     *            upper byte of the 2 bytes length
     * @param lower
     *            lower byte of the 2 bytes length
     */
    public void setByteCount(byte upper, byte lower) {
        this.byteCount = (upper << 8) | lower;
    }

    /**
     * 
     * @return the Upper byte of the byte count
     */
    public byte getByteCountUpper() {
        return (byte) ((this.byteCount >> 8) & 0xFF);
    }

    /**
     * 
     * @return the Lower byte of the byte count
     */
    public byte getByteCountLower() {
        return (byte) (this.byteCount & 0xFF);
    }

    /**
     * @return the descriptor
     */
    public byte getDescriptor() {
        return (byte) (this.descriptor & 0xFF);
    }

    /**
     * @param descriptor
     *            the descriptor to set
     */
    public void setDescriptor(int descriptor) {
        this.descriptor = (descriptor & 0xFF);
        this.isEOF = false;
        if ((this.descriptor & EOF) != 0) {
            this.isEOF = true;
        }
        this.isEOR = false;
        if ((this.descriptor & EOR) != 0) {
            this.isEOR = true;
        }
        this.isERROR = false;
        if ((this.descriptor & ERROR) != 0) {
            this.isERROR = true;
        }
        this.isRESTART = false;
        if ((this.descriptor & RESTART) != 0) {
            this.isRESTART = true;
        }
    }

    /**
     * @return the isEOF
     */
    public boolean isEOF() {
        return this.isEOF;
    }

    /**
     * @param isEOF
     *            the isEOF to set
     */
    public void setEOF(boolean isEOF) {
        this.isEOF = isEOF;
        this.descriptor = this.descriptor | EOF;
    }

    /**
     * @return the isEOR
     */
    public boolean isEOR() {
        return this.isEOR;
    }

    /**
     * @param isEOR
     *            the isEOR to set
     */
    public void setEOR(boolean isEOR) {
        this.isEOR = isEOR;
        this.descriptor = this.descriptor | EOR;
    }

    /**
     * @return the isERROR
     */
    public boolean isERROR() {
        return this.isERROR;
    }

    /**
     * @param isERROR
     *            the isERROR to set
     */
    public void setERROR(boolean isERROR) {
        this.isERROR = isERROR;
        this.descriptor = this.descriptor | ERROR;
    }

    /**
     * @return the isRESTART
     */
    public boolean isRESTART() {
        return this.isRESTART;
    }

    /**
     * @param isRESTART
     *            the isRESTART to set
     */
    public void setRESTART(boolean isRESTART) {
        this.isRESTART = isRESTART;
        this.descriptor = this.descriptor | RESTART;
    }

    /**
     * @return the markers
     */
    public int[] getMarkers() {
        return this.markers;
    }

    /**
     * 
     * @return the 6 bytes representation of the markers
     */
    public byte[] getByteMarkers() {
        byte[] bmarkers = new byte[6];
        if (this.markers == null) {
            for (int i = 0; i < 6; i ++) {
                bmarkers[i] = 0;
            }
        } else {
            for (int i = 0; i < 6; i ++) {
                bmarkers[i] = (byte) (this.markers[i] & 0xFF);
            }
        }
        return bmarkers;
    }

    /**
     * Set the markers and the byte count
     * 
     * @param markers
     *            the markers to set
     */
    public void setMarkers(int[] markers) {
        this.markers = markers;
        this.byteCount = 6;
    }

    /**
     * Clear the object
     * 
     */
    public void clear() {
        this.block = null;
        this.byteCount = -1;
        this.descriptor = 0;
        this.isEOF = false;
        this.isEOR = false;
        this.isERROR = false;
        this.isRESTART = false;
        this.markers = null;
    }

    /**
     * Is this Block cleared
     * 
     * @return True if this Block is cleared
     */
    public boolean isCleared() {
        return (this.byteCount == -1);
    }

    /**
     * Translate the given array of byte into a string in binary format
     * 
     * @param bytes
     * @param cutted
     *            True if each Byte should be 'blank' separated or not
     * @return the string
     */
    public static String toBinaryString(byte[] bytes, boolean cutted) {
        StringBuilder buffer = new StringBuilder();
        boolean first = true;
        for (byte b: bytes) {
            if (cutted) {
                if (first) {
                    first = false;
                } else {
                    buffer.append(' ');
                }
            }
            String bin = Integer.toBinaryString(b & 0xFF);
            bin = bin.substring(0, Math.min(bin.length(), 8));
            for (int j = 0; j < 8 - bin.length(); j ++) {
                buffer.append('0');
            }
            buffer.append(bin);
        }
        return buffer.toString();
    }
}
