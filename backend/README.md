# 🚀 GitHub Repositories Popularity Scorer

A **Spring Boot Web** backend with **Project Loom Virtual Threads** that fetches and ranks GitHub repositories based on popularity. Popularity is calculated using ⭐ stars, 🍴 forks, and ⏱️ recency of updates.

---

## 📘 Project Context

Implements a single **GET** endpoint with virtual thread concurrency:

- User provides **keywords**
- Can filter by **earliest creation date** and **language**
- Backend assigns a **popularity score**
- Uses **Java 21 Virtual Threads** for high-performance concurrent API calls

---

## ⚙️ Prerequisites

| Tool   | Version |
| ------ | ------- |
| Java   | 21      |
| Gradle | 8.4+    |

---

## 🔐 Environment Setup

Create a file named `prod.env` inside a folder called `env` at the project root.

env/prod.env

Content of `prod.env`:

```env
GITHUB_API_TOKEN=<your_token_here>
```

❗ This token is required for accessing the GitHub Search API.

---

## ▶️ Run the App

```
./gradlew bootRun
```

---

## 🔎 API Usage

Endpoint

```
GET /api/search
```

Query Parameters

| Parameter             | Type          | Required | Description                                                           |
| --------------------- | ------------- | -------- | --------------------------------------------------------------------- |
| `keywords`            | String (1–50) | ✅ Yes   | Search keywords                                                       |
| `earliestCreatedDate` | ISO date      | ❌ No    | Filter for repositories created after this date                       |
| `language`            | String        | ❌ No    | Programming language (only one)                                       |
| `maxPages`            | Integer ≤ 5   | ❌ No    | Max number of GitHub result pages to query (100 results per page max) |

---

## 🧪 Example Request

```
http://localhost:8080/api/search?keywords=bot&earliestCreatedDate=2020-01-01&language=Java
```

---

## 🚀 Project Loom Features

This project uses **Java 21 Virtual Threads** for:

- **Concurrent API calls** to GitHub with minimal memory overhead
- **High-throughput request handling** using virtual thread executors
- **Blocking I/O operations** that don't block OS threads
- **Simplified concurrency model** compared to reactive programming

Virtual threads are automatically enabled in Spring Boot via:

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

---

## 🏗️ Architecture

- **Spring Boot Web** (not WebFlux) for traditional blocking endpoints
- **Virtual Thread Executor** for concurrent GitHub API pagination
- **Blocking HTTP client** with retry logic using exponential backoff
- **Standard exception handling** with `@ControllerAdvice`
