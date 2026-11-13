# Employee Leave Management System

A REST API built with **Java 17, Spring Boot 3, Spring Security (JWT), Spring Data JPA/Hibernate, and MySQL** that lets employees apply for leave and track their balance, while managers review, approve, or reject requests.

## Features

- Employee registration & login (JWT-based authentication)
- Two roles: `EMPLOYEE` and `MANAGER`
- Apply for leave (with automatic day-count and balance validation)
- Cancel a pending leave request
- View personal leave history
- View personal leave balance (total / used / remaining)
- Manager dashboard: view all leave requests (optionally filtered by status)
- Manager approve / reject leave requests with an optional comment
- Global exception handling with consistent JSON error responses
- Swagger / OpenAPI docs at `/swagger-ui.html`

## Tech Stack

| Layer            | Technology                          |
|-------------------|--------------------------------------|
| Language           | Java 17                              |
| Framework          | Spring Boot 3.3.4                    |
| Security           | Spring Security 6 + JWT (jjwt 0.12.6)|
| Persistence        | Spring Data JPA / Hibernate          |
| Database           | MySQL 8                              |
| Build Tool         | Maven                                |
| API Docs           | springdoc-openapi (Swagger UI)       |
| Boilerplate        | Lombok                               |

## Project Structure

```
src/main/java/com/company/leavemanagement
├── LeaveManagementApplication.java   # main entry point
├── config/
│   └── SecurityConfig.java           # security filter chain, CORS, password encoder
├── controller/
│   ├── AuthController.java           # /api/auth/**  (register, login)
│   ├── EmployeeController.java       # /api/employee/** (apply, history, balance)
│   └── ManagerController.java        # /api/manager/** (view all, approve/reject)
├── dto/                              # request/response payloads
├── entity/
│   ├── User.java
│   └── LeaveRequest.java
├── enums/
│   ├── Role.java, LeaveStatus.java, LeaveType.java
├── exception/                        # custom exceptions + @RestControllerAdvice
├── repository/                       # Spring Data JPA repositories
├── security/                         # JWT filter, JWT util, UserDetails
└── service / service/impl            # business logic
```

## Prerequisites

- JDK 17+
- Maven 3.8+
- MySQL 8 running locally (or update `application.properties`)


## Setup & Run

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- MySQL 8+

### 1. Clone the repository

git clone https://github.com/Shrish06/Leave-Management--System.git

cd Leave-Management--System

### 2. Create the database

CREATE DATABASE leave_management_db;

### 3. Configure environment variables

Set the following environment variables:

DB_URL=jdbc:mysql://localhost:3306/leave_management_db
DB_USERNAME=your_username
DB_PASSWORD=your_password
JWT_SECRET=your_secure_jwt_secret

Refer to `.env.example` for the required variable names.

Note: Spring Boot reads these values from the operating system environment.

### 4. Run the application

mvn clean install

mvn spring-boot:run

### 5. Access the API

Application: http://localhost:8080

Swagger UI: http://localhost:8080/swagger-ui.html


## API Overview

### Auth (public)
| Method | Endpoint             | Description                    |
|--------|-----------------------|---------------------------------|
| POST   | /api/auth/register    | Register a new employee/manager |
| POST   | /api/auth/login       | Login, returns a JWT            |

### Employee (role: EMPLOYEE or MANAGER)
| Method | Endpoint                          | Description                  |
|--------|-------------------------------------|-------------------------------|
| GET    | /api/employee/profile               | Get logged-in user's profile  |
| POST   | /api/employee/leaves                | Apply for leave                |
| GET    | /api/employee/leaves                | View own leave history         |
| GET    | /api/employee/leaves/balance        | View own leave balance         |
| PATCH  | /api/employee/leaves/{id}/cancel    | Cancel a pending request       |

### Manager (role: MANAGER only)
| Method | Endpoint                              | Description                                |
|--------|-----------------------------------------|----------------------------------------------|
| GET    | /api/manager/leaves?status=PENDING      | View all leave requests (optional filter)     |
| PATCH  | /api/manager/leaves/{id}/review         | Approve or reject a request                   |

All endpoints except `/api/auth/**` require an `Authorization: Bearer <token>` header, obtained from `/api/auth/login`.

## Sample Requests

**Register:**
```json
POST /api/auth/register
{
  "fullName": "Jane Doe",
  "email": "jane@company.com",
  "password": "password123",
  "role": "EMPLOYEE",
  "department": "Engineering"
}
```

**Apply for leave:**
```json
POST /api/employee/leaves
Authorization: Bearer <token>
{
  "leaveType": "CASUAL",
  "startDate": "2026-10-01",
  "endDate": "2026-10-03",
  "reason": "Family function"
}
```

**Manager review:**
```json
PATCH /api/manager/leaves/5/review
Authorization: Bearer <manager_token>
{
  "approve": true,
  "comment": "Approved, enjoy!"
}
```

## Notes

- Every new user gets a default leave balance (`app.leave.default-balance`, 20 days) on registration.
- Leave balance is only deducted once a manager **approves** a request (not while it's pending).
- This project was not compiled in this environment (no Maven/internet access here); run `mvn clean install` locally to build and verify.
