# HireSphere AI - Spring Boot Enterprise Backend

Autonomous Real-Time Talent & Placement Platform backend powered by **Spring Boot 3.2.5 (Java 17)**, **H2 Embedded Persistent Database**, **Spring Data JPA**, and **Google Gemini AI**.

---

## 1. Cloud Deployment Endpoints

| Component | Technology | Cloud Production URL | Local Address | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Backend REST API** | Spring Boot 3.2.5 (Java 17) | **`https://hiresphereai.onrender.com/api`** | `http://localhost:8085/api` | Enterprise Java backend REST endpoints |
| **Interactive API Docs** | Springdoc OpenAPI / Swagger UI | **`https://hiresphereai.onrender.com/swagger-ui.html`** | `http://localhost:8085/swagger-ui.html` | Interactive API testing chamber |
| **Live Event Stream** | Server-Sent Events (SSE) | **`https://hiresphereai.onrender.com/api/realtime/stream`** | `http://localhost:8085/api/realtime/stream` | Live event bus pushing database mutations |
| **H2 Database Console** | Embedded Web Console | **`https://hiresphereai.onrender.com/h2-console`** | `http://localhost:8085/h2-console` | Direct SQL inspection web console (`sa` / empty password) |
| **Agentic AI Engine** | Google Gemini API + Built-in Agent | **`https://hiresphereai.onrender.com/api/ai/*`** | `/api/ai/*` | Autonomous screening, mock interview coach, and job drafting |
| **Frontend Web App** | React 19 + Vite 8 | **`https://hire-sphere-ai-front-end.vercel.app`** | `http://localhost:5175` | Modern responsive SPA |

---

## 2. Docker & Cloud Deployment (Render)

This backend is containerized via multi-stage Dockerfile and deploys natively on Render:
- **Build Stage**: `maven:3.9-eclipse-temurin-17-alpine`
- **Runtime Stage**: `eclipse-temurin:17-jre-alpine`
- **Port**: Auto-binds to `$PORT` (defaults to `8085`)
- **Health Check**: `GET /api/jobs` or `GET /api/ai/status`

---

## 3. Local Development

```bash
# Build package (skipping tests)
mvn clean package -DskipTests

# Run Spring Boot application
mvn spring-boot:run
```

The server starts on `http://localhost:8085`.
Interactive Swagger UI is accessible at `http://localhost:8085/swagger-ui.html`.

---

## 4. Environment Configuration (.env)

See `.env.example` for all configurable keys:

```env
SERVER_PORT=8085
APP_NAME=hiresphere-backend
DB_URL=jdbc:h2:file:./data/hiresphere_realtime_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
DB_USERNAME=sa
DB_PASSWORD=
H2_CONSOLE_ENABLED=true
H2_CONSOLE_PATH=/h2-console
DATA_FILE_PATH=./data/hiresphere_realtime_db.json
GEMINI_API_KEY=
GEMINI_MODEL=gemini-3.5-flash
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent
REALTIME_STREAM_PATH=/api/realtime/stream
CORS_ALLOWED_ORIGINS=http://localhost:5175,http://127.0.0.1:5175,https://hire-sphere-ai-front-end.vercel.app,https://*.vercel.app
```
