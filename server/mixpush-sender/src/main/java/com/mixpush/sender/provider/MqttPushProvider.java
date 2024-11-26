package com.mixpush.sender.provider;

import com.mixpush.sender.MixPushMessage;
import com.mixpush.sender.MixPushProvider;
import com.mixpush.sender.MixPushResult;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class MqttPushProvider extends MixPushProvider {
    public static final String MQTT = "mqtt";
    private final MqttMessaging mqttMessaging;

    public MqttPushProvider(String appId, String appSecret) {
        MqttApp app = InitAppUtils.initializeApp(appId, appSecret);
        this.mqttMessaging = MqttMessaging.getInstance(app);
    }


    @Override
    protected MixPushResult sendMessageToSingle(MixPushMessage mixPushMessage, String regId) {
        Message.Builder builder = toMessage(mixPushMessage);
        builder.addToken(regId);
        try {
            SendResponse response = mqttMessaging.sendMessage(builder.build());
            return toMixPushResult(mixPushMessage, response);
        } catch (MqttMesssagingException e) {
            return new MixPushResult.Builder().provider(this).message(mixPushMessage).error(e).build();
        }
    }

    @Override
    protected MixPushResult sendMessageToList(MixPushMessage mixPushMessage, List<String> regIds) {
        Message.Builder builder = toMessage(mixPushMessage);
        builder.addAllToken(regIds);
        try {
            SendResponse response = mqttMessaging.sendMessage(builder.build());
            return toMixPushResult(mixPushMessage, response);
        } catch (MqttMesssagingException e) {
            return new MixPushResult.Builder().provider(this).message(mixPushMessage).error(e).build();
        }
    }

    @Override
    public MixPushResult broadcastMessageToAll(MixPushMessage mixPushMessage) {
        return new MixPushResult.Builder().provider(this)
                .reason(platformName() + " 不支持全局推送")
                .statusCode(MixPushResult.NOT_SUPPORT_BROADCAST)
                .build();
    }

    private Message.Builder toMessage(MixPushMessage mixPushMessage) {
        Notification notification = Notification.builder().setTitle(mixPushMessage.getTitle())
                .setBody(mixPushMessage.getDescription())
                .build();

        //// mixpush://com.mixpush.mqtt/message?payload=%7b%22url%22%3a%22http%3a%2f%2fsoso.com%22%7d
        String url = "mixpush://com.mixpush.mqtt/message?";
        if (!mixPushMessage.isJustOpenApp()) {
            try {
                url += "payload=" + URLEncoder.encode(mixPushMessage.getPayload(), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

//        LightSettings lightSettings = LightSettings.builder().setColor(Color.builder().setAlpha(0f).setRed(0f).setBlue(1f).setGreen(1f).build())
//                .setLightOnDuration("3.5")
//                .setLightOffDuration("5S")
//                .build();
        ClickAction clickAction = ClickAction.builder()
                .setType(1) // 1：用户自定义点击行为2：点击后打开特定url3：点击后打开应用App4：点击后打开富媒体信息
                .setIntent(url)
                .build();

        AndroidNotification androidNotification = AndroidNotification.builder()
                .setClickAction(clickAction)
//                .setBodyLocKey("M.String.body")
//                .addBodyLocArgs("boy").addBodyLocArgs("dog")
//                .setTitleLocKey("M.String.title")
//                .addTitleLocArgs("Girl").addTitleLocArgs("Cat")
                .setChannelId(mixPushMessage.getConfig().getChannelId())
//                .setNotifySummary("some summary")
//                .setMultiLangkey(JSON.parseObject(mixPushMessage.getPayload()))
//                .setStyle(1)
                .setBigTitle(mixPushMessage.getTitle())
                .setBigBody(mixPushMessage.getDescription())
//                .setAutoClear(86400000)
//                .setNotifyId(486)
//                .setGroup("Group1")
//                .setImportance(Importance.LOW.getValue())
//                .setLightSettings(lightSettings)
//                .setBadge(BadgeNotification.builder().setAddNum(1).setBadgeClass("Classic").build())
//                .setVisibility(Visibility.PUBLIC.getValue())
                .setForegroundShow(true)
                .build();

        AndroidConfig androidConfig = AndroidConfig.builder()
//                .setCollapseKey(-1)
//                .setUrgency(Urgency.HIGH.getValue())
                .setTtl((mixPushMessage.getConfig().getTimeToLive() / 1000) + "s")
//                .setBiTag("the_sample_bi_tag_for_receipt_service")
                .setNotification(androidNotification)
                .build();
        return Message.builder()
                .setNotification(notification)
                .setAndroidConfig(androidConfig);
    }

    private MixPushResult toMixPushResult(MixPushMessage message, SendResponse result) {
        String errorCode = result.getCode();
        String reason = result.getMsg();
        return new MixPushResult.Builder()
                .provider(this)
                .message(message)
                .statusCode(errorCode)
                .reason(reason)
                .extra(result)
                .taskId(result.getRequestId())
                .succeed("Success".equals(result.getMsg()))
                .build();
    }

    @Override
    protected String platformName() {
        return MqttPushProvider.MQTT;
    }

    @Override
    public boolean isSupportBroadcastAll(boolean isPassThrough) {
        return false;
    }

    @Override
    public boolean isSupportPassThrough() {
        return false;
    }
}
