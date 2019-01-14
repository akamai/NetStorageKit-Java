package com.akamai.builders;

import com.akamai.netstorage.APIEventBean;

public class APIEventRename extends APIEventBean {
    private String destination;

    public APIEventRename() {
        super("rename");
    }

    public APIEventBean to(String destination) {
        this.destination = destination;
        return this;
    }

    public String getDestination() {
        return destination;
    }
}
