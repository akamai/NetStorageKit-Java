package com.akamai.builders;

import com.akamai.netstorage.APIEventBean;
import com.akamai.netstorage.parameter.Parameter;

public class APIEventQuickDelete extends APIEventBean {
    @Parameter(name = "quick-delete")
    private final String quickDelete;

    public APIEventQuickDelete() {
        super("quick-delete");
        this.quickDelete = "imreallyreallysure";
    }

    public String getQuickDelete() {
        return quickDelete;
    }
}
