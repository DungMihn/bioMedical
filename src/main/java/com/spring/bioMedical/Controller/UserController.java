package com.spring.bioMedical.Controller;

import com.spring.bioMedical.dto.BookingRequest;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.repository.*;
import com.spring.bioMedical.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.spring.bioMedical.entity.Appointments;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired private UsersService usersService;
    @Autowired private ClinicRepository clinicRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private AppointmentSlotRepository slotRepository;
    @Autowired private AppointmentRepository appointmentRepository;

    //  Common model attributes for all views
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        String username = getCurrentUsername();
        Users currentUser = null;
        if (username != null && !"anonymousUser".equals(username)) {
            currentUser = usersService.findByUsername(username);
            model.addAttribute("appointments", appointmentRepository.findByUserId(currentUser));
        }
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("appointmentForm", new BookingRequest());
        model.addAttribute("clinics", clinicRepository.findAll());
        model.addAttribute("doctors", doctorRepository.findAll());
        model.addAttribute("availableSlots", slotRepository.findAll());
    }

    //  Main pages
    @GetMapping("/index")
    public String index() {
        return "user/index";
    }

    @GetMapping("/about")
    public String about() {
        return "user/about";
    }

    @GetMapping("/blog")
    public String blog() {
        return "user/blog";
    }

    @GetMapping("/blog-single")
    public String blogSingle() {
        return "user/blog-single";
    }

    @GetMapping("/departments")
    public String departments() {
        return "user/departments";
    }

    @GetMapping("/department-single")
    public String departmentSingle() {
        return "user/department-single";
    }

    @GetMapping("/contact")
    public String contact() {
        return "user/contact";
    }

    @GetMapping("/doctor")
    public String doctor() {
        return "user/doctor";
    }

    // Account page
    @GetMapping("/account")
    public String account(Model model) {
        Users currentUser = getCurrentUser();
        if (currentUser != null) {
            model.addAttribute("user", currentUser);

            // 🔽 Lấy danh sách lịch, sau đó sắp xếp giảm dần theo ngày tạo
            List<Appointments> list = appointmentRepository.findByUserId(currentUser);
            list.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())); // newest first

            model.addAttribute("appointments", list);
        }
        return "user/account";
    }


    // Update profile
    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute("user") Users updatedUser, RedirectAttributes redirectAttributes) {
        Users currentUser = getCurrentUser();
        if (currentUser != null) {
            if (updatedUser.getFullName() == null || updatedUser.getFullName().trim().isEmpty() ||
                updatedUser.getPhone() == null || updatedUser.getPhone().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMsg", "Full name and phone number are required!");
                return "redirect:/user/account";
            }

            currentUser.setFullName(updatedUser.getFullName().trim());
            currentUser.setPhone(updatedUser.getPhone().trim());
            currentUser.setGender(updatedUser.getGender());
            currentUser.setDateOfBirth(updatedUser.getDateOfBirth());
            usersService.save(currentUser);

            redirectAttributes.addFlashAttribute("successMsg", "Profile updated successfully!");
        }
        return "redirect:/user/account";
    }


    //  Helper methods
    private Users getCurrentUser() {
        String username = getCurrentUsername();
        return usersService.findByUsername(username);
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails)
            return ((UserDetails) principal).getUsername();
        return principal.toString();
    }
}