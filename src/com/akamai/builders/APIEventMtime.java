package com.akamai.builders;

import com.akamai.netstorage.APIEventBean;
import com.akamai.netstorage.parameter.DateValueFormatter;
import com.akamai.netstorage.parameter.Parameter;

import java.util.Date;

public class APIEventMtime extends APIEventBean {
    @Parameter(name = "mtime", formatter = DateValueFormatter.class)
    private Date mtime;

    public APIEventMtime() {
        super("mtime");
    }

    public APIEventMtime withMtime(Date mtime) {
        this.mtime = mtime;
        return this;
    }

    public Date getMtime() {
        return mtime;
    }
}
