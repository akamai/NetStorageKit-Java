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

import com.akamai.netstorage.parameter.DateValueFormatter;
import com.akamai.netstorage.parameter.Parameter;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit test class for Utils class
 * 
 * @author colinb@akamai.com (Colin Bendell)
 */
public class UtilsTest extends Utils {

    private class POJO {
        private String name;
        @Parameter(name = "newValue", includeNull = true)
        private String value;
        @Parameter(name = "newDate", formatter = DateValueFormatter.class)
        private Date date;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        POJO(String name, String value, Date date) {
            this.name = name;
            this.value = value;
            this.date = date;
        }
    }

    @Test
    public void testComputeHash() throws Exception {
        byte[] data = "Lorem ipsum dolor sit amet, an sea putant quaeque, homero aperiam te eos.".getBytes(StandardCharsets.UTF_8);

        assertEquals(encodeHex(Utils.computeHash(new ByteArrayInputStream(data), HashAlgorithm.MD5)), "7d5efa77cfaaff5f18001612b426fe36");
        assertEquals(encodeHex(Utils.computeHash(new ByteArrayInputStream(data), HashAlgorithm.SHA1)), "5efe96a4d243965e4edd3142d5ee061ab2f57055");
        assertEquals(encodeHex(Utils.computeHash(new ByteArrayInputStream(data), HashAlgorithm.SHA256)), "4e8aecd6dc4c97ae55c30ef9b1e91b4829ef5871b16262b4628838a80dc0c2e2");
        assertNull(Utils.computeHash(null, HashAlgorithm.MD5));
    }

    @Test
    public void testComputeKeyedHash() throws Exception {
        byte[] data = "Lorem ipsum dolor sit amet, an sea putant quaeque, homero aperiam te eos.".getBytes(StandardCharsets.UTF_8);
        String key = "secretkey";

        assertNull(Utils.computeKeyedHash(null, key, KeyedHashAlgorithm.HMACSHA256));
        assertNull(Utils.computeKeyedHash(data, null, KeyedHashAlgorithm.HMACSHA256));
        assertEquals(encodeBase64(Utils.computeKeyedHash(data, key, KeyedHashAlgorithm.HMACMD5)), "kTFtTrQ9vdBM5/97A5kPIQ==");
        assertEquals(encodeBase64(Utils.computeKeyedHash(data, key, KeyedHashAlgorithm.HMACSHA1)), "eXk3UE/WTxvyh9RW8fLNJtwF9j4=");
        assertEquals(encodeBase64(Utils.computeKeyedHash(data, key, KeyedHashAlgorithm.HMACSHA256)), "+jYoZtNP2pVjCx/cMWWM+NCe1kpTW7y1mnM7zi5tr6c=");
    }

    @Test
    public void testEncodeHex() throws Exception {
        byte[] data = "Lorem ipsum".getBytes(StandardCharsets.UTF_8);
        assertNull(Utils.encodeHex(null));

        assertEquals(Utils.encodeHex(data), "4c6f72656d20697073756d");
    }

    @Test
    public void testEncodeBase64() throws Exception {
        byte[] data = "Lorem ipsum dolor sit amet, an sea putant quaeque, homero aperiam te eos.".getBytes(StandardCharsets.UTF_8);

        assertNull(Utils.encodeBase64(null));
        assertEquals(Utils.encodeBase64(data), "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGFuIHNlYSBwdXRhbnQgcXVhZXF1ZSwgaG9tZXJvIGFwZXJpYW0gdGUgZW9zLg==");
    }

    @Test
    public void testConvertObjectAsMap() throws Exception {
        Map<String, String> data;

        data = Utils.convertObjectAsMap(new POJO("value1", "value2", null));
        assertEquals(data.size(), 2);
        assertTrue(data.containsKey("name"));
        assertEquals(data.get("name"), "value1");
        assertTrue(data.containsKey("newValue"));
        assertEquals(data.get("newValue"), "value2");

        data = Utils.convertObjectAsMap(new POJO("value1", null, null));
        assertEquals(data.size(), 2);
        assertTrue(data.containsKey("name"));
        assertEquals(data.get("name"), "value1");
        assertTrue(data.containsKey("newValue"));
        assertNull(data.get("newValue"));

    	SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z", Locale.UK);
        data = Utils.convertObjectAsMap(new POJO("value1", null, sdf.parse("11 November 2013 00:00:00 GMT")));
        assertEquals(data.size(), 3);
        assertTrue(data.containsKey("name"));
        assertEquals(data.get("name"), "value1");
        assertTrue(data.containsKey("newValue"));
        assertNull(data.get("newValue"));
        assertTrue(data.containsKey("newDate"));
        assertEquals(data.get("newDate"), "1384128000");
    }

    @Test
    public void testConvertMapAsQueryParams() throws Exception {
        Map<String, String> data = new HashMap<>();
        data.put("name", "value");
        data.put("name2", "value 2");

        String result = Utils.convertMapAsQueryParams(data);

        assertEquals(result, "name=value&name2=value+2");
    }

    @Test
    public void testReadToEnd() throws Exception {
        Utils.readToEnd(null); // no NPE

        byte[] data = "Lorem ipsum dolor sit amet, an sea putant quaeque, homero aperiam te eos.".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        assertEquals(stream.available(), 73);

        Utils.readToEnd(stream);
        assertEquals(stream.available(), 0);

    }
}
