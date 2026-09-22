# AI Text-to-SQL

## Phase 1 — Project & Database Foundation

This is the foundational setup for the AI Text-to-SQL project. Phase 1 provides a Spring Boot backend connected to PostgreSQL, using Spring Data JPA, exposing a REST API for customers.

## Technologies
- Java 17
- Spring Boot 3.2.x
- Spring Web (REST API)
- Spring Data JPA (Data Access)
- PostgreSQL & PostgreSQL JDBC Driver
- Lombok (Boilerplate reduction)
- Spring Boot Actuator (Health monitoring)
- JUnit 5 / Spring Boot Test (Testing)
- Maven

## Prerequisites
- Java 17 installed
- Maven installed
- PostgreSQL installed and running

## Database Setup
1. Create a PostgreSQL database named `ai_text_to_sql`:
   ```sql
   CREATE DATABASE ai_text_to_sql;
   ```
2. The schema (`customers` and `orders` tables) and sample data will be automatically initialized when the Spring Boot application starts, using the `schema.sql` and `data.sql` scripts located in `src/main/resources`.

## Environment Variables
The application relies on the following environment variables. If not provided, it falls back to default values.

- `DB_URL`: The JDBC URL to the PostgreSQL database (default: `jdbc:postgresql://localhost:5432/ai_text_to_sql`)
- `DB_USERNAME`: The PostgreSQL user (default: `postgres`)
- `DB_PASSWORD`: The PostgreSQL password (default: `postgres`)

You can set these before running:
```bash
export DB_URL=jdbc:postgresql://localhost:5432/ai_text_to_sql
export DB_USERNAME=postgres
export DB_PASSWORD=your_secure_password
```

## Run
To start the application locally:
```bash
mvn spring-boot:run
```

Or build the jar and run:
```bash
mvn clean package
java -jar target/ai-text-to-sql-0.0.1-SNAPSHOT.jar
```

## API

### Get Customers
Retrieves the list of all customers from the database.
```text
GET /api/customers
```
**Sample Response:**
```json
[
  {
    "id": 1,
    "name": "Rahul Sharma",
    "city": "Delhi",
    "email": "rahul@gmail.com",
    "createdAt": "2026-08-01T12:00:00"
  },
  ...
]
```

### Health Check
Checks if the application is running successfully.
```text
GET /actuator/health
```
**Sample Response:**
```json
{"status":"UP"}
```

## Architecture
```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Spring Data JPA
    ↓
PostgreSQL
```
*(Note: Phase 1 utilizes Spring Data JPA based on updated requirements instead of JdbcTemplate)*

---

## Phase 2 — Spring AI + Google Gemini Integration

### Architecture

```text
User / Client
      ↓
AiController (POST /api/ai/chat)
      ↓
AiService (business validation & exception handling)
      ↓
ChatClient (Spring AI vendor-neutral abstraction)
      ↓
Google Gemini API (https://generativelanguage.googleapis.com/v1beta/openai)
      ↓
AI Response Body (JSON)
```

### Environment Variables

| Variable | Description | Default |
|---|---|---|
| `GEMINI_API_KEY` | API key from [Google AI Studio](https://aistudio.google.com/) | *(None - required for live AI calls)* |
| `GEMINI_MODEL` | Google Gemini model name | `gemini-2.0-flash` |
| `GEMINI_BASE_URL` | Gemini OpenAI-compatible API base URL | `https://generativelanguage.googleapis.com/v1beta/openai` |

*Security: Never commit your API key. Pass it as an environment variable or via Docker Compose.*

### API Endpoints

#### `POST /api/ai/chat`
Sends a prompt to the Google Gemini LLM.

**Sample Request:**
```http
POST /api/ai/chat
Content-Type: application/json

{
  "message": "Explain what a database is in simple words"
}
```

**Sample Response (200 OK):**
```json
{
  "message": "Explain what a database is in simple words",
  "response": "A database is an organized collection of data that can be easily accessed, managed, and updated..."
}
```

**Validation Error Response (400 Bad Request):**
```json
{
  "timestamp": "2026-09-04T22:00:00.000",
  "status": 400,
  "error": "message must not be null or blank"
}
```

**Service Failure Response (503 Service Unavailable):**
```json
{
  "timestamp": "2026-09-04T22:00:00.000",
  "status": 503,
  "error": "AI service is temporarily unavailable"
}
```


---

## Phase 3 — Natural Language to SQL

### Overview
Phase 3 introduces the Natural Language to SQL generation capability. Users submit a natural-language query, which is provided alongside the static database schema context to Google Gemini via Spring AI's `ChatClient`. Gemini generates a read-only PostgreSQL query, which is returned in the API response.

> **Important**: In Phase 3, SQL queries are **generated only**. SQL execution against PostgreSQL and SQL validation are intentionally **NOT** implemented in this phase (reserved for Phase 4 & Phase 5).

### Architecture
```text
User / Client
      ↓
TextToSqlController (POST /api/text-to-sql)
      ↓
TextToSqlService (business validation, schema prompt & response normalization)
      ↓
Spring AI ChatClient
      ↓
Google Gemini (system prompt with schema + user question)
      ↓
Generated SQL (normalized, markdown stripped)
      ↓
API Response Body (JSON)
```

### Database Schema Context Provided to Gemini
```text
DATABASE SCHEMA:

Table: customers
- id INTEGER PRIMARY KEY
- name VARCHAR(100)
- city VARCHAR(100)
- email VARCHAR(150)
- created_at TIMESTAMP

Table: orders
- id INTEGER PRIMARY KEY
- customer_id INTEGER
- amount NUMERIC(12,2)
- order_date DATE

Relationship:
orders.customer_id references customers.id
```

### API Endpoint

#### `POST /api/text-to-sql`
Converts a natural language question into a PostgreSQL SQL query.

**Sample Request:**
```http
POST /api/text-to-sql
Content-Type: application/json

{
  "question": "Who has spent the most money?"
}
```

**Sample Response (200 OK):**
```json
{
  "question": "Who has spent the most money?",
  "sql": "SELECT c.name, SUM(o.amount) AS total_amount FROM customers c JOIN orders o ON c.id = o.customer_id GROUP BY c.id, c.name ORDER BY total_amount DESC LIMIT 1;"
}
```

**Sample Request (Unanswerable Question):**
```http
POST /api/text-to-sql
Content-Type: application/json

{
  "question": "Show me employee salary information"
}
```

**Sample Response (400 Bad Request):**
```json
{
  "question": "Show me employee salary information",
  "sql": null,
  "error": "The question cannot be answered using the available database schema."
}
```

**Validation Error Response (400 Bad Request):**
```json
{
  "timestamp": "2026-09-22T22:30:00.000",
  "status": 400,
  "error": "question must not be null or blank"
}
```

**Service Failure Response (503 Service Unavailable):**
```json
{
  "timestamp": "2026-09-22T22:30:00.000",
  "status": 503,
  "error": "AI service is temporarily unavailable"
}
```

