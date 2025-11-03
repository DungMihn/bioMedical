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
import com.spring.bioMedical.entity.Appointments;
import com.spring.bioMedical.service.UsersService;
import com.spring.bioMedical.service.BookingService;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    private final UsersService usersService;
    private final BookingService bookingService;

    @Autowired
    public DoctorController(UsersService usersService, BookingService bookingService) {
        this.usersService = usersService;
        this.bookingService = bookingService;
    }

    @RequestMapping("/index")
    public String index(Model model) {
        String username = getCurrentUsername();
        Users currentUser = usersService.findByUsername(username);

        currentUser.setUpdatedAt(new Date());
        usersService.save(currentUser);

        List<Appointments> list = bookingService.getAllAppointments();

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
