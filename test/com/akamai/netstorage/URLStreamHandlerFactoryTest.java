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

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for testing URLStreams used by the jUnitTest
 * 
 * @author colinb@akamai.com (Colin Bendell)
 */
public class URLStreamHandlerFactoryTest implements URLStreamHandlerFactory {

    private static final Map<String, HttpURLConnectionTest> urlConnection = new HashMap<>();
    private static boolean initComplete = false;

    public URLStreamHandlerFactoryTest() {
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equals("http") || protocol.equals("https"))
            return new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(URL u) throws IOException {
                    HttpURLConnectionTest connection = URLStreamHandlerFactoryTest.getURLConnection(u);

                    if (connection == null)
                        connection = URLStreamHandlerFactoryTest.addURLConnection(u);

                    return connection;
                }
            };
        return null;
    }

    public static HttpURLConnectionTest getURLConnection(URL url) {
        return urlConnection.get(url.toString());
    }

    public static HttpURLConnectionTest addURLConnection(URL url) {
        HttpURLConnectionTest connection = new HttpURLConnectionTest(url);
        urlConnection.put(url.toString(), connection);
        return connection;
    }

    public static void init() {
        if (!initComplete) {
            URL.setURLStreamHandlerFactory(new URLStreamHandlerFactoryTest());
            initComplete = true;
        }
    }
}

class HttpURLConnectionTest extends HttpURLConnection {

    private final Map<String, String> requestHeaders = new HashMap<>();
    private final Map<String, List<String>> responseHeaders = new HashMap<>();
    private final ByteArrayOutputStream requestStream = new ByteArrayOutputStream();
    private final ByteArrayInputStream responseStream = new ByteArrayInputStream(new byte[]{});
    private boolean wasConnected = false;


    @Override
    public void connect() throws IOException {
        this.connected = true;
        this.wasConnected = true;
    }

    public boolean getConnected() {
        return this.connected;
    }

    public boolean getWasConnected() {
        return this.wasConnected;
    }

    /**
     * Constructor for the HttpURLConnection.
     *
     * @param u the URL
     */
    HttpURLConnectionTest(URL u) {
        super(u);
    }

    @Override
    public void disconnect() {
        this.connected = false;
    }

    @Override
    public boolean usingProxy() {
        return false;
    }

    @Override
    public void setRequestProperty(String key, String value) {
        requestHeaders.put(key, value);
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return requestStream;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public String getHeaderField(String name) {
        String value = null;
        if (responseHeaders.containsKey(name) && responseHeaders.get(name) != null) {
            List<String> valueList = responseHeaders.get(name);
            if (valueList.size() > 0)
                value = valueList.get(valueList.size() - 1);
        }
        return value;
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return responseHeaders;
    }

    public int getChunkedLength() {
        return this.chunkLength;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.responseStream;
    }

    @Override
    public long getContentLengthLong() {
        return Math.max(this.fixedContentLengthLong, this.fixedContentLength);
    }
}


