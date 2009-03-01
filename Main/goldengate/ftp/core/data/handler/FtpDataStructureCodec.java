/**
 * Frederic Bregier LGPL 23 janv. 09 
 * FtpDataTypeCodec.java goldengate.ftp.core.file.handler GoldenGateFtp
 * frederic
 */
package goldengate.ftp.core.data.handler;

import goldengate.ftp.core.command.FtpArgumentCode.TransferStructure;
import goldengate.ftp.core.exception.FtpInvalidArgumentException;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 * Third CODEC :<br>
 * - encode/decode : takes {@link FtpDataBlock} and transforms it to a {@link FtpDataBlock}<br>
 * FILE and RECORD are implemented (DataNetworkHandler will do the real job). PAGE is not implemented.<br>
 * Note that real actions are taken in the DataNetworkHandler according to the implementation of FtpFile.
 * @author frederic
 * goldengate.ftp.core.file.handler FtpDataTypeCodec
 * 
 */
@ChannelPipelineCoverage("one")
public class FtpDataStructureCodec extends SimpleChannelHandler {
	/*	 
       3.1.2.  DATA STRUCTURES

         In addition to different representation types, FTP allows the
         structure of a file to be specified.  Three file structures are
         defined in FTP:

            file-structure,     where there is no internal structure and
                                the file is considered to be a
                                continuous sequence of data bytes,

            record-structure,   where the file is made up of sequential
                                records,

            and page-structure, where the file is made up of independent
                                indexed pages.

         File-structure is the default to be assumed if the STRUcture
         command has not been used but both file and record structures
         must be accepted for "text" files (i.e., files with TYPE ASCII
         or EBCDIC) by all FTP implementations.  The structure of a file
         will affect both the transfer mode of a file (see the Section
         on Transmission Modes) and the interpretation and storage of
         the file.

         The "natural" structure of a file will depend on which host
         stores the file.  A source-code file will usually be stored on
         an IBM Mainframe in fixed length records but on a DEC TOPS-20
         as a stream of characters partitioned into lines, for example
         by <CRLF>.  If the transfer of files between such disparate
         sites is to be useful, there must be some way for one site to
         recognize the other's assumptions about the file.

         With some sites being naturally file-oriented and others
         naturally record-oriented there may be problems if a file with
         one structure is sent to a host oriented to the other.  If a
         text file is sent with record-structure to a host which is file
         oriented, then that host should apply an internal
         transformation to the file based on the record structure.
         Obviously, this transformation should be useful, but it must
         also be invertible so that an identical file may be retrieved
         using record structure.

         In the case of a file being sent with file-structure to a
         record-oriented host, there exists the question of what
         criteria the host should use to divide the file into records
         which can be processed locally.  If this division is necessary,
         the FTP implementation should use the end-of-line sequence,

         <CRLF> for ASCII, or <NL> for EBCDIC text files, as the
         delimiter.  If an FTP implementation adopts this technique, it
         must be prepared to reverse the transformation if the file is
         retrieved with file-structure.

         3.1.2.1.  FILE STRUCTURE

            File structure is the default to be assumed if the STRUcture
            command has not been used.

            In file-structure there is no internal structure and the
            file is considered to be a continuous sequence of data
            bytes.

         3.1.2.2.  RECORD STRUCTURE

            Record structures must be accepted for "text" files (i.e.,
            files with TYPE ASCII or EBCDIC) by all FTP implementations.

            In record-structure the file is made up of sequential
            records.

         3.1.2.3.  PAGE STRUCTURE

            To transmit files that are discontinuous, FTP defines a page
            structure.  Files of this type are sometimes known as
            "random access files" or even as "holey files".  In these
            files there is sometimes other information associated with
            the file as a whole (e.g., a file descriptor), or with a
            section of the file (e.g., page access controls), or both.
            In FTP, the sections of the file are called pages.

            To provide for various page sizes and associated
            information, each page is sent with a page header.  The page
            header has the following defined fields:

               Header Length

                  The number of logical bytes in the page header
                  including this byte.  The minimum header length is 4.

               Page Index

                  The logical page number of this section of the file.
                  This is not the transmission sequence number of this
                  page, but the index used to identify this page of the
                  file.

               Data Length

                  The number of logical bytes in the page data.  The
                  minimum data length is 0.

               Page Type

                  The type of page this is.  The following page types
                  are defined:

                     0 = Last Page

                        This is used to indicate the end of a paged
                        structured transmission.  The header length must
                        be 4, and the data length must be 0.

                     1 = Simple Page

                        This is the normal type for simple paged files
                        with no page level associated control
                        information.  The header length must be 4.

                     2 = Descriptor Page

                        This type is used to transmit the descriptive
                        information for the file as a whole.

                     3 = Access Controlled Page

                        This type includes an additional header field
                        for paged files with page level access control
                        information.  The header length must be 5.

               Optional Fields

                  Further header fields may be used to supply per page
                  control information, for example, per page access
                  control.

            All fields are one logical byte in length.  The logical byte
            size is specified by the TYPE command.  See Appendix I for
            further details and a specific case at the page structure.

      A note of caution about parameters:  a file must be stored and
      retrieved with the same parameters if the retrieved version is to

      be identical to the version originally transmitted.  Conversely,
      FTP implementations must return a file identical to the original
      if the parameters used to store and retrieve a file are the same.

	 */
	/**
	 * Structure of transfer
	 */
	private TransferStructure structure = null;
	/**
	 * @param structure
	 */
	public FtpDataStructureCodec(TransferStructure structure) {
		this.structure = structure;
	}

	/**
	 * @return the structure
	 */
	public TransferStructure getStructure() {
		return this.structure;
	}

	/**
	 * @param structure the structure to set
	 */
	public void setStructure(TransferStructure structure) {
		this.structure = structure;
	}
	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelHandler#writeRequested(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
	 */
	@Override
	public void writeRequested(ChannelHandlerContext arg0, MessageEvent arg1) throws Exception {
		Object o = arg1.getMessage();
		if (! (o instanceof FtpDataBlock)) {
			// Type unimplemented
			throw new FtpInvalidArgumentException("Wrong object received in "+this.getClass().getName()+" codec "+o.getClass().getName());
		}
		if (this.structure == TransferStructure.FILE) {
			super.writeRequested(arg0, arg1);
			return;
		} else if (this.structure == TransferStructure.RECORD) {
			super.writeRequested(arg0, arg1);
			return;
		}
		// Type unimplemented
		throw new FtpInvalidArgumentException("Structure unimplemented in "+this.getClass().getName()+" codec "+this.structure.name());
	}

	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
	 */
	@Override
	public void messageReceived(ChannelHandlerContext arg0, MessageEvent arg1) throws Exception {
		Object o = arg1.getMessage();
		if (! (o instanceof FtpDataBlock)) {
			// Type unimplemented
			throw new FtpInvalidArgumentException("Wrong object received in "+this.getClass().getName()+" codec "+o.getClass().getName());
		}
		if (this.structure == TransferStructure.FILE) {
			super.messageReceived(arg0, arg1);
			return;
		} else if (this.structure == TransferStructure.RECORD) {
			super.messageReceived(arg0, arg1);
			return;
		}
		// Type unimplemented
		throw new FtpInvalidArgumentException("Structure unimplemented in "+this.getClass().getName()+" codec "+this.structure.name());
	}
	
}
