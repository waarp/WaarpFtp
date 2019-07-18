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
package org.waarp.ftp.core.data.handler;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import org.waarp.common.exception.InvalidArgumentException;
import org.waarp.common.file.DataBlock;
import org.waarp.common.future.WaarpFuture;
import org.waarp.ftp.core.command.FtpArgumentCode.TransferMode;
import org.waarp.ftp.core.command.FtpArgumentCode.TransferStructure;
import org.waarp.ftp.core.config.FtpConfiguration;
import org.waarp.ftp.core.data.handler.FtpSeekAheadData.SeekAheadNoBackArrayException;

/**
 * First CODEC :<br>
 * - encode : takes a {@link DataBlock} and transforms it to a ByteBuf<br>
 * - decode : takes a ByteBuf and transforms it to a {@link DataBlock}<br>
 * STREAM and BLOCK mode are implemented. COMPRESSED mode is not implemented.
 * 
 * @author Frederic Bregier
 * 
 */
public class FtpDataModeCodec extends ByteToMessageCodec<DataBlock> {
    /*
     * 3.4.1. STREAM MODE The data is transmitted as a stream of bytes. There is no restriction on
     * the representation type used; record structures are allowed. In a record structured file EOR
     * and EOF will each be indicated by a two-byte control code. The first byte of the control code
     * will be all ones, the escape character. The second byte will have the low order bit on and
     * zeros elsewhere for EOR and the second low order bit on for EOF; that is, the byte will have
     * value 1 for EOR and value 2 for EOF. EOR and EOF may be indicated together on the last byte
     * transmitted by turning both low order bits on (i.e., the value 3). If a byte of all ones was
     * intended to be sent as data, it should be repeated in the second byte of the control code. If
     * the structure is a file structure, the EOF is indicated by the sending host closing the data
     * connection and all bytes are data bytes. 3.4.2. BLOCK MODE The file is transmitted as a
     * series of data blocks preceded by one or more header bytes. The header bytes contain a count
     * field, and descriptor code. The count field indicates the total length of the data block in
     * bytes, thus marking the beginning of the next data block (there are no filler bits). The
     * descriptor code defines: last block in the file (EOF) last block in the record (EOR), restart
     * marker (see the Section on Error Recovery and Restart) or suspect data (i.e., the data being
     * transferred is suspected of errors and is not reliable). This last code is NOT intended for
     * error control within FTP. It is motivated by the desire of sites exchanging certain types of
     * data (e.g., seismic or weather data) to send and receive all the data despite local errors
     * (such as "magnetic tape read errors"), but to indicate in the transmission that certain
     * portions are suspect). Record structures are allowed in this mode, and any representation
     * type may be used. The header consists of the three bytes. Of the 24 bits of header
     * information, the 16 low order bits shall represent byte count, and the 8 high order bits
     * shall represent descriptor codes as shown below. Block Header
     * +----------------+----------------+----------------+ | Descriptor | Byte Count | | 8 bits |
     * 16 bits | +----------------+----------------+----------------+ The descriptor codes are
     * indicated by bit flags in the descriptor byte. Four codes have been assigned, where each code
     * number is the decimal value of the corresponding bit in the byte. Code Meaning 128 End of
     * data block is EOR 64 End of data block is EOF 32 Suspected errors in data block 16 Data block
     * is a restart marker With this encoding, more than one descriptor coded condition may exist
     * for a particular block. As many bits as necessary may be flagged. The restart marker is
     * embedded in the data stream as an integral number of 8-bit bytes representing printable
     * characters in the language being used over the control connection (e.g., default--NVT-ASCII).
     * <SP> (Space, in the appropriate language) must not be used WITHIN a restart marker. For
     * example, to transmit a six-character marker, the following would be sent:
     * +--------+--------+--------+ |Descrptr| Byte count | |code= 16| = 6 |
     * +--------+--------+--------+ +--------+--------+--------+ | Marker | Marker | Marker | | 8
     * bits | 8 bits | 8 bits | +--------+--------+--------+ +--------+--------+--------+ | Marker |
     * Marker | Marker | | 8 bits | 8 bits | 8 bits | +--------+--------+--------+
     */
    /**
     * Transfer Mode
     */
    private TransferMode mode = null;

    /**
     * Structure Mode
     */
    private TransferStructure structure = null;

    /**
     * Ftp Data Block
     */
    private DataBlock dataBlock = null;

    /**
     * Last byte for STREAM+RECORD
     */
    private int lastbyte = 0;

    /**
     * Is the underlying DataNetworkHandler ready to receive block
     */
    private volatile boolean isReady = false;

    /**
     * Blocking step between DataNetworkHandler and this Codec in order to wait that the
     * DataNetworkHandler is ready
     */
    private final WaarpFuture codecLocked = new WaarpFuture();

    /**
     * @param mode
     * @param structure
     */
    public FtpDataModeCodec(TransferMode mode, TransferStructure structure) {
        super();
        this.mode = mode;
        this.structure = structure;
    }

    /**
     * Inform the Codec that DataNetworkHandler is ready (called from DataNetworkHandler after
     * setCorrectCodec).
     * 
     */
    public void setCodecReady() {
        codecLocked.setSuccess();
    }

    protected DataBlock decodeRecordStandard(ByteBuf buf, int length) {
        ByteBuf newbuf = ByteBufAllocator.DEFAULT.buffer(length);
        if (lastbyte == 0xFF) {
            int nextbyte = buf.readByte();
            if (nextbyte == 0xFF) {
                newbuf.writeByte((byte) (lastbyte & 0xFF));
            } else {
                if (nextbyte == 1) {
                    dataBlock.setEOR(true);
                } else if (nextbyte == 2) {
                    dataBlock.setEOF(true);
                } else if (nextbyte == 3) {
                    dataBlock.setEOR(true);
                    dataBlock.setEOF(true);
                }
                lastbyte = 0;
            }
        }
        try {
            while (true) {
                lastbyte = buf.readByte();
                if (lastbyte == 0xFF) {
                    int nextbyte = buf.readByte();
                    if (nextbyte == 0xFF) {
                        newbuf.writeByte((byte) (lastbyte & 0xFF));
                    } else {
                        if (nextbyte == 1) {
                            dataBlock.setEOR(true);
                        } else if (nextbyte == 2) {
                            dataBlock.setEOF(true);
                        } else if (nextbyte == 3) {
                            dataBlock.setEOR(true);
                            dataBlock.setEOF(true);
                        }
                    }
                } else {
                    newbuf.writeByte((byte) (lastbyte & 0xFF));
                }
                lastbyte = 0;
            }
        } catch (IndexOutOfBoundsException e) {
            // End of read
        }
        dataBlock.setBlock(newbuf);
        return dataBlock;
    }

    protected DataBlock decodeRecord(ByteBuf buf, int length) {
        FtpSeekAheadData sad = null;
        try {
            sad = new FtpSeekAheadData(buf);
        } catch (SeekAheadNoBackArrayException e1) {
            return decodeRecordStandard(buf, length);
        }
        ByteBuf newbuf = ByteBufAllocator.DEFAULT.buffer(length);
        if (lastbyte == 0xFF) {
            int nextbyte = sad.bytes[sad.pos++];
            if (nextbyte == 0xFF) {
                newbuf.writeByte((byte) (lastbyte & 0xFF));
            } else {
                if (nextbyte == 1) {
                    dataBlock.setEOR(true);
                } else if (nextbyte == 2) {
                    dataBlock.setEOF(true);
                } else if (nextbyte == 3) {
                    dataBlock.setEOR(true);
                    dataBlock.setEOF(true);
                }
                lastbyte = 0;
            }
        }
        try {
            while (sad.pos < sad.limit) {
                lastbyte = sad.bytes[sad.pos++];
                if (lastbyte == 0xFF) {
                    int nextbyte = sad.bytes[sad.pos++];
                    if (nextbyte == 0xFF) {
                        newbuf.writeByte((byte) (lastbyte & 0xFF));
                    } else {
                        if (nextbyte == 1) {
                            dataBlock.setEOR(true);
                        } else if (nextbyte == 2) {
                            dataBlock.setEOF(true);
                        } else if (nextbyte == 3) {
                            dataBlock.setEOR(true);
                            dataBlock.setEOF(true);
                        }
                    }
                } else {
                    newbuf.writeByte((byte) (lastbyte & 0xFF));
                }
                lastbyte = 0;
            }
        } catch (IndexOutOfBoundsException e) {
            // End of read
        }
        sad.setReadPosition(0);
        dataBlock.setBlock(newbuf);
        return dataBlock;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        // First test if the connection is fully ready (block might be
        // transfered
        // by client before connection is ready)
        if (!isReady) {
            if (!codecLocked.await(FtpConfiguration.getDATATIMEOUTCON())) {
                throw new InvalidArgumentException("Codec not unlocked while should be");
            }
            isReady = true;
        }
        if (buf.readableBytes() == 0) {
            return;
        }
        // If STREAM Mode, no task to do, just next filter
        if (mode == TransferMode.STREAM) {
            dataBlock = new DataBlock();
            if (structure != TransferStructure.RECORD) {
                ByteBuf newbuf = Unpooled.wrappedBuffer(buf);
                buf.readerIndex(buf.readableBytes());
                newbuf.retain();
                dataBlock.setBlock(newbuf);
                out.add(dataBlock);
                return;
            }
            // Except if RECORD Structure!
            int length = buf.readableBytes();
            out.add(decodeRecord(buf, length));
            return;
        } else if (mode == TransferMode.BLOCK) {
            // Now we are in BLOCK Mode
            // Make sure if the length field was received.
            if (buf.readableBytes() < 3) {
                // The length field was not received yet - return null.
                // This method will be invoked again when more packets are
                // received and appended to the buffer.
                return;
            }

            // The length field is in the buffer.

            // Mark the current buffer position before reading the length field
            // because the whole frame might not be in the buffer yet.
            // We will reset the buffer position to the marked position if
            // there's not enough bytes in the buffer.
            buf.markReaderIndex();

            if (dataBlock == null) {
                dataBlock = new DataBlock();
            }
            // Read the descriptor
            dataBlock.setDescriptor(buf.readByte());

            // Read the length field.
            byte upper = buf.readByte();
            byte lower = buf.readByte();
            dataBlock.setByteCount(upper, lower);

            // Make sure if there's enough bytes in the buffer.
            if (buf.readableBytes() < dataBlock.getByteCount()) {
                // The whole bytes were not received yet - return null.
                // This method will be invoked again when more packets are
                // received and appended to the buffer.

                // Reset to the marked position to read the length field again
                // next time.
                buf.resetReaderIndex();

                return;
            }
            if (dataBlock.getByteCount() > 0) {
                // There's enough bytes in the buffer. Read it.
                dataBlock.setBlock(buf.readBytes(dataBlock.getByteCount()));
            }
            DataBlock returnDataBlock = dataBlock;
            // Free the datablock for next frame
            dataBlock = null;
            // Successfully decoded a frame. Return the decoded frame.
            out.add(returnDataBlock);
            return;
        }
        // Type unimplemented
        throw new InvalidArgumentException("Mode unimplemented: " + mode.name());
    }

    protected ByteBuf encodeRecordStandard(DataBlock msg, ByteBuf buffer) {
        ByteBuf newbuf = ByteBufAllocator.DEFAULT.buffer(msg.getByteCount());
        int newbyte = 0;
        try {
            while (true) {
                newbyte = buffer.readByte();
                if (newbyte == 0xFF) {
                    newbuf.writeByte((byte) (newbyte & 0xFF));
                }
                newbuf.writeByte((byte) (newbyte & 0xFF));
            }
        } catch (IndexOutOfBoundsException e) {
            // end of read
        }
        int value = 0;
        if (msg.isEOF()) {
            value += 2;
        }
        if (msg.isEOR()) {
            value += 1;
        }
        if (value > 0) {
            newbuf.writeByte((byte) 0xFF);
            newbuf.writeByte((byte) (value & 0xFF));
        }
        msg.clear();
        return newbuf;
    }

    protected ByteBuf encodeRecord(DataBlock msg, ByteBuf buffer) {
        FtpSeekAheadData sad = null;
        try {
            sad = new FtpSeekAheadData(buffer);
        } catch (SeekAheadNoBackArrayException e1) {
            return encodeRecordStandard(msg, buffer);
        }
        ByteBuf newbuf = ByteBufAllocator.DEFAULT.buffer(msg.getByteCount());
        int newbyte = 0;
        try {
            while (sad.pos < sad.limit) {
                newbyte = sad.bytes[sad.pos++];
                if (newbyte == 0xFF) {
                    newbuf.writeByte((byte) (newbyte & 0xFF));
                }
                newbuf.writeByte((byte) (newbyte & 0xFF));
            }
        } catch (IndexOutOfBoundsException e) {
            // end of read
        }
        int value = 0;
        if (msg.isEOF()) {
            value += 2;
        }
        if (msg.isEOR()) {
            value += 1;
        }
        if (value > 0) {
            newbuf.writeByte((byte) 0xFF);
            newbuf.writeByte((byte) (value & 0xFF));
        }
        msg.clear();
        sad.setReadPosition(0);
        return newbuf;
    }

    /**
     * Encode a DataBlock in the correct format for Mode
     * 
     * @param msg
     * @return the ByteBuf or null when the last block is already done
     * @throws InvalidArgumentException
     */
    protected ByteBuf encode(DataBlock msg)
            throws InvalidArgumentException {
        if (msg.isCleared()) {
            return null;
        }
        ByteBuf buffer = msg.getBlock();
        if (mode == TransferMode.STREAM) {
            // If record structure, special attention
            if (structure == TransferStructure.RECORD) {
                return encodeRecord(msg, buffer);
            }
            msg.clear();
            return buffer;
        } else if (mode == TransferMode.BLOCK) {
            int length = msg.getByteCount();
            ByteBuf newbuf = ByteBufAllocator.DEFAULT.buffer(length > 0xFFFF ? 0xFFFF + 3 : length + 3);
            byte[] header = new byte[3];
            // Is there any data left
            if (length == 0) {
                // It could be an empty block for EOR or EOF
                if (msg.isEOF() || msg.isEOR()) {
                    header[0] = msg.getDescriptor();
                    header[1] = 0;
                    header[2] = 0;
                    newbuf.writeBytes(header);
                    // Next call will be the last one
                    msg.clear();
                    // return the last block
                    return newbuf;
                }
                // This was the very last call
                msg.clear();
                // return the end of encode
                return null;
            }
            // Is this a Restart so only Markers
            if (msg.isRESTART()) {
                header[0] = msg.getDescriptor();
                header[1] = msg.getByteCountUpper();
                header[2] = msg.getByteCountLower();
                newbuf.writeBytes(header);
                newbuf.writeBytes(msg.getByteMarkers());
                // Next call will be the last one
                msg.clear();
                // return the last block
                return newbuf;
            }
            // Work on sub block, ignoring descriptor since it is not the last
            // one
            if (length > 0xFFFF) {
                header[0] = 0;
                header[1] = (byte) 0xFF;
                header[2] = (byte) 0xFF;
                newbuf.writeBytes(header);
                // Now take the first 0xFFFF bytes from buffer
                newbuf.writeBytes(msg.getBlock(), 0xFFFF);
                length -= 0xFFFF;
                msg.setByteCount(length);
                // return the sub block
                return newbuf;
            }
            // Last final block, using the descriptor
            header[0] = msg.getDescriptor();
            header[1] = msg.getByteCountUpper();
            header[2] = msg.getByteCountLower();
            newbuf.writeBytes(header);
            // real data
            newbuf.writeBytes(buffer, length);
            // Next call will be the last one
            msg.clear();
            // return the last block
            return newbuf;
        }
        // Mode unimplemented
        throw new InvalidArgumentException("Mode unimplemented: " + mode.name());
    }

    /**
     * @return the mode
     */
    public TransferMode getMode() {
        return mode;
    }

    /**
     * @param mode
     *            the mode to set
     */
    public void setMode(TransferMode mode) {
        this.mode = mode;
    }

    /**
     * @return the structure
     */
    public TransferStructure getStructure() {
        return structure;
    }

    /**
     * @param structure
     *            the structure to set
     */
    public void setStructure(TransferStructure structure) {
        this.structure = structure;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, DataBlock msg, ByteBuf out) throws Exception {
        // First test if the connection is fully ready (block might be
        // transfered
        // by client before connection is ready)
        if (!isReady) {
            if (!codecLocked.await(FtpConfiguration.getDATATIMEOUTCON())) {
                throw new InvalidArgumentException("Codec not unlocked while should be");
            }
            isReady = true;
        }
        ByteBuf next = encode(msg);
        // Could be splitten in several block
        while (next != null) {
            out.writeBytes(next);
            next = encode(msg);
        }
    }
}
