# HireSphere AI &mdash; Spring Boot Enterprise Backend
### Autonomous Real-Time Talent & Placement Microservice

[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Google Gemini](https://img.shields.io/badge/Google_Gemini-3.1_Flash_Lite-4285F4?logo=google&logoColor=white)](https://ai.google.dev/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

HireSphere AI Backend is an enterprise-grade RESTful API service built on **Spring Boot 3.2.5**, **Java 17**, and **Server-Sent Events (SSE)**. It provides autonomous candidate screening, interactive mock interview evaluation, persistent disk DBMS storage, and zero-latency event broadcasting.

---

## 🌐 Deployment Endpoints

| Component | Technology | Cloud Production URL | Local Dev Address | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Backend REST API** | Spring Boot 3.2.5 (Java 17) | `https://<your-backend-domain>.onrender.com/api` | `http://localhost:8085/api` | Primary REST API gateway |
| **Interactive API Docs** | Swagger UI | `https://<your-backend-domain>.onrender.com/swagger-ui.html` | `http://localhost:8085/swagger-ui.html` | Interactive Swagger test chamber |
| **Live Event Stream** | Server-Sent Events (SSE) | `https://<your-backend-domain>.onrender.com/api/realtime/stream` | `http://localhost:8085/api/realtime/stream` | Event bus pushing DB mutations |
| **H2 Database Console** | Embedded Web Console | `https://<your-backend-domain>.onrender.com/h2-console` | `http://localhost:8085/h2-console` | Direct SQL inspection console (`sa` / empty password) |
| **Frontend Web App** | React 19 + Vite 8 | `https://<your-frontend-domain>.vercel.app` | `http://localhost:5175` | Production Single Page Application |

---

## 🤖 3-Agent Autonomous AI Subsystem

The backend features a resilient, 3-agent architecture powered by Google Gemini (`gemini-3.1-flash-lite`) with a 100% offline heuristic fallback guarantee:

1. **Candidate Screening Agent (`POST /api/ai/screen`)**:
   - Cross-analyzes candidate resume data against job specifications.
   - Generates an objective 0–100% score, verified strengths, skill gaps, reasoning narrative, and suggested technical questions.
2. **Interactive AI Mock Interview Coach (`POST /api/ai/interview/*`)**:
   - Manages conversational interview sessions for any technical role.
   - Evaluates answers using STAR criteria, gives constructive feedback tips, and saves every transcript turn to disk.
   - Dynamically adapts when candidates request specific topics (e.g. OOPs, system design, databases).
3. **AI Job Spec Generator (`POST /api/ai/generate-job`)**:
   - Auto-generates full job descriptions, bulleted requirements, and salary benchmarks for recruiters.

---

## ⚡ Real-Time Event Bus (Server-Sent Events)

HireSphere AI utilizes a native Server-Sent Events (SSE) broadcaster (`RealtimeEventService`):
- Connect at `GET /api/realtime/stream`.
- Automatically broadcasts events (`APPLICATION_CREATED`, `APPLICATION_STATUS_UPDATED`, `INTERVIEW_SCHEDULED`, `AI_ANALYSIS_COMPLETED`) to all connected client browsers.
- Ensures zero manual page reloads for both candidates and recruiters.

---

## 📂 Project Architecture

```
backend/hiresphere-backend/
├── src/
│   ├── main/
│   │   ├── java/com/hiresphere/
│   │   │   ├── ai/
│   │   │   │   └── GeminiAiService.java     # Gemini 3.1 integration & autonomous heuristics
│   │   │   ├── controller/
│   │   │   │   ├── AiController.java        # AI Screening & Mock Interview endpoints
│   │   │   │   ├── ApplicationController.java# Application workflow endpoints
│   │   │   │   ├── AuthController.java       # User authentication & registration
│   │   │   │   ├── JobController.java        # Job creation & search endpoints
│   │   │   │   ├── RealtimeStreamController.java # SSE streaming controller
│   │   │   │   └── UserController.java       # Profile endpoints
│   │   │   ├── model/                       # Data entities (User, Job, Application, etc.)
│   │   │   ├── realtime/
│   │   │   │   └── RealtimeEventService.java# Multi-client SSE emitter registry
│   │   │   ├── store/
│   │   │   │   └── DataStore.java           # Thread-safe persistent DBMS store
│   │   │   └── HireSphereBackendApplication.java # Spring Boot main entry
│   │   └── resources/
│   │       └── application.properties       # Spring & AI configuration
│   └── test/                                # JUnit unit & integration tests
├── data/                                    # Persistent DBMS file storage (.mv.db & .json)
├── Dockerfile                               # Multi-stage production container build
├── pom.xml                                  # Maven dependencies & plugins
├── mvnw & mvnw.cmd                          # Maven wrapper binaries
└── .env.example                             # Environment template (Zero secrets)
```

---

## 📋 Comprehensive API Route Reference

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/health` | Health & diagnostics check | Public |
| `POST` | `/api/auth/register` | Register new user account | Public |
| `POST` | `/api/auth/login` | Sign in & retrieve user object | Public |
| `GET` | `/api/jobs` | Retrieve all active job vacancies | Public |
| `POST` | `/api/jobs` | Post new job vacancy | Recruiter |
| `GET` | `/api/jobs/{id}` | Get full job specifications | Public |
| `POST` | `/api/applications` | Apply for a job vacancy | Candidate |
| `GET` | `/api/applications/my` | View candidate's applications | Candidate |
| `GET` | `/api/applications/job/{jobId}` | View applicants for a specific job | Recruiter |
| `PUT` | `/api/applications/{id}/status` | Move pipeline stage & emit SSE | Recruiter |
| `POST` | `/api/ai/screen` | Autonomous candidate screening | Recruiter |
| `POST` | `/api/ai/interview/start` | Initialize AI mock interview chamber | Candidate |
| `POST` | `/api/ai/interview/message` | Submit turn, evaluate, and get feedback | Candidate |
| `GET` | `/api/ai/interview/session/{id}` | Retrieve complete interview transcript | Candidate/Recruiter |
| `POST` | `/api/ai/generate-job` | Generate job description via AI | Recruiter |
| `GET` | `/api/realtime/stream` | Persistent Server-Sent Events stream | Public/Client |

---

## 💾 Persistent Database (DBMS) Details

All platform transactions persist permanently to disk:
- **Storage Location**: `./data/hiresphere_realtime_db.mv.db` and `./data/hiresphere_realtime_db.json`.
- **Durability**: Zero data loss upon server restart or redeployment.
- **Embedded Web Console**: Access `http://localhost:8085/h2-console` with user `sa` and empty password.

---

## 🛠️ Local Development Setup

### Prerequisites
- **Java**: OpenJDK 17 LTS or higher (`java -version`)
- **Maven**: 3.8+ (or use included `./mvnw`)

### Build & Run
```bash
# 1. Clone repository
git clone https://github.com/<your-username>/HireSphereAI.git
cd HireSphereAI/backend/hiresphere-backend

# 2. Compile and package application
./mvnw clean package -DskipTests

# 3. Start Spring Boot microservice
./mvnw spring-boot:run
```

The server starts on port **`8085`** (accessible at `http://localhost:8085`).

### Running via Docker
```bash
# Build container image
docker build -t hiresphere-backend .

# Run container
docker run -p 8085:8085 -e SERVER_PORT=8085 hiresphere-backend
```

---

## ⚙️ Environment Configuration (`.env.example`)

Create a `.env` file from the clean template below:

```env
SERVER_PORT=8085
APP_NAME=hiresphere-backend

# Persistent DBMS Engine
DB_URL=jdbc:h2:file:./data/hiresphere_realtime_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
DB_USERNAME=sa
DB_PASSWORD=
H2_CONSOLE_ENABLED=true
H2_CONSOLE_PATH=/h2-console
DATA_FILE_PATH=./data/hiresphere_realtime_db.json

# Gemini AI (Optional - uses built-in autonomous agent if omitted)
GEMINI_API_KEY=your_gemini_api_key_here
GEMINI_MODEL=gemini-3.1-flash-lite
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent

# Real-Time & Security
REALTIME_STREAM_PATH=/api/realtime/stream
CORS_ALLOWED_ORIGINS=http://localhost:5175,http://127.0.0.1:5175
```

---

## 🔑 Demo Test Accounts

| Role | Email | Password |
| :--- | :--- | :--- |
| **Candidate** | `candidate@example.com` | `candidate123` |
| **Recruiter** | `recruiter@example.com` | `recruiter123` |
| **Admin** | `admin@example.com` | `admin123` |

---

## 📄 License
This project is licensed under the MIT License &mdash; see the LICENSE file for details.
