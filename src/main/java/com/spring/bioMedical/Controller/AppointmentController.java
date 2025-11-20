package com.spring.bioMedical.Controller;

import com.spring.bioMedical.dto.BookingRequest;
import com.spring.bioMedical.dto.GuestBookingForm;
import com.spring.bioMedical.entity.*;
import com.spring.bioMedical.repository.*;
import com.spring.bioMedical.service.BookingService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;

@Controller
@RequestMapping("/user")
public class AppointmentController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private AppointmentSlotRepository slotRepository;

    @Autowired
    private UsersRepository usersRepository;

    // === Hiển thị form đặt lịch ===
    @GetMapping("/appointment")
    public String showAppointmentForm(Model model, @AuthenticationPrincipal Users currentUser) {
        model.addAttribute("appointmentForm", new BookingRequest());
        model.addAttribute("clinics", clinicRepository.findAll());
        model.addAttribute("currentUser", currentUser);
        return "user/index";
    }

    // === Lưu lịch hẹn (và gửi OTP xác nhận) ===
    @PostMapping("/save-app")
    public String saveAppointment(@ModelAttribute BookingRequest req,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
            RedirectAttributes redirectAttributes) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            if (username == null || username.equals("anonymousUser")) {
                // ✅ Khách vãng lai
                GuestBookingForm guest = new GuestBookingForm();
                guest.setFullName(fullName);
                guest.setEmail(email);
                guest.setPhone(phone);
                guest.setGender(gender);
                guest.setDateOfBirth(dateOfBirth);
                guest.setNotes(req.getNotes());
                guest.setClinicId(req.getClinicId());
                guest.setSlotId(req.getSlotId());

                bookingService.createGuestBooking(guest);

                // ✅ Chuyển sang trang nhập OTP
                return "redirect:/verify-appointment-otp?email=" + email;
            }

            // ✅ Người dùng đã đăng nhập
            Users currentUser = usersRepository.findByUsername(username);
            bookingService.createBooking(req, currentUser);

            // ✅ Giống logic cũ: vẫn cần OTP xác nhận
            return "redirect:/verify-appointment-otp?email=" + currentUser.getEmail();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/user/appointment";
        }
    }

    @PostMapping("/ajax-save-app")
    @ResponseBody
    public Map<String, Object> ajaxSaveAppointment(
            @ModelAttribute BookingRequest req,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth) {

        Map<String, Object> response = new HashMap<>();
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            if (username == null || username.equals("anonymousUser")) {
                GuestBookingForm guest = new GuestBookingForm();
                guest.setFullName(fullName);
                guest.setEmail(email);
                guest.setPhone(phone);
                guest.setGender(gender);
                guest.setDateOfBirth(dateOfBirth);
                guest.setNotes(req.getNotes());
                guest.setClinicId(req.getClinicId());
                guest.setSlotId(req.getSlotId());

                bookingService.createGuestBooking(guest);
                response.put("success", true);
                response.put("redirectUrl", "/verify-appointment-otp?email=" + email);
                return response;
            }

            Users currentUser = usersRepository.findByUsername(username);
            bookingService.createBooking(req, currentUser);
            response.put("success", true);
            response.put("redirectUrl", "/verify-appointment-otp?email=" + currentUser.getEmail());
            return response;

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }

    // === API: Lấy danh sách slot trống theo chi nhánh ===
    @GetMapping("/slots-by-clinic/{clinicId}")
    @ResponseBody
    public List<AppointmentSlots> getAvailableSlots(
            @PathVariable Long clinicId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (date != null) {
            // ✅ Đơn giản: date đã là LocalDate rồi, dùng trực tiếp
            return slotRepository.findByClinicIdAndDateAndStatus(clinicId, date, "AVAILABLE");
        }

        // ✅ Nếu chưa có ngày (chưa chọn ngày) thì không trả gì cả
        return List.of();
    }

// ✅ Hủy lịch hẹn
    @PostMapping("/cancel/{appointmentId}")
    @ResponseBody
    public Map<String, Object> cancelAppointment(@PathVariable Long appointmentId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Appointments appointment = bookingService.cancelAppointment(appointmentId);
            res.put("success", true);
            res.put("status", appointment.getStatus());
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return res;
    }

}
