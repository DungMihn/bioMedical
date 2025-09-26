package com.spring.bioMedical.Controller;

import java.time.LocalDate;
import java.util.Date;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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

    // API check username tồn tại (realtime)
    @GetMapping("/check-username")
    @ResponseBody
    public boolean checkUsername(@RequestParam("username") String username) {
        Users u = usersService.findByUsername(username);
        return (u == null); // true = available
    }

    // API check email tồn tại (realtime)
    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam("email") String email) {
        Users u = usersService.findByEmail(email);
        return (u == null); // true = available
    }

    // GET form đăng ký
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView showRegistrationPage() {
        ModelAndView mv = new ModelAndView("register");
        mv.addObject("user", new Users());
        return mv;
    }

    // POST xử lý đăng ký
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView processRegistrationForm(@Valid Users user,
                                                BindingResult bindingResult,
                                                @RequestParam("confirmPassword") String confirmPassword,
                                                HttpServletRequest request) {

        // ====== VALIDATION SERVER-SIDE ======

        // 1) Username required + unique
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            bindingResult.rejectValue("username", "", "Username is required");
        } else {
            Users existedUsername = usersService.findByUsername(user.getUsername().trim());
            if (existedUsername != null) {
                bindingResult.rejectValue("username", "", "Username is already taken");
            }
        }

        // 2) Email required + unique nếu đã kích hoạt
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            bindingResult.rejectValue("email", "", "Email is required");
        } else {
            Users existedEmail = usersService.findByEmail(user.getEmail().trim());
            if (existedEmail != null && Boolean.TRUE.equals(existedEmail.getEnabled())) {
                bindingResult.rejectValue("email", "", "This email is already registered and activated");
            }
        }

        // 3) Password ≥8, 1 hoa, 1 số, 1 ký tự đặc biệt
        String pass = user.getPasswordHash();
        if (pass == null || pass.isEmpty()) {
            bindingResult.rejectValue("passwordHash", "", "Password is required");
        } else if (!pass.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$")) {
            bindingResult.rejectValue("passwordHash", "", "Min 8 chars, 1 uppercase, 1 number, 1 special char");
        }

        // 4) Confirm password
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            bindingResult.rejectValue("passwordHash", "", "Confirm password is required");
        } else if (!confirmPassword.equals(user.getPasswordHash())) {
            bindingResult.rejectValue("passwordHash", "", "Passwords do not match");
        }

        // 5) Phone 10 số
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            bindingResult.rejectValue("phone", "", "Phone is required");
        } else if (!user.getPhone().matches("^[0-9]{10}$")) {
            bindingResult.rejectValue("phone", "", "Phone must be exactly 10 digits");
        }

        // 6) Full name
        if (user.getFullName() == null || user.getFullName().trim().length() < 2) {
            bindingResult.rejectValue("fullName", "", "Full name is required");
        }

        // 7) Gender
        if (user.getGender() == null || user.getGender().trim().isEmpty()) {
            bindingResult.rejectValue("gender", "", "Gender is required");
        }

        // 8) DOB không lớn hơn hôm nay
        if (user.getDateOfBirth() == null) {
            bindingResult.rejectValue("dateOfBirth", "", "Date of birth is required");
        } else if (user.getDateOfBirth().isAfter(LocalDate.now())) {
            bindingResult.rejectValue("dateOfBirth", "", "Date of birth cannot be in the future");
        }

        // Nếu có lỗi => quay lại form
        if (bindingResult.hasErrors()) {
            ModelAndView mv = new ModelAndView("register");
            mv.addObject("user", user);
            mv.addObject("org.springframework.validation.BindingResult.user", bindingResult);
            return mv;
        }

        // ====== LOGIC TẠO/CẬP NHẬT USER ======
        Users userByEmail = usersService.findByEmail(user.getEmail());
        if (userByEmail != null && !Boolean.TRUE.equals(userByEmail.getEnabled())) {
            // Update hồ sơ chưa kích hoạt
            userByEmail.setUsername(user.getUsername());
            userByEmail.setFullName(user.getFullName());
            userByEmail.setPasswordHash(user.getPasswordHash());
            userByEmail.setPhone(user.getPhone());
            userByEmail.setGender(user.getGender());
            userByEmail.setDateOfBirth(user.getDateOfBirth());
            userByEmail.setUpdatedAt(new Date());

            String otp = String.format("%06d", new Random().nextInt(999999));
            userByEmail.setOtpCode(otp);
            userByEmail.setOtpExpiry(new Date(System.currentTimeMillis() + 5 * 60 * 1000));
            usersService.save(userByEmail);

            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(userByEmail.getEmail());
            mail.setSubject("Mã OTP xác nhận đăng ký (cập nhật)");
            mail.setText("Xin chào " + userByEmail.getFullName() +
                    ",\n\nMã OTP mới của bạn là: " + otp +
                    "\nCó hiệu lực trong 5 phút.\n\nRemedic Team");
            mail.setFrom("dungminh2505@gmail.com");
            emailService.sendEmail(mail);

            ModelAndView mv = new ModelAndView("verify-otp");
            mv.addObject("email", userByEmail.getEmail());
            return mv;
        }

        // Tạo user mới
        user.setEnabled(false);
        user.setRole("PATIENT");
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());

        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setOtpCode(otp);
        user.setOtpExpiry(new Date(System.currentTimeMillis() + 5 * 60 * 1000));
        usersService.save(user);

        SimpleMailMessage otpMail = new SimpleMailMessage();
        otpMail.setTo(user.getEmail());
        otpMail.setSubject("Mã OTP xác nhận đăng ký");
        otpMail.setText("Xin chào " + user.getFullName() +
                ",\n\nMã OTP của bạn là: " + otp +
                "\nCó hiệu lực trong 5 phút.\n\nRemedic Team");
        otpMail.setFrom("dungminh2505@gmail.com");
        emailService.sendEmail(otpMail);

        ModelAndView mv = new ModelAndView("verify-otp");
        mv.addObject("email", user.getEmail());
        return mv;
    }
}
