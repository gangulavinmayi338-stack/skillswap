package com.skillswap.skillswap.repository;

import com.skillswap.skillswap.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByReviewedUserId(Long reviewedUserId);

    boolean existsBySessionIdAndReviewerId(
            Long sessionId,
            Long reviewerId
    );

    @Query("""
            SELECT AVG(r.rating)
            FROM Rating r
            WHERE r.reviewedUser.id = :userId
            """)
    Double findAverageRatingByUserId(@Param("userId") Long userId);
}