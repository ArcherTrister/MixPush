package com.mixpush.mqtt;

import android.content.Context;
import com.mixpush.core.BaseMixPushProvider;
import com.mixpush.core.MixPushClient;
import com.mixpush.core.MixPushHandler;
import com.mixpush.core.RegisterType;
import com.mixpush.mqtt.meta.MetaMqttPushCallback;
import com.mixpush.mqtt.meta.MetaMqttPushClient;

import static com.mixpush.mqtt.UnifiedMqttMessageService.TAG;

public class MqttPushProvider extends BaseMixPushProvider {
    public static final String MQTT = "mqtt";

    MixPushHandler handler = MixPushClient.getInstance().getHandler();

    @Override
    public void register(Context context, RegisterType type) {
        String serverUrl = getMetaData(context, "MQTT_SERVER_URL");
        String appId = getMetaData(context, "MQTT_APP_ID");
        String appKey = getMetaData(context, "MQTT_APP_KEY");
        MetaMqttPushClient.getInstance().init(context, serverUrl, appId, appKey);
    }

    @Override
    public void unRegister(Context context) {
        //注销PushToken
        MetaMqttPushClient.getInstance().deletePushToken(context, new MetaMqttPushCallback() {
            @Override
            public void onSuccess() {
                // TODO: 注销PushToken成功
            }

            @Override
            public void onFailure(int errorCode, String errorString) {
                // TODO: 注销PushToken失败
                handler.getLogger().log(TAG, "注销PushToken失败. code: " + errorCode + ", message: " + errorString);
            }
        });
    }

    @Override
    public boolean isSupport(Context context) {
        return MetaMqttPushClient.getInstance().checkSupportMqttPush(context);
    }

    @Override
    public String getPlatformName() {
        return MqttPushProvider.MQTT;
    }

    @Override
    public String getRegisterId(Context context) {
        // 获取PushToken
        return MetaMqttPushClient.getInstance().getPushToken();
    }
}
