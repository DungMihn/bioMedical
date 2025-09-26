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
        modelAndView.addObject("errorMessage", "Email không tồn tại.");
        modelAndView.setViewName("verify-otp");
        return modelAndView;
    }

    if (otp.equals(user.getOtpCode()) &&
            user.getOtpExpiry() != null &&
            user.getOtpExpiry().after(new Date())) {

        // Cập nhật user active
        user.setEnabled(true);
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        usersService.save(user);

        // Gửi mail thông báo thành công
        SimpleMailMessage successMail = new SimpleMailMessage();
        successMail.setTo(user.getEmail());
        successMail.setSubject("Tạo tài khoản thành công");
        successMail.setText("Xin chào " + user.getFullName() +
                ",\n\nTài khoản của bạn đã được kích hoạt thành công!\n\n" +
                "Username: " + user.getUsername() + "\n" +
                "Password: " + user.getPasswordHash() + "\n\nRemedic Team");
        successMail.setFrom("dungminh2505@gmail.com");
        emailService.sendEmail(successMail);

        // Gửi dữ liệu sang view
        modelAndView.setViewName("verify-otp");
        modelAndView.addObject("success", true);
        modelAndView.addObject("email", email);

    } else {
        modelAndView.addObject("errorMessage", "Mã OTP không hợp lệ hoặc đã hết hạn.");
        modelAndView.addObject("email", email);
        modelAndView.setViewName("verify-otp");
    }

    return modelAndView;
}

}
