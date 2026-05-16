-- Seed users for Docker/Postgres. Passwords are BCrypt hashes of "password123".
-- ON CONFLICT DO NOTHING makes this idempotent across restarts.
INSERT INTO users (username, password, email, name, location, date_joined, account_status) VALUES
    ('jdoe',   '$2a$10$PT4OkMDKe1nOdEpjlgjDFeXPiLsYWl3eIyIA1A8k0dmH2hSK3QhBC', 'jdoe@example.com',   'John Doe',    'New York',    '2024-06-01', 'VERIFIED'),
    ('asmith', '$2a$10$PT4OkMDKe1nOdEpjlgjDFeXPiLsYWl3eIyIA1A8k0dmH2hSK3QhBC', 'asmith@example.com', 'Alice Smith', 'Los Angeles', '2024-07-15', 'VERIFIED'),
    ('bwayne', '$2a$10$PT4OkMDKe1nOdEpjlgjDFeXPiLsYWl3eIyIA1A8k0dmH2hSK3QhBC', 'bwayne@example.com', 'Bruce Wayne', 'Gotham',      '2024-05-10', 'VERIFIED'),
    ('admin',  '$2a$10$PT4OkMDKe1nOdEpjlgjDFeXPiLsYWl3eIyIA1A8k0dmH2hSK3QhBC', 'admin@example.com',  'Admin',       NULL,          '2024-05-10', 'TRUSTED')
ON CONFLICT (username) DO NOTHING;
