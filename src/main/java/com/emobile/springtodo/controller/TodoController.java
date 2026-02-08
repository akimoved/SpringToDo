package com.emobile.springtodo.controller;

import com.emobile.springtodo.api.TodoApi;
import com.emobile.springtodo.dto.request.CreateTodoRequest;
import com.emobile.springtodo.dto.request.UpdateTodoRequest;
import com.emobile.springtodo.dto.response.PageResponse;
import com.emobile.springtodo.dto.response.TodoResponse;
import com.emobile.springtodo.mapper.TodoMapper;
import com.emobile.springtodo.model.Todo;
import com.emobile.springtodo.service.TodoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/todos")
public class TodoController implements TodoApi {

    private static final Logger logger = LoggerFactory.getLogger(TodoController.class);

    private final TodoService todoService;
    private final TodoMapper todoMapper;

    @Autowired
    public TodoController(TodoService todoService, TodoMapper todoMapper) {
        this.todoService = todoService;
        this.todoMapper = todoMapper;
    }

    @Override
    @GetMapping
    public PageResponse<TodoResponse> getAllTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("Getting all todos - page: {}, size: {}", page, size);

        List<Todo> todos = todoService.findAll(page, size);
        List<TodoResponse> content = todoMapper.toResponseList(todos);
        long totalElements = todoService.getTotalCount();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        logger.info("Found {} todos, total elements: {}", todos.size(), totalElements);

        return new PageResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
    }

    @Override
    @GetMapping("/{id}")
    public TodoResponse getTodoById(@PathVariable Long id) {
        logger.info("Getting todo by id: {}", id);

        Todo todo = todoService.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Todo not found with id: {}", id);
                    return new RuntimeException("Todo not found with id: " + id);
                });

        logger.info("Found todo: {}", todo.getTitle());
        return todoMapper.toResponse(todo);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TodoResponse createTodo(@Valid @RequestBody CreateTodoRequest request) {
        Todo todo = todoMapper.toEntity(request);
        Todo savedTodo = todoService.create(todo);
        return todoMapper.toResponse(savedTodo);
    }

    @Override
    @PutMapping("/{id}")
    public TodoResponse updateTodo(@PathVariable Long id, @Valid @RequestBody UpdateTodoRequest request) {
        Todo existingTodo = todoService.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found with id: " + id));

        todoMapper.updateEntityFromRequest(request, existingTodo);
        Todo updatedTodo = todoService.update(existingTodo);

        return todoMapper.toResponse(updatedTodo);
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTodo(@PathVariable Long id) {
        if (!todoService.existsById(id)) {
            throw new RuntimeException("Todo not found with id: " + id);
        }
        todoService.deleteById(id);
    }

    @Override
    @GetMapping("/filter")
    public PageResponse<TodoResponse> getTodosByCompleted(
            @RequestParam boolean completed,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<Todo> todos = todoService.findByCompleted(completed, page, size);
        List<TodoResponse> content = todoMapper.toResponseList(todos);
        long totalElements = completed ? todoService.getCompletedCount() :
                todoService.getTotalCount() - todoService.getCompletedCount();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new PageResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
    }
}
