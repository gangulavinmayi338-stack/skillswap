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
