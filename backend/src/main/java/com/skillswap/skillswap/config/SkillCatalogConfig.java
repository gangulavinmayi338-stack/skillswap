package com.skillswap.skillswap.config;

import com.skillswap.skillswap.entity.Skill;
import com.skillswap.skillswap.repository.SkillRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SkillCatalogConfig {

    @Bean
    CommandLineRunner seedSkillCatalog(SkillRepository skills) {
        return args -> {
            List<Skill> defaults = List.of(
                    new Skill("Java", "Programming"),
                    new Skill("Python", "Programming"),
                    new Skill("JavaScript", "Programming"),
                    new Skill("TypeScript", "Programming"),
                    new Skill("C", "Programming"),
                    new Skill("C++", "Programming"),
                    new Skill("C#", "Programming"),
                    new Skill("HTML and CSS", "Web Development"),
                    new Skill("React", "Web Development"),
                    new Skill("Node.js", "Web Development"),
                    new Skill("SQL", "Databases"),
                    new Skill("Data Analysis", "Data and Analytics"),
                    new Skill("Machine Learning", "Data and Analytics"),
                    new Skill("Graphic Design", "Design"),
                    new Skill("UI/UX Design", "Design"),
                    new Skill("Video Editing", "Creative"),
                    new Skill("Photography", "Creative"),
                    new Skill("Digital Marketing", "Business"),
                    new Skill("Public Speaking", "Communication"),
                    new Skill("Creative Writing", "Communication"),
                    new Skill("English", "Languages"),
                    new Skill("Spanish", "Languages"),
                    new Skill("Mathematics", "Academics"),
                    new Skill("Physics", "Academics"),
                    new Skill("Chemistry", "Academics"),
                    new Skill("Guitar", "Music"),
                    new Skill("Piano", "Music"),
                    new Skill("Cooking", "Lifestyle")
            );

            for (Skill skill : defaults) {
                if (!skills.existsBySkillNameIgnoreCase(skill.getSkillName())) {
                    skills.save(skill);
                }
            }
        };
    }
}
