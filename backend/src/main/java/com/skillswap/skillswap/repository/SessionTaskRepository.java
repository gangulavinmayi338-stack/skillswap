package com.skillswap.skillswap.repository;
import com.skillswap.skillswap.entity.SessionTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SessionTaskRepository extends JpaRepository<SessionTask, Long> { List<SessionTask> findBySessionIdOrderByIdAsc(Long sessionId); }
