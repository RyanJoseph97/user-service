DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    location VARCHAR(255),
    date_joined DATE
);

INSERT INTO users (username, password, email, name, location, date_joined) VALUES
('jdoe', 'password123', 'jdoe@example.com', 'John Doe', 'New York', '2024-06-01'),
('asmith', 'securepass', 'asmith@example.com', 'Alice Smith', 'Los Angeles', '2024-07-15'),
('bwayne', 'darkknight', 'bwayne@example.com', 'Bruce Wayne', 'Gotham', '2024-05-10');