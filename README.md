# Blog API - Spring Boot Application

A RESTful API for a blogging platform built with Spring Boot, PostgreSQL, and JWT authentication.

## Features

- User authentication (Register/Login) with JWT
- CRUD operations for blog posts
- Role-based authorization
- Password encryption with BCrypt
- Input validation
- Global exception handling
- CORS configuration

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security**
- **Spring Data JPA**
- **PostgreSQL**
- **JWT (JSON Web Token)**
- **Lombok**
- **Gradle**

## Project Structure

```
src/main/java/com/blog/api/
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   └── BlogPostController.java
├── dto/
│   ├── AuthResponse.java
│   ├── BlogPostRequest.java
│   ├── BlogPostResponse.java
│   ├── LoginRequest.java
│   ├── MessageResponse.java
│   └── RegisterRequest.java
├── exception/
│   └── GlobalExceptionHandler.java
├── model/
│   ├── BlogPost.java
│   └── User.java
├── repository/
│   ├── BlogPostRepository.java
│   └── UserRepository.java
├── security/
│   ├── JwtAuthenticationFilter.java
│   └── JwtUtil.java
├── service/
│   ├── AuthService.java
│   ├── BlogPostService.java
│   └── UserDetailsServiceImpl.java
└── BlogApiApplication.java
```

## Prerequisites

- Java 17 or higher
- PostgreSQL 12 or higher
- Gradle 7.x or higher

## Setup Instructions

### 1. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE blogdb;
```

### 2. Configure Application

Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/blogdb
spring.datasource.username=your_username
spring.datasource.password=your_password

jwt.secret=your-256-bit-secret-key-here-make-it-long-and-secure
jwt.expiration=86400000
```

### 3. Build and Run

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The API will start on `http://localhost:8080`

## API Endpoints

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "johndoe",
  "email": "john@example.com"
}
```

#### Login User
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "johndoe",
  "email": "john@example.com"
}
```

### Blog Post Endpoints

#### Get All Blog Posts (Public)
```http
GET /api/posts
```

**Response:**
```json
[
  {
    "id": 1,
    "title": "My First Blog Post",
    "content": "This is the content...",
    "authorUsername": "johndoe",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

#### Get Single Blog Post (Public)
```http
GET /api/posts/{id}
```

**Response:**
```json
{
  "id": 1,
  "title": "My First Blog Post",
  "content": "This is the full content of the blog post...",
  "authorUsername": "johndoe",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### Create Blog Post (Authenticated)
```http
POST /api/posts
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "My New Blog Post",
  "content": "This is the content of my blog post..."
}
```

**Response:**
```json
{
  "id": 2,
  "title": "My New Blog Post",
  "content": "This is the content of my blog post...",
  "authorUsername": "johndoe",
  "createdAt": "2024-01-15T11:00:00",
  "updatedAt": "2024-01-15T11:00:00"
}
```

#### Update Blog Post (Authenticated - Owner Only)
```http
PUT /api/posts/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Updated Title",
  "content": "Updated content..."
}
```

**Response:**
```json
{
  "id": 2,
  "title": "Updated Title",
  "content": "Updated content...",
  "authorUsername": "johndoe",
  "createdAt": "2024-01-15T11:00:00",
  "updatedAt": "2024-01-15T12:00:00"
}
```

#### Delete Blog Post (Authenticated - Owner Only)
```http
DELETE /api/posts/{id}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "message": "Blog post deleted successfully"
}
```

## Authentication Flow

1. **Register** or **Login** to get a JWT token
2. Include the token in the `Authorization` header for protected endpoints:
   ```
   Authorization: Bearer your_jwt_token_here
   ```
3. The token expires after 24 hours (configurable)

## Security Features

- Passwords are hashed using BCrypt
- JWT tokens for stateless authentication
- CORS enabled for frontend integration
- Method-level security with `@PreAuthorize`
- Only post owners can edit/delete their posts
- Input validation on all requests

## Error Responses

The API returns appropriate HTTP status codes and error messages:

- `400 Bad Request` - Validation errors or bad input
- `401 Unauthorized` - Invalid credentials or missing token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

Example error response:
```json
{
  "error": "Email already exists"
}
```

## Testing with Postman/cURL

### Register a new user
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

### Create a blog post
```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "title": "Test Post",
    "content": "This is a test post"
  }'
```

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);
```

### Blog Posts Table
```sql
CREATE TABLE blog_posts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id)
);
```

## Future Enhancements

- Add pagination for blog posts
- Implement post categories/tags
- Add comments functionality
- Image upload for blog posts
- User profile management
- Search functionality
- Post likes/reactions

## License

This project is open source and available under the MIT License.