package com.skillswap.skillswap.controller;

import com.skillswap.skillswap.entity.*;
import com.skillswap.skillswap.repository.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/admin") @CrossOrigin(origins="http://localhost:5173")
public class AdminController {
    private final UserRepository users; private final SessionRepository sessions; private final SkillRepository skills; private final ConnectionRepository connections; private final RatingRepository ratings; private final QuizAttemptRepository attempts;
    public AdminController(UserRepository users,SessionRepository sessions,SkillRepository skills,ConnectionRepository connections,RatingRepository ratings,QuizAttemptRepository attempts){this.users=users;this.sessions=sessions;this.skills=skills;this.connections=connections;this.ratings=ratings;this.attempts=attempts;}
    @GetMapping("/dashboard") public Map<String,Object> dashboard(){Map<String,Object>m=new LinkedHashMap<>();m.put("totalUsers",users.count());m.put("totalSkills",skills.count());m.put("totalSessions",sessions.count());m.put("completedSessions",sessions.findAll().stream().filter(s->s.getStatus()==SessionStatus.COMPLETED).count());m.put("activeConnections",connections.findAll().stream().filter(c->c.getStatus()==ConnectionStatus.ACCEPTED).count());m.put("ratings",ratings.count());m.put("quizAttempts",attempts.count());m.put("users",users.findAll());m.put("sessions",sessions.findAll());m.put("skills",skills.findAll());return m;}
}
