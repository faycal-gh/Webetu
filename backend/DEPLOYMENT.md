# Deployment Guide

## Koyeb Deployment Setup

### Prerequisites

1. **GitHub Account** with repository access
2. **Koyeb Account** at [koyeb.com](https://koyeb.com)
3. **GitHub Container Registry (GHCR)** access

### Required GitHub Secrets

Configure these secrets in your GitHub repository settings (`Settings > Secrets and variables > Actions`):

| Secret Name | Description |
|-------------|-------------|
| `JWT_SECRET` | 256-bit Base64-encoded JWT signing key. Generate with: `openssl rand -base64 32` |
| `KOYEB_TOKEN` | Koyeb API token from [Koyeb dashboard](https://app.koyeb.com/account/api) |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed origins (e.g., `https://your-frontend.koyeb.app`) |
| `KOYEB_APP_URL` | Your Koyeb app URL for health checks (e.g., `https://progress-api.koyeb.app`) |

### Initial Koyeb Setup

1. **Create Koyeb App** (first-time only):
   ```bash
   # Install Koyeb CLI
   curl -fsSL https://raw.githubusercontent.com/koyeb/koyeb-cli/master/install.sh | bash
   
   # Login
   koyeb login
   
   # Create app
   koyeb app create progress-api
   
   # Create service
   koyeb service create progress-api \
     --app progress-api \
     --docker ghcr.io/YOUR_GITHUB_USERNAME/progress-rebuild/progress-api:latest \
     --docker-private-registry-secret ghcr-secret \
     --env JWT_SECRET=YOUR_JWT_SECRET \
     --env CORS_ALLOWED_ORIGINS=https://your-frontend.koyeb.app \
     --env SPRING_PROFILES_ACTIVE=prod \
     --ports 8080:http \
     --routes /:8080 \
     --checks 8080:http:/actuator/health \
     --regions fra \
     --instance-type nano
   ```

2. **Create GHCR Secret in Koyeb**:
   ```bash
   koyeb secret create ghcr-secret \
     --type registry \
     --url ghcr.io \
     --username YOUR_GITHUB_USERNAME \
     --password YOUR_GITHUB_PAT
   ```

### CI/CD Pipeline Flow

```
Push to main → GitHub Actions CI → Build & Test → OWASP Scan → Build Docker Image → Push to GHCR → Deploy to Koyeb
```

### Manual Deployment

```bash
# Build locally
cd backend
docker build -t progress-api .

# Run locally with production profile
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e JWT_SECRET=$(openssl rand -base64 32) \
  -e CORS_ALLOWED_ORIGINS=http://localhost:3000 \
  progress-api
```

### Health Check

```bash
curl https://YOUR_APP.koyeb.app/actuator/health
# Expected: {"status":"UP"}
```

### Monitoring

- **Koyeb Dashboard**: View logs, metrics, and deployments
- **API Documentation**: `https://YOUR_APP.koyeb.app/swagger-ui/index.html`
