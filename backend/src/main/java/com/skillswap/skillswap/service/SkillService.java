package com.skillswap.skillswap.service;

import com.skillswap.skillswap.entity.Skill;
import com.skillswap.skillswap.entity.SkillLevel;
import com.skillswap.skillswap.entity.SkillType;
import com.skillswap.skillswap.entity.User;
import com.skillswap.skillswap.entity.UserSkill;
import com.skillswap.skillswap.repository.SkillRepository;
import com.skillswap.skillswap.repository.UserRepository;
import com.skillswap.skillswap.repository.UserSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;

    public SkillService(
            SkillRepository skillRepository,
            UserRepository userRepository,
            UserSkillRepository userSkillRepository
    ) {
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
        this.userSkillRepository = userSkillRepository;
    }

    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    public Skill createSkill(Skill skill) {

        if (skill == null) {
            throw new RuntimeException("Skill is required");
        }

        if (skill.getSkillName() == null
                || skill.getSkillName().trim().isEmpty()) {
            throw new RuntimeException("Skill name is required");
        }

        String normalizedName = skill.getSkillName().trim();

        if (skillRepository.existsBySkillNameIgnoreCase(normalizedName)) {
            throw new RuntimeException(
                    "Skill already exists: " + normalizedName
            );
        }

        skill.setSkillName(normalizedName);

        return skillRepository.save(skill);
    }

    public Skill getSkillById(Long skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Skill not found with id: " + skillId
                        )
                );
    }

    public List<UserSkill> getUserSkills(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException(
                    "User not found with id: " + userId
            );
        }

        return userSkillRepository.findByUserId(userId);
    }

    public UserSkill addSkillToUser(
            Long userId,
            Long skillId,
            SkillType type,
            SkillLevel level
    ) {

        if (userId == null) {
            throw new RuntimeException("User id is required");
        }

        if (skillId == null) {
            throw new RuntimeException("Skill id is required");
        }

        if (type == null) {
            throw new RuntimeException("Skill type is required");
        }

        if (level == null) {
            throw new RuntimeException("Skill level is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: " + userId
                        )
                );

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Skill not found with id: " + skillId
                        )
                );

        userSkillRepository
                .findByUserIdAndSkillIdAndType(userId, skillId, type)
                .ifPresent(existing -> {
                    throw new RuntimeException(
                            "This skill is already added as " + type
                    );
                });

        UserSkill userSkill = new UserSkill();

        userSkill.setUser(user);
        userSkill.setSkill(skill);
        userSkill.setType(type);
        userSkill.setLevel(level);

        return userSkillRepository.save(userSkill);
    }

    public List<UserSkill> getTeachingSkills(Long userId) {
        return userSkillRepository.findByUserIdAndType(
                userId,
                SkillType.TEACH
        );
    }

    public List<UserSkill> getLearningSkills(Long userId) {
        return userSkillRepository.findByUserIdAndType(
                userId,
                SkillType.LEARN
        );
    }

    @Transactional
    public void deleteUserSkill(Long userId, Long skillId) {

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException(
                    "User not found with id: " + userId
            );
        }

        if (!skillRepository.existsById(skillId)) {
            throw new RuntimeException(
                    "Skill not found with id: " + skillId
            );
        }

        userSkillRepository.deleteByUserIdAndSkillId(
                userId,
                skillId
        );
    }
}