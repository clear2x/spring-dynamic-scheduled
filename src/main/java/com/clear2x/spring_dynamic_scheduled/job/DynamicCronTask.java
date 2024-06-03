package com.clear2x.spring_dynamic_scheduled.job;

import lombok.Getter;
import lombok.ToString;
import org.springframework.scheduling.support.CronTrigger;

import java.util.Objects;

/**
 * @since 2024-05-31 10:26
 */
@Getter
@ToString(callSuper = true)
public class DynamicCronTask extends DynamicTask {

    private final CronTrigger cron;

    public DynamicCronTask(String taskId, Runnable runnable, String cron) {
        super(taskId, runnable);
        this.cron = new CronTrigger(cron);
    }

    @Override
    protected boolean check() {
        return Objects.nonNull(cron);
    }

}
