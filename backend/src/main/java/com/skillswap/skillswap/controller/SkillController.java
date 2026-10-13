package com.skillswap.skillswap.controller;

import com.skillswap.skillswap.entity.Skill;
import com.skillswap.skillswap.entity.SkillLevel;
import com.skillswap.skillswap.entity.SkillType;
import com.skillswap.skillswap.entity.UserSkill;
import com.skillswap.skillswap.service.SkillService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping("/skills")
    public ResponseEntity<List<Skill>> getAllSkills() {
        return ResponseEntity.ok(
                skillService.getAllSkills()
        );
    }

    @PostMapping("/skills")
    public ResponseEntity<Skill> createSkill(
            @RequestBody Skill skill
    ) {
        return ResponseEntity.ok(
                skillService.createSkill(skill)
        );
    }

    @PostMapping("/users/{userId}/skills")
    public ResponseEntity<UserSkill> addSkillToUser(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request
    ) {

        Long skillId =
                Long.parseLong(request.get("skillId"));

        SkillType type =
                SkillType.valueOf(
                        request.get("type").toUpperCase()
                );

        SkillLevel level =
                SkillLevel.valueOf(
                        request.get("level").toUpperCase()
                );

        UserSkill userSkill =
                skillService.addSkillToUser(
                        userId,
                        skillId,
                        type,
                        level
                );

        return ResponseEntity.ok(userSkill);
    }

    @GetMapping("/users/{userId}/skills")
    public ResponseEntity<List<UserSkill>> getUserSkills(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                skillService.getUserSkills(userId)
        );
    }

    @DeleteMapping("/users/{userId}/skills/{skillId}")
    public ResponseEntity<String> deleteUserSkill(
            @PathVariable Long userId,
            @PathVariable Long skillId
    ) {

        skillService.deleteUserSkill(
                userId,
                skillId
        );

        return ResponseEntity.ok(
                "Skill removed successfully"
        );
    }
}