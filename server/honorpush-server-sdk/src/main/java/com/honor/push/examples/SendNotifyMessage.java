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

import com.alibaba.fastjson.JSONObject;
import com.honor.push.android.AndroidNotification;
import com.honor.push.android.BadgeNotification;
import com.honor.push.android.Button;
import com.honor.push.android.ClickAction;
import com.honor.push.android.Color;
import com.honor.push.android.LightSettings;
import com.honor.push.exception.HonorMesssagingException;
import com.honor.push.message.AndroidConfig;
import com.honor.push.message.Message;
import com.honor.push.message.Notification;
import com.honor.push.messaging.HonorApp;
import com.honor.push.messaging.HonorMessaging;
import com.honor.push.model.Urgency;
import com.honor.push.model.Importance;
import com.honor.push.model.Visibility;
import com.honor.push.reponse.SendResponse;
import com.honor.push.util.InitAppUtils;

public class SendNotifyMessage {
    /**
     * send notification message
     *
     * @throws HonorMesssagingException
     */
    public void sendNotification() throws HonorMesssagingException {
        HonorApp app = InitAppUtils.initializeApp();
        HonorMessaging honorMessaging = HonorMessaging.getInstance(app);

        Notification notification = Notification.builder().setTitle("sample title")
                .setBody("sample message body")
                .build();

        JSONObject multiLangKey = new JSONObject();
        JSONObject titleKey = new JSONObject();
        titleKey.put("en","好友请求");
        JSONObject bodyKey = new JSONObject();
        bodyKey.put("en","My name is %s, I am from %s.");
        multiLangKey.put("key1", titleKey);
        multiLangKey.put("key2", bodyKey);

        LightSettings lightSettings = LightSettings.builder().setColor(Color.builder().setAlpha(0f).setRed(0f).setBlue(1f).setGreen(1f).build())
                .setLightOnDuration("3.5")
                .setLightOffDuration("5S")
                .build();

        AndroidNotification androidNotification = AndroidNotification.builder().setIcon("/raw/ic_launcher2")
                .setColor("#AACCDD")
                .setSound("/raw/shake")
                .setDefaultSound(true)
                .setTag("tagBoom")
                .setClickAction(ClickAction.builder().setType(2).setUrl("https://www.honor.com").build())
                .setBodyLocKey("key2")
                .addBodyLocArgs("boy").addBodyLocArgs("dog")
                .setTitleLocKey("key1")
                .addTitleLocArgs("Girl").addTitleLocArgs("Cat")
                .setChannelId("Your Channel ID")
                .setNotifySummary("some summary")
                .setMultiLangkey(multiLangKey)
                .setStyle(1)
                .setBigTitle("Big Boom Title")
                .setBigBody("Big Boom Body")
                .setAutoClear(86400000)
                .setNotifyId(486)
                .setGroup("Group1")
                .setImportance(Importance.LOW.getValue())
                .setLightSettings(lightSettings)
                .setBadge(BadgeNotification.builder().setAddNum(1).setBadgeClass("Classic").build())
                .setVisibility(Visibility.PUBLIC.getValue())
                .setForegroundShow(true)
                .addInboxContent("content1").addInboxContent("content2").addInboxContent("content3").addInboxContent("content4").addInboxContent("content5")
                .addButton(Button.builder().setName("button1").setActionType(0).build())
                .addButton(Button.builder().setName("button2").setActionType(1).setIntentType(0).setIntent("https://com.honor.hms.hmsdemo/deeplink").build())
                .addButton(Button.builder().setName("button3").setActionType(4).setData("your share link").build())
                .build();

        AndroidConfig androidConfig = AndroidConfig.builder().setCollapseKey(-1)
                .setUrgency(Urgency.HIGH.getValue())
                .setTtl("10000s")
                .setBiTag("the_sample_bi_tag_for_receipt_service")
                .setNotification(androidNotification)
                .build();

        Message message = Message.builder().setNotification(notification)
                .setAndroidConfig(androidConfig)
                .addToken("AND8rUp4etqJvbakK7qQoCVgFHnROXzH8o7B8fTl9rMP5VRFN83zU3Nvmabm3xw7e3gZjyBbp_wfO1jP-UyDQcZN_CtjBpoa7nx1WaVFe_3mqXMJ6nXJNUZcDyO_-k3sSw")
                .build();

        SendResponse response = honorMessaging.sendMessage(message);
    }
}
