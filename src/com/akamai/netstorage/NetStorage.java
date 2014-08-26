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

import static com.akamai.netstorage.Utils.readToEnd;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;

/**
 * The Netstorage class is the preferred interface for calling libraries indending to leverage the Netstorage API.
 * All of the available actions are innumerated in this library and are responsible for the correct business
 * logic to assemble the request to the API. Some early safetys are added in this library to limit errors.
 *
 * TODO: Add "LIST" support for ObjectStore
 * TODO: Detect FileStore v. ObjectStore
 * TODO: Extract xml response from various requests into standard object representation
 *
 * @author colinb@akamai.com (Colin Bendell)
 */
public class NetStorage {

    public String hostName;
    public String username;
    public String key;
    public Boolean useSSL;

    public NetStorage(String hostname, String username, String key) {
        this(hostname, username, key, false);
    }

    public NetStorage(String hostname, String username, String key, Boolean useSSL) {
        this.setHostName(hostname);
        this.setUsername(username);
        this.setKey(key);
        this.setUseSSL(useSSL);
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Boolean getUseSSL() {
        return useSSL;
    }

    public void setUseSSL(Boolean useSSL) {
        this.useSSL = useSSL;
    }

    protected URL getNetstorageUri(String path) {
        try {
            if (!path.startsWith("/")) path = "/" + path;
            return new URL(this.getUseSSL() ? "HTTPS" : "HTTP", this.getHostName(), path);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("This should never Happened! Protocols are locked to HTTPS and HTTP!", e);
        }
    }

    protected InputStream execute(String method, String path, APIEventBean acsParams, InputStream uploadStream, Long size) throws NetStorageException {
        return new NetStorageCMSv35Signer(
                method,
                this.getNetstorageUri(path),
                this.getUsername(),
                this.getKey(),
                acsParams,
                uploadStream,
                size != null && size > 0 ? size : -1
                ).execute();
    }

    protected InputStream execute(String method, String path, APIEventBean acsParams) throws NetStorageException {
        return execute(method, path, acsParams, null, null);
    }

    public boolean delete(String path) throws NetStorageException, IOException {
        APIEventBean action = new APIEventBean();
        action.setAction("delete");
        try(InputStream inputStream = execute("POST", path, action)) {
            readToEnd(inputStream);
        }
        return true;
    }

    public InputStream dir(String path) throws NetStorageException {
        return dir(path, "xml");
    }

    public InputStream dir(String path, String format) throws NetStorageException {
        APIEventBean action = new APIEventBean();
        action.setAction("dir");
        action.setFormat(format);
        return execute("GET", path, action);
    }

    public InputStream download(String path) throws NetStorageException {
        APIEventBean action = new APIEventBean();
        action.setAction("download");
        return execute("GET", path, action);
    }

    public InputStream du(String path) throws NetStorageException {
        return du(path, "xml");
    }
    public InputStream du(String path, String format) throws NetStorageException {
        APIEventBean action = new APIEventBean();
        action.setAction("du");
        action.setFormat(format);
        return execute("GET", path, action);
    }

    public boolean mkdir(String path) throws NetStorageException, IOException {
        APIEventBean action = new APIEventBean();
        action.setAction("mkdir");
        try (InputStream inputStream = execute("PUT", path, action)) {
            readToEnd(inputStream);
        }
        return true;
    }

    public boolean mtime(String path) throws NetStorageException, IOException {
        return mtime(path, null);
    }

    public boolean mtime(String path, Date mtime) throws NetStorageException, IOException {
        //TODO: verify that this is for a file - cannot mtime on symlinks or dirs
        if (mtime == null)
            mtime = new Date();

        APIEventBean action = new APIEventBean();
        action.setAction("mtime");
        action.setMtime(mtime);
        try (InputStream inputStream = execute("PUT", path, action)) {
            readToEnd(inputStream);
        }
        return true;
    }

    public boolean rename(String originalPath, String newPath) throws NetStorageException, IOException {
        //TODO: validate path and destination start with the same cpcode

        APIEventBean action = new APIEventBean();
        action.setAction("rename");
        action.setDestination(newPath);

        try (InputStream inputStream = execute("PUT", originalPath, action)) {
            readToEnd(inputStream);
        }
        return true;
    }

    public boolean rmdir(String path) throws NetStorageException, IOException {
        APIEventBean action = new APIEventBean();
        action.setAction("rmdir");
        try (InputStream inputStream = execute("POST", path, action)) {
            readToEnd(inputStream);
        }
        return true;
    }


    public InputStream stat(String path) throws NetStorageException {
        return stat(path, "xml");
    }

    public InputStream stat(String path, String format) throws NetStorageException {
        APIEventBean action = new APIEventBean();
        action.setAction("stat");
        action.setFormat(format);

        return execute("GET", path, action);
    }

    public boolean symlink(String path, String target) throws NetStorageException, IOException {
        APIEventBean action = new APIEventBean();
        action.setAction("symlink");
        action.setTarget(target);
        try (InputStream inputStream = execute("PUT", path, action)) {
            readToEnd(inputStream);
        }
        return true;
    }

    public boolean quickDelete(String path) throws NetStorageException, IOException {
        APIEventBean action = new APIEventBean();
        action.setAction("quick-delete");
        action.setQuickDelete("imreallyreallysure");

        try (InputStream inputStream = execute("PUT", path, action)) {
            readToEnd(inputStream);
        }
        return true;
    }

    public boolean Upload(String path, InputStream uploadFileStream, Map<String, String> additionalParams, Date mtime, Long size, byte[] md5Checksum, byte[] sha1Checksum, byte[] sha256Checksum, boolean indexZip) throws NetStorageException, IOException {

        APIEventBean action = new APIEventBean();

		action.setAction("upload");
		action.setAdditionalParams(additionalParams);
        action.setMtime(mtime);
        action.setSize(size);
        action.setMd5(md5Checksum);
        action.setSha1(sha1Checksum);
        action.setSha256(sha256Checksum);
        action.setIndexZip(indexZip ? indexZip : null);

        // sanity check to ensure that indexZip is only true if the file destination is also a zip.
        // probably should throw an exception or warning instead.
        if (action.getIndexZip() != null && action.getIndexZip() && !path.endsWith(".zip"))
            action.setIndexZip(null);

        // size is not supported with zip since the index-zip funtionality mutates the file thus inconsistency on which size value to use
        // probably should throw an exception or a warning
        if (action.getSize() != null && action.getIndexZip() != null && action.getIndexZip())
            action.setSize(null);

        try (InputStream inputStream = execute("PUT", path, action, uploadFileStream, size)) {
            readToEnd(inputStream);
        }
        return true;
    }

    public boolean Upload(String path, File srcFile) throws NetStorageException, IOException {
        return this.Upload(path, srcFile, null, false);
    }

    public boolean Upload(String path, File srcFile, Map<String, String> additionalParams) throws NetStorageException, IOException {
        return this.Upload(path, srcFile, additionalParams, false);
    }

    public boolean Upload(String path, File srcFile, Map<String, String> additionalParams, boolean indexZip) throws NetStorageException, IOException {
        if (!srcFile.exists()) throw new FileNotFoundException(String.format("Src file is not accessible %s", srcFile.toString()));

        Date mTime = new Date(srcFile.lastModified());
        byte[] checksum;
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(srcFile))) {
            checksum = Utils.computeHash(inputStream, Utils.HashAlgorithm.SHA256);
        }

        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(srcFile));) {
	        long size = srcFile.length();
	        return this.Upload(path, inputStream, additionalParams, mTime, size, null, null, checksum, indexZip);
        }
    }

	public boolean setmd(String path, Map<String, String> additionalParams)  throws NetStorageException, IOException {

		APIEventBean action = new APIEventBean();
        action.setAction("setmd");
        action.setAdditionalParams(additionalParams);

        try (InputStream inputStream = execute("PUT", path, action)) {
            readToEnd(inputStream);
        }
        return true;
	}

}
