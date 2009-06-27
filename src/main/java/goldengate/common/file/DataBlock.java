/**
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3.0 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package goldengate.common.file;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Main object implementing Data Block whaveter the mode, type, structure used.
 *
 * @author Frederic Bregier
 *
 */
public class DataBlock {
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
    public DataBlock() {
    }

    /**
     * @return the block
     */
    public ChannelBuffer getBlock() {
        return block;
    }

    /**
     * Set the block and the byte count according to the block
     *
     * @param block
     *            the block to set
     */
    public void setBlock(ChannelBuffer block) {
        if (isRESTART) {
            this.block = null;
            markers = new int[6];
            for (int i = 0; i < 6; i ++) {
                markers[i] = block.readByte();
            }
            byteCount = 6;
            return;
        }
        this.block = block;
        if (this.block == null) {
            byteCount = 0;
        } else {
            byteCount = this.block.readableBytes();
        }
    }

    /**
     * @return the byteCount
     */
    public int getByteCount() {
        return byteCount;
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
        byteCount = upper << 8 | lower;
    }

    /**
     *
     * @return the Upper byte of the byte count
     */
    public byte getByteCountUpper() {
        return (byte) (byteCount >> 8 & 0xFF);
    }

    /**
     *
     * @return the Lower byte of the byte count
     */
    public byte getByteCountLower() {
        return (byte) (byteCount & 0xFF);
    }

    /**
     * @return the descriptor
     */
    public byte getDescriptor() {
        return (byte) (descriptor & 0xFF);
    }

    /**
     * @param descriptor
     *            the descriptor to set
     */
    public void setDescriptor(int descriptor) {
        this.descriptor = descriptor & 0xFF;
        isEOF = false;
        if ((this.descriptor & EOF) != 0) {
            isEOF = true;
        }
        isEOR = false;
        if ((this.descriptor & EOR) != 0) {
            isEOR = true;
        }
        isERROR = false;
        if ((this.descriptor & ERROR) != 0) {
            isERROR = true;
        }
        isRESTART = false;
        if ((this.descriptor & RESTART) != 0) {
            isRESTART = true;
        }
    }

    /**
     * @return the isEOF
     */
    public boolean isEOF() {
        return isEOF;
    }

    /**
     * @param isEOF
     *            the isEOF to set
     */
    public void setEOF(boolean isEOF) {
        this.isEOF = isEOF;
        descriptor = descriptor | EOF;
    }

    /**
     * @return the isEOR
     */
    public boolean isEOR() {
        return isEOR;
    }

    /**
     * @param isEOR
     *            the isEOR to set
     */
    public void setEOR(boolean isEOR) {
        this.isEOR = isEOR;
        descriptor = descriptor | EOR;
    }

    /**
     * @return the isERROR
     */
    public boolean isERROR() {
        return isERROR;
    }

    /**
     * @param isERROR
     *            the isERROR to set
     */
    public void setERROR(boolean isERROR) {
        this.isERROR = isERROR;
        descriptor = descriptor | ERROR;
    }

    /**
     * @return the isRESTART
     */
    public boolean isRESTART() {
        return isRESTART;
    }

    /**
     * @param isRESTART
     *            the isRESTART to set
     */
    public void setRESTART(boolean isRESTART) {
        this.isRESTART = isRESTART;
        descriptor = descriptor | RESTART;
    }

    /**
     * @return the markers
     */
    public int[] getMarkers() {
        return markers;
    }

    /**
     *
     * @return the 6 bytes representation of the markers
     */
    public byte[] getByteMarkers() {
        byte[] bmarkers = new byte[6];
        if (markers == null) {
            for (int i = 0; i < 6; i ++) {
                bmarkers[i] = 0;
            }
        } else {
            for (int i = 0; i < 6; i ++) {
                bmarkers[i] = (byte) (markers[i] & 0xFF);
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
        byteCount = 6;
    }

    /**
     * Clear the object
     *
     */
    public void clear() {
        block = null;
        byteCount = -1;
        descriptor = 0;
        isEOF = false;
        isEOR = false;
        isERROR = false;
        isRESTART = false;
        markers = null;
    }

    /**
     * Is this Block cleared
     *
     * @return True if this Block is cleared
     */
    public boolean isCleared() {
        return byteCount == -1;
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
