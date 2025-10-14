package com.spring.bioMedical.Controller;

import com.spring.bioMedical.entity.Clinic;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.service.ClinicService;
import com.spring.bioMedical.service.SpecialtyService;
import com.spring.bioMedical.service.UsersService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/admin-super")
@RequiredArgsConstructor
public class AdminSuperController {

  private final UsersService usersService;
  private final ClinicService clinicService;
  private final SpecialtyService specialtyService;
  private final PasswordEncoder passwordEncoder;

  // Quản lý admin_branch
  @GetMapping("/admin-branch-list")
public String listAdminBranch(Model model) {
  model.addAttribute("users", usersService.findByRole("ADMIN_BRANCH"));
  model.addAttribute("activePage", "branch-list");
  return "admin_super/adminBranchList";
}

  @GetMapping("/add-admin-branch")
  public String showAddAdminBranch(Model model) {
  model.addAttribute("adminBranch", new Users());
  model.addAttribute("clinics", clinicService.findAll());
  model.addAttribute("activePage", "add-branch");
  return "admin_super/addAdminBranch";
}

  @PostMapping("/save-admin-branch")
  public String saveAdminBranch(@ModelAttribute("adminBranch") Users user,
                                @RequestParam("clinicId") Long clinicId) {
    user.setRole("ADMIN_BRANCH");
    user.setEnabled(true);
    // NẾU form gửi password thô ở field passwordHash, bạn nên encode:
    if (user.getPasswordHash() != null && !user.getPasswordHash().isBlank()) {
      user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
    }
    Clinic clinic = clinicService.findById(clinicId);
    usersService.assignClinic(user, clinic);
    usersService.save(user);
    return "redirect:/admin-super/admin-branch-list";
  }

  // Toàn cục
  @GetMapping("/clinics")
  public String clinics(Model model) {
    model.addAttribute("clinics", clinicService.findAll());
    return "admin_super/clinics";
  }

  @GetMapping("/specialties")
  public String specialties(Model model) {
    model.addAttribute("specialties", specialtyService.getAllSpecialties());
    return "admin_super/specialties";
  }

@GetMapping("/profile")
public String profile(Model model, Principal principal) {
  Users me = usersService.findByUsername(principal.getName());
  model.addAttribute("me", me);
  model.addAttribute("activePage", "profile");
  return "admin_super/adminProfile";
}


  @PostMapping("/profile/change-password")
  public String changePasswordOnProfile(@RequestParam String currentPassword,
                                        @RequestParam String newPassword,
                                        @RequestParam String confirmPassword,
                                        Model model,
                                        Principal principal) {
    Users me = usersService.findByUsername(principal.getName());

    if (!passwordEncoder.matches(currentPassword, me.getPasswordHash())) {
      model.addAttribute("pwError", "Mật khẩu hiện tại không đúng!");
      model.addAttribute("me", me);
      return "admin_super/adminProfile";
    }
    if (!newPassword.equals(confirmPassword)) {
      model.addAttribute("pwError", "Mật khẩu xác nhận không khớp!");
      model.addAttribute("me", me);
      return "admin_super/adminProfile";
    }
    if (newPassword.length() < 6) {
      model.addAttribute("pwError", "Mật khẩu mới phải có ít nhất 6 ký tự!");
      model.addAttribute("me", me);
      return "admin_super/adminProfile";
    }

    me.setPasswordHash(passwordEncoder.encode(newPassword));
    usersService.save(me);

    model.addAttribute("pwSuccess", "Đổi mật khẩu thành công!");
    model.addAttribute("me", me);
    return "admin_super/adminProfile";
  }
}
