package com.matiasugluck.deremate_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EnableAsync
@EnableScheduling
@Configuration
public class AsynExecutorConfig implements AsyncConfigurer, SchedulingConfigurer {
    @Override
    public Executor getAsyncExecutor() {
        return Executors.newCachedThreadPool();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ExecutorService executor = Executors.newScheduledThreadPool(5);
        taskRegistrar.setScheduler(executor);
    }
}
