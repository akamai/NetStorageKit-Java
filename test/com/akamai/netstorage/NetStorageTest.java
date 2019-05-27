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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

/**
 * Unit test class for the Netstorage wrapper
 *
 * @author colinb@akamai.com (Colin Bendell)
 */
public class NetStorageTest {

    private NetStorage createNetstorage(String path) {

        URLStreamHandlerFactoryTest.init();

        NetStorage ns = new NetStorage(new DefaultCredential("www.example.com", "user1", "secret1"));

        HttpURLConnectionTest connection = URLStreamHandlerFactoryTest.addURLConnection(ns.getNetstorageUri(path));
        connection.setResponseCode(HttpURLConnection.HTTP_OK);
        return ns;
    }

    @Test()
    public void testGetNetstorageUriHttps() throws Exception {
        // https
        NetStorage ns = new NetStorage(new DefaultCredential("www.example.com", "user1", "secret1"));
        assertEquals(ns.getNetstorageUri("/foobar").toString(), "https://www.example.com/foobar");

    }

    @Test()
    public void testGetNetstorageUriNoPrefix() throws Exception {
        // no / prefix
        NetStorage ns = new NetStorage(new DefaultCredential("www.example.com", "user1", "secret1"));
        assertEquals(ns.getNetstorageUri("foobar").toString(), "https://www.example.com/foobar");
    }

    @Test
    public void testDelete() throws Exception {
        String path = "/foobar";

        NetStorage ns = createNetstorage(path);
        HttpURLConnectionTest connection = URLStreamHandlerFactoryTest.getURLConnection(ns.getNetstorageUri(path));
        Map<String, String> headers = connection.getRequestHeaders();

        ns.delete(path);
        assertEquals(headers.size(), 4);
        assertEquals(headers.get("X-Akamai-ACS-Action"), "action=delete&version=1");
        assertEquals(connection.getRequestMethod(), "POST");
        assertEquals(connection.getContentLength(), 0);
    }

    @Test
    public void testDir() throws Exception {
        String path = "/foobar";
        NetStorage ns = createNetstorage(path);
        HttpURLConnectionTest connection = URLStreamHandlerFactoryTest.getURLConnection(ns.getNetstorageUri(path));
        Map<String, String> headers = connection.getRequestHeaders();

        ns.dir(path);
        assertEquals(headers.size(), 4);
        assertEquals(headers.get("X-Akamai-ACS-Action"), "action=dir&format=xml&version=1");
        assertEquals(connection.getRequestMethod(), "GET");
        assertEquals(connection.getContentLengthLong(), -1);
        assertEquals(connection.getContentLength(), -1);
    }

    @Test
    public void testDownload() throws Exception {
        String path = "/foobar";
        NetStorage ns = createNetstorage(path);
        HttpURLConnectionTest connection = URLStreamHandlerFactoryTest.getURLConnection(ns.getNetstorageUri(path));
        Map<String, String> headers = connection.getRequestHeaders();

        ns.download(path);
        assertEquals(headers.size(), 4);
        assertEquals(headers.get("X-Akamai-ACS-Action"), "action=download&version=1");
        assertEquals(connection.getRequestMethod(), "GET");
        assertEquals(connection.getContentLengthLong(), -1);
        assertEquals(connection.getContentLength(), -1);
    }

    @Test
    public void testDu() throws Exception {
        String path = "/foobar";
        NetStorage ns = createNetstorage(path);
        HttpURLConnectionTest connection = URLStreamHandlerFactoryTest.getURLConnection(ns.getNetstorageUri(path));
        Map<String, String> headers = connection.getRequestHeaders();

        ns.du(path);
        assertEquals(headers.size(), 4);
        assertEquals(headers.get("X-Akamai-ACS-Action"), "action=du&format=xml&version=1");
        assertEquals(connection.getRequestMethod(), "GET");
        assertEquals(connection.getContentLengthLong(), -1);
        assertEquals(connection.getContentLength(), -1);
    }

    @Test
    public void testMkdir() throws Exception {
        String path = "/foobar";
        NetStorage ns = createNetstorage(path);
        HttpURLConnectionTest connection = URLStreamHandlerFactoryTest.getURLConnection(ns.getNetstorageUri(path));
        Map<String, String> headers = connection.getRequestHeaders();

        ns.mkdir(path);
        assertEquals(headers.size(), 4);
        assertEquals(headers.get("X-Akamai-ACS-Action"), "action=mkdir&version=1");
        assertEquals(connection.getRequestMethod(), "PUT");
        assertEquals(connection.getContentLength(), 0);
    }

    @Test
    public void testMtimeWithDate() throws Exception {
        String path = "/foobar";
        NetStorage ns = createNetstorage(path);
        HttpURLConnectionTest connection = URLStreamHandlerFactoryTest.getURLConnection(ns.getNetstorageUri(path));
        Map<String, String> headers = connection.getRequestHeaders();
    	SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z", Locale.UK);
        Date mtime = sdf.parse("11 November 2013 00:00:00 GMT");

        ns.mtime(path, mtime);
        assertEquals(headers.size(), 4);
        assertEquals(headers.get("X-Akamai-ACS-Action"), "action=mtime&mtime=1384128000&version=1");
        assertEquals(connection.getRequestMethod(), "PUT");
        assertEquals(connection.getContentLength(), 0);
    }

    @Test
    public void testMtimeWithNull() throws Exception {
        String path = "/foobar";
        NetStorage ns = createNetstorage(path);
        HttpURLConnectionTest connection = URLStreamHandlerFactoryTest.getURLConnection(ns.getNetstorageUri(path));
        Map<String, String> headers = connection.getRequestHeaders();

        ns.mtime(path);
        assertEquals(headers.size(), 4);
        assertTrue(headers.get("X-Akamai-ACS-Action").matches("action=mtime&mtime=\\d{10}&version=1"));
        assertEquals(connection.getRequestMethod(), "PUT");
        assertEquals(connection.getContentLength(), 0);
    }

    @Test
    public void testRename() throws Exception {
        String path = "/foobar";
        NetStorage ns = createNetstorage(path);
        HttpURLConnectionTest connection = URLStreamHandlerFactoryTest.getURLConnection(ns.getNetstorageUri(path));
        Map<String, String> headers = connection.getRequestHeaders();

        ns.rename(path, "/barfoo");
        assertEquals(headers.size(), 4);
        assertEquals(headers.get("X-Akamai-ACS-Action"), "action=rename&destination=%2Fbarfoo&version=1");
        assertEquals(connection.getRequestMethod(), "PUT");
        assertEquals(connection.getContentLength(), 0);
    }

    @Test
    public void testRmdir() throws Exception {
        String path = "/foobar";
        NetStorage ns = createNetstorage(path);
        HttpURLConnectionTest connection = URLStreamHandlerFactoryTest.getURLConnection(ns.getNetstorageUri(path));
        Map<String, String> headers = connection.getRequestHeaders();

        ns.rmdir(path);
        assertEquals(headers.size(), 4);
        assertEquals(headers.get("X-Akamai-ACS-Action"), "action=rmdir&version=1");
        assertEquals(connection.getRequestMethod(), "POST");
        assertEquals(connection.getContentLength(), 0);
    }

    @Test
    public void testStat() throws Exception {
        String path = "/foobar";
        NetStorage ns = createNetstorage(path);
        HttpURLConnectionTest connection = URLStreamHandlerFactoryTest.getURLConnection(ns.getNetstorageUri(path));
        Map<String, String> headers = connection.getRequestHeaders();

        ns.stat(path);
        assertEquals(headers.size(), 4);
        assertEquals(headers.get("X-Akamai-ACS-Action"), "action=stat&format=xml&version=1");
        assertEquals(connection.getRequestMethod(), "GET");
        assertEquals(connection.getContentLengthLong(), -1);
        assertEquals(connection.getContentLength(), -1);
    }

    @Test
    public void testSymlink() throws Exception {
        String path = "/foobar";
        NetStorage ns = createNetstorage(path);
        HttpURLConnectionTest connection = URLStreamHandlerFactoryTest.getURLConnection(ns.getNetstorageUri(path));
        Map<String, String> headers = connection.getRequestHeaders();

        ns.symlink(path, "/barfoo");
        assertEquals(headers.size(), 4);
        assertEquals(headers.get("X-Akamai-ACS-Action"), "action=symlink&target=%2Fbarfoo&version=1");
        assertEquals(connection.getRequestMethod(), "PUT");
        assertEquals(connection.getContentLength(), 0);
    }

    @Test
    public void testQuickDelete() throws Exception {
        String path = "/foobar";
        NetStorage ns = createNetstorage(path);
        HttpURLConnectionTest connection = URLStreamHandlerFactoryTest.getURLConnection(ns.getNetstorageUri(path));
        Map<String, String> headers = connection.getRequestHeaders();

        ns.quickDelete(path);
        assertEquals(headers.size(), 4);
        assertEquals(headers.get("X-Akamai-ACS-Action"), "action=quick-delete&quick-delete=imreallyreallysure&version=1");
        assertEquals(connection.getRequestMethod(), "PUT");
        assertEquals(connection.getContentLength(), 0);
    }

    @Test
    public void testupload() throws Exception {
        String path = "/foobar";
        NetStorage ns = createNetstorage(path);
        HttpURLConnectionTest connection = URLStreamHandlerFactoryTest.getURLConnection(ns.getNetstorageUri(path));
        Map<String, String> headers = connection.getRequestHeaders();
        ByteArrayOutputStream requestStream = (ByteArrayOutputStream) connection.getOutputStream();

    	SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z", Locale.UK);
        Date mtime = sdf.parse("11 November 2013 00:00:00 GMT");
        byte[] data = "Lorem ipsum dolor sit amet, an sea putant quaeque, homero aperiam te eos.".getBytes(StandardCharsets.UTF_8);

        InputStream stream = new ByteArrayInputStream(data);

        ns.upload(path, stream, null, mtime, 73L, new byte[]{0}, new byte[]{1}, new byte[]{2}, false);
        assertEquals(headers.size(), 4);
        assertEquals(headers.get("X-Akamai-ACS-Action"), "action=upload&md5=00&mtime=1384128000&sha1=01&sha256=02&size=73&version=1");
        assertTrue(Arrays.equals(requestStream.toByteArray(), data));
        assertEquals(connection.getRequestMethod(), "PUT");
        assertEquals(connection.getContentLengthLong(), 73L);

        ns = createNetstorage(path);
        connection = URLStreamHandlerFactoryTest.getURLConnection(ns.getNetstorageUri(path));
        headers = connection.getRequestHeaders();
        requestStream = (ByteArrayOutputStream) connection.getOutputStream();
        stream = new ByteArrayInputStream(data);

        ns.upload(path, stream, null, mtime, 73L, null, null, null, true);
        assertEquals(headers.get("X-Akamai-ACS-Action"), "action=upload&mtime=1384128000&size=73&version=1");
        assertTrue(Arrays.equals(requestStream.toByteArray(), data));
        assertEquals(connection.getRequestMethod(), "PUT");
        assertEquals(connection.getContentLengthLong(), 73L);

        path = "/foobar.zip";
        ns = createNetstorage(path);
        connection = URLStreamHandlerFactoryTest.getURLConnection(ns.getNetstorageUri(path));
        headers = connection.getRequestHeaders();
        requestStream = (ByteArrayOutputStream) connection.getOutputStream();
        stream = new ByteArrayInputStream(data);
        ns.upload(path, stream, null, mtime, 73L, null, null, null, true);
        assertEquals(headers.get("X-Akamai-ACS-Action"), "action=upload&index-zip=1&mtime=1384128000&version=1");
        assertTrue(Arrays.equals(requestStream.toByteArray(), data));
        assertEquals(connection.getRequestMethod(), "PUT");
        assertEquals(connection.getContentLengthLong(), 73L);

        path = "/foobar.zip";
        ns = createNetstorage(path);
        connection = URLStreamHandlerFactoryTest.getURLConnection(ns.getNetstorageUri(path));
        headers = connection.getRequestHeaders();
        requestStream = (ByteArrayOutputStream) connection.getOutputStream();
        stream = new ByteArrayInputStream(data);
        ns.upload(path, stream, null, mtime, null, null, null, null, true);
        assertEquals(headers.get("X-Akamai-ACS-Action"), "action=upload&index-zip=1&mtime=1384128000&version=1");
        assertTrue(Arrays.equals(requestStream.toByteArray(), data));
        assertEquals(connection.getRequestMethod(), "PUT");
        assertEquals(connection.getContentLengthLong(), -1);
        assertEquals(connection.getChunkedLength(), 1024 * 1024);
    }

    @Test(expected = FileNotFoundException.class)
    public void testUploadFile() throws Exception {

        String path = "/foobar";
    	SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z", Locale.UK);
        Date mtime = sdf.parse("11 November 2013 00:00:00 GMT");
        byte[] data = "Lorem ipsum dolor sit amet, an sea putant quaeque, homero aperiam te eos.".getBytes(StandardCharsets.UTF_8);

        File tmpFile = null;
        try {
            tmpFile = File.createTempFile(UUID.randomUUID().toString(), ".txt");
            try (FileOutputStream tmpOutputStream = new FileOutputStream(tmpFile)) {
                tmpOutputStream.write(data);
            }
            tmpFile.setLastModified(mtime.getTime());

            NetStorage ns = createNetstorage(path);
            HttpURLConnectionTest connection = URLStreamHandlerFactoryTest.getURLConnection(ns.getNetstorageUri(path));
            Map<String, String> headers = connection.getRequestHeaders();
            ByteArrayOutputStream requestStream = (ByteArrayOutputStream) connection.getOutputStream();

            ns.upload("/foobar", tmpFile);
            assertEquals(headers.size(), 4);
            assertEquals(headers.get("X-Akamai-ACS-Action"), "action=upload&mtime=1384128000&sha256=4e8aecd6dc4c97ae55c30ef9b1e91b4829ef5871b16262b4628838a80dc0c2e2&size=73&version=1");
            //assertTrue(headers.get("X-Akamai-ACS-Action").matches("version=1&action=upload&mtime=\\d+&size=73&sha256=4e8aecd6dc4c97ae55c30ef9b1e91b4829ef5871b16262b4628838a80dc0c2e2"));
            assertTrue(Arrays.equals(requestStream.toByteArray(), data));
            assertEquals(connection.getRequestMethod(), "PUT");
            assertEquals(connection.getContentLengthLong(), 73L);

            // This should throw an exception
            ns = createNetstorage(path);
            ns.upload(path, new File(tmpFile, "doesnotexist.junk"), null, false);

        } finally {
            if (tmpFile != null && tmpFile.exists())
                tmpFile.delete();
        }
    }
}