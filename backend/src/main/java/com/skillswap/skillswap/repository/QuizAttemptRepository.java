package com.skillswap.skillswap.repository;
import com.skillswap.skillswap.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> { Optional<QuizAttempt> findBySessionIdAndUserId(Long sessionId, Long userId); long countByUserId(Long userId); }
