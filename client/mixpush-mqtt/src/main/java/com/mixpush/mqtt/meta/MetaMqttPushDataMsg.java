package com.mixpush.mqtt.meta;

public class MetaMqttPushDataMsg {
    private Notification notification;
    private String msgId;
    private String data;
    private int type;

    public MetaMqttPushDataMsg() {
        this.notification = null;
        this.type = -1;
    }

    public Notification getNotification() {
        return this.notification;
    }

    public void setNotification(String title, String description) {
        this.notification = new Notification(title, description);
    }

    public String getMsgId() {
        return this.msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public class Notification
    {
        private String title;
        private String description;

        public Notification(String title, String description){
            this.title = title;
            this.description = description;
        }

        public String getTitle() {
            return this.title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return this.description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
