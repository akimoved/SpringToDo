package com.emobile.springtodo.integration;


import com.emobile.springtodo.AbstractIntegrationTest;
import com.emobile.springtodo.dto.request.CreateTodoRequest;
import com.emobile.springtodo.dto.request.UpdateTodoRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Todo Controller Integration Tests")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
class TodoControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Should get all todos with pagination")
    @Sql(scripts = "/sql/insert-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void shouldGetAllTodosWithPagination() throws Exception {
        mockMvc.perform(get("/api/v1/todos")
                        .param("page", "0")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(2)))
                .andExpect(jsonPath("$.totalElements", is(4)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.last", is(false)));
    }

    @Test
    @DisplayName("Should get todo by id successfully")
    @Sql(scripts = "/sql/insert-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void shouldGetTodoByIdSuccessfully() throws Exception {
        mockMvc.perform(get("/api/v1/todos/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Todo 1")))
                .andExpect(jsonPath("$.description", is("Description 1")))
                .andExpect(jsonPath("$.completed", is(false)))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));
    }

    @Test
    @DisplayName("Should return 404 when todo not found")
    void shouldReturn404WhenTodoNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/todos/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("Should create todo successfully")
    void shouldCreateTodoSuccessfully() throws Exception {
        CreateTodoRequest request = new CreateTodoRequest();
        request.setTitle("New Todo");
        request.setDescription("New Description");

        mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("New Todo")))
                .andExpect(jsonPath("$.description", is("New Description")))
                .andExpect(jsonPath("$.completed", is(false)))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));
    }

    @Test
    @DisplayName("Should return validation error for invalid create request")
    void shouldReturnValidationErrorForInvalidCreateRequest() throws Exception {
        CreateTodoRequest request = new CreateTodoRequest();
        request.setTitle(""); // Invalid empty title
        request.setDescription("A".repeat(1001)); // Too long description

        mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.message", containsString("Title is required")))
                .andExpect(jsonPath("$.message", containsString("must not exceed 1000 characters")));
    }

    @Test
    @DisplayName("Should update todo successfully")
    @Sql(scripts = "/sql/insert-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void shouldUpdateTodoSuccessfully() throws Exception {
        UpdateTodoRequest request = new UpdateTodoRequest();
        request.setTitle("Updated Title");
        request.setDescription("Updated Description");
        request.setCompleted(true);

        mockMvc.perform(put("/api/v1/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Updated Title")))
                .andExpect(jsonPath("$.description", is("Updated Description")))
                .andExpect(jsonPath("$.completed", is(true)))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent todo")
    void shouldReturn404WhenUpdatingNonExistentTodo() throws Exception {
        UpdateTodoRequest request = new UpdateTodoRequest();
        request.setTitle("Updated Title");

        mockMvc.perform(put("/api/v1/todos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("Should delete todo successfully")
    @Sql(scripts = "/sql/insert-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void shouldDeleteTodoSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/v1/todos/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/todos/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent todo")
    void shouldReturn404WhenDeletingNonExistentTodo() throws Exception {
        mockMvc.perform(delete("/api/v1/todos/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Todo not found")));
    }

    @Test
    @DisplayName("Should get todos by completion status")
    @Sql(scripts = "/sql/insert-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
    void shouldGetTodosByCompletionStatus() throws Exception {
        mockMvc.perform(get("/api/v1/todos/filter")
                        .param("completed", "true")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].completed", is(true)))
                .andExpect(jsonPath("$.content[1].completed", is(true)));

        mockMvc.perform(get("/api/v1/todos/filter")
                        .param("completed", "false")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].completed", is(false)))
                .andExpect(jsonPath("$.content[1].completed", is(false)));
    }

    @Test
    @DisplayName("Should validate title length boundaries")
    void shouldValidateTitleLengthBoundaries() throws Exception {
        CreateTodoRequest request = new CreateTodoRequest();
        request.setTitle("A".repeat(256)); // 256 characters - exceeds limit
        request.setDescription("Valid description");

        mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.message", containsString("255 characters")));
    }
}