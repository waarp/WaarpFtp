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

import java.nio.charset.Charset;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import org.waarp.common.exception.InvalidArgumentException;
import org.waarp.common.file.DataBlock;
import org.waarp.ftp.core.command.FtpArgumentCode.TransferSubType;
import org.waarp.ftp.core.command.FtpArgumentCode.TransferType;

/**
 * Second CODEC :<br>
 * - encode/decode : takes a {@link DataBlock} and transforms it to a new {@link DataBlock} according
 * to the types<br>
 * Force ASCII, EBCDIC or IMAGE (with NON PRINT). LOCAL and other subtypes are not implemented.
 * 
 * @author Frederic Bregier
 * 
 */
@Sharable
class FtpDataTypeCodec extends MessageToMessageCodec<DataBlock, DataBlock> {
    /*
     * 3.1.1. DATA TYPES Data representations are handled in FTP by a user specifying a
     * representation type. This type may implicitly (as in ASCII or EBCDIC) or explicitly (as in
     * Local byte) define a byte size for interpretation which is referred to as the
     * "logical byte size." Note that this has nothing to do with the byte size used for
     * transmission over the data connection, called the "transfer byte size", and the two should
     * not be confused. For example, NVT-ASCII has a logical byte size of 8 bits. If the type is
     * Local byte, then the TYPE command has an obligatory second parameter specifying the logical
     * byte size. The transfer byte size is always 8 bits. 3.1.1.1. ASCII TYPE This is the default
     * type and must be accepted by all FTP implementations. It is intended primarily for the
     * transfer of text files, except when both hosts would find the EBCDIC type more convenient.
     * The sender converts the data from an internal character representation to the standard 8-bit
     * NVT-ASCII representation (see the Telnet specification). The receiver will convert the data
     * from the standard form to his own internal form. In accordance with the NVT standard, the
     * <CRLF> sequence should be used where necessary to denote the end of a line of text. (See the
     * discussion of file structure at the end of the Section on Data Representation and Storage.)
     * Using the standard NVT-ASCII representation means that data must be interpreted as 8-bit
     * bytes. The Format parameter for ASCII and EBCDIC types is discussed below. 3.1.1.2. EBCDIC
     * TYPE This type is intended for efficient transfer between hosts which use EBCDIC for their
     * internal character representation. For transmission, the data are represented as 8-bit EBCDIC
     * characters. The character code is the only difference between the functional specifications
     * of EBCDIC and ASCII types. End-of-line (as opposed to end-of-record--see the discussion of
     * structure) will probably be rarely used with EBCDIC type for purposes of denoting structure,
     * but where it is necessary the <NL> character should be used. 3.1.1.3. IMAGE TYPE The data are
     * sent as contiguous bits which, for transfer, are packed into the 8-bit transfer bytes. The
     * receiving site must store the data as contiguous bits. The structure of the storage system
     * might necessitate the padding of the file (or of each record, for a record-structured file)
     * to some convenient boundary (byte, word or block). This padding, which must be all zeros, may
     * occur only at the end of the file (or at the end of each record) and there must be a way of
     * identifying the padding bits so that they may be stripped off if the file is retrieved. The
     * padding transformation should be well publicized to enable a user to process a file at the
     * storage site. Image type is intended for the efficient storage and retrieval of files and for
     * the transfer of binary data. It is recommended that this type be accepted by all FTP
     * implementations. 3.1.1.4. LOCAL TYPE The data is transferred in logical bytes of the size
     * specified by the obligatory second parameter, Byte size. The value of Byte size must be a
     * decimal integer; there is no default value. The logical byte size is not necessarily the same
     * as the transfer byte size. If there is a difference in byte sizes, then the logical bytes
     * should be packed contiguously, disregarding transfer byte boundaries and with any necessary
     * padding at the end. When the data reaches the receiving host, it will be transformed in a
     * manner dependent on the logical byte size and the particular host. This transformation must
     * be invertible (i.e., an identical file can be retrieved if the same parameters are used) and
     * should be well publicized by the FTP implementors. For example, a user sending 36-bit
     * floating-point numbers to a host with a 32-bit word could send that data as Local byte with a
     * logical byte size of 36. The receiving host would then be expected to store the logical bytes
     * so that they could be easily manipulated; in this example putting the 36-bit logical bytes
     * into 64-bit double words should suffice. In another example, a pair of hosts with a 36-bit
     * word size may send data to one another in words by using TYPE L 36. The data would be sent in
     * the 8-bit transmission bytes packed so that 9 transmission bytes carried two host words.
     * 3.1.1.5. FORMAT CONTROL The types ASCII and EBCDIC also take a second (optional) parameter;
     * this is to indicate what kind of vertical format control, if any, is associated with a file.
     * The following data representation types are defined in FTP: A character file may be
     * transferred to a host for one of three purposes: for printing, for storage and later
     * retrieval, or for processing. If a file is sent for printing, the receiving host must know
     * how the vertical format control is represented. In the second case, it must be possible to
     * store a file at a host and then retrieve it later in exactly the same form. Finally, it
     * should be possible to move a file from one host to another and process the file at the second
     * host without undue trouble. A single ASCII or EBCDIC format does not satisfy all these
     * conditions. Therefore, these types have a second parameter specifying one of the following
     * three formats: 3.1.1.5.1. NON PRINT This is the default format to be used if the second
     * (format) parameter is omitted. Non-print format must be accepted by all FTP implementations.
     * The file need contain no vertical format information. If it is passed to a printer process,
     * this process may assume standard values for spacing and margins. Normally, this format will
     * be used with files destined for processing or just storage. 3.1.1.5.2. TELNET FORMAT CONTROLS
     * The file contains ASCII/EBCDIC vertical format controls (i.e., <CR>, <LF>, <NL>, <VT>, <FF>)
     * which the printer process will interpret appropriately. <CRLF>, in exactly this sequence,
     * also denotes end-of-line. 3.1.1.5.2. CARRIAGE CONTROL (ASA) The file contains ASA (FORTRAN)
     * vertical format control characters. (See RFC 740 Appendix C; and Communications of the ACM,
     * Vol. 7, No. 10, p. 606, October 1964.) In a line or a record formatted according to the ASA
     * Standard, the first character is not to be printed. Instead, it should be used to determine
     * the vertical movement of the paper which should take place before the rest of the record is
     * printed. The ASA Standard specifies the following control characters: Character Vertical
     * Spacing blank Move paper up one line 0 Move paper up two lines 1 Move paper to top of next
     * page + No movement, i.e., overprint Clearly there must be some way for a printer process to
     * distinguish the end of the structural entity. If a file has record structure (see below) this
     * is no problem; records will be explicitly marked during transfer and storage. If the file has
     * no record structure, the <CRLF> end-of-line sequence is used to separate printing lines, but
     * these format effectors are overridden by the ASA controls.
     */
    /**
     * Charset to use
     */
    private Charset charsetName;

    /**
     * Type of transfer
     */
    private TransferType type = null;

    /**
     * Sub Type of transfer
     */
    private TransferSubType subType = null;

    /**
     * @param type
     * @param subType
     */
    public FtpDataTypeCodec(TransferType type, TransferSubType subType) {
        super();
        setCharset(null);
        this.type = type;
        this.subType = subType;
    }

    /**
     * @return the subType
     */
    public TransferSubType getSubType() {
        return subType;
    }

    /**
     * @param type
     *            the type to set
     * @param subType
     *            the subType to set
     */
    public void setFullType(TransferType type, TransferSubType subType) {
        this.type = type;
        this.subType = subType;
    }

    /**
     * @return the type
     */
    public TransferType getType() {
        return type;
    }

    /**
     * Set the charset
     * 
     * @param charset
     */
    private void setCharset(Charset charset) {
        if (charset == null) {
            charsetName = Charset.defaultCharset();
        } else {
            charsetName = charset;
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, DataBlock msg, List<Object> out) throws Exception {
        // Is an ASCII or EBCDIC mode or IMAGE mode
        if (type == TransferType.IMAGE) {
            out.add(msg);
            return;
        } else if (type == TransferType.ASCII || type == TransferType.EBCDIC) {
            ByteBuf buffer = msg.getBlock();
            msg.setBlock(decode(buffer));
            out.add(msg);
            return;
        }
        // Type unimplemented
        throw new InvalidArgumentException("Type unimplemented in " +
                this.getClass().getName() + " codec " + type.name());
    }

    /**
     * 
     * @param ByteBuf
     * @return the ByteBuf
     * @throws Exception
     */
    protected ByteBuf decode(ByteBuf ByteBuf)
            throws Exception {
        return Unpooled.copiedBuffer(ByteBuf
                .toString(type.charset), charsetName);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, DataBlock msg, List<Object> out) throws Exception {
        // Is an ASCII or EBCDIC mode or IMAGE mode
        if (type == TransferType.IMAGE) {
            out.add(msg);
            return;
        } else if (type == TransferType.ASCII || type == TransferType.EBCDIC) {
            ByteBuf buffer = msg.getBlock();
            msg.setBlock(encode(buffer));
            out.add(msg);
            return;
        }
        // Type unimplemented
        throw new InvalidArgumentException("Type unimplemented in " +
                this.getClass().getName() + " codec " + type.name());
    }

    /**
     * 
     * @param ByteBuf
     * @return the encoded buffer
     * @throws Exception
     */
    protected ByteBuf encode(ByteBuf ByteBuf)
            throws Exception {
        String chString = ByteBuf.toString(charsetName);
        return Unpooled.copiedBuffer(chString, type.charset);
    }
}
