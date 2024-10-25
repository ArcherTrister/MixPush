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
package com.honor.push.messaging;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.honor.push.exception.HonorMesssagingException;
import com.honor.push.message.Message;
import com.honor.push.message.TopicMessage;
import com.honor.push.model.TopicOperation;
import com.honor.push.reponse.SendResponse;
import com.honor.push.util.ValidatorUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the entrance for all server-side HCM actions.
 *
 * <p>You can get a instance of {@link com.honor.push.messaging.HonorMessaging}
 * by a instance of {@link com.honor.push.messaging.HonorApp}, and then use it to send a message
 */
public class HonorMessaging {
    private static final Logger logger = LoggerFactory.getLogger(HonorMessaging.class);

    static final String INTERNAL_ERROR = "internal error";

    static final String UNKNOWN_ERROR = "unknown error";

    static final String KNOWN_ERROR = "known error";

    private final HonorApp app;
    private final Supplier<? extends HonorMessageClient> messagingClient;

    private HonorMessaging(Builder builder) {
        this.app = builder.app;
        this.messagingClient = Suppliers.memoize(builder.messagingClient);
    }

    /**
     * Gets the {@link HonorMessaging} instance for the specified {@link HonorApp}.
     *
     * @return The {@link HonorMessaging} instance for the specified {@link HonorApp}.
     */
    public static synchronized HonorMessaging getInstance(HonorApp app) {
        HonorMessagingService service = ImplHonorTrampolines.getService(app, SERVICE_ID, HonorMessagingService.class);
        if (service == null) {
            service = ImplHonorTrampolines.addService(app, new HonorMessagingService(app));
        }
        return service.getInstance();
    }

    private static HonorMessaging fromApp(final HonorApp app) {
        return HonorMessaging.builder()
                .setApp(app)
                .setMessagingClient(() -> HonorMessageClientImpl.fromApp(app))
                .build();
    }

    HonorMessageClient getMessagingClient() {
        return messagingClient.get();
    }

    /**
     * Sends the given {@link Message} via HCM.
     *
     * @param message A non-null {@link Message} to be sent.
     * @return {@link SendResponse}.
     * @throws HonorMesssagingException If an error occurs while handing the message off to HCM for
     *                                   delivery.
     */
    public SendResponse sendMessage(Message message) throws HonorMesssagingException {
        return sendMessage(message, false);
    }

    /**
     * @param topicMessage topicmessage
     * @return topic subscribe response
     * @throws HonorMesssagingException
     */
    public SendResponse subscribeTopic(TopicMessage topicMessage) throws HonorMesssagingException {
        final HonorMessageClient messagingClient = getMessagingClient();
        return messagingClient.send(topicMessage, TopicOperation.SUBSCRIBE.getValue(), ImplHonorTrampolines.getAccessToken(app));
    }

    /**
     * @param topicMessage topic Message
     * @return topic unsubscribe response
     * @throws HonorMesssagingException
     */
    public SendResponse unsubscribeTopic(TopicMessage topicMessage) throws HonorMesssagingException {
        final HonorMessageClient messagingClient = getMessagingClient();
        return messagingClient.send(topicMessage, TopicOperation.UNSUBSCRIBE.getValue(), ImplHonorTrampolines.getAccessToken(app));
    }

    /**
     * @param topicMessage topic Message
     * @return topic list
     * @throws HonorMesssagingException
     */
    public SendResponse listTopic(TopicMessage topicMessage) throws HonorMesssagingException {
        final HonorMessageClient messagingClient = getMessagingClient();
        return messagingClient.send(topicMessage, TopicOperation.LIST.getValue(), ImplHonorTrampolines.getAccessToken(app));
    }


    /**
     * Sends message {@link Message}
     *
     * <p>If the {@code validateOnly} option is set to true, the message will not be actually sent. Instead
     * HCM performs all the necessary validations, and emulates the send operation.
     *
     * @param message      message {@link Message} to be sent.
     * @param validateOnly a boolean indicating whether to send message for test or not.
     * @return {@link SendResponse}.
     * @throws HonorMesssagingException exception.
     */
    public SendResponse sendMessage(Message message, boolean validateOnly) throws HonorMesssagingException {
        ValidatorUtils.checkArgument(message != null, "message must not be null");
        final HonorMessageClient messagingClient = getMessagingClient();
        return messagingClient.send(message, validateOnly, ImplHonorTrampolines.getAccessToken(app));
    }

    /**
     * HonorMessagingService
     */
    private static final String SERVICE_ID = HonorMessaging.class.getName();

    private static class HonorMessagingService extends HonorService<HonorMessaging> {

        HonorMessagingService(HonorApp app) {
            super(SERVICE_ID, HonorMessaging.fromApp(app));
        }

        @Override
        public void destroy() {

        }
    }

    /**
     * Builder for constructing {@link HonorMessaging}.
     */
    static Builder builder() {
        return new Builder();
    }

    static class Builder {
        private HonorApp app;
        private Supplier<? extends HonorMessageClient> messagingClient;

        private Builder() {
        }

        public Builder setApp(HonorApp app) {
            this.app = app;
            return this;
        }

        public Builder setMessagingClient(Supplier<? extends HonorMessageClient> messagingClient) {
            this.messagingClient = messagingClient;
            return this;
        }

        public HonorMessaging build() {
            return new HonorMessaging(this);
        }
    }
}
