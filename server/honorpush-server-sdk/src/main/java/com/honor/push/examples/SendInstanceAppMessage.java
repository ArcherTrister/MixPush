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
import com.honor.push.exception.HonorMesssagingException;
import com.honor.push.message.AndroidConfig;
import com.honor.push.message.Message;
import com.honor.push.messaging.HonorApp;
import com.honor.push.messaging.HonorMessaging;
import com.honor.push.reponse.SendResponse;
import com.honor.push.util.InitAppUtils;

public class SendInstanceAppMessage {
    /**
     * send instance app message
     *
     * @throws HonorMesssagingException
     */
    public void sendInstanceAppMessage() throws HonorMesssagingException {
        HonorApp app = InitAppUtils.initializeApp();
        HonorMessaging honorMessaging = HonorMessaging.getInstance(app);

        String token = "AI838_-IxzMqKqeIoIqFgL9D5N8YunVqZXFU3jCohcmEkb1RMquoT7uxQkv3cXCv7IXwXjTsH0WK35DRrnLI6RBOWxqjnRqkbp6W5CFQj0zw09FG5sTuyZd2NHtxgVzUUg";

        JSONObject params = new JSONObject();
        params.put("key1", "test1");
        params.put("key2", "test2");

        JSONObject ringtone = new JSONObject();
        ringtone.put("vibration", "true");
        ringtone.put("breathLight", "true");

        JSONObject msgBody = new JSONObject();
        msgBody.put("title", "Welcome to use Honor HMS Push Kit");
        msgBody.put("description", "One of the best push platform on the planet!!!");
        msgBody.put("page", "");
        msgBody.put("params", params);
        msgBody.put("ringtone", ringtone);


        JSONObject msg = new JSONObject();
        msg.put("pushtype", 0);
        msg.put("pushbody", msgBody);

        String data = msg.toJSONString();

        AndroidConfig androidConfig = AndroidConfig.builder().setFastAppTargetType(1).build();

        Message message = Message.builder().setData(data)
                .setAndroidConfig(androidConfig)
                .addToken(token)
                .build();

        SendResponse response = honorMessaging.sendMessage(message);
    }
}
