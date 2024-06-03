package com.clear2x.spring_dynamic_scheduled.job;

import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * @since 2024-05-31 10:26
 */
@Getter
@ToString(callSuper = true)
public class DynamicFixedDelayTask extends DynamicTask {

    private final Instant startTime;
    private final Duration duration;

    public DynamicFixedDelayTask(String taskId, Runnable runnable, Instant startTime, Duration duration) {
        super(taskId, runnable);
        this.startTime = startTime;
        this.duration = duration;
    }

    public DynamicFixedDelayTask(String taskId, Runnable runnable, Duration duration) {
        super(taskId, runnable);
        this.startTime = Instant.now();
        this.duration = duration;
    }

    @Override
    protected boolean check() {
        return Objects.nonNull(startTime) && Objects.nonNull(duration);
    }

}
