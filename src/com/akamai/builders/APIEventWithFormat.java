package com.akamai.builders;

import com.akamai.netstorage.APIEventBean;

public abstract class APIEventWithFormat extends APIEventBean {
    private String format;

    protected APIEventWithFormat(String action) {
        super(action);
    }

    public APIEventBean withFormat(String format) {
        this.format = format;
        return this;
    }

    public String getFormat() {
        return format;
    }
}
