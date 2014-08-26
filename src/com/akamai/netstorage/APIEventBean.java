/*
 * Copyright 2014 Akamai Technologies http://developer.akamai.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.akamai.netstorage;

import java.util.Date;
import java.util.Map;

import com.akamai.netstorage.parameter.BooleanValueFormatter;
import com.akamai.netstorage.parameter.ByteArrayValueFormatter;
import com.akamai.netstorage.parameter.DateValueFormatter;
import com.akamai.netstorage.parameter.Parameter;

/**
 * The APIEvent bean holds the necessary paramters for execution of the various invocation actions
 *
 * @author colinb@akamai.com (Colin Bendell)
 */
public class APIEventBean {
    public final static int VERSION = 1;

    private int version = APIEventBean.VERSION;
    private String action;
    private Map<String, String> additionalParams;
    private String format;
    @Parameter(name="quick-delete") private String quickDelete;
    private String destination;
    private String target;
    @Parameter(name="mtime", formatter=DateValueFormatter.class) private Date mtime;
    private Long size;
    @Parameter(name="md5", formatter=ByteArrayValueFormatter.class) private byte[] md5;
    @Parameter(name="sha1", formatter=ByteArrayValueFormatter.class) private byte[] sha1;
    @Parameter(name="sha256", formatter=ByteArrayValueFormatter.class) private byte[] sha256;
    @Parameter(name="index-zip", formatter=BooleanValueFormatter.class) private Boolean indexZip;

    public int getVersion() {
        return version;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Map<String, String> getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(Map<String, String> additionalParams) {
        this.additionalParams = additionalParams;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getQuickDelete() {
        return quickDelete;
    }

    public void setQuickDelete(String quickDelete) {
        this.quickDelete = quickDelete;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Date getMtime() {
        return mtime;
    }

    public void setMtime(Date mTime) {
        this.mtime = mTime;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public byte[] getMd5() {
        return md5;
    }

    public void setMd5(byte[] md5) {
        this.md5 = md5;
    }

    public byte[] getSha1() {
        return sha1;
    }

    public void setSha1(byte[] sha1) {
        this.sha1 = sha1;
    }

    public byte[] getSha256() {
        return sha256;
    }

    public void setSha256(byte[] sha256) {
        this.sha256 = sha256;
    }

    public Boolean getIndexZip() {
        return indexZip;
    }

    public void setIndexZip(Boolean indexZip) {
        this.indexZip = indexZip;
    }

    public Map<String, String> asQueryParams() {
        Map<String, String> result = Utils.convertObjectAsMap(this);
        if (additionalParams != null && additionalParams.size() > 0)
        	result.putAll(additionalParams);

        return result;
    }
}
