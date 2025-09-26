package com.spring.bioMedical.Controller;

import java.util.Date;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.service.EmailService;
import com.spring.bioMedical.service.UsersService;

@Controller
public class RegisterController {

    private final UsersService usersService;
    private final EmailService emailService;

    @Autowired
    public RegisterController(UsersService usersService, EmailService emailService) {
        this.usersService = usersService;
        this.emailService = emailService;
    }

    // GET form đăng ký
@RequestMapping(value = "/register", method = RequestMethod.GET)
public ModelAndView showRegistrationPage() {
    ModelAndView modelAndView = new ModelAndView("register");
    modelAndView.addObject("user", new Users()); // luôn có object user
    return modelAndView;
}

// POST xử lý đăng ký
// POST xử lý đăng ký
@RequestMapping(value = "/register", method = RequestMethod.POST)
public ModelAndView processRegistrationForm(@Valid Users user,
                                            BindingResult bindingResult,
                                            HttpServletRequest request) {
    ModelAndView modelAndView = new ModelAndView("register");

    try {
        System.out.println("➡️ Bắt đầu xử lý đăng ký: " + user.getEmail());

        // 1. Validation lỗi
        if (bindingResult.hasErrors()) {
            modelAndView.addObject("user", user);
            modelAndView.addObject("errorMessage", "Thông tin nhập chưa hợp lệ!");
            modelAndView.addObject("validationErrors", bindingResult.getAllErrors());
            return modelAndView;
        }

        // 2. Check email
        Users userExists = usersService.findByEmail(user.getEmail());
        if (userExists != null) {
            if (Boolean.TRUE.equals(userExists.getEnabled())) {
                // Nếu đã active thì chặn
                modelAndView.addObject("user", user);
                modelAndView.addObject("errorMessage", "Email này đã được đăng ký và kích hoạt. Vui lòng dùng email khác.");
                return modelAndView;
            } else {
                // ⚡ Nếu chưa active: cập nhật lại thông tin mới
                System.out.println("⚡ Email đã tồn tại nhưng chưa xác thực. Cập nhật lại thông tin...");

                userExists.setUsername(user.getUsername());
                userExists.setFullName(user.getFullName());
                userExists.setPasswordHash(user.getPasswordHash());
                userExists.setPhone(user.getPhone());
                userExists.setGender(user.getGender());
                userExists.setDateOfBirth(user.getDateOfBirth());
                userExists.setUpdatedAt(new Date());

                // Sinh OTP mới
                String otp = String.format("%06d", new Random().nextInt(999999));
                userExists.setOtpCode(otp);
                userExists.setOtpExpiry(new Date(System.currentTimeMillis() + 5 * 60 * 1000));

                usersService.save(userExists);

                // Gửi mail
                SimpleMailMessage otpMail = new SimpleMailMessage();
                otpMail.setTo(userExists.getEmail());
                otpMail.setSubject("Mã OTP xác nhận đăng ký (cập nhật)");
                otpMail.setText("Xin chào " + userExists.getFullName() +
                        ",\n\nMã OTP mới của bạn là: " + otp +
                        "\nCó hiệu lực trong 5 phút.\n\nRemedic Team");
                otpMail.setFrom("dungminh2505@gmail.com");
                emailService.sendEmail(otpMail);

                modelAndView = new ModelAndView("verify-otp");
                modelAndView.addObject("email", userExists.getEmail());
                return modelAndView;
            }
        }

        // 3. Tạo user mới nếu chưa tồn tại
        System.out.println("✅ Email chưa tồn tại, tạo user mới: " + user.getEmail());

        user.setEnabled(false);
        user.setRole("PATIENT");
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());

        // Sinh OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setOtpCode(otp);
        user.setOtpExpiry(new Date(System.currentTimeMillis() + 5 * 60 * 1000));

        usersService.save(user);

        // Gửi mail
        SimpleMailMessage otpMail = new SimpleMailMessage();
        otpMail.setTo(user.getEmail());
        otpMail.setSubject("Mã OTP xác nhận đăng ký");
        otpMail.setText("Xin chào " + user.getFullName() +
                ",\n\nMã OTP của bạn là: " + otp +
                "\nCó hiệu lực trong 5 phút.\n\nRemedic Team");
        otpMail.setFrom("dungminh2505@gmail.com");
        emailService.sendEmail(otpMail);

        modelAndView = new ModelAndView("verify-otp");
        modelAndView.addObject("email", user.getEmail());
        return modelAndView;

    } catch (Exception e) {
        e.printStackTrace();
        modelAndView.addObject("user", user);
        modelAndView.addObject("errorMessage", "❌ Lỗi khi đăng ký: " + e.getMessage());
        return modelAndView;
    }
}

@RequestMapping(value = "/resend-otp", method = RequestMethod.GET)
public ModelAndView resendOtp(String email) {
    ModelAndView modelAndView = new ModelAndView("verify-otp");
    Users user = usersService.findByEmail(email);

    if (user == null) {
        modelAndView.addObject("errorMessage", "Email không tồn tại!");
        return modelAndView;
    }

    // Sinh OTP mới
    String otp = String.format("%06d", new Random().nextInt(999999));
    user.setOtpCode(otp);
    user.setOtpExpiry(new Date(System.currentTimeMillis() + 5 * 60 * 1000));
    usersService.save(user);

    // Gửi mail OTP mới
    SimpleMailMessage otpMail = new SimpleMailMessage();
    otpMail.setTo(user.getEmail());
    otpMail.setSubject("Mã OTP xác nhận đăng ký (cấp lại)");
    otpMail.setText("Xin chào " + user.getFullName() +
            ",\n\nMã OTP mới của bạn là: " + otp +
            "\nCó hiệu lực trong 5 phút.\n\nRemedic Team");
    otpMail.setFrom("dungminh2505@gmail.com");
    emailService.sendEmail(otpMail);

    modelAndView.addObject("email", email);
    modelAndView.addObject("errorMessage", "✅ OTP mới đã được gửi tới email của bạn!");
    return modelAndView;
}


}
