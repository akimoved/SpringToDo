package com.emobile.springtodo.integration;

import com.emobile.springtodo.AbstractIntegrationTest;
import com.emobile.springtodo.dto.request.CreateTodoRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Actuator Metrics Integration Tests")
class ActuatorMetricsIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Should return health status UP")
    void shouldReturnHealthStatusUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    @DisplayName("Should return metrics endpoint")
    void shouldReturnMetricsEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names", not(empty())))
                .andExpect(jsonPath("$.names", hasItem("jvm.memory.used")))
                .andExpect(jsonPath("$.names", hasItem("http.server.requests")));
    }

    @Test
    @DisplayName("Should return custom todo metrics after operations")
    void shouldReturnCustomTodoMetricsAfterOperations() throws Exception {
        CreateTodoRequest request = new CreateTodoRequest();
        request.setTitle("Test Todo for Metrics");
        request.setDescription("Test Description");

        mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names", hasItem("todo.created")));
    }

    @Test
    @DisplayName("Should return specific JVM metric")
    void shouldReturnSpecificJVMMetric() throws Exception {
        mockMvc.perform(get("/actuator/metrics/jvm.memory.used"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("jvm.memory.used")))
                .andExpect(jsonPath("$.measurements", not(empty())));
    }

    @Test
    @DisplayName("Should return HTTP requests metric")
    void shouldReturnHTTPRequestsMetric() throws Exception {
        mockMvc.perform(get("/actuator/metrics/http.server.requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("http.server.requests")));
    }

    @Test
    @DisplayName("Should return info endpoint")
    void shouldReturnInfoEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should increment todo created metric")
    void shouldIncrementTodoCreatedMetric() throws Exception {
        CreateTodoRequest request = new CreateTodoRequest();
        request.setTitle("Metric Test Todo");
        request.setDescription("Testing metrics");

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/v1/todos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }

        mockMvc.perform(get("/actuator/metrics/todo.created"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("todo.created")));
    }
}
