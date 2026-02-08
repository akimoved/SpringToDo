package com.emobile.springtodo.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class TodoMetricsService {

    private final Counter createdCounter;
    private final Counter completedCounter;
    private final Counter deletedCounter;

    public TodoMetricsService(MeterRegistry registry) {
        this.createdCounter = Counter.builder("todo.created")
                .description("Number of created todos")
                .register(registry);

        this.completedCounter = Counter.builder("todo.completed")
                .description("Number of completed todos")
                .register(registry);

        this.deletedCounter = Counter.builder("todo.deleted")
                .description("Number of deleted todos")
                .register(registry);
    }

    public void incrementCreatedCount() {
        createdCounter.increment();
    }

    public void incrementCompletedCount() {
        completedCounter.increment();
    }

    public void incrementDeletedCount() {
        deletedCounter.increment();
    }
}
