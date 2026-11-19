package com.example.helpdesk.config;

import com.example.helpdesk.model.Role;
import com.example.helpdesk.model.User;
import com.example.helpdesk.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeUser(UserService userService) {
        return args -> {
            if (userService.getUserByUsername("admin").isEmpty()) {
                String password = System.getenv("ADMIN_INITIAL_PASSWORD");

                if (password == null || password.isBlank()) {
                    throw new IllegalStateException("ADMIN_INITIAL_PASSWORD saknas");
                }

                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@helpdesk.se");
                admin.setPassword(password);
                admin.setRole(Role.ADMIN);
                admin.setActive(true);

                userService.saveUser(admin);
            }

            if (userService.getUserByUsername("agent").isEmpty()) {
                String password = System.getenv("AGENT_INITIAL_PASSWORD");

                if (password == null || password.isBlank()) {
                    throw new IllegalStateException("AGENT_INITIAL_PASSWORD saknas");
                }

                User agent = new User();
                agent.setUsername("agent");
                agent.setEmail("agent@helpdesk.se");
                agent.setPassword(password);
                agent.setRole(Role.AGENT);
                agent.setActive(true);

                userService.saveUser(agent);
            }

            if (userService.getUserByUsername("testuser").isEmpty()) {
                String password = System.getenv("USER_INITIAL_PASSWORD");

                if (password == null || password.isBlank()) {
                    throw new IllegalStateException("USER_INITIAL_PASSWORD saknas");
                }

                User user = new User();
                user.setUsername("testuser");
                user.setEmail("testuser@helpdesk.se");
                user.setPassword(password);
                user.setRole(Role.USER);
                user.setActive(true);

                userService.saveUser(user);
            }
        };
    }

}
