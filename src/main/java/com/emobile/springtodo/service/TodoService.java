package com.emobile.springtodo.service;

import com.emobile.springtodo.model.Todo;
import com.emobile.springtodo.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@CacheConfig(cacheNames = "todos")
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoMetricsService metricsService;

    @Autowired
    public TodoService(TodoRepository todoRepository, TodoMetricsService metricsService) {
        this.todoRepository = todoRepository;
        this.metricsService = metricsService;
    }

    @CacheEvict(allEntries = true)
    public Todo create(Todo todo) {
        Todo savedTodo = todoRepository.save(todo);
        metricsService.incrementCreatedCount();
        return savedTodo;
    }

    @Cacheable(key = "#id")
    public Optional<Todo> findById(Long id) {
        return todoRepository.findById(id);
    }

    @Cacheable(key = "'all'")
    public List<Todo> findAll(int page, int size) {
        int offset = page * size;
        return todoRepository.findAll(size, offset);
    }

    @Cacheable(key = "'completed_' + #completed")
    public List<Todo> findByCompleted(boolean completed, int page, int size) {
        int offset = page * size;
        return todoRepository.findByCompleted(completed, size, offset);
    }

    @Caching(
            put = @CachePut(key = "#todo.id"),
            evict = {
                    @CacheEvict(key = "'all'"),
                    @CacheEvict(key = "'completed_' + #todo.completed"),
                    @CacheEvict(key = "'completed_' + !#todo.completed")
            }
    )
    public Todo update(Todo todo) {
        Todo updatedTodo = todoRepository.save(todo);
        if (Boolean.TRUE.equals(updatedTodo.getCompleted())) {
            metricsService.incrementCompletedCount();
        }
        return updatedTodo;
    }

    @Caching(
            evict = {
                    @CacheEvict(key = "#id"),
                    @CacheEvict(key = "'all'"),
                    @CacheEvict(key = "'completed_true'"),
                    @CacheEvict(key = "'completed_false'")
            }
    )
    public void deleteById(Long id) {
        todoRepository.deleteById(id);
        metricsService.incrementDeletedCount();
    }

    @Cacheable(key = "'count'")
    public long getTotalCount() {
        return todoRepository.count();
    }

    @Cacheable(key = "'completed_count'")
    public long getCompletedCount() {
        return todoRepository.countByCompleted(true);
    }

    public boolean existsById(Long id) {
        return todoRepository.existsById(id);
    }
}
