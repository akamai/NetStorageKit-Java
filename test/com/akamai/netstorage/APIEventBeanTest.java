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

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

/**
 * Unit test class for the APIEventBean
 *
 * @author colinb@akamai.com (Colin Bendell)
 */
public class APIEventBeanTest {
    @Test
    public void testAsQueryParams() throws Exception {
        Date date = DateFormat.getDateTimeInstance(1, 1, Locale.UK).parse("11 November 2013 00:00:00 GMT");
        APIEventBean action = new APIEventBean();
        action.setAction("download");
        action.setFormat("xml");
        action.setQuickDelete();
        action.setDestination("/foo");
        action.setTarget("/bar");
        action.setMtime(date);
        action.setSize(123L);
        action.setIndexZip(true);

        Map<String, String> result = action.asQueryParams();

        assertEquals(result.size(), 9);
        assertEquals(Utils.convertMapAsQueryParams(result), "action=download&destination=%2Ffoo&format=xml&index-zip=1&mtime=1384128000&quick-delete=imreallyreallysure&size=123&target=%2Fbar&version=1");

        action = new APIEventBean();
        action.setMd5("Lorem ipsum".getBytes(StandardCharsets.UTF_8));
        action.setSha1("Lorem ipsum".getBytes(StandardCharsets.UTF_8));
        action.setSha256("Lorem ipsum".getBytes(StandardCharsets.UTF_8));
        result = action.asQueryParams();

        assertEquals(result.size(), 4);
        assertEquals(Utils.convertMapAsQueryParams(result), "md5=4c6f72656d20697073756d&sha1=4c6f72656d20697073756d&sha256=4c6f72656d20697073756d&version=1");
    }

    @Test
    public void testAdditionalParams() throws Exception {
    	APIEventBean action = new APIEventBean();
    	action.setAction("setmd");

    	Map<String, String> headers = new HashMap<>();
		headers.put("hdr_X-rob", "hello2");
    	action.setAdditionalParams(headers);

    	Map<String, String> result = action.asQueryParams();

    	assertEquals(3, result.size());
    	assertEquals("action=setmd&hdr_X-rob=hello2&version=1", Utils.convertMapAsQueryParams(result));

    }
}
