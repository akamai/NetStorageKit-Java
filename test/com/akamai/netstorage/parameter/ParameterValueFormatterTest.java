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

import org.junit.Test;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit test class for the Paramter Formatter
 * 
 * @author colinb@akamai.com (Colin Bendell)
 */
public class ParameterValueFormatterTest {
    @Test
    public void testBooleanValueFormatter() throws Exception {
        ParameterValueFormatter formatter = new BooleanValueFormatter();
        assertEquals(formatter.valueOf(true), "1");
        assertNull(formatter.valueOf(false));
    }

    @Test
    public void testByteArrayValueFormatter() throws Exception {
        ParameterValueFormatter formatter = new ByteArrayValueFormatter();
        byte[] data = new byte[]{0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf};
        assertEquals(formatter.valueOf(data), "000102030405060708090a0b0c0d0e0f");
    }

    @Test
    public void testDateValueFormatter() throws Exception {
        ParameterValueFormatter formatter = new DateValueFormatter();
        Date data = DateFormat.getDateTimeInstance(1, 1, Locale.UK).parse("11 November 2013 00:00:00 GMT");
        assertEquals(formatter.valueOf(data), "1384128000");
    }

    @Test
    public void testDefaultValueFormatter() throws Exception {
        ParameterValueFormatter formatter = new DefaultValueFormatter();
        assertEquals(formatter.valueOf("Lorem ipsum"), "Lorem ipsum");
    }
}
