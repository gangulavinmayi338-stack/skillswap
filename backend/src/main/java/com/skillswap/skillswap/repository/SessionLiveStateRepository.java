package com.skillswap.skillswap.repository;

import com.skillswap.skillswap.entity.SessionLiveState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionLiveStateRepository extends JpaRepository<SessionLiveState, Long> {
    Optional<SessionLiveState> findBySessionId(Long sessionId);
}
