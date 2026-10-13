package com.skillswap.skillswap.repository;

import com.skillswap.skillswap.entity.Session;
import com.skillswap.skillswap.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByTeacherIdOrLearnerId(
            Long teacherId,
            Long learnerId
    );

    List<Session> findByTeacherIdAndStatus(
            Long teacherId,
            SessionStatus status
    );

    List<Session> findByLearnerIdAndStatus(
            Long learnerId,
            SessionStatus status
    );

    long countByTeacherIdAndStatus(
            Long teacherId,
            SessionStatus status
    );

    long countByLearnerIdAndStatus(
            Long learnerId,
            SessionStatus status
    );
}