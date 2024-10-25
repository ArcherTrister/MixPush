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

 *  2019.12.15-changed method getHonorExecutors
 *  2019.12.15-changed method releaseHonorExecutors
 *  2019.12.15-changed method getExecutor
 *  2019.12.15-changed method releaseExecutor
 *                  Honor Technologies Co., Ltd.
 *
 */
package com.honor.push.messaging;

import com.honor.push.util.ValidatorUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * An interface that controls the thread pools and thread factories used by the HCM SDK. Each
 * instance of HonorApp uses an implementation of this interface to create and manage
 * threads.
 */
public abstract class ThreadManager {
    final HonorExecutors getHonorExecutors(HonorApp app) {
        return new HonorExecutors(getExecutor(app));
    }

    final void releaseHonorExecutors(HonorApp app, HonorExecutors executor) {
        releaseExecutor(app, executor.userExecutor);
    }

    /**
     * Returns the main thread pool for an app.
     *
     * @param app A {@link HonorApp} instance.
     * @return A non-null <code>ExecutorService</code> instance.
     */
    protected abstract ExecutorService getExecutor(HonorApp app);

    /**
     * Cleans up the thread pool associated with an app.
     * This method is invoked when an app is deleted.
     *
     * @param app A {@link HonorApp} instance.
     * @return A non-null <code>ExecutorService</code> instance.
     */
    protected abstract void releaseExecutor(HonorApp app, ExecutorService executor);

    /**
     * Returns the <code>ThreadFactory</code> to be used for creating long-lived threads. This is
     * used mainly to create the long-lived worker threads for the scheduled (periodic) tasks started by the SDK.
     * The SDK guarantees clean termination of all threads started via this <code>ThreadFactory</code>, when the user
     * calls {@link HonorApp#delete()}.
     *
     * <p>If long-lived threads cannot be supported in the current runtime, this method may
     * throw a {@code RuntimeException}.
     *
     * @return A non-null <code>ThreadFactory</code>.
     */
    protected abstract ThreadFactory getThreadFactory();

    static final class HonorExecutors {
        private ExecutorService userExecutor;

        private HonorExecutors(ExecutorService userExecutor) {
            ValidatorUtils.checkArgument(userExecutor != null, "ExecutorService must not be null");
            this.userExecutor = userExecutor;
        }
    }
}
