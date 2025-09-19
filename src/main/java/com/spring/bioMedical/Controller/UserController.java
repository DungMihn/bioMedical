package com.spring.bioMedical.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.entity.Appointment;
import com.spring.bioMedical.service.UsersService;
import com.spring.bioMedical.service.AppointmentServiceImplementation;

@Controller
@RequestMapping("/user")
public class UserController {

    private final AppointmentServiceImplementation appointmentServiceImplementation;
    private final UsersService usersService;

    @Autowired
    public UserController(AppointmentServiceImplementation appointmentServiceImplementation,
                          UsersService usersService) {
        this.appointmentServiceImplementation = appointmentServiceImplementation;
        this.usersService = usersService;
    }

    @GetMapping("/index")
    public String index(Model model) {
        Users currentUser = usersService.findByUsername(getCurrentUsername());

        Appointment app = new Appointment();
        app.setName(currentUser.getFullName());
        app.setEmail(currentUser.getEmail());

        model.addAttribute("app", app);
        return "user/index";
    }

    @PostMapping("/save-app")
    public String saveAppointment(@ModelAttribute("app") Appointment app) {
        appointmentServiceImplementation.save(app);
        return "redirect:/user/index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("app", new Appointment());
        return "user/about";
    }

    @GetMapping("/blog")
    public String blog(Model model) {
        model.addAttribute("app", new Appointment());
        return "user/blog";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("app", new Appointment());
        return "user/contact";
    }

    @GetMapping("/doctor")
    public String doctor(Model model) {
        model.addAttribute("app", new Appointment());
        return "user/doctor";
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }
}
// mới