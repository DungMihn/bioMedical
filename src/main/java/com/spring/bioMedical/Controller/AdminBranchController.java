package com.spring.bioMedical.Controller;

import com.spring.bioMedical.entity.Clinics;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.dto.DoctorForm;
import com.spring.bioMedical.entity.Appointments;
import com.spring.bioMedical.repository.AppointmentBookingRepository;
import com.spring.bioMedical.service.ClinicService;
import com.spring.bioMedical.service.SpecialtyService;
import com.spring.bioMedical.service.UsersService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import com.spring.bioMedical.form.AppointmentForm;
import com.spring.bioMedical.repository.AppointmentSlotRepository;
import com.spring.bioMedical.service.AppointmentService;
import java.time.LocalDate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import com.spring.bioMedical.service.ScheduleService;

@Controller
@RequestMapping("/admin-branch")
public class AdminBranchController {

    private final UsersService usersService;
    private final AppointmentService appointmentService;
    private final ClinicService clinicService;
    private final SpecialtyService specialtyService;
    private final AppointmentBookingRepository bookingRepo;
    private final AppointmentSlotRepository slotRepo;
    private final ScheduleService scheduleService;

    public AdminBranchController(UsersService usersService,
            AppointmentService appointmentService,
            ClinicService clinicService,
            SpecialtyService specialtyService,
            AppointmentBookingRepository bookingRepo,
            AppointmentSlotRepository slotRepo,
            ScheduleService scheduleService) {
        this.usersService = usersService;
        this.appointmentService = appointmentService;
        this.clinicService = clinicService;
        this.specialtyService = specialtyService;
        this.bookingRepo = bookingRepo;
        this.slotRepo = slotRepo;
        this.scheduleService = scheduleService;
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
        List<Clinics> clinics;
        if (clinicId != null) {
            Clinics c = clinicService.findById(clinicId);
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
            List<Clinics> clinics;
            if (clinicId != null) {
                Clinics c = clinicService.findById(clinicId);
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
            Clinics clinic = clinicService.findById(cid);
            usersService.assignClinic(user, clinic);
        }

        usersService.save(user);
        return "redirect:/admin-branch/doctor-details";
    }

    /* ========== APPOINTMENTS ========== */
    @GetMapping("/appointments")
    public String appointments(Model model,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate date) {

        Long clinicId = currentClinicId();
        LocalDate today = LocalDate.now();

        java.util.List<AppointmentForm> rows = (date == null)
                ? bookingRepo.findUpcomingRowsForClinic(clinicId, today) // ✅ Chỉ lấy lịch chưa diễn ra
                : bookingRepo.findRowsForClinicAndDate(clinicId, date);

        model.addAttribute("app", rows);            // 👈 GIỮ NGUYÊN TÊN attribute theo template
        model.addAttribute("activePage", "appointments");
        model.addAttribute("clinicId", clinicId);
        model.addAttribute("date", (date != null) ? date : LocalDate.now());
        return "admin_branch/appointment";
    }

    @PostMapping("/appointments/save")
    public String saveAppointment(@ModelAttribute("appointment") Appointments app) {
        Long clinicId = currentClinicId();
        Clinics clinic = clinicService.findById(clinicId);

        // ✅ SỬA: Set clinic qua slot thay vì trực tiếp
        if (app.getSlot() != null) {
            app.getSlot().setClinic(clinic);
        } else {
            // Nếu slot chưa có, cần xử lý tạo slot hoặc báo lỗi
            throw new IllegalArgumentException("Appointment must have a slot");
        }

        appointmentService.save(app);
        return "redirect:/admin-branch/appointments";
    }

    @GetMapping("/appointments/cancel/{id}")
    public String cancelAppointment(@PathVariable("id") Long id,
            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {

        // huỷ: trả slot -> AVAILABLE + booking -> CANCELLED
        scheduleService.cancel(id);

        // Lấy ngày của slot để giữ filter theo ngày (nếu lấy được)
        java.time.LocalDate slotDate = bookingRepo.findById(id)
                .flatMap(b -> slotRepo.findById(b.getSlotId()))
                .map(com.spring.bioMedical.entity.AppointmentSlots::getSlotDate)
                .orElse(null);

        ra.addFlashAttribute("ok", "Đã huỷ lịch #" + id);

        return (slotDate != null)
                ? "redirect:/admin-branch/appointments?date=" + slotDate // 👈 về Appointments kèm ngày
                : "redirect:/admin-branch/appointments";                    // 👈 về Appointments
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
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        try {
            usersService.findByUsername(username.trim());
            return true; // không ném exception => có user
        } catch (IllegalArgumentException ex) {
            return false; // service của bạn ném lỗi khi không tìm thấy
        }
    }

    private boolean emailTaken(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return usersService.findByEmail(email.trim()) != null;
    }

    /* ========== ADD USER (BRANCH) ========== */
    @GetMapping("/add-user")
    public String showAddUser(Model model) {
        com.spring.bioMedical.dto.UserForm form = new com.spring.bioMedical.dto.UserForm();
        form.setRole("PATIENT");              // 👈 mặc định
        form.setEnabled(Boolean.TRUE);        // 👈 mặc định
        // form.setClinicId(null);            // nếu DTO có clinicId, để null
        model.addAttribute("form", form);
        model.addAttribute("activePage", "add-user");
        // KHÔNG cần allowedRoles/clinicId nữa
        return "admin_branch/addUser";
    }

    @PostMapping("/save-user")
    public String saveUser(
            @Valid @ModelAttribute("form") com.spring.bioMedical.dto.UserForm form,
            org.springframework.validation.BindingResult binding,
            Model model,
            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {

        // ——— validate trùng + whitelist email như đang có ———
        if (binding.hasErrors()) {
            binding.getAllErrors().forEach(e -> System.out.println("[ADD-USER-ERROR] " + e));
            model.addAttribute("activePage", "add-user");
            return "admin_branch/addUser";
        }

        com.spring.bioMedical.entity.Users u = new com.spring.bioMedical.entity.Users();
        u.setUsername(form.getUsername().trim());
        u.setPasswordHash(form.getPassword());   // hoặc encode
        u.setEmail(form.getEmail().trim().toLowerCase());
        u.setPhone(form.getPhone());
        u.setFullName(form.getFullName());
        u.setGender(form.getGender());
        u.setDateOfBirth(form.getDateOfBirth());

        // ✅ ÉP role + enabled, bỏ clinic
        u.setRole("PATIENT");        // luôn là PATIENT
        u.setEnabled(true);          // luôn kích hoạt
        // KHÔNG assign clinic cho patient:
        // Long cid = currentClinicId();  ❌ bỏ
        // usersService.assignClinic(u, clinicService.findById(cid)); ❌ bỏ

        usersService.save(u);
        ra.addFlashAttribute("ok", "Đã tạo tài khoản bệnh nhân: " + u.getUsername());
        return "redirect:/admin-branch/appointments";  // hoặc trang bạn muốn
    }

}
