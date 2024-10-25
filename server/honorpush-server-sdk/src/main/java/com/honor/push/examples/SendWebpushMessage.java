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
package com.honor.push.examples;

import com.honor.push.exception.HonorMesssagingException;
import com.honor.push.message.Message;
import com.honor.push.message.Notification;
import com.honor.push.message.WebPushConfig;
import com.honor.push.messaging.HonorApp;
import com.honor.push.messaging.HonorMessaging;
import com.honor.push.reponse.SendResponse;
import com.honor.push.util.InitAppUtils;
import com.honor.push.webpush.WebActions;
import com.honor.push.webpush.WebHmsOptions;
import com.honor.push.webpush.WebNotification;
import com.honor.push.webpush.WebpushHeaders;

public class SendWebpushMessage {

    public void sendWebpushMessage() throws HonorMesssagingException {
        HonorApp app = InitAppUtils.initializeApp();
        HonorMessaging honorMessaging = HonorMessaging.getInstance(app);

        Notification notification = Notification.builder().setTitle("Big News")
                .setBody("This is a Big apple news1202")
                .build();

        WebpushHeaders webpushHeaders = WebpushHeaders.builder()
                .setTtl("990")
                .setUrgency("low")
                .setTopic("12313")
                .build();

        WebNotification webNotification = WebNotification.builder().setTitle("Web Push Title")
                .setBody("Web Push body")
                .setIcon("https://developer-portalres-drcn.dbankcdn.com/system/modules/org.opencms.portal.template.core/\resources/images/icon_Promotion.png")
                .addAction(WebActions.builder().setAction("click").setIcon("").setTitle("title").build())
                .setBadge("badge")
                .setDir("auto")
                .setImage("image url")
                .setLang("en")
                .setRenotify(false)
                .setRequireInteraction(false)
                .setSilent(true)
                .setTag("tag")
                .setTimestamp(32323L)
                .addVibrate(1).addVibrate(2).addVibrate(3)
                .build();

        WebHmsOptions webHmsOptions = WebHmsOptions.builder().setLink("http://www.xx.com").build();

        WebPushConfig webpush = WebPushConfig.builder().setHeaders(webpushHeaders)
                .setNotification(webNotification)
                .setWebHmsOptions(webHmsOptions)
                .build();

        String token = "cTW+APk7SomjRb2dOB7UIfyn_6q-hdNR8TfbkEcRus7fR2DrfXqS6EwINiuy1dhceiPXgE9t6rYkVNuRrcFcCPsCfNAIVR4N54Whfhow4r51hY05MB43r7461pls0qj9nhF4gQ";

        Message message = Message.builder().setNotification(notification)
                .setData("nb!")
                .setWebpush(webpush)
                .addToken(token)
                .build();

        SendResponse response = honorMessaging.sendMessage(message);
    }
}
