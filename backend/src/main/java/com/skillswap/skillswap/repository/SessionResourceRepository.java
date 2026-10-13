package com.skillswap.skillswap.repository;
import com.skillswap.skillswap.entity.SessionResource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SessionResourceRepository extends JpaRepository<SessionResource, Long> { List<SessionResource> findBySessionIdOrderByCreatedAtDesc(Long sessionId); }
