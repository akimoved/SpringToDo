package com.emobile.springtodo.api;

import com.emobile.springtodo.dto.request.CreateTodoRequest;
import com.emobile.springtodo.dto.request.UpdateTodoRequest;
import com.emobile.springtodo.dto.response.PageResponse;
import com.emobile.springtodo.dto.response.TodoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Todo Management", description = "APIs for managing todo items")
public interface TodoApi {

    @Operation(summary = "Get all todos", description = "Retrieve a paginated list of all todos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved todos"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    PageResponse<TodoResponse> getAllTodos(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size
    );

    @Operation(summary = "Get todo by ID", description = "Retrieve a specific todo by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved todo"),
            @ApiResponse(responseCode = "404", description = "Todo not found")
    })
    TodoResponse getTodoById(@Parameter(description = "Todo ID") @PathVariable Long id);

    @Operation(summary = "Create a new todo", description = "Create a new todo item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created todo"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    TodoResponse createTodo(@Valid @RequestBody CreateTodoRequest request);

    @Operation(summary = "Update a todo", description = "Update an existing todo item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated todo"),
            @ApiResponse(responseCode = "404", description = "Todo not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    TodoResponse updateTodo(
            @Parameter(description = "Todo ID") @PathVariable Long id,
            @Valid @RequestBody UpdateTodoRequest request
    );

    @Operation(summary = "Delete a todo", description = "Delete a todo item by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted todo"),
            @ApiResponse(responseCode = "404", description = "Todo not found")
    })
    void deleteTodo(@Parameter(description = "Todo ID") @PathVariable Long id);

    @Operation(summary = "Get todos by completion status", description = "Retrieve paginated todos filtered by completion status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved todos"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    PageResponse<TodoResponse> getTodosByCompleted(
            @Parameter(description = "Completion status") @RequestParam boolean completed,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size
    );
}
