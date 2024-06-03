package com.clear2x.spring_dynamic_scheduled.job;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
@Slf4j
public class DynamicSchedulingManager implements SchedulingConfigurer {

    private final Map<String, DynamicTask> tasks = new ConcurrentHashMap<>();
    @Getter
    private ScheduledTaskRegistrar taskRegistrar;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        this.taskRegistrar = taskRegistrar;
        taskRegistrar.setScheduler(new ScheduledThreadPoolExecutor(10, new ThreadPoolExecutor.CallerRunsPolicy()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                if (t != null) {
                    log.error("Task execution error: ", t);
                }
                if (r instanceof ScheduledFuture) {
                    ScheduledFuture<?> future = (ScheduledFuture<?>) r;
                    if (future.isDone() || future.isCancelled()) {
                        remove(r);
                        tasks.entrySet().removeIf(entry -> {
                            boolean match = entry.getValue().getFuture() == future;
                            if (match) {
                                log.info("remove task: {}", entry.getValue());
                            }
                            return match;
                        });
                    }
                }
            }
        });
    }

    public boolean add(DynamicTask dynamicTask) {
        if (Objects.isNull(dynamicTask)) {
            return false;
        }
        if (!dynamicTask.valid()) {
            return false;
        }
        if (tasks.containsKey(dynamicTask.getTaskId())) {
            return false;
        }
        ScheduledFuture<?> future = null;
        if (dynamicTask instanceof DynamicCronTask) {
            future = Objects.requireNonNull(taskRegistrar.getScheduler()).schedule(dynamicTask, ((DynamicCronTask) dynamicTask).getCron());
        } else if (dynamicTask instanceof DynamicFixedDelayTask) {
            DynamicFixedDelayTask dynamicFixedDelayTask = (DynamicFixedDelayTask) dynamicTask;
            future = Objects.requireNonNull(taskRegistrar.getScheduler()).scheduleWithFixedDelay(dynamicTask, dynamicFixedDelayTask.getStartTime(), dynamicFixedDelayTask.getDuration());
        } else if (dynamicTask instanceof DynamicFixedRateTask) {
            DynamicFixedRateTask dynamicFixedRateTask = (DynamicFixedRateTask) dynamicTask;
            future = Objects.requireNonNull(taskRegistrar.getScheduler()).scheduleAtFixedRate(dynamicTask, dynamicFixedRateTask.getStartTime(), dynamicFixedRateTask.getPeriod());
        } else if (dynamicTask instanceof DynamicOnceTask) {
            future = Objects.requireNonNull(taskRegistrar.getScheduler()).schedule(dynamicTask, ((DynamicOnceTask) dynamicTask).getStartTime());
        }
        if (Objects.isNull(future)) {
            return false;
        }
        dynamicTask.setFuture(future);
        tasks.put(dynamicTask.getTaskId(), dynamicTask);
        return true;
    }

    public boolean remove(String taskId) {
        if (Objects.isNull(taskId)) {
            return false;
        }
        DynamicTask dynamicTask = tasks.remove(taskId);
        if (dynamicTask == null) {
            return true;
        }
        ScheduledFuture<?> future = dynamicTask.getFuture();
        if (future != null) {
            boolean cancel = future.cancel(false);
            ConcurrentTaskScheduler scheduler = (ConcurrentTaskScheduler) this.getTaskRegistrar().getScheduler();
            assert scheduler != null;
            ScheduledThreadPoolExecutor concurrentExecutor = (ScheduledThreadPoolExecutor) scheduler.getConcurrentExecutor();
            if (future instanceof Runnable) {
                concurrentExecutor.remove((Runnable) future);
            }
            return cancel;
        }
        return true;
    }

    public boolean update(DynamicTask dynamicTask) {
        if (Objects.isNull(dynamicTask)) {
            return false;
        }
        if (!dynamicTask.valid()) {
            return false;
        }
        remove(dynamicTask.getTaskId());
        add(dynamicTask);
        return true;
    }

    public Map<String, DynamicTask> tasks() {
        return Collections.unmodifiableMap(tasks);
    }

    public Optional<DynamicTask> getTask(String taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }

}
