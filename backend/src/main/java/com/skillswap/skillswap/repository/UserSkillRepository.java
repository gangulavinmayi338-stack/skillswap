package com.skillswap.skillswap.repository;

import com.skillswap.skillswap.entity.SkillType;
import com.skillswap.skillswap.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    List<UserSkill> findByUserId(Long userId);

    List<UserSkill> findByUserIdAndType(Long userId, SkillType type);

    List<UserSkill> findBySkillIdAndType(Long skillId, SkillType type);

    Optional<UserSkill> findByUserIdAndSkillIdAndType(
            Long userId,
            Long skillId,
            SkillType type
    );

    void deleteByUserIdAndSkillId(Long userId, Long skillId);
}