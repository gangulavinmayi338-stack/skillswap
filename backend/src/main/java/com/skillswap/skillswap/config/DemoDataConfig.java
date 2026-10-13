package com.skillswap.skillswap.config;

import com.skillswap.skillswap.entity.*;
import com.skillswap.skillswap.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoDataConfig {
    @Bean CommandLineRunner createDemoAdmin(UserRepository users, PasswordEncoder encoder){return args->{
        String email="admin@skillswap.com";
        if(!users.existsByEmail(email)){User u=new User();u.setName("SkillSwap Admin");u.setEmail(email);u.setPassword(encoder.encode("Admin@123"));u.setCollege("SkillSwap");u.setDepartment("Administration");u.setRole(UserRole.ADMIN);users.save(u);}
    };}
}
