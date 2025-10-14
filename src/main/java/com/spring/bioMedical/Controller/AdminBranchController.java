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
import java.util.List;

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
      clinics = Collections.singletonList(c); // Java 8 (thay cho List.of)
    } else {
      clinics = clinicService.findAll();
    }

    model.addAttribute("clinics", clinics);
    model.addAttribute("specialties", specialtyService.getAllSpecialties());
    return "admin_branch/addDoctor";
  }

  /* ========== ADD DOCTOR (SUBMIT) ========== */
  @PostMapping("/save-doctor")
  public String saveDoctor(@ModelAttribute("doctorForm") DoctorForm form) {
    // map DoctorForm -> Users
    Users user = new Users();
    user.setUsername(form.getUsername());
    user.setPasswordHash(form.getPasswordHash());
    user.setEmail(form.getEmail());
    user.setPhone(form.getPhone());
    user.setFullName(form.getFullName());
    user.setGender(form.getGender());
    user.setDateOfBirth(form.getDateOfBirth());
    user.setRole("DOCTOR");
    user.setEnabled(true);

    // lấy clinic từ form, nếu trống thì dùng clinic của admin-branch hiện tại
    Long cid = (form.getClinicId() != null) ? form.getClinicId() : currentClinicId();
    if (cid != null) {
      Clinic clinic = clinicService.findById(cid);
      usersService.assignClinic(user, clinic); // chỉ set user.setClinic(clinic)
    }

    usersService.save(user);

    // TODO (nếu cần): tạo bản ghi Doctor, set specialty + bio, link user

    return "redirect:/admin-branch/doctor-details";
  }

  /* ========== APPOINTMENTS ========== */
  @GetMapping("/appointments")
  public String appointments(Model model) {
    Long clinicId = currentClinicId();
    model.addAttribute("app", appointmentService.findAllByBranch(clinicId)); // ✅ chỉ chi nhánh của mình
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
    return "admin_branch/updateMyProfile";
  }

  @PostMapping("/update")
public String updateProfile(@ModelAttribute("profile") Users form) {
    // Lấy bản ghi gốc
    Users me = usersService.findById(form.getUserId());
    if (me == null) {
        // xử lý not found tùy bạn: throw hoặc redirect
        return "redirect:/admin-branch/doctor-details";
    }

    // Chỉ cập nhật các trường cho phép
    me.setFullName(form.getFullName());
    me.setGender(form.getGender());
    me.setDateOfBirth(form.getDateOfBirth());
    // nếu có phone:
    // me.setPhone(form.getPhone());

    // KHÔNG đụng vào username, role, passwordHash, enabled, clinic
    usersService.save(me);
    return "redirect:/admin-branch/doctor-details";
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
}
