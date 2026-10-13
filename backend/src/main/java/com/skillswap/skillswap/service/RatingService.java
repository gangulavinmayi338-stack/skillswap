package com.skillswap.skillswap.service;

import com.skillswap.skillswap.entity.Rating;
import com.skillswap.skillswap.entity.Session;
import com.skillswap.skillswap.entity.SessionStatus;
import com.skillswap.skillswap.entity.User;
import com.skillswap.skillswap.repository.RatingRepository;
import com.skillswap.skillswap.repository.SessionRepository;
import com.skillswap.skillswap.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public RatingService(
            RatingRepository ratingRepository,
            SessionRepository sessionRepository,
            UserRepository userRepository
    ) {
        this.ratingRepository = ratingRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    public Rating createRating(
            Long sessionId,
            Long reviewerId,
            Long reviewedUserId,
            Integer ratingValue,
            String feedback
    ) {

        if (sessionId == null || reviewerId == null || reviewedUserId == null) {
            throw new RuntimeException("Session, reviewer, and reviewed user ids are required");
        }

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Session not found with id: " + sessionId
                        )
                );

        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new RuntimeException(
                    "Rating can only be given after session completion"
            );
        }

        if (Objects.equals(reviewerId, reviewedUserId)) {
            throw new RuntimeException(
                    "You cannot rate yourself"
            );
        }

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Reviewer not found with id: " + reviewerId
                        )
                );

        User reviewedUser = userRepository.findById(reviewedUserId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Reviewed user not found with id: " + reviewedUserId
                        )
                );

        boolean reviewerIsInSession =
                session.getTeacher().getId().equals(reviewerId)
                        ||
                session.getLearner().getId().equals(reviewerId);

        boolean reviewedUserIsInSession =
                session.getTeacher().getId().equals(reviewedUserId)
                        ||
                session.getLearner().getId().equals(reviewedUserId);

        if (!reviewerIsInSession || !reviewedUserIsInSession) {
            throw new RuntimeException(
                    "Both users must belong to this session"
            );
        }

        if (ratingValue == null || ratingValue < 1 || ratingValue > 5) {
            throw new RuntimeException(
                    "Rating must be between 1 and 5"
            );
        }

        if (ratingRepository.existsBySessionIdAndReviewerId(
                sessionId,
                reviewerId
        )) {
            throw new RuntimeException(
                    "You have already rated this session"
            );
        }

        Rating rating = new Rating();

        rating.setSession(session);
        rating.setReviewer(reviewer);
        rating.setReviewedUser(reviewedUser);
        rating.setRating(ratingValue);
        rating.setFeedback(feedback);

        Rating savedRating = ratingRepository.save(rating);

        updateAverageRating(reviewedUserId);

        return savedRating;
    }

    public List<Rating> getRatingsForUser(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException(
                    "User not found with id: " + userId
            );
        }

        return ratingRepository.findByReviewedUserId(userId);
    }

    public Double getAverageRating(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException(
                    "User not found with id: " + userId
            );
        }

        Double average =
                ratingRepository.findAverageRatingByUserId(userId);

        return average == null ? 0.0 : average;
    }

    private void updateAverageRating(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: " + userId
                        )
                );

        Double average =
                ratingRepository.findAverageRatingByUserId(userId);

        if (average == null) {
            average = 0.0;
        }

        user.setAverageRating(average);

        userRepository.save(user);
    }
}