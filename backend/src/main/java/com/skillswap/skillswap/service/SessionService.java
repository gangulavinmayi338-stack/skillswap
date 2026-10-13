package com.skillswap.skillswap.service;

import com.skillswap.skillswap.entity.Session;
import com.skillswap.skillswap.entity.SessionStatus;
import com.skillswap.skillswap.entity.Skill;
import com.skillswap.skillswap.entity.User;
import com.skillswap.skillswap.repository.SessionRepository;
import com.skillswap.skillswap.repository.SkillRepository;
import com.skillswap.skillswap.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final ConnectionService connectionService;

    public SessionService(
            SessionRepository sessionRepository,
            UserRepository userRepository,
            SkillRepository skillRepository,
            ConnectionService connectionService
    ) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.connectionService = connectionService;
    }

    public Session createSession(
            Long teacherId,
            Long learnerId,
            Long skillId,
            Session sessionRequest
    ) {

        if (teacherId == null || learnerId == null || skillId == null) {
            throw new RuntimeException("Teacher, learner, and skill ids are required");
        }

        if (Objects.equals(teacherId, learnerId)) {
            throw new RuntimeException(
                    "Teacher and learner cannot be the same user"
            );
        }

        if (sessionRequest == null) {
            throw new RuntimeException("Session request is required");
        }

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Teacher not found with id: " + teacherId
                        )
                );

        User learner = userRepository.findById(learnerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Learner not found with id: " + learnerId
                        )
                );

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Skill not found with id: " + skillId
                        )
                );

        if (!connectionService.areUsersConnected(
                teacherId,
                learnerId
        )) {
            throw new RuntimeException(
                    "Teacher and learner must be connected before scheduling a session"
            );
        }

        if (sessionRequest.getSessionDate() == null) {
            throw new RuntimeException(
                    "Session date is required"
            );
        }

        if (sessionRequest.getSessionDate()
                .isBefore(LocalDate.now())) {
            throw new RuntimeException(
                    "Session date cannot be in the past"
            );
        }

        if (sessionRequest.getStartTime() == null) {
            throw new RuntimeException(
                    "Session start time is required"
            );
        }

        if (sessionRequest.getDuration() == null
                || sessionRequest.getDuration() <= 0) {
            throw new RuntimeException(
                    "Session duration must be greater than 0"
            );
        }

        if (sessionRequest.getMode() == null
                || sessionRequest.getMode().trim().isEmpty()) {
            throw new RuntimeException(
                    "Session mode is required"
            );
        }

        Session session = new Session();

        session.setTeacher(teacher);
        session.setLearner(learner);
        session.setSkill(skill);
        session.setSessionDate(
                sessionRequest.getSessionDate()
        );
        session.setStartTime(
                sessionRequest.getStartTime()
        );
        session.setDuration(
                sessionRequest.getDuration()
        );
        session.setMode(
                sessionRequest.getMode().trim()
        );
        session.setStatus(
                SessionStatus.REQUESTED
        );

        return sessionRepository.save(session);
    }

    public List<Session> getUserSessions(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException(
                    "User not found with id: " + userId
            );
        }

        return sessionRepository
                .findByTeacherIdOrLearnerId(
                        userId,
                        userId
                );
    }

    public Session getSessionById(Long sessionId) {

        return sessionRepository.findById(sessionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Session not found with id: " + sessionId
                        )
                );
    }

    public Session confirmSession(Long sessionId) {

        Session session =
                getSessionById(sessionId);

        if (session.getStatus()
                != SessionStatus.REQUESTED) {
            throw new RuntimeException(
                    "Only requested sessions can be confirmed"
            );
        }

        session.setStatus(
                SessionStatus.CONFIRMED
        );

        return sessionRepository.save(session);
    }

    public Session completeSession(Long sessionId) {

        Session session =
                getSessionById(sessionId);

        if (session.getStatus()
                != SessionStatus.CONFIRMED) {
            throw new RuntimeException(
                    "Only confirmed sessions can be completed"
            );
        }

        session.setStatus(
                SessionStatus.COMPLETED
        );

        return sessionRepository.save(session);
    }

    public Session cancelSession(Long sessionId) {

        Session session =
                getSessionById(sessionId);

        if (session.getStatus()
                == SessionStatus.COMPLETED) {
            throw new RuntimeException(
                    "Completed sessions cannot be cancelled"
            );
        }

        if (session.getStatus()
                == SessionStatus.CANCELLED) {
            throw new RuntimeException(
                    "Session is already cancelled"
            );
        }

        session.setStatus(
                SessionStatus.CANCELLED
        );

        return sessionRepository.save(session);
    }

    public long getCompletedSessionCount(Long userId) {

        long teacherCompleted =
                sessionRepository
                        .countByTeacherIdAndStatus(
                                userId,
                                SessionStatus.COMPLETED
                        );

        long learnerCompleted =
                sessionRepository
                        .countByLearnerIdAndStatus(
                                userId,
                                SessionStatus.COMPLETED
                        );

        return teacherCompleted + learnerCompleted;
    }
}