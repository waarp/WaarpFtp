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
package goldengate.common.file.filesystembased;

import goldengate.common.file.OptsMLSxInterface;

/**
 * Class that implements Opts command for MLSx operations. (-1) means not
 * supported, 0 supported but not active, 1 supported and active
 *
 * @author Frederic Bregier
 *
 */
public class FilesystemBasedOptsMLSxImpl implements OptsMLSxInterface {
    /**
     * Size option
     */
    private byte optsSize = -1;

    /**
     * Modify option
     */
    private byte optsModify = -1;

    /**
     * Type option
     */
    private byte optsType = -1;

    /**
     * Perm option
     */
    private byte optsPerm = -1;

    /**
     * Create option
     */
    private byte optsCreate = -1;

    /**
     * Unique option
     */
    private byte optsUnique = -1;

    /**
     * Lang option
     */
    private byte optsLang = -1;

    /**
     * Media-Type option
     */
    private byte optsMediaType = -1;

    /**
     * Charset option
     */
    private byte optsCharset = -1;

    /**
     * Default empty constructor: no support at all of MLSx function
     */
    public FilesystemBasedOptsMLSxImpl() {
    }

    /**
     * (-1) means not supported, 0 supported but not active, 1 supported and
     * active
     *
     * @param optsSize
     * @param optsModify
     * @param optsType
     * @param optsPerm
     * @param optsCreate
     * @param optsUnique
     * @param optsLang
     * @param optsMediaType
     * @param optsCharset
     */
    public FilesystemBasedOptsMLSxImpl(byte optsSize, byte optsModify,
            byte optsType, byte optsPerm, byte optsCreate, byte optsUnique,
            byte optsLang, byte optsMediaType, byte optsCharset) {
        this.optsSize = optsSize;
        this.optsModify = optsModify;
        this.optsType = optsType;
        this.optsPerm = optsPerm;
        this.optsCreate = optsCreate;
        this.optsUnique = optsUnique;
        this.optsLang = optsLang;
        this.optsMediaType = optsMediaType;
        this.optsCharset = optsCharset;
    }

    /**
     * @return the optsCharset
     */
    public byte getOptsCharset() {
        return optsCharset;
    }

    /**
     * @param optsCharset
     *            the optsCharset to set
     */
    public void setOptsCharset(byte optsCharset) {
        this.optsCharset = optsCharset;
    }

    /**
     * @return the optsCreate
     */
    public byte getOptsCreate() {
        return optsCreate;
    }

    /**
     * @param optsCreate
     *            the optsCreate to set
     */
    public void setOptsCreate(byte optsCreate) {
        this.optsCreate = optsCreate;
    }

    /**
     * @return the optsLang
     */
    public byte getOptsLang() {
        return optsLang;
    }

    /**
     * @param optsLang
     *            the optsLang to set
     */
    public void setOptsLang(byte optsLang) {
        this.optsLang = optsLang;
    }

    /**
     * @return the optsMediaType
     */
    public byte getOptsMediaType() {
        return optsMediaType;
    }

    /**
     * @param optsMediaType
     *            the optsMediaType to set
     */
    public void setOptsMediaType(byte optsMediaType) {
        this.optsMediaType = optsMediaType;
    }

    /**
     * @return the optsModify
     */
    public byte getOptsModify() {
        return optsModify;
    }

    /**
     * @param optsModify
     *            the optsModify to set
     */
    public void setOptsModify(byte optsModify) {
        this.optsModify = optsModify;
    }

    /**
     * @return the optsPerm
     */
    public byte getOptsPerm() {
        return optsPerm;
    }

    /**
     * @param optsPerm
     *            the optsPerm to set
     */
    public void setOptsPerm(byte optsPerm) {
        this.optsPerm = optsPerm;
    }

    /**
     * @return the optsSize
     */
    public byte getOptsSize() {
        return optsSize;
    }

    /**
     * @param optsSize
     *            the optsSize to set
     */
    public void setOptsSize(byte optsSize) {
        this.optsSize = optsSize;
    }

    /**
     * @return the optsType
     */
    public byte getOptsType() {
        return optsType;
    }

    /**
     * @param optsType
     *            the optsType to set
     */
    public void setOptsType(byte optsType) {
        this.optsType = optsType;
    }

    /**
     * @return the optsUnique
     */
    public byte getOptsUnique() {
        return optsUnique;
    }

    /**
     * @param optsUnique
     *            the optsUnique to set
     */
    public void setOptsUnique(byte optsUnique) {
        this.optsUnique = optsUnique;
    }

    /**
     *
     * @return the String associated to the feature for MLSx
     */
    public String getFeat() {
        StringBuilder builder = new StringBuilder();
        builder.append(' ');
        if (optsSize >= 0) {
            builder.append("Size");
            if (optsSize > 0) {
                builder.append("*;");
            } else {
                builder.append(";");
            }
        }
        if (optsModify >= 0) {
            builder.append("Modify");
            if (optsModify > 0) {
                builder.append("*;");
            } else {
                builder.append(";");
            }
        }
        if (optsType >= 0) {
            builder.append("Type");
            if (optsType > 0) {
                builder.append("*;");
            } else {
                builder.append(";");
            }
        }
        if (optsPerm >= 0) {
            builder.append("Perm");
            if (optsPerm > 0) {
                builder.append("*;");
            } else {
                builder.append(";");
            }
        }
        if (optsCreate >= 0) {
            builder.append("Create");
            if (optsCreate > 0) {
                builder.append("*;");
            } else {
                builder.append(";");
            }
        }
        if (optsUnique >= 0) {
            builder.append("Unique");
            if (optsUnique > 0) {
                builder.append("*;");
            } else {
                builder.append(";");
            }
        }
        if (optsLang >= 0) {
            builder.append("Lang");
            if (optsLang > 0) {
                builder.append("*;");
            } else {
                builder.append(";");
            }
        }
        if (optsMediaType >= 0) {
            builder.append("Media-Type");
            if (optsMediaType > 0) {
                builder.append("*;");
            } else {
                builder.append(";");
            }
        }
        if (optsCharset >= 0) {
            builder.append("Charset");
            if (optsCharset > 0) {
                builder.append("*;");
            } else {
                builder.append(";");
            }
        }
        builder.append("UNIX.mode;");
        return builder.toString();
    }
}
