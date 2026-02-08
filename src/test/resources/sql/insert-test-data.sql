-- Очистка таблицы
DELETE FROM todos;

-- Вставка тестовых данных
INSERT INTO todos (id, title, description, completed, created_at, updated_at) VALUES
(1, 'Test Todo 1', 'Description 1', false, '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
(2, 'Test Todo 2', 'Description 2', true, '2024-01-02 11:00:00', '2024-01-02 11:00:00'),
(3, 'Test Todo 3', 'Description 3', false, '2024-01-03 12:00:00', '2024-01-03 12:00:00'),
(4, 'Test Todo 4', 'Description 4', true, '2024-01-04 13:00:00', '2024-01-04 13:00:00');