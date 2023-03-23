package com.mixpush.example

import android.app.Application
import android.util.Log
import com.mixpush.core.GetRegisterIdCallback
import com.mixpush.core.MixPushPlatform
import com.mixpush.core.MixPushClient
import com.mixpush.core.MixPushLogger
import com.mixpush.mi.MiPushProvider
import com.mixpush.mqtt.MqttPushProvider


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MixPushClient.getInstance().setLogger(object : MixPushLogger {
            override fun log(tag: String, content: String, throwable: Throwable?) {
                Log.e(tag, content)
                throwable?.printStackTrace()
            }

            override fun log(tag: String, content: String) {
                Log.e(tag, content)
            }
        });
        MixPushClient.getInstance().setPushReceiver(MyMixPushReceiver())
        MixPushClient.getInstance().setPassThroughReceiver(MyPassThroughReceiver())
        //MixPushClient.getInstance().register(this, MiPushProvider.MI, MqttPushProvider.MQTT)
        MixPushClient.getInstance().register(this, MqttPushProvider.MQTT, MqttPushProvider.MQTT)
//        MixPushClient.getInstance().getRegisterId(this, object : GetRegisterIdCallback() {
//            override fun callback(platform: MixPushPlatform?) {
//                Log.e("GetRegisterIdCallback", "notification $platform")
//            }
//        })
    }
}