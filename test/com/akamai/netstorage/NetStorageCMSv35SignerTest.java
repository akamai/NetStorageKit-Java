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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit test class for the NetStorageCMSv35Signer class
 * 
 * @author colinb@akamai.com (Colin Bendell)
 */
public class NetStorageCMSv35SignerTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private static NetStorageCMSv35Signer createAPIConnection() throws MalformedURLException {
        URLStreamHandlerFactoryTest.init();

        APIEventBean event = new APIEventBean();
        event.setAction("download");

        return new NetStorageCMSv35Signer("GET", new URL("http://www.example.com/foobar"), event);
    }

    @Test
    public void testGetActionHeaderValue() throws Exception {
        NetStorageCMSv35Signer apiEvent = createAPIConnection();

        String actionHeader = apiEvent.getActionHeaderValue();
        assertEquals(actionHeader, "action=download&version=1");
    }

    @Test
    public void testGetAuthDataHeaderValueV3() throws Exception {
        NetStorageCMSv35Signer apiEvent = createAPIConnection();
        apiEvent.setSignVersion(NetStorageCMSv35Signer.SignType.HMACMD5);

        String authDataHeader = apiEvent.getAuthDataHeaderValue(new DefaultCredential("www.example.com","user1", "secret1"));
        assertTrue(authDataHeader.matches("3, 0.0.0.0, 0.0.0.0, \\d{10}, \\d+, user1"));
    }

    @Test
    public void testGetAuthDataHeaderValueV4() throws Exception {
        NetStorageCMSv35Signer apiEvent = createAPIConnection();
        apiEvent.setSignVersion(NetStorageCMSv35Signer.SignType.HMACSHA1);

        String authDataHeader = apiEvent.getAuthDataHeaderValue(new DefaultCredential("www.example.com","user1", "secret1"));
        assertTrue(authDataHeader.matches("4, 0.0.0.0, 0.0.0.0, \\d{10}, \\d+, user1"));
    }

    @Test
    public void testGetAuthDataHeaderValueV5() throws Exception {
        NetStorageCMSv35Signer apiEvent = createAPIConnection();

        String authDataHeader = apiEvent.getAuthDataHeaderValue(new DefaultCredential("www.example.com","user1", "secret1"));
        assertTrue(authDataHeader.matches("5, 0.0.0.0, 0.0.0.0, \\d{10}, \\d+, user1"));
    }

    @Test
    public void testGetAuthSignHeaderValueV3() throws Exception {
        NetStorageCMSv35Signer apiEvent = createAPIConnection();
        apiEvent.setSignVersion(NetStorageCMSv35Signer.SignType.HMACMD5);
        assertEquals(
                apiEvent.getAuthSignHeaderValue("version=1&action=download", "5, 0.0.0.0, 0.0.0.0, 1384128000, 1234, user1", new DefaultCredential("www.example.com","user1", "secret1")),
                "2o9oYek9FVnDcTUykNoncg==");
    }

    @Test
    public void testGetAuthSignHeaderValueV4() throws Exception {
        NetStorageCMSv35Signer apiEvent = createAPIConnection();
        apiEvent.setSignVersion(NetStorageCMSv35Signer.SignType.HMACSHA1);
        assertEquals(
                apiEvent.getAuthSignHeaderValue("version=1&action=download", "5, 0.0.0.0, 0.0.0.0, 1384128000, 1234, user1", new DefaultCredential("www.example.com","user1", "secret1")),
                "0Eg7KoGhk4zayO1OQpvqO/xW1IU=");
    }

    @Test
    public void testGetAuthSignHeaderValueV5() throws Exception {
        NetStorageCMSv35Signer apiEvent = createAPIConnection();
        assertEquals(
                apiEvent.getAuthSignHeaderValue("version=1&action=download", "5, 0.0.0.0, 0.0.0.0, 1384128000, 1234, user1", new DefaultCredential("www.example.com","user1", "secret1")),
                "jKA6Rh9lCotwbE6BRPZve1fOl67yqKnZ+Z0b048jwYo=");
    }

    @Test
    public void testComputeHeaders() throws Exception {
        NetStorageCMSv35Signer apiEvent = createAPIConnection();
        Map<String, String> headers = apiEvent.computeHeaders(new DefaultCredential("www.example.com","user1", "secret1"));
        assertEquals(headers.size(), 4);
        assertNotNull(headers.get("X-Akamai-ACS-Action"));
        assertNotNull(headers.get("X-Akamai-ACS-Auth-Data"));
        assertNotNull(headers.get("X-Akamai-ACS-Auth-Sign"));
        assertNotNull(headers.get("X-Akamai-NSKit"));
    }

    @Test
    public void testValidate() throws Exception {
        NetStorageCMSv35Signer netStorageCMSv35Signer = createAPIConnection();
        HttpURLConnectionTest httpURLConnection = new HttpURLConnectionTest(netStorageCMSv35Signer.getUrl());
        httpURLConnection.setResponseCode(HttpURLConnection.HTTP_OK);
        assertTrue(netStorageCMSv35Signer.validate(httpURLConnection));
    }

    @Test
    public void testValidateNoDate() throws Exception {
        exception.expect(NetStorageException.class);
        exception.expectMessage("Unexpected Response from Server:");
        NetStorageCMSv35Signer netStorageCMSv35Signer = createAPIConnection();
        HttpURLConnectionTest httpURLConnection = new HttpURLConnectionTest(netStorageCMSv35Signer.getUrl());
        httpURLConnection.setResponseCode(HttpURLConnection.HTTP_UNAVAILABLE);
        assertTrue(netStorageCMSv35Signer.validate(httpURLConnection));
    }

    @Test
    public void testValidateServerDateSync() throws Exception {
        exception.expect(NetStorageException.class);
        exception.expectMessage("Unexpected Response from Server:");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        NetStorageCMSv35Signer netStorageCMSv35Signer = createAPIConnection();
        HttpURLConnectionTest httpURLConnection = new HttpURLConnectionTest(netStorageCMSv35Signer.getUrl());
        httpURLConnection.setResponseCode(HttpURLConnection.HTTP_UNAVAILABLE);
        httpURLConnection.getHeaderFields().put("Date", Collections.singletonList(dateFormat.format(calendar.getTime())));
        assertTrue(netStorageCMSv35Signer.validate(httpURLConnection));
    }

    @Test
    public void testValidateServerDateOutOfSync() throws Exception {
        exception.expect(NetStorageException.class);
        exception.expectMessage("Local server Date is more than 30s out of sync with Remote server");
        NetStorageCMSv35Signer netStorageCMSv35Signer = createAPIConnection();
        HttpURLConnectionTest httpURLConnection = new HttpURLConnectionTest(netStorageCMSv35Signer.getUrl());
        httpURLConnection.setResponseCode(HttpURLConnection.HTTP_UNAVAILABLE);
        httpURLConnection.getHeaderFields().put("Date", Collections.singletonList("Mon, 11 Nov 1918 11:00:00 GMT"));
        assertTrue(netStorageCMSv35Signer.validate(httpURLConnection));
    }

    @Test
    public void testExecuteOK() throws Exception {
        NetStorageCMSv35Signer netStorageCMSv35Signer = createAPIConnection();
        HttpURLConnectionTest httpURLConnection = URLStreamHandlerFactoryTest.addURLConnection(netStorageCMSv35Signer.getUrl());
        httpURLConnection.setResponseCode(HttpURLConnection.HTTP_OK);

        netStorageCMSv35Signer.execute(new DefaultCredential("www.example.com","user1", "secret1"));
        assertEquals(httpURLConnection.getRequestMethod(), "GET");
        assertEquals(httpURLConnection.getRequestHeaders().size(), 4);
        assertTrue(httpURLConnection.getWasConnected());
        assertTrue(httpURLConnection.getConnected());
    }

    @Test
    public void testExecutePOST() throws Exception {
        NetStorageCMSv35Signer netStorageCMSv35Signer = createAPIConnection();
        HttpURLConnectionTest httpURLConnection = URLStreamHandlerFactoryTest.addURLConnection(netStorageCMSv35Signer.getUrl());
        httpURLConnection.setResponseCode(HttpURLConnection.HTTP_OK);
        netStorageCMSv35Signer.setMethod("POST");

        netStorageCMSv35Signer.execute(new DefaultCredential("www.example.com","user1", "secret1"));
        assertEquals(httpURLConnection.getRequestMethod(), "POST");
        assertEquals(httpURLConnection.getContentLengthLong(), 0);
        assertTrue(httpURLConnection.getWasConnected());
        assertTrue(httpURLConnection.getConnected());
    }

    @Test
    public void testExecutePUT() throws Exception {
        byte[] data = "Lorem ipsum dolor sit amet, an sea putant quaeque, homero aperiam te eos.".getBytes(StandardCharsets.UTF_8);
        NetStorageCMSv35Signer netStorageCMSv35Signer = createAPIConnection();
        HttpURLConnectionTest httpURLConnection = URLStreamHandlerFactoryTest.addURLConnection(netStorageCMSv35Signer.getUrl());
        httpURLConnection.setResponseCode(HttpURLConnection.HTTP_OK);
        netStorageCMSv35Signer.setMethod("PUT");
        netStorageCMSv35Signer.setUploadSize(data.length);
        netStorageCMSv35Signer.setUploadStream(new ByteArrayInputStream(data));
        netStorageCMSv35Signer.execute(new DefaultCredential("www.example.com","user1", "secret1"));
        assertEquals(httpURLConnection.getRequestMethod(), "PUT");
        assertEquals(httpURLConnection.getContentLengthLong(), 73);
        assertTrue(httpURLConnection.getWasConnected());
        assertTrue(httpURLConnection.getConnected());
        assertTrue(Arrays.equals(data, ((ByteArrayOutputStream) httpURLConnection.getOutputStream()).toByteArray()));
    }

    @Test
    public void testExecutePUTWithNoContentLength() throws Exception {
        byte[] data = "Lorem ipsum dolor sit amet, an sea putant quaeque, homero aperiam te eos.".getBytes(StandardCharsets.UTF_8);
        NetStorageCMSv35Signer netStorageCMSv35Signer = createAPIConnection();
        HttpURLConnectionTest httpURLConnection = URLStreamHandlerFactoryTest.addURLConnection(netStorageCMSv35Signer.getUrl());
        httpURLConnection.setResponseCode(HttpURLConnection.HTTP_OK);
        netStorageCMSv35Signer.setMethod("PUT");
        netStorageCMSv35Signer.setUploadStream(new ByteArrayInputStream(data));

        netStorageCMSv35Signer.execute(new DefaultCredential("www.example.com","user1", "secret1"));
        assertEquals(httpURLConnection.getRequestMethod(), "PUT");
        assertEquals(httpURLConnection.getContentLengthLong(), -1);
        assertEquals(httpURLConnection.getChunkedLength(), 1024 * 1024);
        assertTrue(httpURLConnection.getWasConnected());
        assertTrue(httpURLConnection.getConnected());
        assertTrue(Arrays.equals(data, ((ByteArrayOutputStream) httpURLConnection.getOutputStream()).toByteArray()));
    }

    @Test
    public void testExecutePUTWithIOException() throws Exception {
        byte[] data = "Lorem ipsum dolor sit amet, an sea putant quaeque, homero aperiam te eos.".getBytes(StandardCharsets.UTF_8);
        NetStorageCMSv35Signer netStorageCMSv35Signer = createAPIConnection();
        HttpURLConnectionTest httpURLConnection = URLStreamHandlerFactoryTest.addURLConnection(netStorageCMSv35Signer.getUrl());
        httpURLConnection.setResponseCode(HttpURLConnection.HTTP_OK);
        netStorageCMSv35Signer.setMethod("PUT");
        netStorageCMSv35Signer.setUploadStream(new ByteArrayInputStreamBroken(data));

        NetStorageException nse = null;
        try {
            netStorageCMSv35Signer.execute(new DefaultCredential("www.example.com","user1", "secret1"));
        } catch (NetStorageException e) {
            nse = e;
        }
        assertNotNull(nse);
        assertEquals(nse.getMessage(), "Communication Error");
        assertEquals(nse.getCause().getClass(), IOException.class);
        assertTrue(httpURLConnection.getWasConnected());
        assertFalse(httpURLConnection.getConnected());
    }

    private class ByteArrayInputStreamBroken extends ByteArrayInputStream {

        ByteArrayInputStreamBroken(byte[] buf) {
            super(buf);
        }

        @Override
        public void close() throws IOException {
            throw new IOException("No Data!");
        }
    }
}
