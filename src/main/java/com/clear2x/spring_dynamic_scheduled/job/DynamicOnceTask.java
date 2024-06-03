package com.clear2x.spring_dynamic_scheduled.job;

import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.Objects;

/**
 * @since 2024-05-31 10:26
 */
@Getter
@ToString(callSuper = true)
public class DynamicOnceTask extends DynamicTask {

    private final Instant startTime;

    public DynamicOnceTask(String taskId, Runnable runnable, Instant startTime) {
        super(taskId, runnable);
        this.startTime = startTime;
    }

    @Override
    protected boolean check() {
        return Objects.nonNull(startTime);
    }

}
