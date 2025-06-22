# GitHub Repos Observer

A full-stack application for searching and scoring GitHub repositories with **GitHub dark theme UI**, built with Angular frontend and Spring Boot backend using **Project Loom Virtual Threads**.

## ✨ Features

- 🔍 **Smart Repository Search** with keywords, language, and date filters
- ⭐ **Popularity Scoring** based on stars, forks, and recency
- 🎨 **GitHub Dark Theme** with authentic styling and centered layout
- ⚡ **High Performance** with Java 21 Virtual Threads and concurrent API calls
- 🛡️ **Rate Limiting** (0.8 requests/second) with retry logic for 403 errors
- ⏱️ **Timeout Protection** (2-minute timeout) for long-running searches
- 📱 **Responsive Design** with Angular Material components

## 🏗️ Architecture

```
github-repos-observer/
├── frontend/          # Angular 19 with Material Design & GitHub dark theme
├── backend/           # Spring Boot 3.4 with Virtual Threads
└── start-local.sh     # Docker Compose startup script
```

## 🚀 Quick Start

### Prerequisites

- **Docker** and **Docker Compose**
- **Node.js** >= 18.0.0 (for development)
- **Java** >= 21 (for development)

### Environment Setup

Create the GitHub API token file:

```bash
# Create environment file
mkdir -p backend/env
echo "GITHUB_API_TOKEN=your_github_token_here" > backend/env/prod.env
```

### Running with Docker (Recommended)

```bash
# Start both frontend and backend
./start-local.sh

# Access the application
# Frontend: http://localhost:4200
# Backend:  http://localhost:8080
```

### Development Mode

```bash
# Frontend development
cd frontend
npm install
npm start  # http://localhost:4200

# Backend development  
cd backend
./gradlew bootRun  # http://localhost:8080
```

### Building

```bash
# Frontend build
cd frontend && npm run build

# Backend build (includes tests)
cd backend && ./gradlew build
```

### Testing

```bash
# Frontend tests
cd frontend
npm test          # Unit tests (headless)
npm run e2e       # E2E tests with Playwright

# Backend tests
cd backend
./gradlew test    # Unit and integration tests
./gradlew build   # Full build with tests
```

## 📁 Project Structure

### Frontend (Angular 19)
- **Standalone components** with Angular Material
- **GitHub dark theme** styling with authentic colors
- **Reactive forms** with validation (maxPages ≤ 5)
- **HTTP timeout** protection (2-minute timeout)
- **RxJS** for state management
- **Playwright** for E2E testing

### Backend (Spring Boot 3.4)
- **Virtual Threads** for high-performance concurrency
- **Rate limiting** (0.8 requests/second) with exponential backoff
- **Retry logic** for 403 GitHub API errors
- **Input validation** (maxPages ≤ 5, keywords 1-50 chars)
- **Popularity scoring** algorithm for repository ranking

## 🛠️ Available Scripts

### Docker Commands
| Command | Description |
|---------|-------------|
| `./start-local.sh` | Start both services with Docker Compose |

### Frontend Commands
| Command | Description |
|---------|-------------|
| `npm start` | Start Angular dev server |
| `npm run build` | Build for production |
| `npm test` | Run unit tests (headless) |
| `npm run e2e` | Run E2E tests with Playwright |
| `npm run format` | Format code with Prettier |

### Backend Commands
| Command | Description |
|---------|-------------|
| `./gradlew bootRun` | Start Spring Boot dev server |
| `./gradlew build` | Build and run all tests |
| `./gradlew test` | Run tests only |

## 🔧 Configuration

### Environment Variables

Create the environment file:

```bash
# backend/env/prod.env
GITHUB_API_TOKEN=your_github_token_here
```

### API Endpoints

- **Frontend**: http://localhost:4200 (GitHub dark theme UI)
- **Backend**: http://localhost:8080 (REST API)
- **Search API**: `GET /api/search?keywords=...&maxPages=5`

### Search Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `keywords` | String (1-50) | ✅ Yes | Search keywords |
| `language` | String | ❌ No | Programming language filter |
| `earliestCreatedDate` | ISO Date | ❌ No | Filter by creation date |
| `maxPages` | Integer (≤5) | ❌ No | Max GitHub pages to fetch (default: 5) |

## 🧪 Testing Strategy

- **Frontend**: Unit tests (Karma/Jasmine) + E2E tests (Playwright)
- **Backend**: Unit tests + Integration tests (26 tests total)
- **Concurrency Testing**: Virtual threads performance validation
- **Rate Limiting**: 403 error handling and retry logic verification
- **Timeout Testing**: Long-running request handling

## 📚 Documentation

- [Frontend Documentation](./frontend/README.md)
- [Backend Documentation](./backend/README.md)

## 🚀 Deployment

### Docker Deployment (Current)

```bash
# Start with Docker Compose
./start-local.sh

# Services will be available at:
# Frontend: http://localhost:4200
# Backend:  http://localhost:8080
```

### Manual Deployment

```bash
# Build frontend
cd frontend && npm run build

# Build backend
cd backend && ./gradlew build

# Run backend
java -jar backend/build/libs/*.jar
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔗 Links

- [Angular Documentation](https://angular.io/docs)
- [Angular Material](https://material.angular.io/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Project Loom (Virtual Threads)](https://openjdk.org/projects/loom/)
- [GitHub API Documentation](https://docs.github.com/en/rest) 