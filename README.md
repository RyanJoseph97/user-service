# User Service

A Spring Boot microservice for managing user accounts in the EventMaster application. This service handles user registration, authentication, and profile management.

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [Project Structure](#project-structure)
- [Running Tests](#running-tests)
- [Building and Deployment](#building-and-deployment)

## Features

- **User Management**: Create, retrieve, and manage user profiles
- **Multiple Lookup Methods**: Find users by ID, username, or email
- **Exception Handling**: Global exception handling for consistent error responses
- **Database**: H2 in-memory database for development and testing
- **REST API**: RESTful API following Spring Boot conventions
- **JPA/Hibernate**: Object-relational mapping for database operations

## Technology Stack

- **Java 11**
- **Spring Boot 2.7.14**
- **Spring Data JPA**
- **Hibernate**
- **H2 Database**
- **Maven**
- **JUnit 5**

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- Git

## Installation

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd user-service
   ```

2. **Build the project**:
   ```bash
   mvn clean install
   ```

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

The service will start on `http://localhost:8080/user-service`

## Configuration

Configuration is managed in `src/main/resources/application.properties`:

```properties
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### Database
- **Type**: H2 (in-memory)
- **Console**: Available at `http://localhost:8080/user-service/h2-console`

## API Endpoints

### Get User by ID
```http
GET /user-service/users/{id}
```
Returns a user by their unique ID.

**Response** (200 OK):
```json
{
  "id": 1,
  "username": "asmith",
  "password": "encrypted_password",
  "email": "asmith@example.com",
  "name": "Alice Smith",
  "location": "New York, NY",
  "dateJoined": "2025-01-15"
}
```

### Get User by Username
```http
GET /user-service/users/by-username/{username}
```
Returns a user by their username. Throws `UserNotFoundException` if not found.

**Response** (200 OK):
```json
{
  "id": 1,
  "username": "asmith",
  "password": "encrypted_password",
  "email": "asmith@example.com",
  "name": "Alice Smith",
  "location": "New York, NY",
  "dateJoined": "2025-01-15"
}
```

### Get User by Email
```http
GET /user-service/users/by-email/{email}
```
Returns a user by their email address. Throws `UserNotFoundException` if not found.

**Response** (200 OK):
```json
{
  "id": 1,
  "username": "asmith",
  "password": "encrypted_password",
  "email": "asmith@example.com",
  "name": "Alice Smith",
  "location": "New York, NY",
  "dateJoined": "2025-01-15"
}
```

### Get All Users
```http
GET /user-service/users
```
Returns a list of all users in the system.

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "username": "asmith",
    "email": "asmith@example.com",
    "name": "Alice Smith",
    "location": "New York, NY",
    "dateJoined": "2025-01-15"
  },
  {
    "id": 2,
    "username": "jdoe",
    "email": "jdoe@example.com",
    "name": "John Doe",
    "location": "Los Angeles, CA",
    "dateJoined": "2025-02-01"
  }
]
```

### Create User
```http
POST /user-service/users
Content-Type: application/json

{
  "username": "newuser",
  "password": "secure_password",
  "email": "newuser@example.com",
  "name": "New User",
  "location": "Chicago, IL"
}
```

**Response** (201 Created):
```json
{
  "id": 3,
  "username": "newuser",
  "password": "secure_password",
  "email": "newuser@example.com",
  "name": "New User",
  "location": "Chicago, IL",
  "dateJoined": "2025-02-22"
}
```

### Error Responses

**404 Not Found** (for GET endpoints):
```json
{
  "status": 404,
  "message": "User not found with id: 999"
}
```

**Exception** (for `findByUsername` and `findByEmail`):
```
UserNotFoundException: User not found with username: nonexistent
```

## Project Structure

```
user-service/
├── src/
│   ├── main/
│   │   ├── java/com/eventmaster/
│   │   │   ├── UserServiceApplication.java      # Main Spring Boot application
│   │   │   ├── config/
│   │   │   │   └── WebConfig.java               # Web configuration
│   │   │   ├── controller/
│   │   │   │   └── UserController.java          # REST endpoints
│   │   │   ├── service/
│   │   │   │   └── UserService.java             # Business logic
│   │   │   ├── repository/
│   │   │   │   └── UserRepository.java          # Data access layer
│   │   │   ├── model/
│   │   │   │   └── User.java                    # User entity
│   │   │   └── exception/
│   │   │       ├── UserNotFoundException.java   # Custom exception
│   │   │       └── GlobalExceptionHandler.java  # Exception handling
│   │   └── resources/
│   │       ├── application.properties           # Configuration
│   │       └── schema.sql                       # Database schema
│   └── test/
│       └── java/
│           └── UserServiceTest.java             # Unit tests
├── pom.xml                                      # Maven configuration
├── deploy.bat                                   # Deployment script
└── README.md                                    # This file
```

## Running Tests

Run all tests:
```bash
mvn test
```

Run a specific test:
```bash
mvn test -Dtest=UserServiceTest#testUserFindByEmail
```

Run tests with coverage:
```bash
mvn test jacoco:report
```

## Building and Deployment

### Build JAR
```bash
mvn clean package
```

The JAR file will be created in `target/user-service-1.0-SNAPSHOT.jar`

### Run JAR
```bash
java -jar target/user-service-1.0-SNAPSHOT.jar
```

### Using deploy.bat
On Windows, you can use the provided deployment script:
```bash
deploy.bat
```

## Key Classes

### User Entity (`User.java`)
- Represents a user in the system
- JPA entity with automatic ID generation
- Fields: id, username, password, email, name, location, dateJoined

### UserService (`UserService.java`)
- Business logic layer
- Methods:
  - `saveUser(User)` - Create or update a user
  - `findById(Long)` - Find user by ID (returns Optional)
  - `findByUsername(String)` - Find user by username (throws exception if not found)
  - `findByEmail(String)` - Find user by email (throws exception if not found)
  - `getAllUsers()` - Get all users

### UserController (`UserController.java`)
- REST API endpoints
- Handles HTTP requests and responses
- Maps service methods to endpoints

### GlobalExceptionHandler (`GlobalExceptionHandler.java`)
- Handles exceptions across the application
- Provides consistent error responses

## Notes

- The H2 console is enabled for development purposes at `/user-service/h2-console`
- Passwords should be encrypted in production (currently stored as plain text for development)
- The database is in-memory and will be reset on application restart

## License

See LICENSE file for details.

## Support

For issues or questions, please contact the development team.

