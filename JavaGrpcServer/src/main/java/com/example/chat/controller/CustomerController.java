package com.example.chat.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling customer-related requests.
 * This class defines endpoints that are accessible to users with the CUSTOMER role.
 */
@RestController
@RequestMapping("/api")
public class CustomerController {

    /**
     * Endpoint accessible only to users with the CUSTOMER role.
     * Returns a secure customer profile page.
     *
     * @return HTML content with customer profile information
     */
    @GetMapping("/customers/customerProfile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String customerProfile() {
        return "<body><h1 style=\"color:blue;\">Customer Profile page</h1>" +
                "<h2>Customer Profile page is secure.</h2>" +
                "<p>With Spring Security, we can configure Authentication and Authorization.</p></body>";
    }
}

