package com.spring.bioMedical.Controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
@RequestMapping("/admin")
public class AdminController {

    private final UsersService usersService;
    private final AppointmentServiceImplementation appointmentServiceImplementation;

    @Autowired
    public AdminController(UsersService usersService,
                           AppointmentServiceImplementation appointmentServiceImplementation) {
        this.usersService = usersService;
        this.appointmentServiceImplementation = appointmentServiceImplementation;
    }

    @RequestMapping("/user-details")
    public String userDetails(Model model) {
        List<Users> list = usersService.findByRole("PATIENT");
        model.addAttribute("user", list);
        return "admin/user";
    }

    @RequestMapping("/doctor-details")
    public String doctorDetails(Model model) {
        List<Users> list = usersService.findByRole("DOCTOR");
        model.addAttribute("user", list);
        return "admin/doctor";
    }

    @RequestMapping("/admin-details")
    public String adminDetails(Model model) {
        List<Users> list = usersService.findByRole("ADMIN");
        model.addAttribute("user", list);
        return "admin/admin";
    }

    @GetMapping("/add-doctor")
    public String showFormForAddDoctor(Model model) {
        Users user = new Users();
        user.setRole("DOCTOR");
        model.addAttribute("doctor", user);
        return "admin/addDoctor";
    }

    @PostMapping("/save-doctor")
    public String saveDoctor(@ModelAttribute("doctor") Users user) {
        user.setRole("DOCTOR");
        user.setEnabled(true);
        user.setPasswordHash("default"); // TODO: mã hóa password
        usersService.save(user);
        return "redirect:/admin/doctor-details";
    }

    @GetMapping("/add-admin")
    public String showFormForAddAdmin(Model model) {
        Users user = new Users();
        user.setRole("ADMIN");
        model.addAttribute("admin", user);
        return "admin/addAdmin";
    }

    @PostMapping("/save-admin")
    public String saveAdmin(@ModelAttribute("admin") Users user) {
        user.setRole("ADMIN");
        user.setEnabled(true);
        user.setPasswordHash("default"); // TODO: mã hóa password
        usersService.save(user);
        return "redirect:/admin/admin-details";
    }

    @GetMapping("/edit-my-profile")
    public String editProfile(Model model) {
        String username = getCurrentUsername();
        Users currentUser = usersService.findByUsername(username);
        model.addAttribute("profile", currentUser);
        return "admin/updateMyProfile";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute("profile") Users user) {
        usersService.save(user);
        return "redirect:/admin/user-details";
    }

    @RequestMapping("/appointments")
    public String appointments(Model model) {
        List<Appointment> list = appointmentServiceImplementation.findAll();
        model.addAttribute("app", list);
        return "admin/appointment";
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