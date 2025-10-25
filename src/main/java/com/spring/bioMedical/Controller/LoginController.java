package com.spring.bioMedical.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 
 * @author Soumyadip Chowdhury
 * @github soumyadip007
 *
 */
@Controller
public class LoginController {

    @GetMapping("/showMyLoginPage")
    public String showLoginPage() {
        return "login"; // templates/login.html
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access_denied"; // templates/access_denied.html
    }
}
