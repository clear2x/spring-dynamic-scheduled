package com.clear2x.spring_dynamic_scheduled.job;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

/**
 * @since 2024-05-31 10:26
 */

@Slf4j
public abstract class DynamicTask implements Runnable {

    @Getter
    private final String taskId;
    private TaskStatus status = TaskStatus.READY;
    @Getter
    private final Runnable runnable;
    @Setter
    @Getter
    private ScheduledFuture<?> future;

    public DynamicTask(String taskId, Runnable runnable) {
        this.taskId = taskId;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        log.info("run task: {}", taskId);
        this.status = TaskStatus.RUNNING;
        this.getRunnable().run();
        this.status = TaskStatus.READY;
    }

    public TaskStatus getStatus() {
        if (future == null) {
            return TaskStatus.NOT_EXIST;
        } else if (future.isCancelled()) {
            return TaskStatus.CANCELLED;
        } else if (future.isDone()) {
            return TaskStatus.DONE;
        } else {
            return status;
        }
    }

    public boolean valid() {
        return Objects.nonNull(taskId) && Objects.nonNull(status) && Objects.nonNull(runnable) && check();
    }

    protected abstract boolean check();

    @Override
    public String toString() {
        return "taskId='" + taskId + '\'' + ", status=" + status;
    }

    public enum TaskStatus {
        NOT_EXIST,
        RUNNING,
        READY,
        CANCELLED,
        DONE,
        ERROR,
        ;
    }

}
