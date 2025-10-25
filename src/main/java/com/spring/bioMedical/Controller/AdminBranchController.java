package com.spring.bioMedical.Controller;

import com.spring.bioMedical.dto.DoctorForm;
import com.spring.bioMedical.entity.Appointment;
import com.spring.bioMedical.entity.Clinic;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.service.AppointmentServiceImplementation;
import com.spring.bioMedical.service.ClinicService;
import com.spring.bioMedical.service.SpecialtyService;
import com.spring.bioMedical.service.UsersService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;

@Controller
@RequestMapping("/admin-branch")
public class AdminBranchController {

  private final UsersService usersService;
  private final AppointmentServiceImplementation appointmentService;
  private final ClinicService clinicService;
  private final SpecialtyService specialtyService;

  public AdminBranchController(UsersService usersService,
                               AppointmentServiceImplementation appointmentService,
                               ClinicService clinicService,
                               SpecialtyService specialtyService) {
    this.usersService = usersService;
    this.appointmentService = appointmentService;
    this.clinicService = clinicService;
    this.specialtyService = specialtyService;
  }

  /* ========== DOCTOR LIST ========== */
  @GetMapping("/doctor-details")
  public String doctors(Model model) {
    Long clinicId = currentClinicId();
    model.addAttribute("users", usersService.findDoctorsByClinic(clinicId));
    model.addAttribute("activePage", "doctor-details");
    return "admin_branch/doctor";
  }

  /* ========== ADD DOCTOR (FORM) ========== */
  @GetMapping("/add-doctor")
  public String showAddDoctorForm(Model model) {
    model.addAttribute("doctorForm", new DoctorForm());

    Long clinicId = currentClinicId();
    List<Clinic> clinics;
    if (clinicId != null) {
      Clinic c = clinicService.findById(clinicId);
      clinics = Collections.singletonList(c); // Java 8
    } else {
      clinics = clinicService.findAll();
    }

    model.addAttribute("clinics", clinics);
    model.addAttribute("specialties", specialtyService.getAllSpecialties());
    model.addAttribute("activePage", "add-doctor");

    return "admin_branch/addDoctor";
  }

  /* ========== ADD DOCTOR (SUBMIT) ========== */
  @PostMapping("/save-doctor")
  public String saveDoctor(@Valid @ModelAttribute("doctorForm") DoctorForm form,
                           org.springframework.validation.BindingResult binding,
                           Model model) {

    // Kiểm tra trùng username/email
    if (!binding.hasFieldErrors("username") && usernameTaken(form.getUsername())) {
      binding.rejectValue("username", "exists", "Username đã tồn tại");
    }
    if (!binding.hasFieldErrors("email") && emailTaken(form.getEmail())) {
      binding.rejectValue("email", "exists", "Email đã được sử dụng");
    }

    if (binding.hasErrors()) {
      Long clinicId = currentClinicId();
      List<Clinic> clinics;
      if (clinicId != null) {
        Clinic c = clinicService.findById(clinicId);
        clinics = java.util.Collections.singletonList(c);
      } else {
        clinics = clinicService.findAll();
      }
      model.addAttribute("clinics", clinics);
      model.addAttribute("specialties", specialtyService.getAllSpecialties());
      return "admin_branch/addDoctor";
    }

    // Map form -> Users
    Users user = new Users();
    user.setUsername(form.getUsername().trim());
    user.setPasswordHash(form.getPasswordHash()); // Khuyến nghị mã hoá nếu có PasswordEncoder
    user.setEmail(form.getEmail() != null ? form.getEmail().trim() : null);
    user.setPhone(form.getPhone());
    user.setFullName(form.getFullName());
    user.setGender(form.getGender());
    user.setDateOfBirth(form.getDateOfBirth());
    user.setRole("DOCTOR");
    user.setEnabled(true);

    Long cid = (form.getClinicId() != null) ? form.getClinicId() : currentClinicId();
    if (cid != null) {
      Clinic clinic = clinicService.findById(cid);
      usersService.assignClinic(user, clinic);
    }

    usersService.save(user);
    return "redirect:/admin-branch/doctor-details";
  }

  /* ========== APPOINTMENTS ========== */
  @GetMapping("/appointments")
  public String appointments(Model model) {
    Long clinicId = currentClinicId();
    model.addAttribute("app", appointmentService.findAllByBranch(clinicId)); // ✅ chỉ chi nhánh của mình
    model.addAttribute("activePage", "appointments");
    return "admin_branch/appointment";
  }

  @PostMapping("/appointments/save")
  public String saveAppointment(@ModelAttribute("appointment") Appointment app) {
    Long clinicId = currentClinicId();
    Clinic clinic = clinicService.findById(clinicId);
    app.setClinic(clinic);                     // ✅ ràng chi nhánh
    appointmentService.save(app);
    return "redirect:/admin-branch/appointments";
  }

  /* ========== PROFILE ========== */
  @GetMapping("/edit-my-profile")
  public String editProfile(Model model) {
    Users me = usersService.findByUsername(currentUsername());
    model.addAttribute("profile", me);
    model.addAttribute("activePage", "my-profile");
    return "admin_branch/updateMyProfile";
  }

  @PostMapping("/update")
  public String updateProfile(@ModelAttribute("profile") Users form) {
    Users me = usersService.findById(form.getUserId());
    if (me == null) {
      return "redirect:/admin-branch/doctor-details";
    }
    me.setFullName(form.getFullName());
    me.setGender(form.getGender());
    me.setDateOfBirth(form.getDateOfBirth());
    usersService.save(me);
    return "redirect:/admin-branch/doctor-details";
  }

  // === AJAX VALIDATION API ===
  // Trả JSON để form blur-check username/email
  @GetMapping("/api/check-username")
  @ResponseBody
  public Map<String, Object> checkUsername(@RequestParam("u") String u) {
    boolean ok = (u != null && u.trim().length() > 0 && !usernameTaken(u));
    Map<String, Object> m = new HashMap<String, Object>();
    m.put("valid", ok);
    m.put("message", ok ? "" : "Username đã tồn tại");
    return m;
  }

  @GetMapping("/api/check-email")
  @ResponseBody
  public Map<String, Object> checkEmail(@RequestParam("e") String e) {
    boolean ok = (e != null && e.trim().length() > 0 && !emailTaken(e));
    Map<String, Object> m = new HashMap<String, Object>();
    m.put("valid", ok);
    m.put("message", ok ? "" : "Email đã được sử dụng");
    return m;
  }

  /* ========== HELPERS (CHỈ 1 BỘ, KHÔNG ĐƯỢC TRÙNG) ========== */
  private String currentUsername() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof UserDetails) {
      return ((UserDetails) principal).getUsername();
    }
    return String.valueOf(principal);
  }

  private Long currentClinicId() {
    Users me = usersService.findByUsername(currentUsername());
    return (me != null && me.getClinic() != null) ? me.getClinic().getClinicId() : null;
  }

  // === helpers for AJAX (không cần sửa UsersService hiện tại) ===
  private boolean usernameTaken(String username) {
    if (username == null || username.trim().isEmpty()) return false;
    try {
      usersService.findByUsername(username.trim());
      return true; // không ném exception => có user
    } catch (IllegalArgumentException ex) {
      return false; // service của bạn ném lỗi khi không tìm thấy
    }
  }

  private boolean emailTaken(String email) {
    if (email == null || email.trim().isEmpty()) return false;
    return usersService.findByEmail(email.trim()) != null;
  }
}
