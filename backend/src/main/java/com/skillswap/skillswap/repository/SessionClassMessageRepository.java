package com.skillswap.skillswap.repository;

import com.skillswap.skillswap.entity.SessionClassMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionClassMessageRepository extends JpaRepository<SessionClassMessage, Long> {
    List<SessionClassMessage> findBySessionIdOrderBySentAtAsc(Long sessionId);
}
