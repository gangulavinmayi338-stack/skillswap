package com.skillswap.skillswap.service;

import com.skillswap.skillswap.config.LiveClassWebSocketHandler;
import com.skillswap.skillswap.entity.*;
import com.skillswap.skillswap.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QuizService {
    private final QuizQuestionRepository questions;
    private final QuizAttemptRepository attempts;
    private final SessionRepository sessions;
    private final UserRepository users;
    private final LiveClassWebSocketHandler socket;

    public QuizService(
            QuizQuestionRepository questions,
            QuizAttemptRepository attempts,
            SessionRepository sessions,
            UserRepository users,
            LiveClassWebSocketHandler socket
    ) {
        this.questions = questions;
        this.attempts = attempts;
        this.sessions = sessions;
        this.users = users;
        this.socket = socket;
    }

    private Session session(Long id) {
        return sessions.findById(id).orElseThrow(() -> new RuntimeException("Session not found"));
    }

    private void participant(Session s, Long userId) {
        if (!Objects.equals(s.getTeacher().getId(), userId)
                && !Objects.equals(s.getLearner().getId(), userId)) {
            throw new RuntimeException("Only session participants can access this quiz");
        }
    }

    public Map<String, Object> getQuiz(Long sessionId, Long userId) {
        Session s = session(sessionId);
        participant(s, userId);
        List<QuizQuestion> qs = questions.findBySessionIdOrderByIdAsc(sessionId);
        List<Map<String, Object>> safe = qs.stream().map(q -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", q.getId());
            m.put("question", q.getQuestion());
            m.put("optionA", q.getOptionA());
            m.put("optionB", q.getOptionB());
            m.put("optionC", q.getOptionC());
            m.put("optionD", q.getOptionD());
            return m;
        }).toList();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("questions", safe);
        out.put("available", s.getStatus() == SessionStatus.COMPLETED && !qs.isEmpty());
        out.put("attempt", attempts.findBySessionIdAndUserId(sessionId, userId).orElse(null));
        return out;
    }

    public QuizQuestion addQuestion(Long sessionId, Long userId, Map<String, Object> b) {
        Session s = session(sessionId);
        participant(s, userId);
        if (!Objects.equals(s.getTeacher().getId(), userId)) {
            throw new RuntimeException("Only the teacher can create quiz questions");
        }
        QuizQuestion q = new QuizQuestion();
        q.setSession(s);
        q.setQuestion(req(b, "question"));
        q.setOptionA(req(b, "optionA"));
        q.setOptionB(req(b, "optionB"));
        q.setOptionC(req(b, "optionC"));
        q.setOptionD(req(b, "optionD"));
        String correct = req(b, "correctOption").toUpperCase();
        if (!List.of("A", "B", "C", "D").contains(correct)) {
            throw new RuntimeException("Correct option must be A, B, C, or D");
        }
        q.setCorrectOption(correct);
        QuizQuestion saved = questions.save(q);
        socket.broadcastEvent(sessionId, Map.of("type", "quiz-refresh"));
        return saved;
    }

    public void deleteQuestion(Long questionId, Long userId) {
        QuizQuestion q = questions.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        participant(q.getSession(), userId);
        if (!Objects.equals(q.getSession().getTeacher().getId(), userId)) {
            throw new RuntimeException("Only the teacher can delete quiz questions");
        }
        Long sessionId = q.getSession().getId();
        questions.delete(q);
        socket.broadcastEvent(sessionId, Map.of("type", "quiz-refresh"));
    }

    public QuizAttempt submit(Long sessionId, Long userId, Map<String, Object> answers) {
        Session s = session(sessionId);
        participant(s, userId);
        if (s.getStatus() != SessionStatus.COMPLETED) {
            throw new RuntimeException("Quiz becomes available after the session is completed");
        }
        if (attempts.findBySessionIdAndUserId(sessionId, userId).isPresent()) {
            throw new RuntimeException("Quiz already submitted");
        }
        List<QuizQuestion> qs = questions.findBySessionIdOrderByIdAsc(sessionId);
        if (qs.isEmpty()) throw new RuntimeException("No quiz questions available");
        int score = 0;
        for (QuizQuestion q : qs) {
            Object a = answers.get(String.valueOf(q.getId()));
            if (a != null && q.getCorrectOption().equalsIgnoreCase(a.toString())) score++;
        }
        QuizAttempt at = new QuizAttempt();
        at.setSession(s);
        at.setUser(users.findById(userId).orElseThrow(() -> new RuntimeException("User not found")));
        at.setScore(score);
        at.setTotal(qs.size());
        QuizAttempt saved = attempts.save(at);
        socket.broadcastEvent(sessionId, Map.of("type", "quiz-refresh"));
        return saved;
    }

    private String req(Map<String, Object> b, String k) {
        Object v = b.get(k);
        if (v == null || v.toString().isBlank()) throw new RuntimeException(k + " is required");
        return v.toString().trim();
    }
}
