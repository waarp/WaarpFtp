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

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import org.waarp.common.exception.InvalidArgumentException;
import org.waarp.common.file.DataBlock;
import org.waarp.ftp.core.command.FtpArgumentCode.TransferStructure;

/**
 * Third CODEC :<br>
 * - encode/decode : takes {@link DataBlock} and transforms it to a {@link DataBlock}<br>
 * FILE and RECORD are implemented (DataNetworkHandler will do the real job). PAGE is not
 * implemented.<br>
 * Note that real actions are taken in the DataNetworkHandler according to the implementation of
 * FtpFile.
 * 
 * @author Frederic Bregier
 * 
 */
@Sharable
class FtpDataStructureCodec extends MessageToMessageCodec<DataBlock, DataBlock> {
    /*
     * 3.1.2. DATA STRUCTURES In addition to different representation types, FTP allows the
     * structure of a file to be specified. Three file structures are defined in FTP:
     * file-structure, where there is no internal structure and the file is considered to be a
     * continuous sequence of data bytes, record-structure, where the file is made up of sequential
     * records, and page-structure, where the file is made up of independent indexed pages.
     * FileInterface-structure is the default to be assumed if the STRUcture command has not been
     * used but both file and record structures must be accepted for "text" files (i.e., files with
     * TYPE ASCII or EBCDIC) by all FTP implementations. The structure of a file will affect both
     * the transfer mode of a file (see the Section on Transmission Modes) and the interpretation
     * and storage of the file. The "natural" structure of a file will depend on which host stores
     * the file. A source-code file will usually be stored on an IBM Mainframe in fixed length
     * records but on a DEC TOPS-20 as a stream of characters partitioned into lines, for example by
     * <CRLF>. If the transfer of files between such disparate sites is to be useful, there must be
     * some way for one site to recognize the other's assumptions about the file. With some sites
     * being naturally file-oriented and others naturally record-oriented there may be problems if a
     * file with one structure is sent to a host oriented to the other. If a text file is sent with
     * record-structure to a host which is file oriented, then that host should apply an internal
     * transformation to the file based on the record structure. Obviously, this transformation
     * should be useful, but it must also be invertible so that an identical file may be retrieved
     * using record structure. In the case of a file being sent with file-structure to a
     * record-oriented host, there exists the question of what criteria the host should use to
     * divide the file into records which can be processed locally. If this division is necessary,
     * the FTP implementation should use the end-of-line sequence, <CRLF> for ASCII, or <NL> for
     * EBCDIC text files, as the delimiter. If an FTP implementation adopts this technique, it must
     * be prepared to reverse the transformation if the file is retrieved with file-structure.
     * 3.1.2.1. FILE STRUCTURE FileInterface structure is the default to be assumed if the STRUcture
     * command has not been used. In file-structure there is no internal structure and the file is
     * considered to be a continuous sequence of data bytes. 3.1.2.2. RECORD STRUCTURE Record
     * structures must be accepted for "text" files (i.e., files with TYPE ASCII or EBCDIC) by all
     * FTP implementations. In record-structure the file is made up of sequential records. 3.1.2.3.
     * PAGE STRUCTURE To transmit files that are discontinuous, FTP defines a page structure. Files
     * of this type are sometimes known as "random access files" or even as "holey files". In these
     * files there is sometimes other information associated with the file as a whole (e.g., a file
     * descriptor), or with a section of the file (e.g., page access controls), or both. In FTP, the
     * sections of the file are called pages. To provide for various page sizes and associated
     * information, each page is sent with a page header. The page header has the following defined
     * fields: Header Length The number of logical bytes in the page header including this byte. The
     * minimum header length is 4. Page Index The logical page number of this section of the file.
     * This is not the transmission sequence number of this page, but the index used to identify
     * this page of the file. Data Length The number of logical bytes in the page data. The minimum
     * data length is 0. Page Type The type of page this is. The following page types are defined: 0
     * = Last Page This is used to indicate the end of a paged structured transmission. The header
     * length must be 4, and the data length must be 0. 1 = Simple Page This is the normal type for
     * simple paged files with no page level associated control information. The header length must
     * be 4. 2 = Descriptor Page This type is used to transmit the descriptive information for the
     * file as a whole. 3 = Access Controlled Page This type includes an additional header field for
     * paged files with page level access control information. The header length must be 5. Optional
     * Fields Further header fields may be used to supply per page control information, for example,
     * per page access control. All fields are one logical byte in length. The logical byte size is
     * specified by the TYPE command. See Appendix I for further details and a specific case at the
     * page structure. A note of caution about parameters: a file must be stored and retrieved with
     * the same parameters if the retrieved version is to be identical to the version originally
     * transmitted. Conversely, FTP implementations must return a file identical to the original if
     * the parameters used to store and retrieve a file are the same.
     */
    /**
     * Structure of transfer
     */
    private TransferStructure structure = null;

    /**
     * @param structure
     */
    public FtpDataStructureCodec(TransferStructure structure) {
        super();
        this.structure = structure;
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
    protected void encode(ChannelHandlerContext ctx, DataBlock msg, List<Object> out) throws Exception {
        if (structure == TransferStructure.FILE) {
            out.add(msg);
            return;
        } else if (structure == TransferStructure.RECORD) {
            out.add(msg);
            return;
        }
        // Type unimplemented
        throw new InvalidArgumentException("Structure unimplemented in " +
                this.getClass().getName() + " codec " + structure.name());
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, DataBlock msg, List<Object> out) throws Exception {
        if (structure == TransferStructure.FILE) {
            out.add(msg);
            return;
        } else if (structure == TransferStructure.RECORD) {
            out.add(msg);
            return;
        }
        // Type unimplemented
        throw new InvalidArgumentException("Structure unimplemented in " +
                this.getClass().getName() + " codec " + structure.name());
    }

}
