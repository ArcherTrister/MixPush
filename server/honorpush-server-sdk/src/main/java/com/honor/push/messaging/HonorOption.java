/*
 * Copyright 2020. Honor Technologies Co., Ltd. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.honor.push.messaging;

import com.honor.push.util.IgnoreSSLUtils;
import com.honor.push.util.ValidatorUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/** Configurable HCM options. */
public class HonorOption {
    private static final Logger logger = LoggerFactory.getLogger(HonorOption.class);

    private final HonorCredential credential;
    private final CloseableHttpClient httpClient;
    private final ThreadManager threadManager;

    private HonorOption(HonorOption.Builder builder) {
        ValidatorUtils.checkArgument(builder.credential != null, "HonorOption must be initialized with setCredential()");
        this.credential = builder.credential;

        ValidatorUtils.checkArgument(builder.httpClient != null, "HonorOption must be initialized with a non-null httpClient");
        this.httpClient = builder.httpClient;

        ValidatorUtils.checkArgument(builder.threadManager != null, "HonorOption must be initialized with a non-null threadManager");
        this.threadManager = builder.threadManager;
    }

    /**
     * Returns a instance of HonorCredential used for refreshing token.
     *
     * @return A <code>HonorCredential</code> instance.
     */
    public HonorCredential getCredential() {
        return credential;
    }

    /**
     * Returns a instance of httpclient used for sending http request.
     *
     * @return A <code>httpclient</code> instance.
     */
    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public ThreadManager getThreadManager() {
        return threadManager;
    }

    /**
     * Builder for constructing {@link HonorOption}.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private HonorCredential credential;
        private CloseableHttpClient httpClient;

        {
            try {
                httpClient = IgnoreSSLUtils.createClient();
            } catch (KeyManagementException | NoSuchAlgorithmException e) {
                logger.debug("Fail to create httpClient for sending message", e);
            }
        }

        private ThreadManager threadManager = HonorThreadManager.DEFAULT_THREAD_MANAGER;


        public Builder() {}

        public Builder setCredential(HonorCredential credential) {
            this.credential = credential;
            return this;
        }

        public Builder setHttpClient(CloseableHttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder setThreadManager(ThreadManager threadManager) {
            this.threadManager = threadManager;
            return this;
        }

        public HonorOption build() {
            return new HonorOption(this);
        }
    }
}
