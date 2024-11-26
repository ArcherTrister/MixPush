package com.mixpush.mqtt;

import com.mixpush.core.MixPushClient;
import com.mixpush.core.MixPushHandler;
import com.mixpush.core.MixPushMessage;
import com.mixpush.core.MixPushPlatform;
import com.mixpush.mqtt.meta.MetaMqttPushDataMsg;
import com.mixpush.mqtt.meta.MetaMqttService;

public class UnifiedMqttMessageService extends MetaMqttService {
    public static final String TAG = "MqttPushProvider";
    MixPushHandler handler = MixPushClient.getInstance().getHandler();

     /**
      * 接收透传消息方法。
      */
     @Override
     public void onPassThroughMessageReceived(MetaMqttPushDataMsg message) {
         MixPushMessage mixPushMessage = new MixPushMessage();
         mixPushMessage.setPlatform(MqttPushProvider.MQTT);
         if (message.getNotification() != null) {
             mixPushMessage.setTitle(message.getNotification().getTitle());
             mixPushMessage.setDescription(message.getNotification().getDescription());
         }
         mixPushMessage.setPayload(message.getData());
         mixPushMessage.setPassThrough(message.getType() == 0);
         handler.getPassThroughReceiver().onReceiveMessage(this, mixPushMessage);
     }

    /**
     * 接收通知消息方法。
     */
    @Override
    public void onPushMessageReceived(MetaMqttPushDataMsg message) {
        MixPushMessage mixPushMessage = new MixPushMessage();
        mixPushMessage.setPlatform(MqttPushProvider.MQTT);
        message.setMsgId(message.getMsgId());
        mixPushMessage.setTitle(message.getNotification().getTitle());
        mixPushMessage.setDescription(message.getNotification().getDescription());
        mixPushMessage.setPayload(message.getData());
        handler.getPushReceiver().onNotificationMessageArrived(this, mixPushMessage);
    }

//     /**
//      * 服务端更新token回调方法。
//      * APP调用getToken接口向服务端申请token，如果服务端当次没有返回token值，后续服务端返回token通过此接口返回。主要包含如下三种场景：
//      * 1、申请Token如果当次调用失败，PUSH会自动重试申请，成功后则以onNewToken接口返回。
//      * 2、如果服务端识别token过期，服务端刷新token也会以onNewToken方式返回。
//      * 3、华为设备上EMUI版本低于10.0申请token时，以onNewToken方式返回。
//      */
//     @Override
//     public void onNewToken(String token) {
//         MixPushPlatform mixPushPlatform = new MixPushPlatform(MqttPushProvider.MQTT, token);
//         handler.getPushReceiver().onRegisterSucceed(this, mixPushPlatform);
//     }
}
