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
package com.akamai.netstorage.parameter;

/**
 * Boolean Value Formatter which represents boolean values as either 1 or null. 
 * Null is used instead of Zero (0) in this case as an optimization relevant to 
 * the NetstorageSDK where parameters should be excluded if they are set to True
 * 
 * @author colinb@akamai.com (Colin Bendell)
 */
public class BooleanValueFormatter implements ParameterValueFormatter {
    @Override
    public String valueOf(Object o) {
        if (o == null) return null;
        return ((Boolean) o) ? "1" : null;
    }
}
