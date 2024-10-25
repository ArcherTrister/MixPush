/*
 * Copyright (c) Honor Technologies Co., Ltd. 2019-2024. All rights reserved.
 */
package com.honor.push.util;

import com.honor.push.messaging.HonorApp;
import com.honor.push.messaging.HonorCredential;
import com.honor.push.messaging.HonorOption;

import java.util.ResourceBundle;

public class InitAppUtils {
    /**
     * @return HonorApp
     */
    public static HonorApp initializeApp() {
        String appId = ResourceBundle.getBundle("url").getString("appid");
        String appSecret = ResourceBundle.getBundle("url").getString("appsecret");
        // Create HonorCredential
        // This appId and appSecret come from Honor Developer Alliance
        return initializeApp(appId, appSecret);
    }

    public static HonorApp initializeApp(String appId, String appSecret) {
        HonorCredential credential = HonorCredential.builder()
                .setAppId(appId)
                .setAppSecret(appSecret)
                .build();

        // Create HonorOption
        HonorOption option = HonorOption.builder()
                .setCredential(credential)
                .build();

        // Initialize HonorApp
//        return HonorApp.initializeApp(option);
        return HonorApp.getInstance(option);
    }
}
