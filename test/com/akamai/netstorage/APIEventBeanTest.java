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

import com.akamai.builders.*;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Unit test class for the APIEventBean
 *
 * @author colinb@akamai.com (Colin Bendell)
 */
public class APIEventBeanTest {
    @Test
    public void testUploadZipAsQueryParams() throws Exception {
        Date date = DateFormat.getDateTimeInstance(1, 1, Locale.UK).parse("11 November 2013 00:00:00 GMT");
        APIEventBean action = new APIEventUpload().withMtime(date).ofSize(123L).isIndexZip(true);

        Map<String, String> result = action.asQueryParams();

        assertThat(result.size(), is(4));
        assertThat(Utils.convertMapAsQueryParams(result), is("action=upload&index-zip=1&mtime=1384128000&version=1"));
    }

    @Test
    public void testUploadNoZipAsQueryParams() throws Exception {
        Date date = DateFormat.getDateTimeInstance(1, 1, Locale.UK).parse("11 November 2013 00:00:00 GMT");
        APIEventBean action = new APIEventUpload().withMtime(date).ofSize(123L).isIndexZip(false);

        Map<String, String> result = action.asQueryParams();

        assertThat(result.size(), is(4));
        assertThat(Utils.convertMapAsQueryParams(result), is("action=upload&mtime=1384128000&size=123&version=1"));
    }

    @Test
    public void testQuickDeleteAsQueryParams() throws Exception {
        APIEventBean action = new APIEventQuickDelete();

        Map<String, String> result = action.asQueryParams();

        assertThat(result.size(), is(3));
        assertThat(Utils.convertMapAsQueryParams(result), is("action=quick-delete&quick-delete=imreallyreallysure&version=1"));
    }

    @Test
    public void testSymlinkAsQueryParams() throws Exception {
        APIEventBean action = new APIEventSymlink().to("/bar");

        Map<String, String> result = action.asQueryParams();

        assertThat(result.size(), is(3));
        assertThat(Utils.convertMapAsQueryParams(result), is("action=symlink&target=%2Fbar&version=1"));
    }

    @Test
    public void testStatAsQueryParams() throws Exception {
        APIEventBean action = new APIEventStat().withFormat("xml");

        Map<String, String> result = action.asQueryParams();

        assertThat(result.size(), is(3));
        assertThat(Utils.convertMapAsQueryParams(result), is("action=stat&format=xml&version=1"));
    }

    @Test
    public void testRenameAsQueryParams() throws Exception {
        APIEventBean action = new APIEventRename().to("/foo");

        Map<String, String> result = action.asQueryParams();

        assertThat(result.size(), is(3));
        assertThat(Utils.convertMapAsQueryParams(result), is("action=rename&destination=%2Ffoo&version=1"));
    }

    @Test
    public void testUploadAsQuaryParamsAlgos() {
        byte[] loremIpsumBytes = "Lorem ipsum".getBytes(StandardCharsets.UTF_8);
        APIEventUpload action = new APIEventUpload()
                .withMd5(loremIpsumBytes)
                .withSha1(loremIpsumBytes)
                .withSha256(loremIpsumBytes);
        Map<String, String> result = action.asQueryParams();

        assertThat(result.size(), is(5));
        assertThat(Utils.convertMapAsQueryParams(result), is("action=upload&md5=4c6f72656d20697073756d&sha1=4c6f72656d20697073756d&sha256=4c6f72656d20697073756d&version=1"));
    }

    @Test
    public void testAdditionalParams() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("hdr_X-rob", "hello2");
        APIEventBean action = new APIEventSetmd().withAdditionalParams(headers);

        Map<String, String> result = action.asQueryParams();

        assertThat(result.size(), is(3));
        assertThat(Utils.convertMapAsQueryParams(result), is("action=setmd&hdr_X-rob=hello2&version=1"));
    }
}
