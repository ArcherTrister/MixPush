package com.mixpush.mqtt.meta;

public interface MetaMqttPushCallback {
    void onSuccess();

    void onFailure(int errorCode, String errorString);
}
