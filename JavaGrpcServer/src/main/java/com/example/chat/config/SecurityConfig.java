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

/**
 * Security configuration for the Chat System application.
 * This class sets up user authentication and authorization using Spring Security.
 * It defines in-memory users with roles, configures HTTP security for REST endpoints,
 * and provides password encoding.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Configures in-memory user details service with predefined users.
     * Creates admin, user, and customer accounts for testing and demonstration.
     *
     * @param encoder the password encoder to use for encoding passwords
     * @return the configured UserDetailsService
     */
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

    /**
     * Configures HTTP security for REST endpoints.
     * Defines access rules based on user roles and secures the endpoints.
     * Public endpoints are accessible without authentication, while protected
     * endpoints require specific roles.
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
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

    /**
     * Provides a password encoder bean using BCrypt.
     * This bean is used to encode passwords for user authentication.
     *
     * @return the BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

