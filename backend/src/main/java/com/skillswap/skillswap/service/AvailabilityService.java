package com.skillswap.skillswap.service;

import com.skillswap.skillswap.entity.Availability;
import com.skillswap.skillswap.entity.User;
import com.skillswap.skillswap.repository.AvailabilityRepository;
import com.skillswap.skillswap.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;

    public AvailabilityService(
            AvailabilityRepository availabilityRepository,
            UserRepository userRepository
    ) {
        this.availabilityRepository = availabilityRepository;
        this.userRepository = userRepository;
    }

    public Availability addAvailability(
            Long userId,
            Availability availability
    ) {

        if (userId == null) {
            throw new RuntimeException("User id is required");
        }

        if (availability == null) {
            throw new RuntimeException("Availability is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: " + userId
                        )
                );

        if (availability.getStartTime() == null
                || availability.getEndTime() == null) {

            throw new RuntimeException(
                    "Start time and end time are required"
            );
        }

        if (!availability.getEndTime()
                .isAfter(availability.getStartTime())) {

            throw new RuntimeException(
                    "End time must be after start time"
            );
        }

        availability.setUser(user);

        return availabilityRepository.save(availability);
    }

    public List<Availability> getUserAvailability(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException(
                    "User not found with id: " + userId
            );
        }

        return availabilityRepository.findByUserId(userId);
    }

    public Availability getAvailabilityById(Long id) {

        return availabilityRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Availability not found with id: " + id
                        )
                );
    }

    public Availability updateAvailability(
            Long id,
            Availability updatedAvailability
    ) {

        if (id == null) {
            throw new RuntimeException("Availability id is required");
        }

        if (updatedAvailability == null) {
            throw new RuntimeException("Updated availability is required");
        }

        Availability existingAvailability =
                getAvailabilityById(id);

        if (updatedAvailability.getStartTime() == null
                || updatedAvailability.getEndTime() == null) {

            throw new RuntimeException(
                    "Start time and end time are required"
            );
        }

        if (!updatedAvailability.getEndTime()
                .isAfter(updatedAvailability.getStartTime())) {

            throw new RuntimeException(
                    "End time must be after start time"
            );
        }

        existingAvailability.setDay(
                updatedAvailability.getDay()
        );

        existingAvailability.setStartTime(
                updatedAvailability.getStartTime()
        );

        existingAvailability.setEndTime(
                updatedAvailability.getEndTime()
        );

        return availabilityRepository.save(
                existingAvailability
        );
    }

    public void deleteAvailability(Long id) {

        Availability availability =
                getAvailabilityById(id);

        availabilityRepository.delete(availability);
    }
}