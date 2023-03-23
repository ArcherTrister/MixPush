package com.mixpush.mqtt.meta;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.UUID;

public class MetaMqttPushClient {
    public static final String serverAddress = "serverAddress";
    public static final String userName = "userName";
    public static final String password = "password";
    public static final String clientId = "clientId";
    private static final String editKey = "mixPushClientId";
    private static final String intentAction = "com.mixpush.mqtt.push.action.MESSAGING_EVENT";
    private static MetaMqttPushClient INSTANCE;
    /**
     * 上下文
     */
    private Context mContext;
    /**
     * MQTT userName
     */
    private String mAppId;
    /**
     * MQTT 客户端ID
     */
    private String mClientId;

    /**
     * MetaMqttPushClient
     * @return MetaMqttPushClient
     */
    public static MetaMqttPushClient getInstance() {
        if (INSTANCE == null) {
            synchronized(MetaMqttPushClient.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MetaMqttPushClient();
                }
            }
        }

        return INSTANCE;
    }

    public void init(Context context, String serverUrl, String appId, String appSecret)
    {
        if(TextUtils.isEmpty(serverUrl) || TextUtils.isEmpty(appId) || TextUtils.isEmpty(appSecret))
        {
            throw new RuntimeException("serverUrl appId appSecret 不能为空");
        }
        mContext = context;
        mAppId = appId;
        mClientId = getClientId(context);
        //开启MQTT服务
        Intent serviceIntent = new Intent();
        serviceIntent.setAction(intentAction);
        // serviceIntent.addCategory();
        serviceIntent.setPackage(context.getPackageName());
        serviceIntent.putExtra(serverAddress, serverUrl);
        serviceIntent.putExtra(userName, appId);
        serviceIntent.putExtra(password, appSecret);
        serviceIntent.putExtra(clientId, mClientId);
        //mContext.bindService(serviceIntent, mqttServiceConnect, BIND_AUTO_CREATE);
        context.startService(serviceIntent);
    }

    public boolean checkSupportMqttPush(Context context)
    {
        return true;
    }

    public String getPushToken()
    {
        return mClientId;
    }

    public void deletePushToken(Context context, MetaMqttPushCallback callback)
    {
        if (context == null) {
            callback.onFailure(0, "上下文为空");
        }else{
            try {
                SharedPreferences preferences = context.getSharedPreferences(this.mAppId, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                preferences.edit().remove(editKey).apply();
                editor.commit();
                callback.onSuccess();
            }catch (Exception ex){
                callback.onFailure(1, ex.getMessage());
            }
        }
    }

    public void close()
    {
        Intent serviceIntent = new Intent();
        serviceIntent.setAction(intentAction);
        // serviceIntent.addCategory();
        serviceIntent.setPackage(mContext.getPackageName());
        //mContext.unbindService(mqttServiceConnect);
        mContext.stopService(serviceIntent);
    }

    private String getClientId(Context context) {
        String clientId = getClientIdFromSharedPreferences(context);
        if (TextUtils.isEmpty(clientId)) {
            String uuid = UUID.randomUUID().toString().replaceAll("-","");
            clientId = mAppId + "_" + uuid;
            saveClientIdToSharedPreferences(context, clientId);
        }
        return clientId;
    }

    private void saveClientIdToSharedPreferences(Context context, String clientId) {
        if (context == null) {
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences(this.mAppId, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(editKey, clientId).apply();
        editor.commit();
    }

    private String getClientIdFromSharedPreferences(Context context) {
        if (context == null) {
            return "";
        }
        SharedPreferences preferences = context.getSharedPreferences(this.mAppId, Context.MODE_PRIVATE);
        return preferences.getString(editKey, "");
    }
}
