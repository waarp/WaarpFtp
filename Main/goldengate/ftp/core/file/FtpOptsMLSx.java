/**
 * 
 */
package goldengate.ftp.core.file;

/**
 * Class that implements Opts command for MLSx operations. (-1) means not
 * supported, 0 supported but not active, 1 supported and active
 * 
 * @author fbregier
 * 
 */
public class FtpOptsMLSx {
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
    public FtpOptsMLSx() {
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
    public FtpOptsMLSx(byte optsSize, byte optsModify, byte optsType,
            byte optsPerm, byte optsCreate, byte optsUnique, byte optsLang,
            byte optsMediaType, byte optsCharset) {
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
        return this.optsCharset;
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
        return this.optsCreate;
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
        return this.optsLang;
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
        return this.optsMediaType;
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
        return this.optsModify;
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
        return this.optsPerm;
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
        return this.optsSize;
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
        return this.optsType;
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
        return this.optsUnique;
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
        if (this.optsSize >= 0) {
            builder.append("Size");
            if (this.optsSize > 0) {
                builder.append("*;");
            } else {
                builder.append(";");
            }
        }
        if (this.optsModify >= 0) {
            builder.append("Modify");
            if (this.optsModify > 0) {
                builder.append("*;");
            } else {
                builder.append(";");
            }
        }
        if (this.optsType >= 0) {
            builder.append("Type");
            if (this.optsType > 0) {
                builder.append("*;");
            } else {
                builder.append(";");
            }
        }
        if (this.optsPerm >= 0) {
            builder.append("Perm");
            if (this.optsPerm > 0) {
                builder.append("*;");
            } else {
                builder.append(";");
            }
        }
        if (this.optsCreate >= 0) {
            builder.append("Create");
            if (this.optsCreate > 0) {
                builder.append("*;");
            } else {
                builder.append(";");
            }
        }
        if (this.optsUnique >= 0) {
            builder.append("Unique");
            if (this.optsUnique > 0) {
                builder.append("*;");
            } else {
                builder.append(";");
            }
        }
        if (this.optsLang >= 0) {
            builder.append("Lang");
            if (this.optsLang > 0) {
                builder.append("*;");
            } else {
                builder.append(";");
            }
        }
        if (this.optsMediaType >= 0) {
            builder.append("Media-Type");
            if (this.optsMediaType > 0) {
                builder.append("*;");
            } else {
                builder.append(";");
            }
        }
        if (this.optsCharset >= 0) {
            builder.append("Charset");
            if (this.optsCharset > 0) {
                builder.append("*;");
            } else {
                builder.append(";");
            }
        }
        builder.append("UNIX.mode;");
        return builder.toString();
    }
}
