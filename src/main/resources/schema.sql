DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    location VARCHAR(255),
    date_joined DATE
);

INSERT INTO users (username, password, email, name, location, date_joined) VALUES
                                                                               ('jdoe', '$2a$10$PT4OkMDKe1nOdEpjlgjDFeXPiLsYWl3eIyIA1A8k0dmH2hSK3QhBC', 'jdoe@example.com', 'John Doe', 'New York', '2024-06-01'),
                                                                               ('asmith', '$2a$10$PT4OkMDKe1nOdEpjlgjDFeXPiLsYWl3eIyIA1A8k0dmH2hSK3QhBC', 'asmith@example.com', 'Alice Smith', 'Los Angeles', '2024-07-15'),
                                                                               ('bwayne', '$2a$10$PT4OkMDKe1nOdEpjlgjDFeXPiLsYWl3eIyIA1A8k0dmH2hSK3QhBC', 'bwayne@example.com', 'Bruce Wayne', 'Gotham', '2024-05-10');