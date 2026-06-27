-- Demo seed users for the deployed stack so the UI isn't empty on first boot.
-- Passwords are BCrypt hashes of "password123". ON CONFLICT keeps this safe if the
-- rows already exist (e.g. a pre-Flyway dev volume baselined at V1).
--
-- WARNING: these are known-credential accounts (including `admin`). Acceptable for a
-- demo/portfolio deployment; remove or gate this migration behind a non-prod Flyway
-- location before any deployment that handles real users.
INSERT INTO users (username, password, email, name, location, date_joined, account_status, private_profile) VALUES
    ('jdoe',   '$2a$10$PT4OkMDKe1nOdEpjlgjDFeXPiLsYWl3eIyIA1A8k0dmH2hSK3QhBC', 'jdoe@example.com',   'John Doe',    'New York',    '2024-06-01', 'VERIFIED', FALSE),
    ('asmith', '$2a$10$PT4OkMDKe1nOdEpjlgjDFeXPiLsYWl3eIyIA1A8k0dmH2hSK3QhBC', 'asmith@example.com', 'Alice Smith', 'Los Angeles', '2024-07-15', 'VERIFIED', FALSE),
    ('bwayne', '$2a$10$PT4OkMDKe1nOdEpjlgjDFeXPiLsYWl3eIyIA1A8k0dmH2hSK3QhBC', 'bwayne@example.com', 'Bruce Wayne', 'Gotham',      '2024-05-10', 'VERIFIED', FALSE),
    ('admin',  '$2a$10$PT4OkMDKe1nOdEpjlgjDFeXPiLsYWl3eIyIA1A8k0dmH2hSK3QhBC', 'admin@example.com',  'Admin',       NULL,          '2024-05-10', 'TRUSTED',  FALSE)
ON CONFLICT (username) DO NOTHING;
