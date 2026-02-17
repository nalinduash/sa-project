package com.bookfair.config;

import com.bookfair.model.User;
import com.bookfair.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class EmployeeInitializer {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Bean
    public CommandLineRunner initEmployee() {
        return args -> {
            if (!userRepository.existsByEmail("employee@bookfair.lk")) {
                User employee = User.builder()
                    .email("employee@bookfair.lk")
                    .password(passwordEncoder.encode("employee123"))
                    .businessName("Book Fair Organizers")
                    .contactPerson("Admin")
                    .phone("+94 11 2345678")
                    .address("Colombo, Sri Lanka")
                    .userType(User.UserType.EMPLOYEE)
                    .build();
                userRepository.save(employee);
                System.out.println("Default employee account created:");
                System.out.println("Email: employee@bookfair.lk");
                System.out.println("Password: employee123");
            }
        };
    }
}