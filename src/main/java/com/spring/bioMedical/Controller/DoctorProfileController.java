/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.bioMedical.Controller;

import com.spring.bioMedical.entity.Doctors;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.service.DoctorProfileService;
import com.spring.bioMedical.service.UsersService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author Admin
 */
@Controller
@RequestMapping("/doctor")
public class DoctorProfileController {

    @Autowired
    private DoctorProfileService doctorProfileService;

    @Autowired
    private UsersService usersService;

    @GetMapping("/profile")
    public String viewProfile(Model model) {
        try {
            System.out.println("=== DEBUG: Bắt đầu load profile ===");

            String username = getCurrentUsername();
            System.out.println("=== DEBUG: Username: " + username);

            Users currentUser = usersService.findByUsername(username);
            System.out.println("=== DEBUG: Current user: " + (currentUser != null ? currentUser.getFullName() : "null"));

            if (currentUser != null) {
                System.out.println("=== DEBUG: User ID: " + currentUser.getUserId());

                Map<String, Object> profileData = doctorProfileService.getDoctorProfileWithDetails(currentUser.getUserId());
                System.out.println("=== DEBUG: Profile data loaded: " + (profileData != null));

                model.addAllAttributes(profileData);
                System.out.println("=== DEBUG: Trả về trang profile ===");
                return "doctor/profile";
            } else {
                System.out.println("=== DEBUG: Current user is null ===");
                return "redirect:/doctor/index?error=UserNotFound";
            }

        } catch (Exception e) {
            System.err.println("=== DEBUG: Lỗi trong viewProfile: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/doctor/index?error=" + e.getMessage();
        }
    }

    @GetMapping("/profile/edit")
    public String editProfileForm(Model model) {
        try {
            String username = getCurrentUsername();
            Users currentUser = usersService.findByUsername(username);

            if (currentUser != null) {
                Doctors doctor = doctorProfileService.getDoctorProfile(currentUser.getUserId());
                model.addAttribute("doctor", doctor);
                model.addAttribute("user", currentUser);
            }

            return "doctor/profile-edit";

        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải trang chỉnh sửa");
            return "redirect:/doctor/index?error=" + URLEncoder.encode("Thông báo lỗi", StandardCharsets.UTF_8);
        }
    }

    @PostMapping("/profile/update-bio")
    public String updateBio(@RequestParam String bio, Model model) {
        try {
            String username = getCurrentUsername();
            Users currentUser = usersService.findByUsername(username);

            if (currentUser != null) {
                Doctors doctor = doctorProfileService.getDoctorProfile(currentUser.getUserId());
                doctorProfileService.updateDoctorBio(doctor.getDoctorId(), bio);
                model.addAttribute("success", "Cập nhật thông tin thành công!");
            }

            return "redirect:/doctor/profile";

        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi cập nhật thông tin");
            return "redirect:/doctor/index?error=" + URLEncoder.encode("Thông báo lỗi", StandardCharsets.UTF_8);
        }
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
