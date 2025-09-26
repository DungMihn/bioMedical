package com.spring.bioMedical.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.bioMedical.service.EmailService;

@RestController
public class MailTestController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/test-mail")
    public String sendTestMail() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("dungdm@ames.edu.vn"); // 📌 Đổi thành email thật bạn muốn nhận test
            message.setSubject("Test Email from Spring Boot");
            message.setText("Hello! This is a test email from Spring Boot using Gmail SMTP.");
            message.setFrom("dungminh2505@gmail.com"); // 📌 Gmail của bạn

            emailService.sendEmail(message);

            return "✅ Test mail sent successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Failed to send mail: " + e.getMessage();
        }
    }
}
