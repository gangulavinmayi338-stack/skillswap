package com.skillswap.skillswap.service;

import com.skillswap.skillswap.entity.Connection;
import com.skillswap.skillswap.entity.ConnectionStatus;
import com.skillswap.skillswap.entity.Session;
import com.skillswap.skillswap.entity.SessionStatus;
import com.skillswap.skillswap.entity.SkillType;
import com.skillswap.skillswap.entity.User;
import com.skillswap.skillswap.repository.ConnectionRepository;
import com.skillswap.skillswap.repository.SessionRepository;
import com.skillswap.skillswap.repository.UserRepository;
import com.skillswap.skillswap.repository.UserSkillRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final ConnectionRepository connectionRepository;
    private final SessionRepository sessionRepository;
    private final MatchingService matchingService;

    public DashboardService(
            UserRepository userRepository,
            UserSkillRepository userSkillRepository,
            ConnectionRepository connectionRepository,
            SessionRepository sessionRepository,
            MatchingService matchingService
    ) {
        this.userRepository = userRepository;
        this.userSkillRepository = userSkillRepository;
        this.connectionRepository = connectionRepository;
        this.sessionRepository = sessionRepository;
        this.matchingService = matchingService;
    }

    public Map<String, Object> getDashboard(Long userId) {

        if (userId == null) {
            throw new RuntimeException("User id is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: " + userId
                        )
                );

        int teachingSkills =
                userSkillRepository
                        .findByUserIdAndType(
                                userId,
                                SkillType.TEACH
                        )
                        .size();

        int learningSkills =
                userSkillRepository
                        .findByUserIdAndType(
                                userId,
                                SkillType.LEARN
                        )
                        .size();

        List<Connection> connections =
                connectionRepository
                        .findBySenderIdOrReceiverId(
                                userId,
                                userId
                        );

        long acceptedConnections =
                connections.stream()
                        .filter(connection ->
                                connection.getStatus()
                                        == ConnectionStatus.ACCEPTED
                        )
                        .count();

        List<Session> sessions =
                sessionRepository
                        .findByTeacherIdOrLearnerId(
                                userId,
                                userId
                        );

        long completedSessions =
                sessions.stream()
                        .filter(session ->
                                session.getStatus()
                                        == SessionStatus.COMPLETED
                        )
                        .count();

        int learningHours =
                sessions.stream()
                        .filter(session ->
                                session.getStatus()
                                        == SessionStatus.COMPLETED
                        )
                        .mapToInt(session ->
                                session.getDuration() == null
                                        ? 0
                                        : session.getDuration()
                        )
                        .sum();

        double hours =
                learningHours / 60.0;

        double averageRating =
                user.getAverageRating() == null
                        ? 0.0
                        : user.getAverageRating();

        Map<String, Object> dashboard =
                new HashMap<>();

        dashboard.put("userId", user.getId());
        dashboard.put("name", user.getName());

        dashboard.put(
                "teachingSkillsCount",
                teachingSkills
        );

        dashboard.put(
                "learningSkillsCount",
                learningSkills
        );

        dashboard.put(
                "connectionsCount",
                acceptedConnections
        );

        dashboard.put(
                "completedSessions",
                completedSessions
        );

        dashboard.put(
                "learningHours",
                Math.round(hours * 10.0) / 10.0
        );

        dashboard.put(
                "averageRating",
                Math.round(averageRating * 10.0) / 10.0
        );

        dashboard.put(
                "recommendedMatches",
                matchingService
                        .getMatches(userId)
                        .stream()
                        .limit(5)
                        .toList()
        );

        return dashboard;
    }
}