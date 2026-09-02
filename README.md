# ENSAM Analytics Chatbot

A Spring Boot API that answers questions about ENSAM alumni profiles and
aggregated career trends. Ollama converts natural-language questions into a
validated intent, while MongoDB remains the source of truth for profile and
analytics results.

## Features

- Person lookups for education, skills, PFE placement, and current employment
- Aggregated rankings for employers, job titles, PFE companies, and skills
- Optional promotion-year and normalized-major filters
- Local LLM integration through Ollama
- MongoDB aggregation pipelines for deterministic answers
- Request validation and consistent JSON errors
- Typed, timeout-protected Ollama client
- OpenAPI documentation and automated tests

## Technology

- Java 17 and Spring Boot 3
- Spring MVC, WebClient, Validation, and Spring Data MongoDB
- MongoDB 7 and Ollama
- JUnit 5, MockMvc, Mockito, Maven, Docker Compose, and GitHub Actions

## Architecture

```text
HTTP request
    -> ChatController (validation)
    -> ChatService (business orchestration)
    -> IntentDetectionService -> Ollama
    -> Profile repositories -> MongoDB
    -> ChatResponse
```

Ollama only classifies the question. Database queries and returned statistics
are executed by the application, which avoids asking the model to invent alumni
data.

## Run locally

Requirements: Java 17, Docker, and an Ollama-compatible model.

Start MongoDB and Ollama:

```bash
docker compose up -d
docker compose exec ollama ollama pull llama3.1:8b
```

Start the API:

```bash
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd spring-boot:run`. The API starts at
`http://localhost:8080`; Swagger UI is available at
`http://localhost:8080/swagger-ui.html`.

The `.env.example` file documents supported configuration values. Spring Boot
does not load it automatically: export the variables in your shell or configure
them in your IDE/deployment environment.

## Configuration

| Variable | Default | Purpose |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | API port |
| `MONGODB_URI` | `mongodb://localhost:27017/linkedin_ensam` | MongoDB connection URI |
| `MONGODB_DATABASE` | `linkedin_ensam` | Database containing `profiles` |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama API URL |
| `OLLAMA_MODEL` | `llama3.1:8b` | Intent-classification model |
| `OLLAMA_CONNECT_TIMEOUT` | `5s` | Maximum connection time |
| `OLLAMA_RESPONSE_TIMEOUT` | `30s` | Maximum model response time |
| `HTTP_REQUEST_TIMEOUT` | `35s` | MVC asynchronous request timeout |

Never commit credentials or a populated `.env` file.

## API

### Ask a question

```http
POST /api/chat
Content-Type: application/json

{
  "question": "Top skills for the 2025 Big Data and IoT promotion"
}
```

Example response:

```json
{
  "answer": "Top skills (promo 2025) (major BIG_DATA_IOT):\n1) Java — 12",
  "source": "mongodb"
}
```

The question is required and limited to 500 characters.

## MongoDB document shape

The application reads the `profiles` collection. A document can contain:

```json
{
  "fullName": "Example Student",
  "mainEducation": {
    "endYear": 2025,
    "majorRaw": "Big Data and IoT",
    "majorNorm": "BIG_DATA_IOT"
  },
  "skills": ["Java", "Spring Boot", "MongoDB"],
  "pfe": {
    "company": "Example Company",
    "location": "Casablanca"
  },
  "experiences": []
}
```

## Tests

```bash
./mvnw test
```

The suite covers request validation, controller responses, structured intent
parsing, and invalid model-output fallback behavior. GitHub Actions runs
`mvnw verify` for every pull request and push to `main`.

## Author

Developed and maintained by [Souilimaa](https://github.com/souilimaa).
