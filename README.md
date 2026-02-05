# Progress - Algerian Student Progress Tracker

[![Contribution Guidelines](https://img.shields.io/badge/Contributor%20Guide-READ-blue?style=for-the-badge&logo=github)](docs/CONTRIBUTING.md)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./backend/LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)
[![Next.js](https://img.shields.io/badge/Next.js-14-black.svg)](https://nextjs.org/)

A secure, production-ready application for Algerian students to track their academic progress. Built with **Spring Boot** backend and **Next.js** frontend.

---

##  Quick Start

Choose your preferred setup method:

### Option 1: Docker (Recommended for Production)

```bash
# Clone the repository
git clone https://github.com/faycal-gh/Progress-rebuild
cd Progress-rebuild

# Start both frontend and backend
docker-compose up --build
```

Access the application:
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **API Docs**: http://localhost:8080/swagger-ui/index.html

### Option 2: Local Development (Without Docker)

#### Prerequisites
- **Java 17** or higher
- **Node.js 18+** with **pnpm** (or npm)
- **Maven 3.9+** (optional - wrapper included)

#### Step 1: Start the Backend

```bash
cd backend

# Set environment variables (PowerShell)
# Generate a secret: openssl rand -base64 32
$env:JWT_SECRET = "REPLACE_WITH_YOUR_GENERATED_SECRET_KEY"
$env:CORS_ALLOWED_ORIGINS = "http://localhost:3000,http://localhost:5173"

# Or on Linux/Mac
export JWT_SECRET="REPLACE_WITH_YOUR_GENERATED_SECRET_KEY"
export CORS_ALLOWED_ORIGINS="http://localhost:3000,http://localhost:5173"

# Run the backend
mvn spring-boot:run
```

> **Note**: If you don't have Maven installed, you can copy `mvnw.cmd` (Windows) from a fresh Spring Boot project or install Maven globally.

Backend will be available at: **http://localhost:8080**

#### Step 2: Start the Frontend

Open a new terminal:

```bash
cd frontend

# Install dependencies
pnpm install

# Start development server
pnpm dev
```

Frontend will be available at: **http://localhost:3000**

---

##  Project Structure

```
Progress-rebuild/
├── backend/                    # Spring Boot API
│   ├── src/main/java/          # Java source code
│   ├── src/main/resources/     # Configuration files
│   ├── Dockerfile              # Docker build
│   ├── pom.xml                 # Maven dependencies
│   └── README.md               # Backend documentation
├── frontend/                   # Next.js Frontend
│   ├── src/app/                # App router pages
│   ├── src/components/         # React components
│   ├── src/contexts/           # React contexts
│   └── .env.local              # Frontend environment
├── docker-compose.yml          # Docker orchestration
└── README.md                   # This file
```

---

## ️ Environment Configuration

### Backend Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `JWT_SECRET` | **Yes*** | Dev key | Base64-encoded 256-bit secret for JWT |
| `CORS_ALLOWED_ORIGINS` | No | `localhost:3000,5173` | Comma-separated allowed origins |
| `GROQ_API_KEY` | No | - | API key for AI recommendations |
| `PORT` | No | `8080` | Server port |

> Generate a secure JWT secret: `openssl rand -base64 32`

### Frontend Environment Variables

Create `frontend/.env.local`:

```env
# API URL (same for Docker and local development)
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

---

##  API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/auth/login` | POST | Authenticate with Progres credentials |
| `/api/auth/logout` | POST | Logout and invalidate token |
| `/api/student/data` | GET | Get student academic data |
| `/api/student/exams/{id}` | GET | Get exam results |
| `/api/recommendations` | POST | Get AI major recommendations |
| `/actuator/health` | GET | Health check |
| `/swagger-ui/index.html` | GET | Interactive API docs |

> **External Documentation**: For detailed information about the upstream MESRS API, see [Progres API Documentation](./progres_api_docs.md).

---

## ️ Architecture

```
┌─────────────────┐          ┌─────────────────┐
│   Next.js       │  HTTP    │  Spring Boot    │
│   Frontend      │ ───────► │  Backend        │
│   (Port 3000)   │          │  (Port 8080)    │
└─────────────────┘          └────────┬────────┘
                                      │
                                      │ HTTPS
                                      ▼
                             ┌─────────────────┐
                             │  Progres API    │
                             │ mesrs.dz        │
                             └─────────────────┘
```

### Key Features

-  **JWT Authentication** with token blacklisting
-  **Rate Limiting** (100 requests per 15 minutes)
-  **CORS Protection** with configurable origins
-  **Security Headers** (CSP, HSTS, XSS Protection)
-  **AI Recommendations** using Groq LLM
-  **API Documentation** with Swagger UI

---

##  Docker Commands

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Rebuild after code changes
docker-compose up --build

# Remove all containers and volumes
docker-compose down -v
```

---

##  Testing

### Backend Tests

```bash
cd backend
mvn test
```

### Frontend Tests

```bash
cd frontend
pnpm test
```

---

##  Security

See [SECURITY.md](./backend/SECURITY.md) for security policies and reporting vulnerabilities.

### Production Checklist

- [ ] Set strong `JWT_SECRET` (min 256-bit)
- [ ] Configure specific `CORS_ALLOWED_ORIGINS`
- [ ] Use HTTPS in production
- [ ] Set `SPRING_PROFILES_ACTIVE=prod`
- [ ] Review rate limiting settings

---

##  Contributing

We welcome contributions! Please see [CONTRIBUTING.md](./CONTRIBUTING.md) for guidelines.

---



---



---

**Made with ️ for Algerian Students**
