package com.skillswap.skillswap.repository;
import com.skillswap.skillswap.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> { List<QuizQuestion> findBySessionIdOrderByIdAsc(Long sessionId); long countBySessionId(Long sessionId); }
