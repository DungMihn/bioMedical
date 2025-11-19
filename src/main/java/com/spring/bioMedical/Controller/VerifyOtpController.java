package com.spring.bioMedical.Controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.service.EmailService;
import com.spring.bioMedical.service.UsersService;

@Controller
public class VerifyOtpController {

    @Autowired
    private UsersService usersService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/verify-otp")
    public ModelAndView verifyOtp(@RequestParam("email") String email,
                                  @RequestParam("otp") String otp) {
        ModelAndView modelAndView = new ModelAndView();

        Users user = usersService.findByEmail(email);
        if (user == null) {
            modelAndView.addObject("errorMessage", "Email does not exist.");
            modelAndView.setViewName("verify-otp");
            return modelAndView;
        }

        if (otp.equals(user.getOtpCode()) &&
                user.getOtpExpiry() != null &&
                user.getOtpExpiry().after(new Date())) {

            // Activate user account
            user.setEnabled(true);
            user.setOtpCode(null);
            user.setOtpExpiry(null);
            usersService.save(user);

            // Send success email
            SimpleMailMessage successMail = new SimpleMailMessage();
            successMail.setTo(user.getEmail());
            successMail.setSubject("Account Created Successfully");
            successMail.setText("Hello " + user.getFullName() +
                    ",\n\nYour account has been successfully activated!\n\n" +
                    "Username: " + user.getUsername() + "\n" +
                    "Password: " + user.getPasswordHash() + "\n\nRemedic Team");
            successMail.setFrom("dungminh2505@gmail.com");
            emailService.sendEmail(successMail);

            // Send data to view
            modelAndView.setViewName("verify-otp");
            modelAndView.addObject("success", true);
            modelAndView.addObject("email", email);

        } else {
            modelAndView.addObject("errorMessage", "Invalid or expired OTP code.");
            modelAndView.addObject("email", email);
            modelAndView.setViewName("verify-otp");
        }

        return modelAndView;
    }
}