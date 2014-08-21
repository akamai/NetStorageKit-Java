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

import java.util.Date;

/**
 * Date Value Formatter which represents dates in epoch seconds
 * 
 * @author colinb@akamai.com (Colin Bendell)
 */
public class DateValueFormatter implements ParameterValueFormatter {
    @Override
    public String valueOf(Object o) {
        if (o == null) return null;
        return String.valueOf(((Date) o).getTime() / 1000);
    }
}
