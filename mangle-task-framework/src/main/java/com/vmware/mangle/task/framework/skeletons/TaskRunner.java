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

package com.vmware.mangle.task.framework.skeletons;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleTaskException;

/**
 * @author hkilari
 *
 * @param <T>
 */
public interface TaskRunner<T extends Task<? extends TaskSpec>> {

    /**
     * Starts the execution of a {@link AbstractTask}.
     *
     * @param task
     *            the task to start
     * @throws MangleTaskException
     *             when the task has a wrong state
     * @throws InterruptedException
     *             when the current thread got interrupted
     * @throws MangleException
     */
    void execute(final T task) throws InterruptedException, MangleException;

    /**
     * Cancel a running {@link AbstractTask}.
     *
     * @param task
     *            the task to start
     * @throws MangleTaskException
     *             when the task has a wrong state or doesn't belong to this
     *             {@link TaskRunner}-Instance
     */
    void cancel(final T task) throws MangleTaskException;

    /**
     * Test whether the given task is currently executed by this TaskRunner.
     *
     * @param task
     *            the task to test
     * @return true when the task is executing
     */
    boolean isExecuting(final T task);

    /**
     * Test whether the given task is already completed by this TaskRunner.
     *
     * @param task
     *            the task to test
     * @return true when the task is completed
     */
    boolean isComplete(final T task);

    /**
     * Test whether the given task is currently executing or is already finished.
     *
     * @param task
     *            the task to test
     * @return true when the task is executing or completed
     */
    boolean hasStarted(final T task);

    /**
     * Return the number of currently processed tasks.
     *
     * @return the number of currently processed tasks
     */
    int getNumberOfExecutingTasks();

    /**
     * Wait for a collection of tasks.
     *
     * @param tasks
     *            the iterator for the tasks
     * @throws InterruptedException
     *             when the current thread got interrupted while waiting
     * @throws MangleTaskException
     *             when a task was in a wrong state
     */
    void join(final Iterable<T> tasks) throws InterruptedException, MangleTaskException;

    /**
     * Wait for one task to be completed.
     *
     * @param task
     *            the task to wait for
     * @throws InterruptedException
     *             when the current thread got interrupted while waiting
     * @throws MangleTaskException
     *             when a task was in a wrong state
     */
    void join(final T task) throws InterruptedException, MangleTaskException;


    /**
     * Wait for all tasks to be completed.
     *
     * @throws InterruptedException
     *             when the current thread got interrupted while waiting
     * @throws MangleTaskException
     *             when a task was in a wrong state
     */
    void join() throws InterruptedException, MangleTaskException;

    /**
     * Disposes this task runenr
     */
    void dispose() throws MangleTaskException, InterruptedException;
}
