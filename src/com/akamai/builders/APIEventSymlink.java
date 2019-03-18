package com.akamai.builders;

import com.akamai.netstorage.APIEventBean;

public class APIEventSymlink extends APIEventBean {
    private String target;

    public APIEventSymlink() {
        super("symlink");
    }

    public APIEventSymlink to(String target) {
        this.target = target;
        return this;
    }

    public String getTarget() {
        return target;
    }
}
