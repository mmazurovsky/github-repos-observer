# 🚀 GitHub Repositories Popularity Scorer

A **Spring Boot WebFlux** backend that fetches and ranks GitHub repositories based on popularity. Popularity is calculated using ⭐ stars, 🍴 forks, and ⏱️ recency of updates.

---

## 📘 Project Context

Implements a single reactive **GET** endpoint to fulfill the coding challenge:

- User provides **keywords**
- Can filter by **earliest creation date** and **language**
- Backend assigns a **popularity score**

---

## ⚙️ Prerequisites

| Tool       | Version    |
|------------|------------|
| Java       | 21         |



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

| Parameter            | Type          | Required | Description                                                           |
|----------------------|---------------|----------|-----------------------------------------------------------------------|
| `keywords`           | String (1–50) | ✅ Yes   | Search keywords                                                       |
| `earliestCreatedDate`| ISO date      | ❌ No    | Filter for repositories created after this date                       |
| `language`           | String        | ❌ No    | Programming language (only one)                                       |
| `maxPages`           | Integer ≤ 10  | ❌ No    | Max number of GitHub result pages to query (100 results per page max) |

---

## 🧪 Example Request

```
http://localhost:8080/api/search?keywords=bot&earliestCreatedDate=2020-01-01&language=Java
```



