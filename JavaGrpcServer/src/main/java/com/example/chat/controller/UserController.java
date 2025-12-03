package com.example.chat.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class UserController {

    @GetMapping("/welcome")
    public String welcome() {
        return "<body><h1>Welcome, this endpoint is not secure</h1></body>";
    }

    @GetMapping("/user/userProfile")
    @PreAuthorize("hasRole('USER')") // role-based access control
    public String userProfile() {
        return "<body><h1>Welcome to User Profile</h1>" +
                "<h2>User Profile page is secure.</h2>" +
                "</body>";
    }

    @GetMapping("/admin/adminProfile")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminProfile() {
        return "<body><h1 style=\"background-color:powderblue;\">Welcome to Admin Profile</h1>" +
                "<h2>Admin Profile page is secure.</h2>" +
                "<p>With Spring Security, we can configure Authentication and Authorization.</p></body>";
    }
}

