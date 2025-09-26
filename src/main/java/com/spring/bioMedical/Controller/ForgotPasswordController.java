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

    // Hiển thị form quên mật khẩu
@GetMapping("/forgot-password")
public ModelAndView showForgotPasswordForm() {
    return new ModelAndView("forgot-password");
}

// Nhận email và gửi link
@PostMapping("/forgot-password")
public ModelAndView forgotPassword(@RequestParam("email") String email) {
    ModelAndView mv = new ModelAndView("forgot-password");
    Users user = usersService.findByEmail(email);

    if (user == null || !Boolean.TRUE.equals(user.getEnabled())) {
        mv.addObject("errorMessage", "Email không tồn tại hoặc chưa kích hoạt!");
        return mv;
    }

    String token = UUID.randomUUID().toString();
    user.setOtpCode(token);
    user.setOtpExpiry(new Date(System.currentTimeMillis() + 15 * 60 * 1000));
    usersService.save(user);

    String resetLink = "http://localhost:8089/reset-password?token=" + token;

    SimpleMailMessage mail = new SimpleMailMessage();
    mail.setTo(user.getEmail());
    mail.setSubject("Password Reset Link");
    mail.setText("Xin chào " + user.getFullName() +
            ",\n\nClick vào link sau để đặt lại mật khẩu:\n" + resetLink +
            "\n\nLink có hiệu lực trong 15 phút.\n\nNếu bạn không yêu cầu, hãy bỏ qua email này.");
    mail.setFrom("dungminh2505@gmail.com");
    emailService.sendEmail(mail);

    mv.addObject("successMessage", "✅ Link reset mật khẩu đã gửi đến email của bạn!");
    return mv;
}

    // Step 2: mở form reset mật khẩu khi click link trong email
    @GetMapping("/reset-password")
    public ModelAndView showResetPasswordForm(@RequestParam("token") String token) {
        ModelAndView mv = new ModelAndView("reset-password");
        Users user = usersService.findByOtpCode(token);

        if (user == null || user.getOtpExpiry() == null || user.getOtpExpiry().before(new Date())) {
            mv.setViewName("login");
            mv.addObject("errorMessage", "❌ Link reset không hợp lệ hoặc đã hết hạn.");
            return mv;
        }

        mv.addObject("token", token);
        return mv;
    }

    // Step 3: cập nhật mật khẩu mới
    @PostMapping("/reset-password")
public ModelAndView resetPassword(@RequestParam("token") String token,
                                  @RequestParam("newPassword") String newPassword) {
    Users user = usersService.findByOtpCode(token);

    if (user == null || user.getOtpExpiry() == null || user.getOtpExpiry().before(new Date())) {
        ModelAndView mv = new ModelAndView("login");
        mv.addObject("errorMessage", "❌ Link reset không hợp lệ hoặc đã hết hạn.");
        return mv;
    }

    user.setPasswordHash(newPassword);
    user.setOtpCode(null);
    user.setOtpExpiry(null);
    usersService.save(user);

    // Gửi mail thông báo
    SimpleMailMessage successMail = new SimpleMailMessage();
    successMail.setTo(user.getEmail());
    successMail.setSubject("Đặt lại mật khẩu thành công");
    successMail.setText("Xin chào " + user.getFullName() +
            ",\n\nMật khẩu của bạn đã thay đổi thành công.\nNếu không phải bạn thực hiện, hãy liên hệ quản trị viên ngay.");
    successMail.setFrom("dungminh2505@gmail.com");
    emailService.sendEmail(successMail);

    // ⚡ Điều hướng sang trang reset-success
    return new ModelAndView("reset-success");
}

}
