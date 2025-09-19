package com.spring.bioMedical.Controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.entity.Appointment;
import com.spring.bioMedical.service.UsersService;
import com.spring.bioMedical.service.AppointmentServiceImplementation;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    private final UsersService usersService;
    private final AppointmentServiceImplementation appointmentServiceImplementation;

    @Autowired
    public DoctorController(UsersService usersService,
                            AppointmentServiceImplementation appointmentServiceImplementation) {
        this.usersService = usersService;
        this.appointmentServiceImplementation = appointmentServiceImplementation;
    }

    @RequestMapping("/index")
    public String index(Model model) {
        // Lấy username hiện tại
        String username = getCurrentUsername();

        // Lấy user từ DB
        Users currentUser = usersService.findByUsername(username);

        // Cập nhật last seen
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        currentUser.setUpdatedAt(new Date());
        usersService.save(currentUser);

        // Lấy danh sách appointments
        List<Appointment> list = appointmentServiceImplementation.findAll();

        model.addAttribute("name", currentUser.getFullName());
        model.addAttribute("email", currentUser.getEmail());
        model.addAttribute("user", currentUser.getFullName());
        model.addAttribute("app", list);

        return "doctor/index";
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
