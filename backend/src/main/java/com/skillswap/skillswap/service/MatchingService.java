package com.skillswap.skillswap.service;

import com.skillswap.skillswap.entity.Availability;
import com.skillswap.skillswap.entity.SkillLevel;
import com.skillswap.skillswap.entity.SkillType;
import com.skillswap.skillswap.entity.User;
import com.skillswap.skillswap.entity.UserSkill;
import com.skillswap.skillswap.repository.AvailabilityRepository;
import com.skillswap.skillswap.repository.UserRepository;
import com.skillswap.skillswap.repository.UserSkillRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final AvailabilityRepository availabilityRepository;

    public MatchingService(
            UserRepository userRepository,
            UserSkillRepository userSkillRepository,
            AvailabilityRepository availabilityRepository
    ) {
        this.userRepository = userRepository;
        this.userSkillRepository = userSkillRepository;
        this.availabilityRepository = availabilityRepository;
    }

    public List<Map<String, Object>> getMatches(Long userId) {

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: " + userId
                        )
                );

        List<User> allUsers = userRepository.findAll();

        List<Map<String, Object>> results = new ArrayList<>();

        for (User candidate : allUsers) {

            if (candidate.getId().equals(userId)) {
                continue;
            }

            double matchPercentage =
                    calculateMatchPercentage(
                            currentUser,
                            candidate
                    );

            Map<String, Object> match = new HashMap<>();

            match.put("userId", candidate.getId());
            match.put("name", candidate.getName());
            match.put("email", candidate.getEmail());
            match.put("college", candidate.getCollege());
            match.put("department", candidate.getDepartment());
            match.put("averageRating",
                    candidate.getAverageRating() == null
                            ? 0.0
                            : candidate.getAverageRating());

            match.put(
                    "matchPercentage",
                    Math.round(matchPercentage)
            );

            results.add(match);
        }

        results.sort(
                (a, b) -> Long.compare(
                        ((Number) b.get("matchPercentage")).longValue(),
                        ((Number) a.get("matchPercentage")).longValue()
                )
        );

        return results;
    }

    public double calculateMatchPercentage(
            User currentUser,
            User candidate
    ) {

        double skillCompatibilityScore =
                calculateSkillCompatibility(
                        currentUser.getId(),
                        candidate.getId()
                );

        double learningRequirementScore =
                calculateLearningRequirement(
                        currentUser.getId(),
                        candidate.getId()
                );

        double availabilityScore =
                calculateAvailabilityScore(
                        currentUser.getId(),
                        candidate.getId()
                );

        double skillLevelScore =
                calculateSkillLevelScore(
                        currentUser.getId(),
                        candidate.getId()
                );

        double ratingScore =
                calculateRatingScore(candidate);

        double preferenceScore =
                calculatePreferenceScore(
                        currentUser,
                        candidate
                );

        return
                skillCompatibilityScore * 0.40
                        +
                learningRequirementScore * 0.25
                        +
                availabilityScore * 0.15
                        +
                skillLevelScore * 0.10
                        +
                ratingScore * 0.05
                        +
                preferenceScore * 0.05;
    }

    private double calculateSkillCompatibility(
            Long userId,
            Long candidateId
    ) {

        List<UserSkill> userLearningSkills =
                userSkillRepository.findByUserIdAndType(
                        userId,
                        SkillType.LEARN
                );

        List<UserSkill> candidateTeachingSkills =
                userSkillRepository.findByUserIdAndType(
                        candidateId,
                        SkillType.TEACH
                );

        if (userLearningSkills.isEmpty()) {
            return 0.0;
        }

        Set<Long> candidateTeachingSkillIds =
                candidateTeachingSkills.stream()
                        .map(userSkill ->
                                userSkill.getSkill().getId()
                        )
                        .collect(Collectors.toSet());

        long matches =
                userLearningSkills.stream()
                        .filter(userSkill ->
                                candidateTeachingSkillIds.contains(
                                        userSkill.getSkill().getId()
                                )
                        )
                        .count();

        return ((double) matches
                / userLearningSkills.size()) * 100.0;
    }

    private double calculateLearningRequirement(
            Long userId,
            Long candidateId
    ) {

        List<UserSkill> userTeachingSkills =
                userSkillRepository.findByUserIdAndType(
                        userId,
                        SkillType.TEACH
                );

        List<UserSkill> candidateLearningSkills =
                userSkillRepository.findByUserIdAndType(
                        candidateId,
                        SkillType.LEARN
                );

        if (candidateLearningSkills.isEmpty()) {
            return 0.0;
        }

        Set<Long> userTeachingSkillIds =
                userTeachingSkills.stream()
                        .map(userSkill ->
                                userSkill.getSkill().getId()
                        )
                        .collect(Collectors.toSet());

        long matches =
                candidateLearningSkills.stream()
                        .filter(userSkill ->
                                userTeachingSkillIds.contains(
                                        userSkill.getSkill().getId()
                                )
                        )
                        .count();

        return ((double) matches
                / candidateLearningSkills.size()) * 100.0;
    }

    private double calculateAvailabilityScore(
            Long userId,
            Long candidateId
    ) {

        List<Availability> userAvailability =
                availabilityRepository.findByUserId(userId);

        List<Availability> candidateAvailability =
                availabilityRepository.findByUserId(candidateId);

        if (userAvailability.isEmpty()
                || candidateAvailability.isEmpty()) {
            return 0.0;
        }

        int totalComparisons = 0;
        int overlappingSlots = 0;

        for (Availability userSlot : userAvailability) {

            for (Availability candidateSlot :
                    candidateAvailability) {

                if (!userSlot.getDay()
                        .equals(candidateSlot.getDay())) {
                    continue;
                }

                totalComparisons++;

                boolean overlap =
                        userSlot.getStartTime()
                                .isBefore(candidateSlot.getEndTime())
                                &&
                        candidateSlot.getStartTime()
                                .isBefore(userSlot.getEndTime());

                if (overlap) {
                    overlappingSlots++;
                }
            }
        }

        if (totalComparisons == 0) {
            return 0.0;
        }

        return Math.min(
                100.0,
                ((double) overlappingSlots
                        / totalComparisons) * 100.0
        );
    }

    private double calculateSkillLevelScore(
            Long userId,
            Long candidateId
    ) {

        List<UserSkill> userLearningSkills =
                userSkillRepository.findByUserIdAndType(
                        userId,
                        SkillType.LEARN
                );

        List<UserSkill> candidateTeachingSkills =
                userSkillRepository.findByUserIdAndType(
                        candidateId,
                        SkillType.TEACH
                );

        if (userLearningSkills.isEmpty()
                || candidateTeachingSkills.isEmpty()) {
            return 0.0;
        }

        double totalScore = 0.0;
        int matchedSkills = 0;

        for (UserSkill learningSkill : userLearningSkills) {

            for (UserSkill teachingSkill :
                    candidateTeachingSkills) {

                if (learningSkill.getSkill().getId()
                        .equals(
                                teachingSkill.getSkill().getId()
                        )) {

                    totalScore += compareSkillLevels(
                            learningSkill.getLevel(),
                            teachingSkill.getLevel()
                    );

                    matchedSkills++;
                }
            }
        }

        if (matchedSkills == 0) {
            return 0.0;
        }

        return totalScore / matchedSkills;
    }

    private double compareSkillLevels(
            SkillLevel learnerLevel,
            SkillLevel teacherLevel
    ) {

        int learnerValue =
                getSkillLevelValue(learnerLevel);

        int teacherValue =
                getSkillLevelValue(teacherLevel);

        if (teacherValue > learnerValue) {
            return 100.0;
        }

        if (teacherValue == learnerValue) {
            return 80.0;
        }

        return 40.0;
    }

    private int getSkillLevelValue(
            SkillLevel level
    ) {

        if (level == SkillLevel.BEGINNER) {
            return 1;
        }

        if (level == SkillLevel.INTERMEDIATE) {
            return 2;
        }

        return 3;
    }

    private double calculateRatingScore(
            User candidate
    ) {

        Double rating =
                candidate.getAverageRating();

        if (rating == null) {
            return 0.0;
        }

        return Math.min(
                100.0,
                (rating / 5.0) * 100.0
        );
    }

    private double calculatePreferenceScore(
            User currentUser,
            User candidate
    ) {

        if (currentUser.getPreferences() == null
                || currentUser.getPreferences().isBlank()) {
            return 50.0;
        }

        if (candidate.getPreferences() == null
                || candidate.getPreferences().isBlank()) {
            return 50.0;
        }

        String currentPreferences =
                currentUser.getPreferences().toLowerCase();

        String candidatePreferences =
                candidate.getPreferences().toLowerCase();

        if (currentPreferences.equals(
                candidatePreferences
        )) {
            return 100.0;
        }

        Set<String> userWords =
                Arrays.stream(
                                currentPreferences.split("[,\\s]+")
                        )
                        .filter(word -> !word.isBlank())
                        .collect(Collectors.toSet());

        Set<String> candidateWords =
                Arrays.stream(
                                candidatePreferences.split("[,\\s]+")
                        )
                        .filter(word -> !word.isBlank())
                        .collect(Collectors.toSet());

        if (userWords.isEmpty()
                || candidateWords.isEmpty()) {
            return 50.0;
        }

        long commonWords =
                userWords.stream()
                        .filter(candidateWords::contains)
                        .count();

        if (commonWords == 0) {
            return 25.0;
        }

        double score =
                ((double) commonWords
                        / Math.max(
                                userWords.size(),
                                candidateWords.size()
                        )) * 100.0;

        return Math.min(100.0, score);
    }
}