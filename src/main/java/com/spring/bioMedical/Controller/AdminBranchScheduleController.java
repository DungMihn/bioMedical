package com.spring.bioMedical.Controller;

import com.spring.bioMedical.entity.AppointmentBooking;
import com.spring.bioMedical.entity.AppointmentSlot;
import com.spring.bioMedical.entity.Clinic;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.repository.ClinicRepository;
import com.spring.bioMedical.repository.UsersRepository;
import com.spring.bioMedical.service.ScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping({"/admin-branch/schedule"})
public class AdminBranchScheduleController {

    private final ScheduleService scheduleService;
    private final ClinicRepository clinicRepo;
    private final UsersRepository usersRepository;

    public AdminBranchScheduleController(ScheduleService scheduleService,
            ClinicRepository clinicRepo,
            UsersRepository usersRepository) {
        this.scheduleService = scheduleService;
        this.clinicRepo = clinicRepo;
        this.usersRepository = usersRepository;
    }

    /**
     * Java 8-compatible: lấy clinicId từ principal (Users hoặc UserDetails hoặc
     * String).
     */
    private Long currentClinicId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }

        Object p = auth.getPrincipal();

        // Trường hợp principal là entity Users
        if (p instanceof Users) {
            Users u = (Users) p;
            return u.getClinicId();
        }

        // Trường hợp principal là UserDetails hoặc String (username)
        String username = null;
        if (p instanceof UserDetails) {
            username = ((UserDetails) p).getUsername();
        } else if (p instanceof String) {
            username = (String) p;
        }

        if (username != null) {
            return usersRepository.findByUsername(username)
                    .map(Users::getClinicId)
                    .orElse(null);
        }
        return null;
    }

    @GetMapping
    public String viewDay(Model model,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        model.addAttribute("activePage", "schedule");
        LocalDate d = (date != null) ? date : LocalDate.now();

        Long clinicId = currentClinicId();
        if (clinicId == null) {
            throw new IllegalStateException("Không xác định được chi nhánh của tài khoản.");
        }

        Clinic clinic = clinicRepo.findById(clinicId).orElse(null);
        List<AppointmentSlot> slots = scheduleService.listSlots(clinicId, d);

        model.addAttribute("date", d);
        model.addAttribute("clinicId", clinicId); // chỉ để hiển thị
        model.addAttribute("clinic", clinic);
        model.addAttribute("slots", slots);
        return "admin_branch/schedule/day";
    }

    // Cập nhật giờ mở/đóng & sinh slot cho ngày đang xem (slotTime cố định 60)
    @PostMapping("/update-hours")
    public String updateHours(@RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime openTime,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime closeTime,
            @RequestParam Integer slotTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            RedirectAttributes ra) {
        try {
            Long clinicId = currentClinicId();
            if (clinicId == null) {
                throw new IllegalStateException("Không xác định được chi nhánh của tài khoản.");
            }

            scheduleService.updateClinicHours(clinicId, openTime, closeTime, slotTime);

            // Sinh slot 60' theo giờ mới, bỏ 12:00–13:00
            LocalTime noonStart = LocalTime.of(12, 0);
            LocalTime noonEnd = LocalTime.of(13, 0);
            int n = scheduleService.generateSlotsForDateWithBreak(clinicId, date, noonStart, noonEnd);

            ra.addFlashAttribute("ok", "Đã cập nhật giờ và tạo " + n + " slot cho ngày " + date + ".");
        } catch (Exception e) {
            ra.addFlashAttribute("err", e.getMessage());
        }
        return "redirect:/admin-branch/schedule?date=" + date;
    }

    // Sinh slot 1 ngày theo giờ hiện tại (có chọn doctorId)
    @PostMapping("/generate")
    public String generate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long doctorId,
            RedirectAttributes ra) {
        Long clinicId = currentClinicId();
        if (clinicId == null) {
            throw new IllegalStateException("Không xác định được chi nhánh của tài khoản.");
        }
        int n = scheduleService.generateSlotsForDate(clinicId, date, doctorId);
        ra.addFlashAttribute("ok", "Đã tạo " + n + " slot giờ.");
        return "redirect:/admin-branch/schedule?date=" + date;
    }

    // Đặt lịch bằng Gmail (slotId + email + ghi chú)
    @PostMapping("/book-by-email")
    public String bookByEmail(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long slotId,
            @RequestParam String email,
            @RequestParam(required = false) String note,
            RedirectAttributes ra) {
        try {
            Long clinicId = currentClinicId();
            if (clinicId == null) {
                throw new IllegalStateException("Không xác định được chi nhánh của tài khoản.");
            }

            String norm = (email == null ? "" : email.trim().toLowerCase());
          
            if (!norm.matches("^[^@\\s]+@(gmail\\.com|example\\.com|exampl\\.com)$")) {
                ra.addFlashAttribute("err", "Vui lòng nhập email hợp lệ (…@gmail.com, …@example.com hoặc …@exampl.com).");
                return "redirect:/admin-branch/schedule?date=" + date;
            }

            // Tìm user theo email
            java.util.Optional<Users> uOpt = usersRepository.findByEmailIgnoreCase(norm);
            if (!uOpt.isPresent()) {
                ra.addFlashAttribute("err", "Không tìm thấy người dùng với email: " + norm);
                return "redirect:/admin-branch/schedule?date=" + date;
            }

            // ĐẶT lịch nhưng có kiểm tra slot thuộc đúng chi nhánh:
            // (yêu cầu bạn đã thêm method bookIntoClinic trong ScheduleService)
            AppointmentBooking booking
                    = scheduleService.bookIntoClinic(uOpt.get().getUserId(), slotId, note, clinicId);

            ra.addFlashAttribute("ok", "Đã đặt lịch cho " + norm + " (mã hẹn: " + booking.getAppointmentId() + ").");
        } catch (Exception e) {
            ra.addFlashAttribute("err", e.getMessage());
        }
        return "redirect:/admin-branch/schedule?date=" + date;
    }
}
