/*
 * Copyright 2016 Akamai Technologies http://developer.akamai.com.
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

import com.akamai.auth.ClientCredential;

/**
 * Default implementation of the {@link ClientCredential}.
 *
 */
public class DefaultCredential implements ClientCredential {

    public final static String HOSTNAME_PROPERTY = "host";
    public final static String USERNAME_PROPERTY = "username";
    public final static String KEY_PROPERTY = "key";

    /**
     * The hostname for the connection.
     */
    private final String hostname;

    /**
     * The client token (username).
     */
    private final String username;

    /**
     * The secret associated with the client token.
     */
    private final String key;

    /**
     * Constructor.
     *
     * @param clientToken the client token, cannot be null or empty.
     * @param accessToken the access token, cannot be null or empty.
     * @param clientSecret the client secret, cannot be null or empty.
     *
     * @throws IllegalArgumentException if any of the parameters is null or empty.
     */

    /**
     * Default constructor
     *
     * @param hostname hostname name for the connection
     * @param username the username
     * @param key the secret key
     * @throws IllegalArgumentException if any of the parameters is null or empty.

     */
    public DefaultCredential(String hostname, String username, String key) {
        if (hostname == null || hostname.equals("")) {
            throw new IllegalArgumentException("hostname cannot be empty.");
        }
        if (username == null || username.equals("")) {
            throw new IllegalArgumentException("username cannot be empty.");
        }
        if (key == null || key.equals("")) {
            throw new IllegalArgumentException("key cannot be empty.");
        }

        this.hostname = hostname;
        this.username = username;
        this.key = key;
    }

    /**
     * Gets the client hostname.
     * @return The client hostname (without port or protocol).
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Gets the username .
     * @return the username for the connection.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the secret associated with the client token.
     * @return the secret.
     */
    public String getKey() {
        return key;
    }

}
