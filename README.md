# SoundFlow — Hackathon Spotify-style Clone

## Stack
- Frontend: React + Vite
- Backend: Java 17 + Spring Boot + Maven
- Database: MySQL + Spring Data JPA
- Redis: shared Redis instance
- Authentication: JWT

## API contract
POST /api/auth/login
POST /api/auth/register
GET  /api/songs
GET  /api/search?q=
POST /api/play
GET  /api/playlists
POST /api/playlists
GET  /api/profile
GET  /api/history

## Rate limiter integration
The backend contains `RateLimiterContractFilter`.

The intended pipeline is:

Client
  -> Authentication Filter
  -> RateLimiter Filter
  -> Controller
  -> Service
  -> MySQL / external music API

Your friend's rate limiter should be called from the TODO section in:
backend/src/main/java/com/hackathon/spotify/ratelimit/RateLimiterContractFilter.java

Identifiers:
- login: request IP
- authenticated APIs: authenticated userId + endpoint

Plan must be read from User in the database:
FREE / PRO / PREMIUM

Do not trust plan/userId from frontend request data.

When the limiter rejects:
HTTP 429
X-Ratelimit-Limit
X-Ratelimit-Remaining
X-Ratelimit-Retry-After

Frontend already handles 429 on play requests with:
"Too many requests, please try again later"

## Fixed rate-limit requirements

FREE
- Login 5/min
- Search 10/min
- Play 20/min
- Playlist 5/min
- Profile/History 10/min

PRO
- Login 10/min
- Search 50/min
- Play 100/min
- Playlist 20/min
- Profile/History 50/min

PREMIUM
- Login 20/min
- Search 100/min
- Play 200/min
- Playlist 50/min
- Profile/History 100/min

Algorithms:
- LOGIN -> Fixed Window Counter
- SEARCH + PLAY -> Token Bucket
- PLAYLIST + PROFILE + HISTORY -> Sliding Window Counter

## Run

### 1. Start infrastructure
From project root:
docker compose up -d

### 2. Start backend
cd backend
mvn spring-boot:run

Backend:
http://localhost:8080

### 3. Start frontend
cd frontend
npm install
npm run dev

Frontend:
http://localhost:5173

## Important
This project intentionally does NOT implement the rate-limiting algorithms. The filter is the integration point for the separate Rate Limiter module.
