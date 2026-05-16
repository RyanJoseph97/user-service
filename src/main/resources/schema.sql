DROP TABLE IF EXISTS user_follows;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    location VARCHAR(255),
    date_joined DATE NOT NULL,
    account_status VARCHAR(20) NOT NULL DEFAULT 'UNVERIFIED'
);

CREATE TABLE user_follows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    followee_id BIGINT NOT NULL,
    followed_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_follower FOREIGN KEY (follower_id) REFERENCES users(id),
    CONSTRAINT fk_followee FOREIGN KEY (followee_id) REFERENCES users(id),
    CONSTRAINT uq_follow UNIQUE (follower_id, followee_id)
);

INSERT INTO users (username, password, email, name, location, date_joined, account_status) VALUES
    ('jdoe',   '$2a$10$PT4OkMDKe1nOdEpjlgjDFeXPiLsYWl3eIyIA1A8k0dmH2hSK3QhBC', 'jdoe@example.com',   'John Doe',    'New York',    '2024-06-01', 'VERIFIED'),
    ('asmith', '$2a$10$PT4OkMDKe1nOdEpjlgjDFeXPiLsYWl3eIyIA1A8k0dmH2hSK3QhBC', 'asmith@example.com', 'Alice Smith', 'Los Angeles', '2024-07-15', 'VERIFIED'),
    ('bwayne', '$2a$10$PT4OkMDKe1nOdEpjlgjDFeXPiLsYWl3eIyIA1A8k0dmH2hSK3QhBC', 'bwayne@example.com', 'Bruce Wayne', 'Gotham',      '2024-05-10', 'VERIFIED'),
    ('admin',  '$2a$10$PT4OkMDKe1nOdEpjlgjDFeXPiLsYWl3eIyIA1A8k0dmH2hSK3QhBC', 'admin@example.com',  'Admin',       'Gotham',      '2024-05-10', 'TRUSTED');