package com.skillswap.skillswap.service;

import com.skillswap.skillswap.entity.User;
import com.skillswap.skillswap.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found with id: " + id)
                );
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found with email: " + email)
                );
    }

    public User updateUser(Long id, User updatedUser) {

        if (id == null) {
            throw new RuntimeException("User id is required");
        }

        if (updatedUser == null) {
            throw new RuntimeException("Updated user data is required");
        }

        User existingUser = getUserById(id);

        if (updatedUser.getName() != null
                && !updatedUser.getName().trim().isEmpty()) {
            existingUser.setName(updatedUser.getName().trim());
        }

        if (updatedUser.getCollege() != null) {
            existingUser.setCollege(updatedUser.getCollege());
        }

        if (updatedUser.getDepartment() != null) {
            existingUser.setDepartment(updatedUser.getDepartment());
        }

        if (updatedUser.getYear() != null) {
            existingUser.setYear(updatedUser.getYear());
        }

        if (updatedUser.getBio() != null) {
            existingUser.setBio(updatedUser.getBio());
        }

        if (updatedUser.getPreferences() != null) {
            existingUser.setPreferences(updatedUser.getPreferences());
        }

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {

        User user = getUserById(id);

        userRepository.delete(user);
    }
}