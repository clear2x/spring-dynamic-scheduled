package com.clear2x.spring_dynamic_scheduled;


import com.clear2x.spring_dynamic_scheduled.job.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author gengya.yuan
 * @since 2024-05-31 10:08
 */
@SpringBootTest
@Slf4j
public class DynamicSchedulingManagerTest {

    @Autowired
    private DynamicSchedulingManager dynamicSchedulingManager;

    @Test
    public void testCase() throws InterruptedException {
        dynamicSchedulingManager.add(new DynamicFixedDelayTask("test1", () -> {
            log.info("start DynamicFixedDelayTask....");
            try {
                TimeUnit.SECONDS.sleep(60);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("end DynamicFixedDelayTask....");
        }, Duration.ofSeconds(3)));

        Map<String, DynamicTask> tasks = dynamicSchedulingManager.tasks();

        TimeUnit.SECONDS.sleep(10);

        log.info("test1 : {}", dynamicSchedulingManager.remove("test1"));

        boolean result = dynamicSchedulingManager.remove("test1");
        log.info("remove task {}: {}", "test1", result);

        dynamicSchedulingManager.add(new DynamicFixedDelayTask("test2", () -> {
            log.info("start DynamicFixedDelayTask....");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("end DynamicFixedDelayTask....");
        }, Duration.ofSeconds(2)));

        System.out.println("==================");
        dynamicSchedulingManager.add(new DynamicOnceTask("test3", () -> {
            log.info("start DynamicOnceTask...");
        }, Instant.now().plusSeconds(30)));

        log.info("test3: {}", dynamicSchedulingManager.getTask("test3"));

        TimeUnit.HOURS.sleep(1);
    }

    @Test
    public void testCase2() throws InterruptedException {
        String taskId = "test1";
        dynamicSchedulingManager.add(new DynamicFixedDelayTask(taskId, () -> {
            log.info("start {}....", taskId);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("end {}....", taskId);
        }, Duration.ofSeconds(5)));

        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            dynamicSchedulingManager.remove(taskId);
        }).start();

        while (true) {
            dynamicSchedulingManager.tasks().forEach((k, v) -> {
                System.out.println(v);
            });
            TimeUnit.MILLISECONDS.sleep(500);
        }
    }

    @Test
    public void testCase3() throws InterruptedException {
        String taskId = "test1";
        dynamicSchedulingManager.add(new DynamicOnceTask(taskId, () -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, Instant.now().plusSeconds(10)));

        while (true) {
            System.out.println("========");
            dynamicSchedulingManager.tasks().forEach((k, v) -> {
                System.out.println(v);
            });
            System.out.println("========");
            TimeUnit.MILLISECONDS.sleep(500);
        }
    }

    @Test
    public void testCase4() throws InterruptedException {
        for (int i = 0; i < 100; i++) {

            if (i < 5) {
                dynamicSchedulingManager.add(new DynamicFixedDelayTask("DynamicFixedDelayTask-" + (i + 1), () -> {
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }, Duration.ofSeconds(i + 1)));
            }

            dynamicSchedulingManager.add(new DynamicOnceTask("DynamicOnceTask-" + (i + 1), () -> {
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, Instant.now().plusSeconds(i > 30 ? i / 10 : i)));

        }

        while (true) {
            log.info("当前任务：{}", dynamicSchedulingManager.tasks().size());
            TimeUnit.SECONDS.sleep(5);
        }
    }

    @Test
    public void testCase5() throws InterruptedException {
        log.info("start....");
        dynamicSchedulingManager.add(new DynamicCronTask("test1", () -> {
            try {
                TimeUnit.SECONDS.sleep(9);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "*/10 * * * * *"));

        TimeUnit.MINUTES.sleep(60);
    }


}
