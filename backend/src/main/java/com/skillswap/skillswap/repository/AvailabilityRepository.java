package com.skillswap.skillswap.repository;

import com.skillswap.skillswap.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByUserId(Long userId);

    List<Availability> findByUserIdAndDay(Long userId, DayOfWeek day);
}