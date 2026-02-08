package com.emobile.springtodo.repository;

import com.emobile.springtodo.model.Todo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class TodoRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Todo> todoRowMapper = (rs, rowNum) -> {
        Todo todo = new Todo();
        todo.setId(rs.getLong("id"));
        todo.setTitle(rs.getString("title"));
        todo.setDescription(rs.getString("description"));
        todo.setCompleted(rs.getBoolean("completed"));
        todo.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        todo.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return todo;
    };

    @Autowired
    public TodoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Todo save(Todo todo) {
        if (todo.getId() == null) {
            return insert(todo);
        } else {
            return update(todo);
        }
    }

    private Todo insert(Todo todo) {
        String sql = "INSERT INTO todos (title, description, completed, created_at, updated_at) VALUES (?, ?, ?, ?, ?) RETURNING *";

        return jdbcTemplate.queryForObject(sql, todoRowMapper,
                todo.getTitle(),
                todo.getDescription(),
                todo.getCompleted() != null && todo.getCompleted(),
                Timestamp.valueOf(LocalDateTime.now()),
                Timestamp.valueOf(LocalDateTime.now())
        );
    }

    private Todo update(Todo todo) {
        String sql = "UPDATE todos SET title = ?, description = ?, completed = ?, updated_at = ? WHERE id = ? RETURNING *";

        return jdbcTemplate.queryForObject(sql, todoRowMapper,
                todo.getTitle(),
                todo.getDescription(),
                todo.getCompleted(),
                Timestamp.valueOf(LocalDateTime.now()),
                todo.getId()
        );
    }

    public Optional<Todo> findById(Long id) {
        String sql = "SELECT * FROM todos WHERE id = ?";
        try {
            Todo todo = jdbcTemplate.queryForObject(sql, todoRowMapper, id);
            return Optional.ofNullable(todo);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Todo> findAll(int limit, int offset) {
        String sql = "SELECT * FROM todos ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, todoRowMapper, limit, offset);
    }

    public List<Todo> findByCompleted(boolean completed, int limit, int offset) {
        String sql = "SELECT * FROM todos WHERE completed = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, todoRowMapper, completed, limit, offset);
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM todos WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM todos";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public long countByCompleted(boolean completed) {
        String sql = "SELECT COUNT(*) FROM todos WHERE completed = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, completed);
    }

    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM todos WHERE id = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, id);
        return count > 0;
    }
}
