package com.spring.bioMedical.Controller;

import com.spring.bioMedical.dto.AdminBranchForm;
import com.spring.bioMedical.entity.Clinic;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.service.ClinicService;
import com.spring.bioMedical.service.SpecialtyService;
import com.spring.bioMedical.service.UsersService;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
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

  @GetMapping("/admin-branch-list")
  public String listAdminBranch(Model model) {
    model.addAttribute("users", usersService.findByRole("ADMIN_BRANCH"));
    model.addAttribute("activePage", "branch-list");
    return "admin_super/adminBranchList";
  }

  // ==== ADD ADMIN BRANCH (FORM) ====
  @GetMapping("/add-admin-branch")
  public String showAddAdminBranch(Model model) {
    model.addAttribute("adminBranch", new AdminBranchForm());
    model.addAttribute("clinics", clinicService.findAll());
    model.addAttribute("activePage", "add-branch");
    return "admin_super/addAdminBranch";
  }

  // ==== ADD ADMIN BRANCH (SUBMIT) ====
  @PostMapping("/save-admin-branch")
  public String saveAdminBranch(@Valid @ModelAttribute("adminBranch") AdminBranchForm form,
                                org.springframework.validation.BindingResult binding,
                                Model model) {

    // check trùng
    if (!binding.hasFieldErrors("username") && usersService.usernameTaken(form.getUsername())) {
      binding.rejectValue("username", "exists", "Username đã tồn tại");
    }
    if (!binding.hasFieldErrors("email") && usersService.emailTaken(form.getEmail())) {
      binding.rejectValue("email", "exists", "Email đã được sử dụng");
    }

    if (binding.hasErrors()) {
      model.addAttribute("clinics", clinicService.findAll());
      model.addAttribute("activePage", "add-branch");
      return "admin_super/addAdminBranch";
    }

    // Map DTO -> Users
    Users user = new Users();
    user.setUsername(form.getUsername().trim());
    user.setPasswordHash(passwordEncoder.encode(form.getPasswordHash()));
    user.setEmail(form.getEmail().trim());
    user.setPhone(form.getPhone());
    user.setFullName(form.getFullName());
    user.setRole("ADMIN_BRANCH");
    user.setEnabled(true);

    Clinic clinic = clinicService.findById(form.getClinicId());
    usersService.assignClinic(user, clinic);
    usersService.save(user);

    return "redirect:/admin-super/admin-branch-list";
  }

  // ==== AJAX realtime validation ====
  @GetMapping("/api/check-username")
  @ResponseBody
  public Map<String,Object> suCheckUsername(@RequestParam("u") String u) {
    boolean ok = (u != null && u.trim().length() > 0 && !usersService.usernameTaken(u));
    Map<String,Object> m = new HashMap<String,Object>();
    m.put("valid", ok);
    m.put("message", ok ? "" : "Username đã tồn tại");
    return m;
  }

  @GetMapping("/api/check-email")
  @ResponseBody
  public Map<String,Object> suCheckEmail(@RequestParam("e") String e) {
    boolean ok = (e != null && e.trim().length() > 0 && !usersService.emailTaken(e));
    Map<String,Object> m = new HashMap<String,Object>();
    m.put("valid", ok);
    m.put("message", ok ? "" : "Email đã được sử dụng");
    return m;
  }

  // ===== phần còn lại của controller giữ nguyên =====

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
