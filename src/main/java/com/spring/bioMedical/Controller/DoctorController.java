package com.spring.bioMedical.Controller;

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
import com.spring.bioMedical.entity.Doctor;
import com.spring.bioMedical.service.UsersService;
import com.spring.bioMedical.service.AppointmentServiceImplementation;
import com.spring.bioMedical.service.DoctorService;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    private final UsersService usersService;
    private final AppointmentServiceImplementation appointmentServiceImplementation;
    private final DoctorService doctorService; // 👈 thêm field này

    @Autowired
    public DoctorController(UsersService usersService,
                            AppointmentServiceImplementation appointmentServiceImplementation,
                            DoctorService doctorService) { // 👈 truyền vào constructor
        this.usersService = usersService;
        this.appointmentServiceImplementation = appointmentServiceImplementation;
        this.doctorService = doctorService;
    }

   @RequestMapping("/index")
public String index(Model model) {
    String username = getCurrentUsername();
    Users currentUser = usersService.findByUsername(username);

    currentUser.setUpdatedAt(LocalDateTime.now());
    usersService.save(currentUser);

    // lấy thông tin Doctor từ user_id
    Doctor doctor = doctorService.getDoctorByUserId(currentUser.getUserId());

    List<Appointment> list = appointmentServiceImplementation.findAll();

    model.addAttribute("name", currentUser.getFullName());
    model.addAttribute("email", currentUser.getEmail());
    model.addAttribute("bio", doctor != null ? doctor.getBio() : "");
    model.addAttribute("clinic", doctor != null ? doctor.getClinic().getName() : "");
    model.addAttribute("specialty", doctor != null ? doctor.getSpecialty().getName() : "");
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
