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

import java.lang.annotation.*;

/**
 * Paramter Annotation used to self declare the appropriate formatter necessary
 * for API serialization
 * 
 * @author colinb@akamai.com (Colin Bendell)
 */
@Documented
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {
    String name();
    Class<? extends ParameterValueFormatter> formatter() default DefaultValueFormatter.class;
    boolean includeNull() default false;
}