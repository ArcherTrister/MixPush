/* Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 *  2019.12.15-Changed constructor HonorApp
 *  2019.12.15-Changed method initializeApp
 *                  Honor Technologies Co., Ltd.
 *
 */
package com.honor.push.messaging;

import com.google.common.collect.ImmutableList;
import com.honor.push.util.ValidatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The entry point of Honor Java SDK.
 * <p>{@link HonorApp#initializeApp(HonorOption)} initializes the app instance.
 */
public class HonorApp {
    private static final Logger logger = LoggerFactory.getLogger(HonorApp.class);

    private String appId;
    private HonorOption option;

    /** Global lock */
    private static final Object appsLock = new Object();

    /** Store a map of <appId, HonorApp> */
    private static final Map<String, HonorApp> instances = new HashMap<>();

    /** HonorMessaging can be added in the pattern of service, whcih is designed for Scalability */
    private final Map<String, HonorService> services = new HashMap<>();

    private TokenRefresher tokenRefresher;

    private volatile ScheduledExecutorService scheduledExecutor;

    private ThreadManager threadManager;

    private ThreadManager.HonorExecutors executors;

    private final AtomicBoolean deleted = new AtomicBoolean();

    /** lock for synchronizing all internal HonorApp state changes */
    private final Object lock = new Object();

    private HonorApp(HonorOption option) {
        ValidatorUtils.checkArgument(option != null, "HonorOption must not be null");
        this.option = option;
        this.appId = option.getCredential().getAppId();
        this.tokenRefresher = new TokenRefresher(this);
        this.threadManager = option.getThreadManager();
        this.executors = threadManager.getHonorExecutors(this);
    }

    public HonorOption getOption() {
        return option;
    }

    public String getAppId() {
        return appId;
    }

    /**
     * Returns the instance identified by the unique appId, or throws if it does not exist.
     *
     * @param appId represents the id of the {@link HonorApp} instance.
     * @return the {@link HonorApp} corresponding to the id.
     * @throws IllegalStateException if the {@link HonorApp} was not initialized, either via {@link
     *     #initializeApp(HonorOption)} or {@link #getApps()}.
     */
    public static HonorApp getInstance(HonorOption option) {
        String appId = option.getCredential().getAppId();
        synchronized (appsLock) {
            HonorApp app = instances.get(appId);
            if (app != null) {
                return app;
            }
//            String errorMessage = MessageFormat.format("HonorApp with id {0} doesn't exist", appId);
//            throw new IllegalStateException(errorMessage);
            return initializeApp(option);
        }
    }

    /**
     * Initializes the {@link HonorApp} instance using the given option.
     *
     * @throws IllegalStateException if the app instance has already been initialized.
     */
    public static HonorApp initializeApp(HonorOption option) {
        String appId = option.getCredential().getAppId();
        final HonorApp app;
        synchronized (appsLock) {
            if (!instances.containsKey(appId)) {
                ValidatorUtils.checkState(!instances.containsKey(appId), "HonorApp with id " + appId + " already exists!");
                app = new HonorApp(option);
                instances.put(appId, app);
                app.startTokenRefresher();
            } else {
                app = getInstance(option);
            }

        }
        return app;
    }

    /** Returns a list of all HonorApps. */
    public static List<HonorApp> getApps() {
        synchronized (appsLock) {
            return ImmutableList.copyOf(instances.values());
        }
    }

    /**
     * Get all appIds which are be stored in the instances
     */
    public static List<String> getAllAppIds() {
        Set<String> allAppIds = new HashSet<>();
        synchronized (appsLock) {
            for (HonorApp app : instances.values()) {
                allAppIds.add(app.getAppId());
            }
        }
        List<String> sortedIdList = new ArrayList<>(allAppIds);
        Collections.sort(sortedIdList);
        return sortedIdList;
    }

    /**
     * Deletes the {@link HonorApp} and all its data. All calls to this {@link HonorApp}
     * instance will throw once it has been called.
     *
     * <p>A no-op if delete was called before.
     */
    public void delete() {
        synchronized (lock) {
            boolean valueChanged = deleted.compareAndSet(false /* expected */, true);
            if (!valueChanged) {
                return;
            }

            try {
                this.getOption().getHttpClient().close();
                this.getOption().getCredential().getHttpClient().close();
            } catch (IOException e) {
                logger.debug("Fail to close httpClient");
            }

            for (HonorService service : services.values()) {
                service.destroy();
            }
            services.clear();
            tokenRefresher.stop();

            threadManager.releaseHonorExecutors(this, executors);
            if (scheduledExecutor != null) {
                scheduledExecutor.shutdown();
                scheduledExecutor = null;
            }
        }

        synchronized (appsLock) {
            instances.remove(this.getAppId());
        }
    }

    /**
     * Check the app is not deleted, whcic is the premisi of some methods
     */
    private void checkNotDeleted() {
        String errorMessage = MessageFormat.format("HonorApp with id {0} was deleted", getAppId());
        ValidatorUtils.checkState(!deleted.get(), errorMessage);
    }

    /**
     * Singleton mode, ensure the scheduleExecutor is singleton
     */
    private ScheduledExecutorService singleScheduledExecutorService() {
        if (scheduledExecutor == null) {
            synchronized (lock) {
                checkNotDeleted();
                if (scheduledExecutor == null) {
                    scheduledExecutor = new HonorScheduledExecutor(getThreadFactory(), "honor-scheduled-worker");
                }
            }
        }
        return scheduledExecutor;
    }

    public ThreadFactory getThreadFactory() {
        return threadManager.getThreadFactory();
    }

    private ScheduledExecutorService getScheduledExecutorService() {
        return singleScheduledExecutorService();
    }

    ScheduledFuture<?> schedule(Runnable runnable, long initialDelay, long period) {
        return getScheduledExecutorService().scheduleWithFixedDelay(runnable, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    /**
     * Add service to the app, such as HonorMessaging, other services can be added if needed
     */
    void addService(HonorService service) {
        synchronized (lock) {
            checkNotDeleted();
            ValidatorUtils.checkArgument(!services.containsKey(service.getId()), "service already exists");
            services.put(service.getId(), service);
        }
    }

    HonorService getService(String id) {
        synchronized (lock) {
            return services.get(id);
        }
    }

    /**
     * Start the scheduled task for refreshing token automatically
     */
    public void startTokenRefresher() {
        synchronized (lock) {
            checkNotDeleted();
            tokenRefresher.start();
        }
    }

    /** It is just for test */
    public static void clearInstancesForTest() {
        synchronized (appsLock) {
            //copy before delete
            for (HonorApp app : ImmutableList.copyOf(instances.values())) {
                app.delete();
            }
            instances.clear();
        }
    }
}
