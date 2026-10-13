# SkillSwap — Final Project Edition

SkillSwap is a full-stack peer-to-peer student skill exchange platform built with React, Spring Boot, Spring Data JPA, MySQL, REST APIs, and WebSockets.

## Final-project features

- Registration and login with BCrypt password hashing
- User profile and teach/learn skill management
- Availability management
- Intelligent weighted matching engine
- Connection requests and acceptance/rejection
- Real-time direct messaging with online status and typing indicators
- Session scheduling and session lifecycle
- **Live Interactive Classroom**
  - Teacher starts/ends a live class
  - Teacher/learner online presence
  - Synchronized class timer
  - Live topic checklist and progress
  - Live shared-note drafting and saved notes
  - Real-time tasks/checklists
  - Real-time resource sharing
  - Persistent classroom chat
  - Learner Raise Hand / Lower Hand
- **Post-session quiz**
  - Teacher prepares multiple-choice questions
  - Quiz unlocks when the teacher ends the class
  - Automatic scoring
  - One stored attempt per participant/session
- Ratings and feedback
- Progress dashboard
- Admin dashboard and platform statistics
- Responsive presentation-ready UI

## Important

Audio/video calling has been intentionally removed. The project uses WebSockets for real-time classroom collaboration and messaging instead.

## Database

Create a MySQL database:

```sql
CREATE DATABASE skillswap;
```

Set the database credentials as environment variables before starting the backend:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-local-mysql-password"
```

`application.properties` reads these values from `DB_USERNAME` and `DB_PASSWORD`;
credentials are not stored in the repository.

Hibernate is configured with `spring.jpa.hibernate.ddl-auto=update`, so the application creates/updates project tables automatically.

## Run backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Backend: `http://localhost:8080`

## Run frontend

Open another PowerShell terminal:

```powershell
cd frontend
npm install
npm run dev
```

Frontend: `http://localhost:5173`

## Deploy with Render and Vercel

The backend runs as a Docker web service on Render. The React/Vite frontend is
deployed separately to Vercel. Create a MySQL database that is reachable from
Render; the Render blueprint does not provision a database.

1. Push this repository to GitHub.
2. Create a managed MySQL database with a publicly reachable TLS connection and
   create a database named `skillswap`.
3. In Render, choose **New → Blueprint**, connect this repository, and deploy
   the `render.yaml` blueprint. Set the prompted `DB_URL`, `DB_USERNAME`, and
   `DB_PASSWORD` values. For `APP_FRONTEND_URL`, enter the temporary origin
   `https://placeholder.vercel.app`; replace it with the real Vercel origin in
   step 6. For `DB_URL`, use the provider's JDBC URL, for example
   `jdbc:mysql://HOST:3306/skillswap?sslMode=REQUIRED&serverTimezone=UTC`.
   Render does not provide a managed MySQL service.
4. In Vercel, import the same GitHub repository and set the project root
   directory to `frontend`. Use the Vite framework preset, `npm run build` as the
   build command, and `dist` as the output directory.
5. Add the Vercel project environment variable `VITE_API_URL`, set to
   `https://YOUR-RENDER-SERVICE.onrender.com/api`, then deploy or redeploy the
   frontend.
6. Once Vercel provides the production URL, set Render's `APP_FRONTEND_URL` to
   that exact HTTPS origin (for example, `https://YOUR-PROJECT.vercel.app`) and
   redeploy/restart the Render service.
7. Verify `https://YOUR-RENDER-SERVICE.onrender.com/api/health`, then test
   registration, login, direct messages, and the interactive classroom over
   HTTPS/WebSockets.

The Vite setting also configures both WebSocket connections to use `wss://`
when the Render API uses HTTPS. The backend accepts browser and WebSocket
requests only from the configured frontend origin. Set local frontend
configuration by copying `frontend/.env.example` to `frontend/.env.local`.

**Demo/security note:** This project seeds a public demo administrator account
with the credentials shown below and currently permits API requests without
server-side authentication. Do not use real or sensitive data, or expose this
deployment as a production service, until the demo credentials and API
authorization are hardened.

## Admin account

- Email: `admin@skillswap.com`
- Password: `Admin@123`

Normal students can be created from the Register page.

## Best final presentation demo

Use two browser sessions:

1. Chrome normal window — log in as the teacher.
2. Chrome Incognito — log in as the learner.
3. Create/confirm a learning session.
4. Open **Interactive Class** from both accounts.
5. Teacher clicks **Start Live Class**.
6. Show both users appearing online and the synchronized class timer.
7. Add/toggle a topic — it appears on the other browser without refresh.
8. Type shared notes — the other browser sees the live draft.
9. Add a task or learning resource — the other browser updates immediately.
10. Learner clicks **Raise Hand** — teacher receives the live alert.
11. Exchange messages in **Class Chat**.
12. Teacher prepares quiz questions.
13. Teacher clicks **End Class & Unlock Quiz**.
14. Learner completes the post-session quiz and sees the score.
15. Show progress/rating and admin dashboards.

## Live classroom backend additions

- `LiveClassWebSocketHandler.java`
- `SessionLiveState.java`
- `SessionClassMessage.java`
- `SessionLiveService.java`
- `/ws/classroom`
- `/api/session-room/{sessionId}/live/start`
- `/api/session-room/{sessionId}/live/end`
- `/api/session-room/{sessionId}/chat`

## Project structure

```text
SkillSwap/
├── backend/
│   ├── src/main/java/com/skillswap/skillswap/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   └── service/
│   └── pom.xml
└── frontend/
    ├── src/
    │   ├── components/
    │   ├── pages/
    │   └── services/
    └── package.json
```
