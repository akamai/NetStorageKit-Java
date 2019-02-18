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

import java.util.Map;

/**
 * The APIEvent bean holds the necessary parameters for execution of the various invocation actions
 *
 * @author colinb@akamai.com (Colin Bendell)
 */
public class APIEventBean {
    public final static int VERSION = 1;

    private int version = APIEventBean.VERSION;
    private String action;
    private Map<String, String> additionalParams;

    public APIEventBean(String action) {
        this.action = action;
    }

    public int getVersion() {
        return version;
    }

    public String getAction() {
        return action;
    }

    public Map<String, String> getAdditionalParams() {
        return additionalParams;
    }

    public APIEventBean withAdditionalParams(Map<String, String> additionalParams) {
        this.additionalParams = additionalParams;
        return this;
    }

    public Map<String, String> asQueryParams() {
        Map<String, String> result = Utils.convertObjectAsMap(this);
        if (additionalParams != null && additionalParams.size() > 0)
        	result.putAll(additionalParams);

        return result;
    }
}
