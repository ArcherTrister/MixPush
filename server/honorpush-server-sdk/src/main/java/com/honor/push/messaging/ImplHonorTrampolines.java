/* Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 *  2019.12.15-added method getAccessToken
 *                  Honor Technologies Co., Ltd.
 *
 */
package com.honor.push.messaging;

import org.apache.http.client.HttpClient;

/**
 * Provides trampolines into package-private APIs used by components of HCM
 */
public final class ImplHonorTrampolines {
    private ImplHonorTrampolines() {}

    public static HonorCredential getCredential(HonorApp app) {
        return app.getOption().getCredential();
    }

    public static String getAccessToken(HonorApp app) {
        return app.getOption().getCredential().getAccessToken();
    }

    public static String getAppId(HonorApp app) {
        return app.getOption().getCredential().getAppId();
    }

    public static HttpClient getHttpClient(HonorApp app) {
        return app.getOption().getHttpClient();
    }

    public static <T extends HonorService> T getService(HonorApp app, String id, Class<T> type) {
        return type.cast(app.getService(id));
    }

    public static <T extends HonorService> T addService(HonorApp app, T service) {
        app.addService(service);
        return service;
    }
}
