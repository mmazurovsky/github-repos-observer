# GitHub Repos Observer

A full-stack application for searching and scoring GitHub repositories, built with Angular frontend and Spring Boot backend.

## 🏗️ Architecture

```
github-repos-observer/
├── frontend/          # Angular 19 application
├── backend/           # Spring Boot 3.4 application  
├── docs/             # Documentation
└── scripts/          # Build and deployment scripts
```

## 🚀 Quick Start

### Prerequisites

- **Node.js** >= 18.0.0
- **Java** >= 21
- **npm** >= 9.0.0

### Installation

```bash
# Install all dependencies
npm run install:all

# Or install individually
npm install                    # Root + frontend dependencies
cd backend && ./gradlew build  # Backend dependencies
```

### Development

```bash
# Start both frontend and backend in development mode
npm run dev

# Or start individually
npm run dev:frontend  # Starts Angular dev server on http://localhost:4200
npm run dev:backend   # Starts Spring Boot on http://localhost:8080
```

### Building

```bash
# Build everything
npm run build

# Build individually  
npm run build:frontend
npm run build:backend
```

### Testing

```bash
# Run all tests
npm test

# Run specific test suites
npm run test:frontend  # Angular unit tests
npm run test:backend   # Spring Boot tests  
npm run test:e2e      # Playwright E2E tests
```

## 📁 Project Structure

### Frontend (Angular 19)
- **Standalone components** architecture
- **Reactive forms** for user input
- **RxJS** for state management
- **Playwright** for E2E testing
- **Karma/Jasmine** for unit testing

### Backend (Spring Boot 3.4)
- **WebFlux** for reactive programming
- **Spring Retry** for resilient API calls
- **Validation** for input sanitization
- **JUnit 5** for testing

## 🛠️ Available Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start both frontend and backend |
| `npm run build` | Build production bundles |
| `npm test` | Run all tests |
| `npm run clean` | Clean all build artifacts |
| `npm run lint` | Run linting |
| `npm run format` | Format code with Prettier |

## 🔧 Configuration

### Environment Variables

Create a `.env` file in the backend directory:

```bash
GITHUB_API_TOKEN=your_github_token_here
```

### API Endpoints

- **Frontend**: http://localhost:4200
- **Backend**: http://localhost:8080
- **API Docs**: http://localhost:8080/docs (when implemented)

## 🧪 Testing Strategy

- **Unit Tests**: Components, services, and business logic
- **Integration Tests**: API endpoints and database interactions  
- **E2E Tests**: Complete user journeys
- **Test Organization**: Separate `tests/` directory for clean structure

## 📚 Documentation

- [Frontend Documentation](./frontend/README.md)
- [Backend Documentation](./backend/README.md)
- [API Documentation](./docs/api.md) (when available)
- [Deployment Guide](./docs/deployment.md) (when available)

## 🚀 Deployment

### Production Build

```bash
npm run build
```

### Docker (when implemented)

```bash
docker-compose up --build
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
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [GitHub API Documentation](https://docs.github.com/en/rest) 