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

    // API check username exists (realtime)
    @GetMapping("/check-username")
    @ResponseBody
    public boolean checkUsername(@RequestParam("username") String username) {
        Users u = usersService.findByUsername(username);
        return (u == null); // true = available
    }

    // API check email exists (realtime)
    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam("email") String email) {
        Users u = usersService.findByEmail(email);
        return (u == null); // true = available
    }

    // GET registration form
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView showRegistrationPage() {
        ModelAndView mv = new ModelAndView("register");
        mv.addObject("user", new Users());
        return mv;
    }

    // POST handle registration
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView processRegistrationForm(@Valid Users user,
                                                BindingResult bindingResult,
                                                @RequestParam("confirmPassword") String confirmPassword,
                                                HttpServletRequest request) {

        // ====== SERVER-SIDE VALIDATION ======

        // 1) Username required + unique
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            bindingResult.rejectValue("username", "", "Username is required");
        } else {
            Users existedUsername = usersService.findByUsername(user.getUsername().trim());
            if (existedUsername != null) {
                bindingResult.rejectValue("username", "", "Username is already taken");
            }
        }

        // 2) Email required + unique if activated
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            bindingResult.rejectValue("email", "", "Email is required");
        } else {
            Users existedEmail = usersService.findByEmail(user.getEmail().trim());
            if (existedEmail != null && Boolean.TRUE.equals(existedEmail.getEnabled())) {
                bindingResult.rejectValue("email", "", "This email is already registered and activated");
            }
        }

        // 3) Password ≥8, 1 uppercase, 1 number, 1 special char
        String pass = user.getPasswordHash();
        if (pass == null || pass.isEmpty()) {
            bindingResult.rejectValue("passwordHash", "", "Password is required");
        } else if (!pass.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$")) {
            bindingResult.rejectValue("passwordHash", "", "Minimum 8 characters, 1 uppercase, 1 number, 1 special character");
        }

        // 4) Confirm password
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            bindingResult.rejectValue("passwordHash", "", "Confirm password is required");
        } else if (!confirmPassword.equals(user.getPasswordHash())) {
            bindingResult.rejectValue("passwordHash", "", "Passwords do not match");
        }

        // 5) Phone number (10 digits)
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            bindingResult.rejectValue("phone", "", "Phone number is required");
        } else if (!user.getPhone().matches("^[0-9]{10}$")) {
            bindingResult.rejectValue("phone", "", "Phone number must be exactly 10 digits");
        }

        // 6) Full name
        if (user.getFullName() == null || user.getFullName().trim().length() < 2) {
            bindingResult.rejectValue("fullName", "", "Full name is required");
        }

        // 7) Gender
        if (user.getGender() == null || user.getGender().trim().isEmpty()) {
            bindingResult.rejectValue("gender", "", "Gender is required");
        }

        // 8) DOB not in the future
        if (user.getDateOfBirth() == null) {
            bindingResult.rejectValue("dateOfBirth", "", "Date of birth is required");
        } else if (user.getDateOfBirth().isAfter(LocalDate.now())) {
            bindingResult.rejectValue("dateOfBirth", "", "Date of birth cannot be in the future");
        }

        // If validation errors => return to form
        if (bindingResult.hasErrors()) {
            ModelAndView mv = new ModelAndView("register");
            mv.addObject("user", user);
            mv.addObject("org.springframework.validation.BindingResult.user", bindingResult);
            return mv;
        }

        // ====== USER CREATION / UPDATE LOGIC ======
        Users userByEmail = usersService.findByEmail(user.getEmail());
        if (userByEmail != null && !Boolean.TRUE.equals(userByEmail.getEnabled())) {
            // Update unverified profile
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
            mail.setSubject("Registration OTP Code (Updated)");
            mail.setText("Hello " + userByEmail.getFullName() +
                    ",\n\nYour new OTP code is: " + otp +
                    "\nValid for 5 minutes.\n\nRemedic Team");
            mail.setFrom("dungminh2505@gmail.com");
            emailService.sendEmail(mail);

            ModelAndView mv = new ModelAndView("verify-otp");
            mv.addObject("email", userByEmail.getEmail());
            return mv;
        }

        // Create new user
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
        otpMail.setSubject("Registration OTP Code");
        otpMail.setText("Hello " + user.getFullName() +
                ",\n\nYour OTP code is: " + otp +
                "\nValid for 5 minutes.\n\nRemedic Team");
        otpMail.setFrom("dungminh2505@gmail.com");
        emailService.sendEmail(otpMail);

        ModelAndView mv = new ModelAndView("verify-otp");
        mv.addObject("email", user.getEmail());
        return mv;
    }
}
