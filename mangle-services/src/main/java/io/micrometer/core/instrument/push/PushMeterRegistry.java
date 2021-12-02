/*
 * Copyright (c) 2016-2019 VMware, Inc. All Rights Reserved.
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices
 * and license terms. Your use of these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package io.micrometer.core.instrument.push;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.lang.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author dbhat
 *
 */

public abstract class PushMeterRegistry extends MeterRegistry {
    private static final Logger logger = LoggerFactory.getLogger(PushMeterRegistry.class);
    private final PushRegistryConfig config;

    @Nullable
    private ScheduledExecutorService scheduledExecutorService;

    protected PushMeterRegistry(PushRegistryConfig config, Clock clock) {
        super(clock);
        this.config = config;
    }

    protected abstract void publish();

    /**
     * Catch uncaught exceptions thrown from {@link #publish()}.
     */
    private void publishSafely() {
        try {
            publish();
        } catch (Throwable e) {
            logger.warn("Unexpected exception thrown while publishing metrics for " + this.getClass().getSimpleName(),
                    e);
        }
    }

    /**
     * @deprecated Use {@link #start(ThreadFactory)} instead.
     */
    @Deprecated
    public final void start() {
        start(Executors.defaultThreadFactory());
    }

    public void start(ThreadFactory threadFactory) {
        if (scheduledExecutorService != null) {
            stop();
        }

        if (config.enabled()) {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(threadFactory);
            scheduledExecutorService.scheduleAtFixedRate(this::publishSafely, config.step().toMillis(),
                    config.step().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    public void stop() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
            scheduledExecutorService = null;
        }
    }

    @Override
    public void close() {
        if (config.enabled()) {
            publishSafely();
        }
        stop();
        super.close();

        stop();
    }
}
