# User Service

A Spring Boot microservice for managing user accounts in the EventMaster application. This service handles user registration, authentication, and profile management.

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Logging](#logging)
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

## Logging

This application uses **SLF4J** with **Logback** as the default logging implementation. No additional logging library (like Log4j) is needed.

### Logging Configuration

Logging is configured in two places:

#### 1. `application.properties` (Basic configuration)
```properties
logging.level.root=INFO
logging.level.com.eventmaster=INFO
logging.level.org.springframework.web=INFO
logging.level.org.hibernate.SQL=DEBUG
logging.file.name=logs/user-service.log
logging.file.max-size=10MB
logging.file.max-history=30
```

#### 2. `logback-spring.xml` (Advanced configuration)
Provides detailed control over:
- **Console output**: Real-time logs in the terminal
- **File output**: Logs written to `logs/user-service.log`
- **Error logs**: Separate file for errors only at `logs/user-service-error.log`
- **Rolling policies**: Automatic log rotation based on size and date
- **Log patterns**: Customizable timestamp, thread, level, and message formats

### Log Output

#### Console Output
When running the application, logs will appear in the console with the format:
```
2025-02-22 10:15:30 [main] INFO com.eventmaster.service.UserService - Found user with username: asmith
```

#### File Output
Logs are stored in:
- `logs/user-service.log` - All application logs
- `logs/user-service-error.log` - Error logs only

### Log Levels

| Level | Usage | When to Use |
|-------|-------|------------|
| **TRACE** | Very detailed tracing | SQL parameter binding, low-level debugging |
| **DEBUG** | Detailed information | Method entry/exit, variable values, flow tracking |
| **INFO** | General information | User actions, service operations, important events |
| **WARN** | Warning messages | Unusual but recoverable situations |
| **ERROR** | Error messages | Errors that don't stop the application |

### Configuring Log Levels

To change log levels, edit `application.properties`:

```properties
# Change application logging to DEBUG
logging.level.com.eventmaster=DEBUG

# Change Spring Web logging to DEBUG
logging.level.org.springframework.web=DEBUG

# Change Hibernate SQL logging to DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### Log Examples

#### User Creation
```
2025-02-22 10:15:30 [http-nio-8080-exec-1] DEBUG com.eventmaster.controller.UserController - POST request received to create user: newuser
2025-02-22 10:15:30 [http-nio-8080-exec-1] INFO com.eventmaster.service.UserService - Attempting to save user with username: newuser
2025-02-22 10:15:30 [http-nio-8080-exec-1] INFO com.eventmaster.service.UserService - Successfully saved user with id: 1 and username: newuser
2025-02-22 10:15:30 [http-nio-8080-exec-1] INFO com.eventmaster.controller.UserController - User created successfully with id: 1
```

#### User Not Found
```
2025-02-22 10:16:45 [http-nio-8080-exec-2] DEBUG com.eventmaster.controller.UserController - GET request received for username: nonexistent
2025-02-22 10:16:45 [http-nio-8080-exec-2] DEBUG com.eventmaster.service.UserService - Searching for user by username: nonexistent
2025-02-22 10:16:45 [http-nio-8080-exec-2] WARN com.eventmaster.service.UserService - User not found with username: nonexistent
```

### Spring Boot Profiles

The application supports different log levels based on active profiles:

#### Development Profile
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```
- Application logs: DEBUG
- Spring Web logs: DEBUG
- Hibernate SQL logs: DEBUG

#### Production Profile
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```
- Application logs: INFO
- Spring Framework logs: WARN



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
  "timestamp": "2026-02-22T11:15:30",
  "status": 404,
  "error": "User Not Found",
  "message": "User not found with id: 999"
}
```

**409 Conflict** (duplicate username or email):
```json
{
  "timestamp": "2026-02-22T11:15:30",
  "status": 409,
  "error": "Conflict",
  "message": "The email address is already registered. Please use a different email."
}
```

**500 Internal Server Error** (other exceptions):
```json
{
  "timestamp": "2026-02-22T11:15:30",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
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

### Testing Constraint Violations

You can test the new constraint violation handling with curl:

**Test duplicate email:**
```bash
curl -X POST http://localhost:8080/user-service/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "password123",
    "email": "jdoe@example.com",
    "name": "New User",
    "location": "Test City"
  }'
```

**Expected response (409 Conflict):**
```json
{
  "timestamp": "2026-02-22T11:20:45",
  "status": 409,
  "error": "Conflict",
  "message": "The email address is already registered. Please use a different email."
}
```

**Test duplicate username:**
```bash
curl -X POST http://localhost:8080/user-service/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "asmith",
    "password": "password123",
    "email": "different@example.com",
    "name": "Another Smith",
    "location": "Test City"
  }'
```

**Expected response (409 Conflict):**
```json
{
  "timestamp": "2026-02-22T11:20:45",
  "status": 409,
  "error": "Conflict",
  "message": "The username is already taken. Please choose a different username."
}
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

