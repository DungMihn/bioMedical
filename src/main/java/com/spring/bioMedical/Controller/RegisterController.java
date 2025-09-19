package com.spring.bioMedical.Controller;

import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
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

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView showRegistrationPage(ModelAndView modelAndView, Users user) {
        modelAndView.addObject("user", user);
        modelAndView.setViewName("register");
        return modelAndView;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView processRegistrationForm(ModelAndView modelAndView,
                                                @Valid Users user,
                                                BindingResult bindingResult,
                                                HttpServletRequest request) {

        Users userExists = usersService.findByEmail(user.getEmail());
        if (userExists != null) {
            modelAndView.addObject("alreadyRegisteredMessage",
                    "Oops! Đã có tài khoản đăng ký bằng email này.");
            modelAndView.setViewName("register");
            bindingResult.reject("email");
        }

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("register");
        } else {
            user.setEnabled(true);
            user.setRole("ROLE_USER");
            user.setPasswordHash(user.getPasswordHash());
            user.setUsername(user.getUsername());
            user.setCreatedAt(new java.util.Date());
            user.setUpdatedAt(new java.util.Date());

            // Tạo token xác nhận
            user.setEmail(user.getEmail());
            usersService.save(user);

            String appUrl = "http://localhost:8080";
            SimpleMailMessage registrationEmail = new SimpleMailMessage();
            registrationEmail.setTo(user.getEmail());
            registrationEmail.setSubject("Registration Confirmation");
            registrationEmail.setText("Vui lòng xác nhận email bằng link sau:\n"
                    + appUrl + "/confirm?token=" + UUID.randomUUID());
            registrationEmail.setFrom("spring.email.auth@gmail.com");

            emailService.sendEmail(registrationEmail);

            modelAndView.addObject("confirmationMessage",
                    "Email xác nhận đã được gửi tới " + user.getEmail());
            modelAndView.setViewName("register");
        }

        return modelAndView;
    }

    @RequestMapping(value = "/confirm", method = RequestMethod.GET)
    public ModelAndView confirmRegistration(ModelAndView modelAndView,
                                            @RequestParam("token") String token) {
        // TODO: xử lý token thực tế
        modelAndView.addObject("confirmationToken", token);
        modelAndView.setViewName("confirm");
        return modelAndView;
    }

    @RequestMapping(value = "/confirm", method = RequestMethod.POST)
    public ModelAndView confirmRegistration(ModelAndView modelAndView,
                                            BindingResult bindingResult,
                                            @RequestParam Map<String, String> requestParams,
                                            RedirectAttributes redir) {

        Zxcvbn passwordCheck = new Zxcvbn();
        Strength strength = passwordCheck.measure(requestParams.get("password"));

        if (strength.getScore() < 3) {
            redir.addFlashAttribute("errorMessage",
                    "Mật khẩu quá yếu. Hãy chọn mật khẩu mạnh hơn.");
            modelAndView.setViewName("redirect:confirm?token=" + requestParams.get("token"));
            return modelAndView;
        }

        Users user = usersService.findByEmail(requestParams.get("email"));
        if (user != null) {
            user.setPasswordHash(requestParams.get("password"));
            user.setEnabled(true);
            usersService.save(user);
            modelAndView.addObject("successMessage", "Mật khẩu đã được đặt thành công!");
        }
        modelAndView.setViewName("confirm");
        return modelAndView;
    }
}
