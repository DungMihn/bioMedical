/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.bioMedical.Controller;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.service.EmailService;
import com.spring.bioMedical.service.UsersService;

@Controller
public class ForgotPasswordController {

    @Autowired
    private UsersService usersService;

    @Autowired
    private EmailService emailService;

    // Display forgot password form
    @GetMapping("/forgot-password")
    public ModelAndView showForgotPasswordForm() {
        return new ModelAndView("forgot-password");
    }

    // Receive email and send reset link
    @PostMapping("/forgot-password")
    public ModelAndView forgotPassword(@RequestParam("email") String email) {
        ModelAndView mv = new ModelAndView("forgot-password");
        Users user = usersService.findByEmail(email);

        if (user == null || !Boolean.TRUE.equals(user.getEnabled())) {
            mv.addObject("errorMessage", "Email does not exist or is not activated!");
            return mv;
        }

        String token = UUID.randomUUID().toString();
        user.setOtpCode(token);
        user.setOtpExpiry(new Date(System.currentTimeMillis() + 15 * 60 * 1000));
        usersService.save(user);

        String resetLink = "http://localhost:8080/reset-password?token=" + token;

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(user.getEmail());
        mail.setSubject("Password Reset Link");
        mail.setText("Hello " + user.getFullName() +
                ",\n\nClick the link below to reset your password:\n" + resetLink +
                "\n\nThis link will expire in 15 minutes.\n\nIf you did not request this, please ignore this email.");
        mail.setFrom("dungminh2505@gmail.com");
        emailService.sendEmail(mail);

        mv.addObject("successMessage", "✅ The password reset link has been sent to your email!");
        return mv;
    }

    // Step 2: Show reset password form when link is clicked
    @GetMapping("/reset-password")
    public ModelAndView showResetPasswordForm(@RequestParam("token") String token) {
        ModelAndView mv = new ModelAndView("reset-password");
        Users user = usersService.findByOtpCode(token);

        if (user == null || user.getOtpExpiry() == null || user.getOtpExpiry().before(new Date())) {
            mv.setViewName("login");
            mv.addObject("errorMessage", "❌ Reset link is invalid or has expired.");
            return mv;
        }

        mv.addObject("token", token);
        return mv;
    }

    // Step 3: Update new password
    @PostMapping("/reset-password")
    public ModelAndView resetPassword(@RequestParam("token") String token,
                                      @RequestParam("newPassword") String newPassword) {
        Users user = usersService.findByOtpCode(token);

        if (user == null || user.getOtpExpiry() == null || user.getOtpExpiry().before(new Date())) {
            ModelAndView mv = new ModelAndView("login");
            mv.addObject("errorMessage", "❌ Reset link is invalid or has expired.");
            return mv;
        }

        user.setPasswordHash(newPassword);
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        usersService.save(user);

        // Send success email
        SimpleMailMessage successMail = new SimpleMailMessage();
        successMail.setTo(user.getEmail());
        successMail.setSubject("Password Reset Successful");
        successMail.setText("Hello " + user.getFullName() +
                ",\n\nYour password has been successfully changed.\nIf this was not you, please contact the administrator immediately.");
        successMail.setFrom("dungminh2505@gmail.com");
        emailService.sendEmail(successMail);

        // ⚡ Redirect to reset-success page
        return new ModelAndView("reset-success");
    }
}
