package com.skillswap.skillswap.repository;
import com.skillswap.skillswap.entity.SessionTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SessionTopicRepository extends JpaRepository<SessionTopic, Long> { List<SessionTopic> findBySessionIdOrderByIdAsc(Long sessionId); }
