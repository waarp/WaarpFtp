/**
   This file is part of GoldenGate Project (named also GoldenGate or GG).

   Copyright 2009, Frederic Bregier, and individual contributors by the @author
   tags. See the COPYRIGHT.txt in the distribution for a full listing of
   individual contributors.

   All GoldenGate Project is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   GoldenGate is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with GoldenGate .  If not, see <http://www.gnu.org/licenses/>.
 */
package goldengate.ftp.core.data.handler;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * SeekAheadData Class used to optimize access to the incoming buffer
 * @author Frederic Bregier
 *
 */
public class FtpSeekAheadData {
    /**
     * Exception when NO Backend Array is found
     */
     static class SeekAheadNoBackArrayException extends Exception {
         private static final long serialVersionUID = -630418804938699495L;
     }
     
    byte[] bytes;

    int readerIndex;

    int pos;

    int limit;

    ChannelBuffer buffer;

    /**
    * @param buffer
    */
    FtpSeekAheadData(ChannelBuffer buffer) throws SeekAheadNoBackArrayException {
        if (!buffer.hasArray()) {
            throw new SeekAheadNoBackArrayException();
        }
        this.buffer = buffer;
        this.bytes = buffer.array();
        this.pos = this.readerIndex = buffer.readerIndex();
        this.limit = buffer.writerIndex();
    }

    /**
    *
    * @param minus this value will be used as (currentPos - minus) to set
    * the current readerIndex in the buffer.
    */
    void setReadPosition(int minus) {
        pos -= minus;
        readerIndex = pos;
        buffer.readerIndex(readerIndex);
    }

    void clear() {
        this.buffer = null;
        this.bytes = null;
        this.limit = 0;
        this.pos = 0;
        this.readerIndex = 0;
    }
}
