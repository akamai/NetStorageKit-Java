package com.akamai.builders;

import com.akamai.netstorage.APIEventBean;
import com.akamai.netstorage.parameter.BooleanValueFormatter;
import com.akamai.netstorage.parameter.ByteArrayValueFormatter;
import com.akamai.netstorage.parameter.DateValueFormatter;
import com.akamai.netstorage.parameter.Parameter;

import java.util.Date;

public class APIEventUpload extends APIEventBean {
    @Parameter(name = "mtime", formatter = DateValueFormatter.class)
    private Date mtime;
    private Long size;
    @Parameter(name = "md5", formatter = ByteArrayValueFormatter.class)
    private byte[] md5;
    @Parameter(name = "sha1", formatter = ByteArrayValueFormatter.class)
    private byte[] sha1;
    @Parameter(name = "sha256", formatter = ByteArrayValueFormatter.class)
    private byte[] sha256;
    @Parameter(name = "index-zip", formatter = BooleanValueFormatter.class)
    private Boolean indexZip;

    public APIEventUpload() {
        super("upload");
    }

    public APIEventUpload withMtime(Date mtime) {
        this.mtime = mtime;
        return this;
    }

    public Date getMtime() {
        return mtime;
    }

    public APIEventUpload ofSize(Long size) {
        this.size = size;
        resetSizeIfNeeded();
        return this;
    }

    public Long getSize() {
        return size;
    }

    public APIEventUpload withMd5(byte[] md5Checksum) {
        this.md5 = md5Checksum;
        return this;
    }

    public byte[] getMd5() {
        return md5;
    }

    public APIEventUpload withSha1(byte[] sha1Checksum) {
        this.sha1 = sha1Checksum;
        return this;
    }

    public byte[] getSha1() {
        return sha1;
    }

    public APIEventUpload withSha256(byte[] sha256Checksum) {
        this.sha256 = sha256Checksum;
        return this;
    }

    public byte[] getSha256() {
        return sha256;
    }

    public APIEventUpload isIndexZip(boolean indexZip) {
        this.indexZip = indexZip ? indexZip : null;
        resetSizeIfNeeded();
        return this;
    }

    private void resetSizeIfNeeded() {
        // size is not supported with zip since the index-zip functionality mutates the file thus inconsistency on which size value to use
        // probably should throw an exception or a warning
        if (getSize() != null && getIndexZip() != null && getIndexZip())
            this.size = null;
    }

    public Boolean getIndexZip() {
        return indexZip;
    }
}
