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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.akamai.auth.ClientCredential;
import com.akamai.auth.RequestSigner;
import com.akamai.auth.RequestSigningException;
import com.akamai.netstorage.Utils.KeyedHashAlgorithm;

/**
 * The NetStorageCMSv35Signer is responsible for brokering the communication between the software layer and the API. This
 * includes the signing and formatting the request appropriately so that the implementation detail is abstracted from
 * calling libraries. The intended calling library is the NetStorage class, but this layer can be called directly and is
 * offered as a convenience interface for enhanced implementations.
 *
 * TODO: support rebinding on IO communication errors (eg: connection reset)
 * TODO: support async IO
 * TODO: support multiplexing of uploads
 * TODO: optimize and adapt throughput based on connection latency
 * TODO: support HTTP trailers for late SHA256 validation
 *
 * @author colinb@akamai.com (Colin Bendell)
 */
public class NetStorageCMSv35Signer implements RequestSigner {

    private static final String KITVERSION = "Java/3.6";
    private static final String KITVERSION_HEADER = "X-Akamai-NSKit";

    //Main headers used for communication to the API
    private static final String ACTION_HEADER = "X-Akamai-ACS-Action";
    private static final String AUTH_DATA_HEADER = "X-Akamai-ACS-Auth-Data";
    private static final String AUTH_SIGN_HEADER = "X-Akamai-ACS-Auth-Sign";

    // defaults
    private int connectTimeout = 10000;
    private int readTimeout = 10000;

	/**
	 * There are multiple types of Net Storage. The following types are
	 * detected:
	 * - FileStore (also known as Net Storage 3).
	 * - ObjectStore (also known as Net Storage 4).
	 */
	public enum NetStorageType {
		FileStore,
		ObjectStore,
		Unknown;
	}

    /**
     * Currently only 3 signing hash types are supported. Each are indicated with a version. They are:
     * Hmac-MD5 = v3
     * Hmac-SHA1 = v4
     * Hmac-SHA256 = v5
     * <p>
     * (don't ask what v1 and v2 were. You don't want to know. It will make you cry.)
     */
    public enum SignType {
        HMACMD5(KeyedHashAlgorithm.HMACMD5, 3),
        HMACSHA1(KeyedHashAlgorithm.HMACSHA1, 4),
        HMACSHA256(KeyedHashAlgorithm.HMACSHA256, 5);
        private final int value;
        private final KeyedHashAlgorithm algorithm;

        SignType(KeyedHashAlgorithm algorithm, int value) {
            this.value = value;
            this.algorithm = algorithm;
        }

        public int getValue() {
            return this.value;
        }

        public KeyedHashAlgorithm getAlgorithm() {
            return this.algorithm;
        }
    }


    /**
     * Primary invocation for an API communication. This constructor is used for convenience when not uploading content
     *
     * @param method an HTTP verb (GET, POST, PUT)
     * @param url    the url to interact with (eg: http://example.akamaihd.net/254462 )
     * @param params the set of bean parameters to be sent in the API request
     */
    public NetStorageCMSv35Signer(String method, URL url, APIEventBean params) {
        this(method, url, params, null, -1L, 10000, 10000);
    }

    /**
     * Primary invocation for an API communication. This constructor is used when uploading content is likely
     * <p>
     * Generally, we don't expose that there are other sign versions available and assume it will be HMac-SHA256. However,
     * this can be overridden in the setSignVersion() method;
     *
     * @param method       an HTTP verb (GET, POST, PUT)
     * @param url          the url to interact with (eg: http://example.akamaihd.net/254462 )
     * @param params       the set of bean parameters to be sent in the API request
     * @param uploadStream the inputStream to read the bytes to upload
     * @param uploadSize   if available, the total size of the inputStream. Note, that this could be different than the Size parameter in the action line
     */
    public NetStorageCMSv35Signer(String method, URL url, APIEventBean params, InputStream uploadStream, long uploadSize, int connectTimeout, int readTimeout) {
        this.setMethod(method);
        this.setUrl(url);
        this.setParams(params);
        this.setUploadStream(uploadStream);
        this.setUploadSize(uploadSize);
        this.setSignVersion(SignType.HMACSHA256);
        this.setConnectTimeout(connectTimeout);
        this.setReadTimeout(readTimeout);
    }

    private String method;
    private URL url;
    private APIEventBean params;
    private InputStream uploadStream = null;
    private long uploadSize;
    private SignType signVersion = null;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public APIEventBean getParams() {
        return params;
    }

    public void setParams(APIEventBean params) {
        this.params = params;
    }

    public InputStream getUploadStream() {
        return uploadStream;
    }

    public void setUploadStream(InputStream uploadStream) {
        this.uploadStream = uploadStream;
    }

    public long getUploadSize() {
        return uploadSize;
    }

    public void setUploadSize(long uploadSize) {
        this.uploadSize = uploadSize;
    }

    public SignType getSignVersion() {
        return signVersion;
    }

    public void setSignVersion(SignType signVersion) {
        this.signVersion = signVersion;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * Computes the value for the the X-Akamai-ACS-Action: header. This is a url query-string encoded separated
     * list of parameters in the form of name=value&amp;name2=value2. For extensibility purposes, we use generic method
     * to convert the bean fields &amp; parameters into name-value pairs and encode appropriately
     *
     * @return a url encoded query string of name-value pairs from the {@link com.akamai.netstorage.APIEventBean}
     */
    protected String getActionHeaderValue() {
        return Utils.convertMapAsQueryParams(this.getParams().asQueryParams());
    }

    /**
     * Constructs the X-Akamai-ACS-Auth-Data header which contains the signing version, the current time, a random number
     * and the username that is used to sign the data.
     *
     * @param credential client credentials
     * @return the data field in a comma separated list
     */
    protected String getAuthDataHeaderValue(ClientCredential credential) {
        Date currentTime = new Date();
        int rand = new Random().nextInt(Integer.MAX_VALUE);

        return String.format(
                "%d, 0.0.0.0, 0.0.0.0, %d, %d, %s",
                this.getSignVersion().getValue(),
                currentTime.getTime() / 1000,
                rand,
                credential.getUsername());
    }

    /**
     * Computes the X-Akamai-ACS-Auth-Sign header for a given Action and Data header values. This results in a base64
     * encoded representation of the hash as required by the spec. The api server will compute this same hash to validate
     * the request
     *
     * @param action   action header values {@link #getActionHeaderValue()}
     * @param authData data header values {@link #getAuthDataHeaderValue(ClientCredential credential)}
     * @param credential user credentials
     * @return a base64 encoded return string
     */
    protected String getAuthSignHeaderValue(String action, String authData, ClientCredential credential) {
        String signData = String.format(
                "%s%s\n%s:%s\n",
                authData,
                this.getUrl().getPath(),
                NetStorageCMSv35Signer.ACTION_HEADER.toLowerCase(),
                action);
        byte[] hash = Utils.computeKeyedHash(signData.getBytes(StandardCharsets.UTF_8), credential.getKey(), this.getSignVersion().getAlgorithm());

        return Utils.encodeBase64(hash);
    }

    /**
     * Assmembles the HTTP Headers necessary for API communication
     *
     * @param credential user credentials
     * @return Map of name-value pairs representing HTTP Headers and values.
     */
    public Map<String, String> computeHeaders(ClientCredential credential) {
        final Map<String, String> headers = new HashMap<>(3);
        final String action = getActionHeaderValue();
        final String authData = getAuthDataHeaderValue(credential);
        final String authSign = getAuthSignHeaderValue(action, authData, credential);

        headers.put(NetStorageCMSv35Signer.KITVERSION_HEADER, NetStorageCMSv35Signer.KITVERSION);
        headers.put(NetStorageCMSv35Signer.ACTION_HEADER, action);
        headers.put(NetStorageCMSv35Signer.AUTH_DATA_HEADER, authData);
        headers.put(NetStorageCMSv35Signer.AUTH_SIGN_HEADER, authSign);
        return headers;
    }

    /**
     * Attempt to validate the response and detect common causes of errors. The most common being time drift.
     * <p>
     * TODO: catch rate limitting errors. Should delay and retry.
     *
     * @param connection an open url connection
     * @return true if 200 OK response, false otherwise.
     * @throws NetStorageException wrapped exception if it is a recoverable exception
     * @throws IOException         shouldn't be called at this point, but could be triggered when interrogating the response
     */
    public boolean validate(HttpURLConnection connection) throws NetStorageException, IOException {
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
            return true;

        // Validate Server-Time drift
        Date currentDate = new Date();
        long responseDate = connection.getHeaderFieldDate("Date", 0);
        if (responseDate != 0 && currentDate.getTime() - responseDate > 30 * 1000)
            throw new NetStorageException("Local server Date is more than 30s out of sync with Remote server");

        // generic response
        throw new NetStorageException(String.format("Unexpected Response from Server: %d %s\n%s",
                connection.getResponseCode(), connection.getResponseMessage(), connection.getHeaderFields()), connection.getResponseCode());
    }

    /**
     * @param request    the request to sign.
     * @param credential the credential used in the signing.
     * @return open connection
     * @throws RequestSigningException signing exception
     */
    public HttpURLConnection sign(HttpURLConnection request, ClientCredential credential) throws RequestSigningException {
        try {
            if (request == null) {
                request = (HttpURLConnection) this.getUrl().openConnection();
            }

            request.setRequestMethod(this.getMethod());
            for (Map.Entry<String, String> entry : this.computeHeaders(credential).entrySet())
                request.setRequestProperty(entry.getKey(), entry.getValue());

            return request;
        } catch (IOException ex) {
            throw new RequestSigningException(ex);
        }
    }

    /**
     * Opens the connection to Netstorage, assembles the signing headers and uploads any files.
     *
     * @param request    an open request
     * @param credential user credentials
     * @return the InputStream from the response if successful
     * @throws RequestSigningException if an error occurred during the communication
     */
    public InputStream execute(HttpURLConnection request, ClientCredential credential) throws RequestSigningException {
        try {
            request = sign(request, credential);
            request.setConnectTimeout(this.getConnectTimeout());
            request.setReadTimeout(this.getReadTimeout());

            if (this.getMethod().equals("PUT") || this.getMethod().equals("POST")) {
                request.setDoOutput(true);
                if (this.getUploadStream() == null) {
                    request.setFixedLengthStreamingMode(0);
                    request.connect();
                } else {
                    byte[] buffer = new byte[1024 * 1024];

                    if (this.getUploadSize() > 0)
                        request.setFixedLengthStreamingMode(this.getUploadSize());
                    else
                        request.setChunkedStreamingMode(buffer.length);

                    request.connect();

                    try (BufferedInputStream input = new BufferedInputStream(this.getUploadStream())) {
                        try (OutputStream output = request.getOutputStream()) {
                            for (int length; (length = input.read(buffer)) > 0; ) {
                                output.write(buffer, 0, length);
                            }
                            output.flush();
                        }
                    }
                }
            } else {
                request.connect();
            }

            validate(request);

            return new SignerInputStream(request.getInputStream(), request);

        } catch (NetStorageException | IOException e) {
            if (request != null) {
                try (InputStream is = request.getInputStream()) {}
                catch (IOException ioException) {}
                try (InputStream is = request.getErrorStream()) {}
                catch (IOException ioException) {}
            }
            throw new NetStorageException("Communication Error", e);
        }
    }

    public InputStream execute(ClientCredential credential) throws RequestSigningException {
        return execute(null, credential);
    }
}

class SignerInputStream extends BufferedInputStream
{
    HttpURLConnection request;
    public SignerInputStream(InputStream stream, HttpURLConnection request) {
        super(stream);
        this.request = request;
    }

    public HttpURLConnection getHttpRequest() {
        return request;
    }

}
