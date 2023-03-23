package com.mixpush.mqtt.meta;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.UUID;

public class MetaMqttPushClient {
    private static MetaMqttPushClient INSTANCE;
    /**
     * 上下文
     */
    private Context mContext;
    /**
     * MQTT 客户端ID
     */
    private String clientId;

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
        clientId = encrypt(appId +"_"+ getUUId(context), new int[]{3, 5, 7}) ;
        //开启MQTT服务
        Intent serviceIntent = new Intent();
        serviceIntent.setAction("com.mixpush.mqtt.push.action.MESSAGING_EVENT");
        serviceIntent.setPackage(context.getPackageName());
        serviceIntent.putExtra("serverUrl", serverUrl);
        serviceIntent.putExtra("username", appId);
        serviceIntent.putExtra("password", appSecret);
        serviceIntent.putExtra("clientId", clientId);
        //mContext.bindService(serviceIntent, mqttServiceConnect, BIND_AUTO_CREATE);
        context.startService(serviceIntent);
    }

    public boolean checkSupportMqttPush(Context context)
    {
        return true;
    }

    public String getPushToken()
    {
        return clientId;
    }

    public void deletePushToken(Context context, MetaMqttPushCallback callback)
    {
        if (context == null) {
            callback.onFailure(0, "上下文为空");
        }else{
            try {
                SharedPreferences preferences = context.getSharedPreferences("mixpush", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                preferences.edit().remove("MixPushClientId").apply();
                editor.commit();
                callback.onSuccess();
            }catch (Exception ex){
                callback.onFailure(1, ex.getMessage());
            }
        }
    }

    public void close()
    {
        Intent serviceIntent = new Intent(mContext, MetaMqttService.class);
        //mContext.unbindService(mqttServiceConnect);
        mContext.stopService(serviceIntent);
    }

    private static String getUUId(Context context) {
        String uuid = getUuidFromSharedPreferences(context);
        if (TextUtils.isEmpty(uuid)) {
            uuid = UUID.randomUUID().toString();
            saveUuidToSharedPreferences(context, uuid);
        }
        return uuid;
    }

    private static void saveUuidToSharedPreferences(Context context, String uuid) {
        if (context == null) {
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences("mixpush", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("MixPushClientId", uuid).apply();
        editor.commit();
    }

    private static String getUuidFromSharedPreferences(Context context) {
        if (context == null) {
            return "";
        }
        SharedPreferences preferences = context.getSharedPreferences("mixpush", Context.MODE_PRIVATE);
        String uuid = preferences.getString("MixPushClientId", "");
        return uuid;
    }

    // A helper method to shift a single character by a given offset
    private static char shift(char c, int offset) {
        // If c is not a letter, return it unchanged
        if (!Character.isLetter(c)) return c;
        // Convert c to uppercase and get its ASCII code
        int code = (int) Character.toUpperCase(c);
        // Shift the code by the offset and wrap around if necessary
        code = (code + offset - 65) % 26 + 65;
        // Convert the code back to a character and return it
        return (char) code;
    }

    // A method to encrypt a message using a variant Caesar cipher
    private static String encrypt(String message, int[] offsets) {
        // Initialize an empty string for the ciphertext
        String ciphertext = "";
        // Loop through each character in the message
        for (int i = 0; i < message.length(); i++) {
            // Get the corresponding offset from the offsets array
            int offset = offsets[i % offsets.length];
            // Shift the character by the offset and append it to the ciphertext
            ciphertext += shift(message.charAt(i), offset);
        }
        // Return the ciphertext
        return ciphertext;
    }

    // A method to decrypt a message using a variant Caesar cipher
    private static String decrypt(String ciphertext, int[] offsets) {
        // Initialize an empty string for the plaintext
        String plaintext = "";
        // Loop through each character in the ciphertext
        for (int i = 0; i < ciphertext.length(); i++) {
            // Get the corresponding offset from the offsets array
            int offset = offsets[i % offsets.length];
            // Shift the character by the negative offset and append it to the plaintext
            plaintext += shift(ciphertext.charAt(i), -offset);
        }
        // Return the plaintext
        return plaintext;
    }
}
