package com.example.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Create users
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        // setup users utilizing InMemoryUserDetailsManager
        UserDetails admin = User.withUsername("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN", "USER")
                .build();
        UserDetails user = User.withUsername("user")
                .password(encoder.encode("user123"))
                .roles("USER")
                .build();
        UserDetails customer = User.withUsername("customer")
                .password(encoder.encode("customer123"))
                .roles("CUSTOMER")
                .build();
        return new InMemoryUserDetailsManager(admin, user, customer);
    }

    // Configure HttpSecurity
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for API endpoints
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/auth/welcome").permitAll()
                        .requestMatchers("/api/health/**").permitAll()
                        
                        // Chat system endpoints - require authentication
                        .requestMatchers("/auth/user/**").hasAnyRole("USER", "ADMIN") // User profile endpoints
                        .requestMatchers("/auth/admin/**").hasRole("ADMIN") // Admin endpoints
                        .requestMatchers("/api/customers/**").hasAnyRole("CUSTOMER", "ADMIN") // Customer endpoints
                        
                        // Allow all other requests (gRPC uses different protocol/port)
                        .anyRequest().permitAll()
                )
                .httpBasic(withDefaults()) // Use HTTP Basic authentication for REST endpoints
                .formLogin(withDefaults()); // Also enable form-based login for browser testing
        return http.build();
    }

    // Password Encoding
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

