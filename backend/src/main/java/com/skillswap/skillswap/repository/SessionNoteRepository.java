package com.skillswap.skillswap.repository;
import com.skillswap.skillswap.entity.SessionNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SessionNoteRepository extends JpaRepository<SessionNote, Long> { List<SessionNote> findBySessionIdOrderByUpdatedAtDesc(Long sessionId); }
