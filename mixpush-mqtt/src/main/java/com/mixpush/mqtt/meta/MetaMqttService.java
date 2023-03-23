package com.mixpush.mqtt.meta;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.mixpush.mqtt.UnifiedMqttMessageService.TAG;

/**
 * @author coder ArcherTrister
 * @time 2022/03/09
 * <p>
 * META MQTT BY ArcherTrister
 */
public abstract class MetaMqttService extends Service {
    private MqttClient mqttClient;
    private MqttConnectOptions options;
    private ScheduledExecutorService scheduler;
//    // Service内部回调
//    private MetaMqttCallBack mCallBack;

    private String serverUrl = "";
    private String clientId = "";
    private String username = "";
    private String password = "";
    private Integer timeout = 10;
    private Integer beatTime = 20;
    private String topic = "";
    private Integer reConnectTime = 10;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // 部分信息
        serverUrl = intent.getStringExtra("serverUrl");
        clientId = intent.getStringExtra("clientId");
        username = intent.getStringExtra("username");
        password = intent.getStringExtra("password");
        //topic = intent.getStringExtra("topic");
        topic = "push/message/"+clientId+"/inbox";
        //timeout = intent.getIntExtra("timeout", 10);
        //beatTime = intent.getIntExtra("beatTime", 20);
        //reConnectTime = intent.getIntExtra("reConnectTime", 10);
        return new MsgBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 部分信息
        serverUrl = intent.getStringExtra("serverUrl");
        clientId = intent.getStringExtra("clientId");
        username = intent.getStringExtra("username");
        password = intent.getStringExtra("password");
        //topic = intent.getStringExtra("topic");
        topic = "push/message/"+clientId+"/inbox";
        //timeout = intent.getIntExtra("timeout", 10);
        //beatTime = intent.getIntExtra("beatTime", 20);
        //reConnectTime = intent.getIntExtra("reConnectTime", 10);
        // 连接mqtt服务
        scheduler = Executors.newSingleThreadScheduledExecutor();
        // 延迟执行连接到mqtt，因为callback还没准备好
        scheduler.schedule(this::ToConnectMqtt, 1000, TimeUnit.MICROSECONDS);
        return super.onStartCommand(intent, flags, startId);
    }

    public class MsgBinder extends Binder {
        // binder内部回调
        public MetaMqttService getService() {
            return MetaMqttService.this;
        }
    }

    public void onPassThroughMessageReceived(MetaMqttPushDataMsg message){}

    public void onPushMessageReceived(MetaMqttPushDataMsg message){
        //TODO: 本地通知
    }

//    public void onNewToken(String token) {
//
//    }

    public void pushComplete(){}

//    /**
//     * 申请token失败回调方法。
//     */
//    public void onTokenError(Exception exception) {
//        Log.e(TAG, "申请token失败", exception);
//    }


    private void ToConnectMqtt() {
        Log.e(TAG, "ToConnect");
        try {
            // 创建mqttClient
            mqttClient = new MqttClient(serverUrl, clientId, new MemoryPersistence());
            // 初始化配置
            options = new MqttConnectOptions();
            options.setCleanSession(false);
            options.setUserName(username);
            //遗嘱消息
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    options.setWill("died/", ("mqtt died at time -- " + TimeUtils.getNowString()).getBytes(StandardCharsets.UTF_8), 1, false);
//                }
            options.setPassword(password.toCharArray());
            options.setConnectionTimeout(timeout);
            options.setKeepAliveInterval(beatTime);
            // 设置接收监听
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "连接丢失");
                    // 为减少系统轮询开销，应该在连接丢失时开启重连机制，等连接成功后再关闭重连机制
                    //mCallBack.connectLost();
                    startConnectMachine();
                }

                @Override
                public void messageArrived(String arriveTopic, MqttMessage message) {
                    // 消息收到,自己的消息直接return
                    //Log.e(TAG, "收到MQTT消息" + arriveTopic);
                    //message.getId()
                    //message.getPayload();
                    //message.toString();
                    Log.e(TAG, "收到MQTT消息" + message.toString());
                    // mCallBack.messageArrived(arriveTopic, message);
                    try {
                        MetaMqttPushDataMsg msg = new Gson().fromJson(message.toString(), MetaMqttPushDataMsg.class);
                        if(msg.getType() == 0)
                        {
                            onPassThroughMessageReceived(new MetaMqttPushDataMsg());
                        }else if(msg.getType() == 1){
                            //TYPE_MSG_NOTIFICATION
                            onPushMessageReceived(new MetaMqttPushDataMsg());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "转发消息失败" + e.getMessage());
                        e.printStackTrace();
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    pushComplete();
                }
            });
            // 连接mqtt
            mqttClient.connect(options);
            // 订阅
            mqttClient.subscribe(topic, 1);
            Log.e(TAG, "连接MQTT成功");
            // 连接成功后将client返回，方便activity层面可以发消息
            //mCallBack.connectSuccess(mqttClient);
            // 连接成功停止重连机制
            stopConnectMachine();
        } catch (MqttException e) {
            e.printStackTrace();
            Log.e(TAG, "connect failed" + e.toString());
            //mCallBack.connectFailed(e);
            // 开启重连机制
            startConnectMachine();
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "connect failed" + e.toString());
            //mCallBack.connectFailed(e);
            // 开启重连机制
            // startConnectMachine();
        }
    }

    /**
     * 开启重连机制
     */
    public void startConnectMachine() {
        // 每10S重连一次  开启重连机制
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(() -> {
            if (mqttClient != null) {
                if (!mqttClient.isConnected()) {
                    try {
                        mqttClient.connect(options);
                        mqttClient.subscribe(topic);
                        // 连接成功
                        Log.e(TAG, "重连MQTT成功");
                        //mCallBack.reConnectSuccess();
                        stopConnectMachine();
                    } catch (Exception e) {
                        e.printStackTrace();
                        // 连接失败
                        Log.e(TAG, "重连MQTT失败");
                        //mCallBack.reConnectFailed();
                    }
                } else {
                    Log.e(TAG, "检测MQTT正常");
                }
            } else {
                Log.e(TAG, "Client为空");
                //mCallBack.connectClientError();
            }
        }, 0, reConnectTime * 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * 关闭重连机制
     */
    public void stopConnectMachine() {
        if (scheduler != null) {
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        }
    }
}
