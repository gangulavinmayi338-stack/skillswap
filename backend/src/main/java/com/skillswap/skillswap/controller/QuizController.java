package com.skillswap.skillswap.controller;

import com.skillswap.skillswap.entity.*;
import com.skillswap.skillswap.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/quizzes")
@CrossOrigin(origins="${APP_FRONTEND_URL:http://localhost:5173}")
public class QuizController {
    private final QuizService service;
    public QuizController(QuizService service){ this.service=service; }

    @GetMapping("/session/{sessionId}")
    public Map<String,Object> getQuiz(@PathVariable Long sessionId,@RequestParam Long userId){ return service.getQuiz(sessionId,userId); }

    @PostMapping("/session/{sessionId}/questions")
    public QuizQuestion addQuestion(@PathVariable Long sessionId,@RequestBody Map<String,Object> body){
        Object userValue=body.get("userId");
        if(userValue==null) throw new RuntimeException("userId is required");
        return service.addQuestion(sessionId,Long.valueOf(userValue.toString()),body);
    }

    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<Void> delete(@PathVariable Long questionId,@RequestParam Long userId){ service.deleteQuestion(questionId,userId); return ResponseEntity.noContent().build(); }

    @PostMapping("/session/{sessionId}/submit")
    public QuizAttempt submit(@PathVariable Long sessionId,@RequestBody Map<String,Object> body){
        Object userValue=body.get("userId");
        if(userValue==null) throw new RuntimeException("userId is required");
        Map<String,Object> answers=new HashMap<>();
        Object raw=body.get("answers");
        if(raw instanceof Map<?,?>){
            Map<?,?> map=(Map<?,?>)raw;
            for(Map.Entry<?,?> entry:map.entrySet()){ answers.put(String.valueOf(entry.getKey()),entry.getValue()); }
        }
        return service.submit(sessionId,Long.valueOf(userValue.toString()),answers);
    }
}
