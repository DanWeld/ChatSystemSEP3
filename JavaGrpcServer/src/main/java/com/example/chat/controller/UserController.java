package com.example.chat.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling user-related requests.
 * This class defines endpoints that are accessible to users with USER and ADMIN roles.
 */
@RestController
@RequestMapping("/auth")
public class UserController {

    /**
     * Public welcome endpoint accessible without authentication.
     *
     * @return HTML content with a welcome message
     */
    @GetMapping("/welcome")
    public String welcome() {
        return "<body><h1>Welcome, this endpoint is not secure</h1></body>";
    }

    /**
     * Endpoint accessible only to users with the USER role.
     * Returns a secure user profile page.
     *
     * @return HTML content with user profile information
     */
    @GetMapping("/user/userProfile")
    @PreAuthorize("hasRole('USER')")
    public String userProfile() {
        return "<body><h1>Welcome to User Profile</h1>" +
                "<h2>User Profile page is secure.</h2>" +
                "</body>";
    }

    /**
     * Endpoint accessible only to users with the ADMIN role.
     * Returns a secure admin profile page.
     *
     * @return HTML content with admin profile information
     */
    @GetMapping("/admin/adminProfile")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminProfile() {
        return "<body><h1 style=\"background-color:powderblue;\">Welcome to Admin Profile</h1>" +
                "<h2>Admin Profile page is secure.</h2>" +
                "<p>With Spring Security, we can configure Authentication and Authorization.</p></body>";
    }
}

