# Booking System API

## 📌 Project Overview
This is a Spring Boot backend for a booking system with:
- User registration & login
- Package purchase (credits, expiry, country restriction)
- Class schedule & booking
- Booking concurrency prevention with Redis
- Authentication: Basic Auth (before login), JWT Bearer Token (after login)

## ⚙️ Tech Stack
- Java 21
- Spring Boot (MVC, Security, Data JPA)
- Hibernate + JPA
- PostgreSQL
- Redis (caching + concurrency lock)
- Swagger UI (API docs) (http://localhost:8080/swagger-ui/index.html)

## 📊 Database Design
![Database Design](docs/database_design.png)
