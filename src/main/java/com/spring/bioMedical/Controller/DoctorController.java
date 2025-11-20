package com.spring.bioMedical.Controller;

import com.spring.bioMedical.DTO.AppointmentHistoryDTO;
import com.spring.bioMedical.DTO.ConfirmedAppointmentDTO;
import com.spring.bioMedical.entity.*;
import com.spring.bioMedical.repository.MedicalRecordsRepository;
import com.spring.bioMedical.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private UserDoctorService userDoctorService;

    @Autowired
    private MedicalRecordsService medicalRecordsService;

    @Autowired
    private ITextReportService iTextReportService;

    @Autowired
    private MedicalRecordsRepository medicalRecordsRepository;

    @GetMapping("/index")
    public String doctorIndex(Model model) {
        try {
            String username = getCurrentUsername();
            Users currentUser = usersService.findByUsername(username);
            System.out.println("[DEBUG] =========== DOCTOR DASHBOARD DEBUG ===========");
            System.out.println("[DEBUG] Current username: " + username);
            System.out.println("[DEBUG] Current user ID: " + (currentUser != null ? currentUser.getUserId() : "null"));

            if (currentUser != null) {
                Optional<Doctors> doctorOpt = doctorService.getDoctorByUserId(currentUser.getUserId());
                System.out.println("[DEBUG] Doctor found: " + doctorOpt.isPresent());

                if (doctorOpt.isPresent()) {
                    Doctors doctor = doctorOpt.get();
                    Long doctorId = doctor.getDoctorId();
                    System.out.println("[DEBUG] Doctor ID: " + doctorId);

                    // CHỈ DEBUG - không ảnh hưởng logic chính
                    try {
                        List<AppointmentSlots> slots = appointmentService.getSlotsByDoctorId(doctorId);
                        
                        
                        System.out.println("[DEBUG] Total slots for doctor: " + slots.size());
                    } catch (Exception e) {
                        System.out.println("[DEBUG] Could not get slots: " + e.getMessage());
                    }

                    // Lấy appointments - QUAN TRỌNG
                    List<Appointments> allAppointments = appointmentService.getAppointmentsByDoctorId(doctorId);
                    System.out.println("[DEBUG] Total appointments found: " + allAppointments.size());

                    // DEBUG chi tiết từng appointment
                    for (Appointments appt : allAppointments) {
                        System.out.println("[DEBUG] Appointment " + appt.getAppointmentId()
                                + " - Status: " + appt.getStatus()
                                + " - Slot ID: " + appt.getSlotId()
                                + " - User ID: " + appt.getUserId());
                    }

                    // GIỮ NGUYÊN LOGIC CỦA BẠN - không thay đổi
                    List<Appointments> todayAppointments = getTodayAppointmentsWithExpiredCheck(allAppointments);
                    List<Appointments> pendingAppointments = getPendingAppointmentsWithExpiredCheck(allAppointments);
                    List<Appointments> confirmedAppointments = getConfirmedAppointments(allAppointments);
                    List<Appointments> completedAppointments = getCompletedAppointments(allAppointments);
                    List<Appointments> expiredAppointments = getExpiredAppointments(allAppointments);

                    // Lấy appointments sắp tới cho thông báo
                    List<Appointments> upcomingNotifications = appointmentService.getUpcomingAppointmentsForNotifications(doctorId);
                    List<Appointments> recentAutoActions = getRecentAutoActions(doctorId);

                    model.addAttribute("doctor", doctor);
                    model.addAttribute("specialtyName", getDoctorSpecialtyName(doctor));
                    model.addAttribute("todayAppointments", todayAppointments);
                    model.addAttribute("pendingAppointments", pendingAppointments);
                    model.addAttribute("confirmedAppointments", confirmedAppointments);
                    model.addAttribute("completedAppointments", completedAppointments);
                    model.addAttribute("expiredAppointments", expiredAppointments);
                    model.addAttribute("upcomingNotifications", upcomingNotifications);
                    model.addAttribute("recentAutoActions", recentAutoActions);

                    // Counts với logic chính xác
                    model.addAttribute("todayAppointmentsCount", todayAppointments.size());
                    model.addAttribute("pendingAppointmentsCount", pendingAppointments.size());
                    model.addAttribute("confirmedAppointmentsCount", confirmedAppointments.size());
                    model.addAttribute("completedAppointmentsCount", completedAppointments.size());
                    model.addAttribute("expiredAppointmentsCount", expiredAppointments.size());

                    model.addAttribute("hasPendingAppointments", !pendingAppointments.isEmpty());
                    model.addAttribute("hasExpiredAppointments", !expiredAppointments.isEmpty());

                    // Stats với logic đồng bộ
                    Map<String, Object> stats = getAccurateAppointmentStats(allAppointments);
                    model.addAttribute("stats", stats);
                }
            }

            model.addAttribute("name", currentUser != null ? currentUser.getFullName() : "Doctor");
            model.addAttribute("email", currentUser != null ? currentUser.getEmail() : "");
            model.addAttribute("user", currentUser);

            System.out.println("[DEBUG] =========== END DEBUG ===========");
            return "doctor/index";

        } catch (Exception e) {
            System.err.println("Error in doctorIndex: " + e.getMessage());
            e.printStackTrace();
            return handleError(model, "Dashboard");
        }
    }

// ==================== HELPER METHODS VỚI LOGIC ĐỒNG BỘ ====================
    private List<Appointments> getTodayAppointmentsWithExpiredCheck(List<Appointments> allAppointments) {
        LocalDate today = LocalDate.now();

        return allAppointments.stream()
                .filter(apt -> {
                    try {
                        if (apt.getAppointmentDate() == null) {
                            return false;
                        }

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), formatter);

                        // Chỉ lấy appointments của hôm nay
                        boolean isToday = aptDate.equals(today);

                        // Chỉ lấy PENDING và CONFIRMED chưa expired
                        boolean isValidStatus = "PENDING".equals(apt.getStatus()) || "CONFIRMED".equals(apt.getStatus());
                        boolean isNotExpired = !appointmentService.isAppointmentExpired(apt);

                        return isToday && isValidStatus && isNotExpired;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    private List<Appointments> getPendingAppointmentsWithExpiredCheck(List<Appointments> allAppointments) {
        return allAppointments.stream()
                .filter(apt -> "PENDING".equals(apt.getStatus()))
                .filter(apt -> !appointmentService.isAppointmentExpired(apt)) // Loại bỏ expired
                .collect(Collectors.toList());
    }

    private List<Appointments> getConfirmedAppointments(List<Appointments> allAppointments) {
        return allAppointments.stream()
                .filter(apt -> "CONFIRMED".equals(apt.getStatus()))
                .collect(Collectors.toList());
    }

    private List<Appointments> getCompletedAppointments(List<Appointments> allAppointments) {
        return allAppointments.stream()
                .filter(apt -> "COMPLETED".equals(apt.getStatus()))
                .collect(Collectors.toList());
    }

    private List<Appointments> getExpiredAppointments(List<Appointments> allAppointments) {
        return allAppointments.stream()
                .filter(apt -> {
                    // Lấy cả PENDING và CONFIRMED đã expired
                    boolean isPendingOrConfirmed = "PENDING".equals(apt.getStatus()) || "CONFIRMED".equals(apt.getStatus());
                    return isPendingOrConfirmed && appointmentService.isAppointmentExpired(apt);
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> getAccurateAppointmentStats(List<Appointments> allAppointments) {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();

        try {
            // Today's appointments (chỉ PENDING và CONFIRMED chưa expired)
            long todayCount = allAppointments.stream()
                    .filter(apt -> {
                        try {
                            if (apt.getAppointmentDate() == null) {
                                return false;
                            }
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), formatter);

                            boolean isToday = aptDate.equals(today);
                            boolean isValidStatus = "PENDING".equals(apt.getStatus()) || "CONFIRMED".equals(apt.getStatus());
                            boolean isNotExpired = !appointmentService.isAppointmentExpired(apt);

                            return isToday && isValidStatus && isNotExpired;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .count();

            // Upcoming appointments (next 7 days, chỉ CONFIRMED)
            long upcomingCount = allAppointments.stream()
                    .filter(apt -> "CONFIRMED".equals(apt.getStatus()))
                    .filter(apt -> {
                        try {
                            if (apt.getAppointmentDate() == null) {
                                return false;
                            }
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), formatter);
                            return !aptDate.isBefore(today) && aptDate.isBefore(today.plusDays(7));
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .count();

            // Recent auto actions (last 24 hours)
            long recentAutoActions = allAppointments.stream()
                    .filter(apt -> "AUTO_CANCELLED".equals(apt.getStatus()) || "NO_SHOW".equals(apt.getStatus()))
                    .filter(apt -> {
                        if (apt.getCreatedAt() == null) {
                            return false;
                        }
                        Instant aptTime = apt.getCreatedAt().toInstant();
                        Instant yesterday = Instant.now().minus(24, ChronoUnit.HOURS);
                        return aptTime.isAfter(yesterday);
                    })
                    .count();

            // Expired appointments count
            long expiredCount = allAppointments.stream()
                    .filter(apt -> {
                        boolean isPendingOrConfirmed = "PENDING".equals(apt.getStatus()) || "CONFIRMED".equals(apt.getStatus());
                        return isPendingOrConfirmed && appointmentService.isAppointmentExpired(apt);
                    })
                    .count();

            stats.put("todayCount", todayCount);
            stats.put("upcomingCount", upcomingCount);
            stats.put("recentAutoActions", recentAutoActions);
            stats.put("expiredCount", expiredCount);
            stats.put("totalAppointments", allAppointments.size());

        } catch (Exception e) {
            stats.put("todayCount", 0L);
            stats.put("upcomingCount", 0L);
            stats.put("recentAutoActions", 0L);
            stats.put("expiredCount", 0L);
            stats.put("totalAppointments", 0L);
        }

        return stats;
    }

    // ==================== PROCESSING METHODS - SỬA LẠI LOGIC THỜI GIAN ====================
    private List<ConfirmedAppointmentDTO> processConfirmedAppointments(List<Appointments> appointments, String period) {
        if (appointments == null || appointments.isEmpty()) {
            return new ArrayList<>();
        }

        LocalDateTime now = LocalDateTime.now();

        return appointments.stream()
                .filter(apt -> {
                    try {
                        if (apt.getAppointmentDate() == null || apt.getAppointmentTime() == null) {
                            return false;
                        }

                        // Tạo LocalDateTime từ appointment
                        LocalDateTime aptDateTime = createAppointmentDateTime(apt);
                        if (aptDateTime == null) {
                            return false;
                        }

                        switch (period) {
                            case "today":
                                // Lịch hẹn trong ngày hôm nay (bất kể giờ nào)
                                return aptDateTime.toLocalDate().equals(now.toLocalDate());
                            case "tomorrow":
                                // Lịch hẹn trong ngày mai
                                return aptDateTime.toLocalDate().equals(now.toLocalDate().plusDays(1));
                            case "week":
                                // Lịch hẹn trong 7 ngày tới (từ hôm nay)
                                LocalDate weekEnd = now.toLocalDate().plusDays(7);
                                return !aptDateTime.toLocalDate().isBefore(now.toLocalDate())
                                        && aptDateTime.toLocalDate().isBefore(weekEnd);
                            case "upcoming":
                                // Lịch hẹn chưa diễn ra (cả thời gian trong tương lai)
                                return aptDateTime.isAfter(now);
                            case "past":
                                // Lịch hẹn đã qua (đã quá thời gian hẹn)
                                return aptDateTime.isBefore(now);
                            case "active":
                                // Lịch hẹn đang hoạt động (hôm nay và chưa quá 2 giờ so với thời gian hẹn)
                                return isActiveAppointment(apt, now);
                            default: // "all"
                                return true;
                        }
                    } catch (Exception e) {
                        return false;
                    }
                })
                .sorted((a1, a2) -> {
                    try {
                        LocalDateTime dt1 = createAppointmentDateTime(a1);
                        LocalDateTime dt2 = createAppointmentDateTime(a2);

                        if (dt1 == null || dt2 == null) {
                            return 0;
                        }

                        return dt1.compareTo(dt2); // Sắp xếp tăng dần theo thời gian
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .map(apt -> {
                    // Tạo DTO với các thuộc tính đã xử lý
                    LocalDateTime aptDateTime = createAppointmentDateTime(apt);
                    String timeIndicatorClass = calculateTimeIndicatorClass(apt, aptDateTime);
                    String timeIndicatorText = calculateTimeIndicatorText(apt, aptDateTime);
                    boolean todayOrPast = isAppointmentTodayOrPast(apt, aptDateTime);
                    boolean past = isAppointmentPast(apt, aptDateTime);
                    boolean canCreateMedicalRecord = canCreateMedicalRecord(apt, aptDateTime);

                    return new ConfirmedAppointmentDTO(apt, timeIndicatorClass, timeIndicatorText,
                            todayOrPast, past, canCreateMedicalRecord);
                })
                .collect(Collectors.toList());
    }

// Helper để tạo LocalDateTime từ appointment
    private LocalDateTime createAppointmentDateTime(Appointments appointment) {
        try {
            if (appointment.getAppointmentDate() == null || appointment.getAppointmentTime() == null) {
                return null;
            }

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            LocalDate date = LocalDate.parse(appointment.getAppointmentDate(), dateFormatter);
            LocalTime time = LocalTime.parse(appointment.getAppointmentTime(), timeFormatter);

            return LocalDateTime.of(date, time);
        } catch (Exception e) {
            return null;
        }
    }

// Helper để xác định class CSS cho time indicator - SỬA LẠI LOGIC
    private String calculateTimeIndicatorClass(Appointments appointment, LocalDateTime aptDateTime) {
        if (aptDateTime == null) {
            return "time-past";
        }

        LocalDateTime now = LocalDateTime.now();

        if (aptDateTime.isBefore(now)) {
            return "time-past";
        } else if (aptDateTime.toLocalDate().equals(now.toLocalDate())) {
            // Hôm nay nhưng chưa đến giờ
            return "time-today";
        } else {
            return "time-upcoming";
        }
    }

// Helper để xác định text cho time indicator - SỬA LẠI LOGIC
    private String calculateTimeIndicatorText(Appointments appointment, LocalDateTime aptDateTime) {
        if (aptDateTime == null) {
            return "ĐÃ QUA";
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        if (aptDateTime.isBefore(now)) {
            return "ĐÃ QUA";
        } else if (aptDateTime.toLocalDate().equals(today)) {
            long minutesUntil = ChronoUnit.MINUTES.between(now, aptDateTime);
            if (minutesUntil <= 60) {
                // Còn dưới 1 tiếng
                return minutesUntil + " PHÚT";
            } else {
                // Còn trên 1 tiếng
                long hoursUntil = ChronoUnit.HOURS.between(now, aptDateTime);
                return hoursUntil + " GIỜ";
            }
        } else if (aptDateTime.toLocalDate().equals(today.plusDays(1))) {
            return "NGÀY MAI";
        } else {
            long daysBetween = ChronoUnit.DAYS.between(today, aptDateTime.toLocalDate());
            return daysBetween + " NGÀY TỚI";
        }
    }

// Helper để kiểm tra appointment có phải hôm nay hoặc đã qua không
    private boolean isAppointmentTodayOrPast(Appointments appointment, LocalDateTime aptDateTime) {
        if (aptDateTime == null) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        return !aptDateTime.isAfter(now); // Đã qua hoặc đang diễn ra
    }

// Helper để kiểm tra appointment đã qua
    private boolean isAppointmentPast(Appointments appointment, LocalDateTime aptDateTime) {
        if (aptDateTime == null) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        return aptDateTime.isBefore(now);
    }

// Helper để kiểm tra có thể tạo hồ sơ bệnh án không
    private boolean canCreateMedicalRecord(Appointments appointment, LocalDateTime aptDateTime) {
        if (aptDateTime == null) {
            return true; // Nếu không có thời gian, vẫn cho phép tạo
        }

        LocalDateTime now = LocalDateTime.now();

        // Cho phép tạo hồ sơ nếu:
        // 1. Đã qua thời gian hẹn
        // 2. Hoặc còn trong vòng 30 phút trước khi hẹn (cho trường hợp bệnh nhân đến sớm)
        return aptDateTime.isBefore(now)
                || ChronoUnit.MINUTES.between(now, aptDateTime) <= 30;
    }

// Helper để kiểm tra appointment đang active (cho filter active)
    private boolean isActiveAppointment(Appointments appointment, LocalDateTime now) {
        LocalDateTime aptDateTime = createAppointmentDateTime(appointment);
        if (aptDateTime == null) {
            return false;
        }

        // Appointment active nếu:
        // - Là hôm nay VÀ chưa quá 2 giờ so với thời gian hẹn
        return aptDateTime.toLocalDate().equals(now.toLocalDate())
                && ChronoUnit.HOURS.between(aptDateTime, now) <= 2;
    }

    // Trong DoctorController, thêm method mới để xử lý expired appointments
    @PostMapping("/appointments/{id}/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, String>> confirmAppointment(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        try {
            Appointments appointment = appointmentService.getAppointmentById(id);

            // Kiểm tra xem appointment có expired không
            if (appointmentService.isAppointmentExpired(appointment)) {
                response.put("status", "ERROR");
                response.put("message", "Không thể xác nhận lịch hẹn đã quá hạn");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            boolean success = appointmentService.confirmAppointment(id);
            if (success) {
                response.put("status", "SUCCESS");
                response.put("message", "Đã xác nhận lịch hẹn thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "ERROR");
                response.put("message", "Không thể xác nhận lịch hẹn");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/appointments/{id}/complete")
    @ResponseBody
    public ResponseEntity<Map<String, String>> completeAppointment(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean success = appointmentService.completeAppointment(id);
            if (success) {
                response.put("status", "SUCCESS");
                response.put("message", "Đã hoàn thành lịch hẹn và có thể tạo hồ sơ bệnh án");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "ERROR");
                response.put("message", "Chỉ có thể hoàn thành lịch hẹn đã được xác nhận");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

// Thêm endpoint để xử lý expired appointments
    @GetMapping("/appointments/expired")
    public String expiredAppointments(Model model) {
        try {
            String username = getCurrentUsername();
            Users user = usersService.findByUsername(username);

            if (user != null) {
                Optional<Doctors> doctorOpt = doctorService.getDoctorByUserId(user.getUserId());
                if (doctorOpt.isPresent()) {
                    Doctors doctor = doctorOpt.get();
                    Long doctorId = doctor.getDoctorId();

                    List<Appointments> allAppointments = appointmentService.getAppointmentsByDoctorId(doctorId);
                    List<Appointments> expiredAppointments = allAppointments.stream()
                            .filter(apt -> "EXPIRED".equals(apt.getStatus()))
                            .collect(Collectors.toList());

                    model.addAttribute("appointments", expiredAppointments);
                    model.addAttribute("doctor", doctor);
                    model.addAttribute("totalCount", expiredAppointments.size());
                }
            }

            return "doctor/expired-appointments";

        } catch (Exception e) {
            return handleError(model, "Expired Appointments");
        }
    }

    @GetMapping("/appointments/confirmed")
    public String confirmedAppointments(
            @RequestParam(defaultValue = "upcoming") String period,
            Model model) {

        try {
            String username = getCurrentUsername();
            Users user = usersService.findByUsername(username);

            if (user != null) {
                Optional<Doctors> doctorOpt = doctorService.getDoctorByUserId(user.getUserId());
                if (doctorOpt.isPresent()) {
                    Doctors doctor = doctorOpt.get();
                    Long doctorId = doctor.getDoctorId();

                    // Lấy TẤT CẢ confirmed appointments
                    List<Appointments> allConfirmedAppointments = appointmentService.getConfirmedAppointmentsByDoctorId(doctorId);

                    // Lọc theo period với logic chính xác
                    List<ConfirmedAppointmentDTO> filteredAppointments = processConfirmedAppointments(allConfirmedAppointments, period);

                    // Lấy thống kê
                    Map<String, Long> periodCounts = calculateConfirmedPeriodCounts(allConfirmedAppointments);

                    model.addAttribute("appointments", filteredAppointments);
                    model.addAttribute("doctor", doctor);
                    model.addAttribute("period", period);
                    model.addAttribute("periodCounts", periodCounts);
                    model.addAttribute("totalCount", allConfirmedAppointments.size());
                    model.addAttribute("filteredCount", filteredAppointments.size());
                    model.addAttribute("upcomingAppointmentsCount", periodCounts.get("upcoming"));

                    // Thêm empty state text
                    model.addAttribute("emptyStateText", getEmptyStateText(period));
                }
            }

            return "doctor/confirmed-appointments";

        } catch (Exception e) {
            System.err.println("Error in confirmedAppointments: " + e.getMessage());
            e.printStackTrace();
            return handleError(model, "Confirmed Appointments");
        }
    }

// Helper để lấy text cho empty state
    private String getEmptyStateText(String period) {
        switch (period) {
            case "today":
                return "cho hôm nay";
            case "tomorrow":
                return "cho ngày mai";
            case "week":
                return "trong tuần này";
            case "past":
                return "đã qua";
            case "all":
                return "đã xác nhận";
            default: // upcoming
                return "sắp diễn ra";
        }
    }

    // Thêm các helper methods này trong DoctorController
// Helper để xác định class CSS cho time indicator
    public String getTimeIndicatorClass(Appointments appointment) {
        try {
            if (appointment.getAppointmentDate() == null) {
                return "time-past";
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate aptDate = LocalDate.parse(appointment.getAppointmentDate(), formatter);
            LocalDate today = LocalDate.now();

            if (aptDate.isBefore(today)) {
                return "time-past";
            } else if (aptDate.equals(today)) {
                return "time-today";
            } else {
                return "time-upcoming";
            }
        } catch (Exception e) {
            return "time-past";
        }
    }

// Helper để xác định text cho time indicator
    public String getTimeIndicatorText(Appointments appointment) {
        try {
            if (appointment.getAppointmentDate() == null) {
                return "ĐÃ QUA";
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate aptDate = LocalDate.parse(appointment.getAppointmentDate(), formatter);
            LocalDate today = LocalDate.now();

            if (aptDate.isBefore(today)) {
                return "ĐÃ QUA";
            } else if (aptDate.equals(today)) {
                return "HÔM NAY";
            } else if (aptDate.equals(today.plusDays(1))) {
                return "NGÀY MAI";
            } else {
                long daysBetween = ChronoUnit.DAYS.between(today, aptDate);
                return daysBetween + " NGÀY TỚI";
            }
        } catch (Exception e) {
            return "ĐÃ QUA";
        }
    }

    private Map<String, Long> calculateConfirmedPeriodCounts(List<Appointments> appointments) {
        Map<String, Long> counts = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        if (appointments == null || appointments.isEmpty()) {
            counts.put("today", 0L);
            counts.put("tomorrow", 0L);
            counts.put("week", 0L);
            counts.put("upcoming", 0L);
            counts.put("past", 0L);
            counts.put("active", 0L);
            counts.put("all", 0L);
            return counts;
        }

        counts.put("today", appointments.stream()
                .filter(apt -> {
                    LocalDateTime aptDateTime = createAppointmentDateTime(apt);
                    return aptDateTime != null && aptDateTime.toLocalDate().equals(now.toLocalDate());
                })
                .count());

        counts.put("tomorrow", appointments.stream()
                .filter(apt -> {
                    LocalDateTime aptDateTime = createAppointmentDateTime(apt);
                    return aptDateTime != null && aptDateTime.toLocalDate().equals(now.toLocalDate().plusDays(1));
                })
                .count());

        counts.put("week", appointments.stream()
                .filter(apt -> {
                    LocalDateTime aptDateTime = createAppointmentDateTime(apt);
                    if (aptDateTime == null) {
                        return false;
                    }
                    LocalDate weekEnd = now.toLocalDate().plusDays(7);
                    return !aptDateTime.toLocalDate().isBefore(now.toLocalDate())
                            && aptDateTime.toLocalDate().isBefore(weekEnd);
                })
                .count());

        counts.put("upcoming", appointments.stream()
                .filter(apt -> {
                    LocalDateTime aptDateTime = createAppointmentDateTime(apt);
                    return aptDateTime != null && aptDateTime.isAfter(now);
                })
                .count());

        counts.put("past", appointments.stream()
                .filter(apt -> {
                    LocalDateTime aptDateTime = createAppointmentDateTime(apt);
                    return aptDateTime != null && aptDateTime.isBefore(now);
                })
                .count());

        counts.put("active", appointments.stream()
                .filter(apt -> isActiveAppointment(apt, now))
                .count());

        counts.put("all", (long) appointments.size());

        return counts;
    }

    // ==================== APPOINTMENT HISTORY ====================
    @GetMapping("/appointments/history")
    public String appointmentHistory(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "") String search,
            Model model) {

        try {
            String username = getCurrentUsername();
            Users user = usersService.findByUsername(username);

            if (user != null) {
                Optional<Doctors> doctorOpt = doctorService.getDoctorByUserId(user.getUserId());
                if (doctorOpt.isPresent()) {
                    Doctors doctor = doctorOpt.get();
                    Long doctorId = doctor.getDoctorId();

                    // Lấy tất cả appointments
                    List<Appointments> allAppointments = appointmentService.getAppointmentsByDoctorId(doctorId);

                    // Lọc theo điều kiện
                    List<AppointmentHistoryDTO> historyAppointments = filterHistoryAppointments(allAppointments, days, status, search);

                    // Thống kê
                    Map<String, Object> stats = calculateHistoryStats(allAppointments, days);

                    model.addAttribute("appointments", historyAppointments);
                    model.addAttribute("doctor", doctor);
                    model.addAttribute("days", days);
                    model.addAttribute("status", status);
                    model.addAttribute("search", search);
                    model.addAttribute("stats", stats);
                    model.addAttribute("totalCount", historyAppointments.size());

                    // Các options cho filter
                    model.addAttribute("dayOptions", Arrays.asList(7, 30, 90, 180, 365));
                    model.addAttribute("statusOptions", getStatusOptions());
                }
            }

            return "doctor/appointment-history";

        } catch (Exception e) {
            System.err.println("Error in appointmentHistory: " + e.getMessage());
            e.printStackTrace();
            return handleError(model, "Appointment History");
        }
    }

    private List<AppointmentHistoryDTO> filterHistoryAppointments(List<Appointments> appointments,
            int days, String status, String search) {
        if (appointments == null || appointments.isEmpty()) {
            return new ArrayList<>();
        }

        LocalDate startDate = LocalDate.now().minusDays(days);

        return appointments.stream()
                .filter(apt -> {
                    try {
                        if (apt.getAppointmentDate() == null) {
                            return false;
                        }

                        // Filter theo ngày
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), formatter);
                        boolean withinDateRange = !aptDate.isBefore(startDate);

                        // Filter theo status
                        boolean statusMatch = "all".equals(status) || status.equals(apt.getStatus());

                        // Filter theo search
                        boolean searchMatch = true;
                        if (search != null && !search.trim().isEmpty()) {
                            String searchLower = search.toLowerCase().trim();
                            String patientName = apt.getPatientName() != null ? apt.getPatientName().toLowerCase() : "";
                            String patientEmail = apt.getPatientEmail() != null ? apt.getPatientEmail().toLowerCase() : "";
                            searchMatch = patientName.contains(searchLower) || patientEmail.contains(searchLower);
                        }

                        return withinDateRange && statusMatch && searchMatch;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .sorted((a1, a2) -> {
                    try {
                        // Sắp xếp giảm dần theo thời gian (mới nhất lên đầu)
                        if (a1.getCreatedAt() == null || a2.getCreatedAt() == null) {
                            return 0;
                        }
                        return a2.getCreatedAt().compareTo(a1.getCreatedAt());
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .map(apt -> {
                    // Tạo DTO với các thuộc tính đã xử lý
                    String timeAgo = calculateTimeAgo(apt.getCreatedAt());
                    boolean canViewMedicalRecord = canViewMedicalRecord(apt);
                    boolean canCreateMedicalRecord = canCreateMedicalRecord(apt);
                    String statusBadgeClass = getStatusBadgeClass(apt.getStatus());

                    return new AppointmentHistoryDTO(apt, timeAgo, canViewMedicalRecord,
                            canCreateMedicalRecord, statusBadgeClass);
                })
                .collect(Collectors.toList());
    }

// Helper để tính thời gian đã qua
    private String calculateTimeAgo(Date date) {
        if (date == null) {
            return "Không xác định";
        }

        Instant instant = date.toInstant();
        Instant now = Instant.now();

        long seconds = Duration.between(instant, now).getSeconds();

        if (seconds < 60) {
            return "Vừa xong";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + " phút trước";
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + " giờ trước";
        } else {
            long days = seconds / 86400;
            if (days < 30) {
                return days + " ngày trước";
            } else if (days < 365) {
                long months = days / 30;
                return months + " tháng trước";
            } else {
                long years = days / 365;
                return years + " năm trước";
            }
        }
    }

// Helper để kiểm tra có thể xem hồ sơ bệnh án không
    private boolean canViewMedicalRecord(Appointments appointment) {
        // Có thể xem nếu appointment đã hoàn thành
        return "COMPLETED".equals(appointment.getStatus());
    }

// Helper để lấy class CSS cho status badge
    private String getStatusBadgeClass(String status) {
        switch (status) {
            case "COMPLETED":
                return "status-COMPLETED";
            case "CONFIRMED":
                return "status-CONFIRMED";
            case "CANCELLED":
                return "status-CANCELLED";
            case "NO_SHOW":
                return "status-NO_SHOW";
            case "AUTO_CANCELLED":
                return "status-AUTO_CANCELLED";
            case "EXPIRED":
                return "status-EXPIRED";
            default:
                return "status-PENDING";
        }
    }

// Helper để tính thống kê
    private Map<String, Object> calculateHistoryStats(List<Appointments> appointments, int days) {
        Map<String, Object> stats = new HashMap<>();
        LocalDate startDate = LocalDate.now().minusDays(days);

        try {
            long total = appointments.stream()
                    .filter(apt -> {
                        try {
                            if (apt.getAppointmentDate() == null) {
                                return false;
                            }
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), formatter);
                            return !aptDate.isBefore(startDate);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .count();

            long completed = appointments.stream()
                    .filter(apt -> "COMPLETED".equals(apt.getStatus()))
                    .filter(apt -> {
                        try {
                            if (apt.getAppointmentDate() == null) {
                                return false;
                            }
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), formatter);
                            return !aptDate.isBefore(startDate);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .count();

            long cancelled = appointments.stream()
                    .filter(apt -> Arrays.asList("CANCELLED", "AUTO_CANCELLED").contains(apt.getStatus()))
                    .filter(apt -> {
                        try {
                            if (apt.getAppointmentDate() == null) {
                                return false;
                            }
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), formatter);
                            return !aptDate.isBefore(startDate);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .count();

            stats.put("total", total);
            stats.put("completed", completed);
            stats.put("cancelled", cancelled);
            stats.put("completionRate", total > 0 ? Math.round((completed * 100.0) / total) : 0);

        } catch (Exception e) {
            stats.put("total", 0L);
            stats.put("completed", 0L);
            stats.put("cancelled", 0L);
            stats.put("completionRate", 0L);
        }

        return stats;
    }

// Helper để lấy danh sách status options
    private List<String> getStatusOptions() {
        return Arrays.asList("all", "COMPLETED", "CONFIRMED", "CANCELLED", "NO_SHOW", "AUTO_CANCELLED", "EXPIRED", "PENDING");
    }

    // ==================== AUTO ACTIONS LOG ====================
    @GetMapping("/appointments/auto-actions")
    public String autoActionsLog(
            @RequestParam(defaultValue = "7") int days,
            Model model) {

        try {
            String username = getCurrentUsername();
            Users user = usersService.findByUsername(username);

            if (user != null) {
                Optional<Doctors> doctorOpt = doctorService.getDoctorByUserId(user.getUserId());
                if (doctorOpt.isPresent()) {
                    Doctors doctor = doctorOpt.get();
                    Long doctorId = doctor.getDoctorId();

                    List<Appointments> autoActions = getAutoActions(doctorId, days);

                    model.addAttribute("appointments", autoActions);
                    model.addAttribute("doctor", doctor);
                    model.addAttribute("days", days);
                    model.addAttribute("totalCount", autoActions.size());
                }
            }

            return "doctor/auto-actions-log";

        } catch (Exception e) {
            System.err.println("Error in autoActionsLog: " + e.getMessage());
            return handleError(model, "Auto Actions Log");
        }
    }

    // ==================== HELPER METHODS ====================
    private List<Appointments> getRecentAutoActions(Long doctorId) {
        try {
            List<Appointments> allAppointments = appointmentService.getAppointmentsByDoctorId(doctorId);
            return allAppointments.stream()
                    .filter(apt -> "AUTO_CANCELLED".equals(apt.getStatus()) || "NO_SHOW".equals(apt.getStatus()))
                    .filter(apt -> {
                        if (apt.getCreatedAt() == null) {
                            return false;
                        }
                        Instant aptTime = apt.getCreatedAt().toInstant();
                        Instant yesterday = Instant.now().minus(24, ChronoUnit.HOURS);
                        return aptTime.isAfter(yesterday);
                    })
                    .sorted((a1, a2) -> {
                        if (a1.getCreatedAt() == null || a2.getCreatedAt() == null) {
                            return 0;
                        }
                        return a2.getCreatedAt().compareTo(a1.getCreatedAt());
                    })
                    .limit(10)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<Appointments> filterConfirmedAppointmentsByPeriod(List<Appointments> appointments, String period) {
        LocalDate today = LocalDate.now();

        return appointments.stream()
                .filter(apt -> {
                    try {
                        if (apt.getAppointmentDate() == null) {
                            return false;
                        }
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), formatter);

                        switch (period) {
                            case "today":
                                return aptDate.equals(today);
                            case "tomorrow":
                                return aptDate.equals(today.plusDays(1));
                            case "week":
                                return !aptDate.isBefore(today) && aptDate.isBefore(today.plusDays(7));
                            default: // "all"
                                return !aptDate.isBefore(today);
                        }
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    private List<Appointments> getAppointmentHistory(Long doctorId, int days, String status) {
        try {
            List<Appointments> allAppointments = appointmentService.getAppointmentsByDoctorId(doctorId);
            LocalDate startDate = LocalDate.now().minusDays(days);

            return allAppointments.stream()
                    .filter(apt -> {
                        try {
                            if (apt.getAppointmentDate() == null) {
                                return false;
                            }
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), formatter);
                            return !aptDate.isBefore(startDate);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .filter(apt -> {
                        if ("all".equals(status)) {
                            return true;
                        }
                        return status.equals(apt.getStatus());
                    })
                    .sorted((a1, a2) -> {
                        try {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            LocalDate date1 = LocalDate.parse(a1.getAppointmentDate(), formatter);
                            LocalDate date2 = LocalDate.parse(a2.getAppointmentDate(), formatter);
                            return date2.compareTo(date1);
                        } catch (Exception e) {
                            return 0;
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<Appointments> getAutoActions(Long doctorId, int days) {
        try {
            List<Appointments> allAppointments = appointmentService.getAppointmentsByDoctorId(doctorId);
            LocalDate startDate = LocalDate.now().minusDays(days);

            return allAppointments.stream()
                    .filter(apt -> "AUTO_CANCELLED".equals(apt.getStatus()) || "NO_SHOW".equals(apt.getStatus()))
                    .filter(apt -> {
                        try {
                            if (apt.getAppointmentDate() == null) {
                                return false;
                            }
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), formatter);
                            return !aptDate.isBefore(startDate);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .sorted((a1, a2) -> {
                        if (a1.getCreatedAt() == null || a2.getCreatedAt() == null) {
                            return 0;
                        }
                        return a2.getCreatedAt().compareTo(a1.getCreatedAt());
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

// ==================== APPOINTMENTS MANAGEMENT ====================
    @GetMapping("/appointments")
    public String appointments(
            @RequestParam(defaultValue = "all") String filter,
            Model model) {

        try {
            String username = getCurrentUsername();
            Users user = usersService.findByUsername(username);

            if (user != null) {
                Optional<Doctors> doctorOpt = doctorService.getDoctorByUserId(user.getUserId());
                if (doctorOpt.isPresent()) {
                    Doctors doctor = doctorOpt.get();
                    Long doctorId = doctor.getDoctorId();

                    // Lấy tất cả appointments
                    List<Appointments> allAppointments = appointmentService.getAppointmentsByDoctorId(doctorId);

                    // DEBUG: In ra để kiểm tra
                    System.out.println("[DEBUG] Total appointments: " + allAppointments.size());
                    allAppointments.forEach(apt -> {
                        System.out.println("[DEBUG] Appointment " + apt.getAppointmentId()
                                + " - Status: " + apt.getStatus()
                                + " - Date: " + apt.getAppointmentDate()
                                + " - Time: " + apt.getAppointmentTime());
                    });

                    // Lọc theo filter
                    List<Appointments> filteredAppointments = filterAppointments(allAppointments, filter);

                    // Tính counts
                    Map<String, Long> counts = calculateAppointmentCounts(allAppointments);

                    model.addAttribute("appointments", filteredAppointments);
                    model.addAttribute("filter", filter);
                    model.addAttribute("appointmentCounts", counts);
                    model.addAttribute("doctor", doctor);

                    // Thêm counts riêng biệt cho template
                    model.addAttribute("todayAppointmentsCount", counts.get("today"));
                    model.addAttribute("pendingAppointmentsCount", counts.get("pending"));
                    model.addAttribute("confirmedAppointmentsCount", counts.get("confirmed"));
                    model.addAttribute("completedAppointmentsCount", counts.get("completed"));
                    model.addAttribute("cancelledAppointmentsCount", counts.get("cancelled"));
                    model.addAttribute("expiredAppointmentsCount", counts.get("expired"));
                }
            }

            return "doctor/appointments";

        } catch (Exception e) {
            System.err.println("Error in appointments: " + e.getMessage());
            e.printStackTrace();
            return handleError(model, "Appointments");
        }
    }

// Helper method: Lọc appointments với logic cải tiến
    private List<Appointments> filterAppointments(List<Appointments> appointments, String filter) {
        if (appointments == null || appointments.isEmpty()) {
            return new ArrayList<>();
        }

        // Xử lý trường hợp filter rỗng hoặc null
        if (filter == null || filter.isEmpty() || filter.equals("all")) {
            System.out.println("[DEBUG] Showing ALL appointments: " + appointments.size());
            return appointments;
        }

        System.out.println("[DEBUG] Filtering with: " + filter);

        switch (filter) {
            case "today":
                List<Appointments> todayApps = appointments.stream()
                        .filter(apt -> {
                            try {
                                if (apt.getAppointmentDate() == null) {
                                    return false;
                                }
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                                LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), formatter);
                                LocalDate today = LocalDate.now();
                                boolean isToday = aptDate.equals(today);
                                boolean isValidStatus = "CONFIRMED".equals(apt.getStatus())
                                        || "PENDING".equals(apt.getStatus());
                                return isToday && isValidStatus;
                            } catch (Exception e) {
                                System.err.println("Error parsing date: " + apt.getAppointmentDate());
                                return false;
                            }
                        })
                        .collect(Collectors.toList());
                System.out.println("[DEBUG] Today appointments: " + todayApps.size());
                return todayApps;

            case "pending":
                List<Appointments> pendingApps = appointments.stream()
                        .filter(apt -> "PENDING".equals(apt.getStatus()))
                        .collect(Collectors.toList());
                System.out.println("[DEBUG] Pending appointments: " + pendingApps.size());
                return pendingApps;

            case "confirmed":
                List<Appointments> confirmedApps = appointments.stream()
                        .filter(apt -> "CONFIRMED".equals(apt.getStatus()))
                        .collect(Collectors.toList());
                System.out.println("[DEBUG] Confirmed appointments: " + confirmedApps.size());
                return confirmedApps;

            case "completed":
                List<Appointments> completedApps = appointments.stream()
                        .filter(apt -> "COMPLETED".equals(apt.getStatus()))
                        .collect(Collectors.toList());
                System.out.println("[DEBUG] Completed appointments: " + completedApps.size());
                return completedApps;

            case "cancelled":
                List<Appointments> cancelledApps = appointments.stream()
                        .filter(apt -> "CANCELLED".equals(apt.getStatus()))
                        .collect(Collectors.toList());
                System.out.println("[DEBUG] Cancelled appointments: " + cancelledApps.size());
                return cancelledApps;

            case "expired":
                List<Appointments> expiredApps = appointments.stream()
                        .filter(apt -> isAppointmentExpired(apt))
                        .filter(apt -> "PENDING".equals(apt.getStatus()) || "CONFIRMED".equals(apt.getStatus()))
                        .collect(Collectors.toList());
                System.out.println("[DEBUG] Expired appointments: " + expiredApps.size());
                return expiredApps;

            default:
                // Nếu filter không hợp lệ, trả về tất cả
                System.out.println("[DEBUG] Unknown filter: " + filter + ", showing ALL appointments");
                return appointments;
        }
    }

// Helper method: Tính counts với logic cải tiến
    private Map<String, Long> calculateAppointmentCounts(List<Appointments> appointments) {
        Map<String, Long> counts = new HashMap<>();

        if (appointments == null) {
            appointments = new ArrayList<>();
        }

        // All appointments
        counts.put("all", (long) appointments.size());

        // Today appointments (only confirmed and pending for today)
        long todayCount = appointments.stream()
                .filter(apt -> {
                    try {
                        if (apt.getAppointmentDate() == null) {
                            return false;
                        }
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), formatter);
                        LocalDate today = LocalDate.now();
                        boolean isToday = aptDate.equals(today);
                        boolean isValidStatus = "CONFIRMED".equals(apt.getStatus())
                                || "PENDING".equals(apt.getStatus());
                        return isToday && isValidStatus;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();
        counts.put("today", todayCount);

        // Status-based counts
        counts.put("pending", appointments.stream().filter(apt -> "PENDING".equals(apt.getStatus())).count());
        counts.put("confirmed", appointments.stream().filter(apt -> "CONFIRMED".equals(apt.getStatus())).count());
        counts.put("completed", appointments.stream().filter(apt -> "COMPLETED".equals(apt.getStatus())).count());
        counts.put("cancelled", appointments.stream().filter(apt -> "CANCELLED".equals(apt.getStatus())).count());

        // Expired appointments (pending or confirmed appointments that are expired)
        long expiredCount = appointments.stream()
                .filter(apt -> isAppointmentExpired(apt))
                .filter(apt -> "PENDING".equals(apt.getStatus()) || "CONFIRMED".equals(apt.getStatus()))
                .count();
        counts.put("expired", expiredCount);

        System.out.println("[DEBUG] Calculated counts: " + counts);
        return counts;
    }

// Helper method: Kiểm tra appointment expired với logic đơn giản
    private boolean isAppointmentExpired(Appointments appointment) {
        try {
            if (appointment == null || appointment.getAppointmentDate() == null) {
                return false;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate appointmentDate = LocalDate.parse(appointment.getAppointmentDate(), formatter);
            LocalDate today = LocalDate.now();

            // Nếu ngày hẹn đã qua
            return appointmentDate.isBefore(today);

        } catch (Exception e) {
            System.err.println("Error checking expiration for appointment: " + appointment.getAppointmentId());
            return false;
        }
    }

    // ==================== MEDICAL RECORDS ====================
    @GetMapping("/medical-records")
    public String medicalRecords(@RequestParam(required = false) String search, Model model) {
        try {
            String username = getCurrentUsername();
            Users user = usersService.findByUsername(username);

            if (user != null) {
                Optional<Doctors> doctorOpt = doctorService.getDoctorByUserId(user.getUserId());
                if (doctorOpt.isPresent()) {
                    Doctors doctor = doctorOpt.get();

                    List<MedicalRecords> records;
                    if (search != null && !search.trim().isEmpty()) {
                        // Gọi search service
                        records = medicalRecordsService.searchMedicalRecords(search.trim(), doctor.getDoctorId());
                    } else {
                        // Lấy tất cả records
                        records = medicalRecordsService.getMedicalRecordsByDoctorId(doctor.getDoctorId());
                    }

                    model.addAttribute("records", records);
                    model.addAttribute("search", search);
                    model.addAttribute("doctor", doctor);

                    // DEBUG: In ra để kiểm tra
                    System.out.println("[DEBUG] Search term: " + search);
                    System.out.println("[DEBUG] Found records: " + records.size());
                    records.forEach(record -> {
                        if (record.getAppointment() != null) {
                            Users patient = usersService.findById(record.getAppointment().getUserId());
                            System.out.println("[DEBUG] Patient: " + (patient != null ? patient.getFullName() : "null"));
                        }
                    });
                }
            }

            return "doctor/medical-records";

        } catch (Exception e) {
            System.err.println("Error in medicalRecords: " + e.getMessage());
            e.printStackTrace();
            return handleError(model, "Medical Records");
        }
    }

    private List<Users> getRecentPatients(Long doctorId) {
        try {
            List<MedicalRecords> allRecords = medicalRecordsService.getMedicalRecordsByDoctorId(doctorId);

            return allRecords.stream()
                    .sorted((r1, r2) -> {
                        if (r1.getCreatedAt() == null && r2.getCreatedAt() == null) {
                            return 0;
                        }
                        if (r1.getCreatedAt() == null) {
                            return 1;
                        }
                        if (r2.getCreatedAt() == null) {
                            return -1;
                        }
                        return r2.getCreatedAt().compareTo(r1.getCreatedAt());
                    })
                    .map(record -> {
                        try {
                            if (record.getAppointment() != null) {
                                Users patient = usersService.findById(record.getAppointment().getUserId());
                                if (patient != null) {
                                    // Đảm bảo có đủ thông tin
                                    return patient;
                                }
                            }
                            return null;
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .distinct()
                    .limit(5)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error getting recent patients: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @GetMapping("/medical-record/{id}")
    public String viewMedicalRecord(@PathVariable Long id, Model model) {
        try {
            MedicalRecords record = medicalRecordsService.getMedicalRecordById(id);
            if (record != null) {
                Appointments appointment = appointmentService.getAppointmentById(record.getAppointmentId());
                Users patient = usersService.findById(appointment.getUserId());
                List<Prescriptions> prescriptions = medicalRecordsService.getPrescriptionsByRecordId(id);

                model.addAttribute("medicalRecord", record);
                model.addAttribute("appointment", appointment);
                model.addAttribute("patient", patient);
                model.addAttribute("prescriptions", prescriptions);
                return "doctor/medical-record-detail";
            }
            return "redirect:/doctor/medical-records";
        } catch (Exception e) {
            return "redirect:/doctor/medical-records";
        }
    }

    // ==================== MEDICAL RECORD CREATION ====================
    @GetMapping("/medical-record/create")
    public String createMedicalRecordForm(@RequestParam(required = false) Long appointmentId, Model model) {
        try {
            String username = getCurrentUsername();
            Users user = usersService.findByUsername(username);

            if (user != null) {
                Optional<Doctors> doctorOpt = doctorService.getDoctorByUserId(user.getUserId());
                if (doctorOpt.isPresent()) {
                    model.addAttribute("doctor", doctorOpt.get());
                }
            }

            MedicalRecords medicalRecord = new MedicalRecords();

            if (appointmentId != null) {
                Appointments appointment = appointmentService.getAppointmentById(appointmentId);
                if (appointment != null) {
                    Users patient = usersService.findById(appointment.getUserId());
                    model.addAttribute("appointment", appointment);
                    model.addAttribute("patient", patient);
                    medicalRecord.setAppointmentId(appointmentId);
                }
            }

            model.addAttribute("medicalRecord", medicalRecord);
            return "doctor/medical-record-form";

        } catch (Exception e) {
            return "redirect:/doctor/medical-records";
        }
    }

    @PostMapping("/medical-record/save")
    public String saveMedicalRecord(@ModelAttribute MedicalRecords medicalRecord,
            @RequestParam(required = false) List<String> medicationNames,
            @RequestParam(required = false) List<String> dosages,
            @RequestParam(required = false) List<String> frequencies,
            @RequestParam(required = false) List<String> durations,
            @RequestParam(required = false) List<String> notes) {

        try {
            medicalRecord.setCreatedAt(new Date());

            if (medicalRecord.getAppointmentId() == null) {
                return "redirect:/doctor/medical-record/create?error=AppointmentId is required";
            }

            List<Prescriptions> prescriptions = createPrescriptionsList(medicationNames, dosages, frequencies, durations, notes);
            medicalRecordsService.saveMedicalRecord(medicalRecord, prescriptions);

            return "redirect:/doctor/medical-records";

        } catch (Exception e) {
            return "redirect:/doctor/medical-record/create?error=Error saving medical record";
        }
    }

    // ==================== HELPER METHODS ====================
    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    private ResponseEntity<Map<String, String>> executeAppointmentAction(Long appointmentId, String action) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean success = false;

            switch (action) {
                case "confirm":
                    success = appointmentService.confirmAppointment(appointmentId);
                    break;
                case "cancel":
                    success = appointmentService.updateAppointmentStatus(appointmentId, "CANCELLED");
                    break;
                case "complete":
                    success = appointmentService.updateAppointmentStatus(appointmentId, "COMPLETED");
                    break;
            }

            if (success) {
                response.put("status", "SUCCESS");
                response.put("message", "Appointment " + action + "ed successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "ERROR");
                response.put("message", "Cannot " + action + " appointment");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Error " + action + "ing appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private List<Prescriptions> createPrescriptionsList(List<String> medicationNames,
            List<String> dosages, List<String> frequencies,
            List<String> durations, List<String> notes) {
        List<Prescriptions> prescriptions = new ArrayList<>();

        if (medicationNames != null) {
            for (int i = 0; i < medicationNames.size(); i++) {
                String medicationName = medicationNames.get(i);
                if (medicationName != null && !medicationName.trim().isEmpty()) {
                    Prescriptions prescription = new Prescriptions();
                    prescription.setMedicationName(medicationName.trim());

                    if (dosages != null && i < dosages.size()) {
                        prescription.setDosage(dosages.get(i));
                    }
                    if (frequencies != null && i < frequencies.size()) {
                        prescription.setFrequency(frequencies.get(i));
                    }
                    if (durations != null && i < durations.size()) {
                        prescription.setDuration(durations.get(i));
                    }
                    if (notes != null && i < notes.size()) {
                        prescription.setNotes(notes.get(i));
                    }

                    prescriptions.add(prescription);
                }
            }
        }
        return prescriptions;
    }

    private void generatePdfReport(Long id, HttpServletResponse response, String disposition) {
        try {
            MedicalRecords record = medicalRecordsService.getMedicalRecordById(id);
            if (record == null) {
                response.sendError(404, "Medical record not found");
                return;
            }

            Appointments appointment = appointmentService.getAppointmentById(record.getAppointmentId());
            Users patient = usersService.findById(appointment.getUserId());
            List<Prescriptions> prescriptions = medicalRecordsService.getPrescriptionsByRecordId(id);
            String username = getCurrentUsername();
            Users currentUser = usersService.findByUsername(username);

            byte[] pdfBytes = iTextReportService.generateMedicalRecordReport(record, patient, currentUser, prescriptions);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", disposition + "; filename=medical-record-" + id + ".pdf");
            response.setContentLength(pdfBytes.length);

            OutputStream out = response.getOutputStream();
            out.write(pdfBytes);
            out.flush();

        } catch (IOException e) {
            try {
                response.sendError(500, "Error generating PDF: " + e.getMessage());
            } catch (IOException ex) {
                System.err.println("Error sending error response: " + ex.getMessage());
            }
        }
    }

    private String handleError(Model model, String page) {
        model.addAttribute("error", "Có lỗi xảy ra khi tải trang " + page);
        return "doctor/error";
    }

    // ==================== NO-SHOW MANAGEMENT ====================
    @PostMapping("/appointments/{id}/mark-no-show")
    @ResponseBody
    public ResponseEntity<Map<String, String>> markAsNoShow(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean success = appointmentService.markAsNoShow(id);
            if (success) {
                response.put("status", "SUCCESS");
                response.put("message", "Đã đánh dấu bệnh nhân không đến khám");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "ERROR");
                response.put("message", "Không thể đánh dấu no-show");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Autowired
    private SpecialtyService specialtyService;

// Thêm method helper để lấy tên chuyên khoa
    private String getDoctorSpecialtyName(Doctors doctor) {
        if (doctor != null && doctor.getSpecialtyId() != null) {
            try {
                Specialties specialty = specialtyService.getSpecialtyById(doctor.getSpecialtyId());
                return specialty != null ? specialty.getName() : "Đa khoa";
            } catch (Exception e) {
                return "Đa khoa";
            }
        }
        return "Đa khoa";
    }

    public boolean canCreateMedicalRecord(Appointments appointment) {
        if (appointment == null) {
            return false;
        }

        // Có thể tạo hồ sơ nếu:
        // 1. Appointment đã CONFIRMED và đã qua thời gian hẹn
        // 2. Hoặc appointment đã COMPLETED
        if ("COMPLETED".equals(appointment.getStatus())) {
            return true;
        }

        if ("CONFIRMED".equals(appointment.getStatus())) {
            return isAppointmentExpired(appointment);
        }

        return false;
    }

    @GetMapping("/appointments/{id}")
    public String viewAppointment(@PathVariable Long id, Model model) {
        try {
            String username = getCurrentUsername();
            Users currentUser = usersService.findByUsername(username);

            if (currentUser != null) {
                Optional<Doctors> doctorOpt = doctorService.getDoctorByUserId(currentUser.getUserId());
                if (doctorOpt.isPresent()) {
                    Doctors doctor = doctorOpt.get();
                    model.addAttribute("doctor", doctor);
                }
            }

            // Lấy thông tin appointment
            Appointments appointment = appointmentService.getAppointmentById(id);
            if (appointment == null) {
                return "redirect:/doctor/appointments?error=Appointment not found";
            }

            // Lấy thông tin patient
            Users patient = usersService.findById(appointment.getUserId());

            // Làm giàu dữ liệu appointment
            appointmentService.enrichAppointmentDataPub(appointment);

            // Tính toán các thuộc tính
            boolean isExpired = appointmentService.isAppointmentExpired(appointment);
            boolean canCreateMedicalRecord = appointmentService.canCreateMedicalRecord(appointment);
            String timeStatusClass = appointmentService.getTimeStatusClass(appointment);
            String timeStatusText = appointmentService.getTimeStatusText(appointment);

            model.addAttribute("appointment", appointment);
            model.addAttribute("patient", patient);
            model.addAttribute("isAppointmentExpired", isExpired);
            model.addAttribute("canCreateMedicalRecord", canCreateMedicalRecord);
            model.addAttribute("timeStatusClass", timeStatusClass);
            model.addAttribute("timeStatusText", timeStatusText);

            return "doctor/appointment-detail";

        } catch (Exception e) {
            System.err.println("Error in viewAppointment: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/doctor/appointments?error=Error loading appointment details";
        }
    }

    @GetMapping("/medical-records/search-suggestions")
    @ResponseBody
    public ResponseEntity<List<Map<String, String>>> searchSuggestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit) {

        try {
            String username = getCurrentUsername();
            Users user = usersService.findByUsername(username);

            List<Map<String, String>> suggestions = new ArrayList<>();

            if (user != null) {
                Optional<Doctors> doctorOpt = doctorService.getDoctorByUserId(user.getUserId());
                if (doctorOpt.isPresent()) {
                    Doctors doctor = doctorOpt.get();

                    // Lấy records của doctor
                    List<MedicalRecords> records = medicalRecordsService.getMedicalRecordsByDoctorId(doctor.getDoctorId());

                    // Tạo suggestions từ patients
                    Set<Long> processedPatientIds = new HashSet<>();
                    for (MedicalRecords record : records) {
                        if (record.getAppointment() != null) {
                            Users patient = usersService.findById(record.getAppointment().getUserId());
                            if (patient != null && !processedPatientIds.contains(patient.getUserId())) {
                                processedPatientIds.add(patient.getUserId());

                                // Check if patient matches search
                                if (patient.getFullName() != null && patient.getFullName().toLowerCase().contains(query.toLowerCase())
                                        || (patient.getPhone() != null && patient.getPhone().contains(query))) {

                                    Map<String, String> suggestion = new HashMap<>();
                                    suggestion.put("name", patient.getFullName());
                                    suggestion.put("phone", patient.getPhone() != null ? patient.getPhone() : "");
                                    suggestion.put("type", "Bệnh nhân");
                                    suggestion.put("value", patient.getFullName()); // Use name for search
                                    suggestions.add(suggestion);

                                    if (suggestions.size() >= limit) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return ResponseEntity.ok(suggestions);

        } catch (Exception e) {
            System.err.println("Error in searchSuggestions: " + e.getMessage());
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // Trong DoctorController
    @GetMapping("/medical-record/select-appointment")
    public String selectAppointmentForMedicalRecord(Model model) {
        try {
            String username = getCurrentUsername();
            Users user = usersService.findByUsername(username);

            if (user != null) {
                Optional<Doctors> doctorOpt = doctorService.getDoctorByUserId(user.getUserId());
                if (doctorOpt.isPresent()) {
                    Doctors doctor = doctorOpt.get();

                    // Lấy appointments có thể tạo hồ sơ (đã completed hoặc confirmed + expired)
                    List<Appointments> eligibleAppointments = appointmentService.getAppointmentsByDoctorId(doctor.getDoctorId())
                            .stream()
                            .filter(apt -> appointmentService.canCreateMedicalRecord(apt))
                            .collect(Collectors.toList());

                    model.addAttribute("appointments", eligibleAppointments);
                    model.addAttribute("doctor", doctor);
                }
            }

            return "doctor/select-appointment-for-record";

        } catch (Exception e) {
            return "redirect:/doctor/medical-records?error=Error loading appointments";
        }
    }

    @GetMapping("/medical-record/appointment/{appointmentId}")
    public String viewMedicalRecordByAppointment(@PathVariable Long appointmentId, Model model) {
        try {
            MedicalRecords record = medicalRecordsService.getMedicalRecordByAppointmentId(appointmentId);

            if (record != null) {
                Appointments appointment = appointmentService.getAppointmentById(appointmentId);
                Users patient = usersService.findById(appointment.getUserId());
                List<Prescriptions> prescriptions = medicalRecordsService.getPrescriptionsByRecordId(record.getRecordId());

                model.addAttribute("medicalRecord", record);
                model.addAttribute("appointment", appointment);
                model.addAttribute("patient", patient);
                model.addAttribute("prescriptions", prescriptions);

                return "doctor/medical-record-detail";
            } else {
                return "redirect:/doctor/medical-record/create?appointmentId=" + appointmentId;
            }
        } catch (Exception e) {
            System.err.println("Error viewing medical record: " + e.getMessage());
            return "redirect:/doctor/medical-records?error=Record not found";
        }
    }

    // API cho quản lý đơn thuốc
    @GetMapping("/prescription/{id}")
    @ResponseBody
    public Prescriptions getPrescription(@PathVariable Long id) {
        return medicalRecordsService.getPrescriptionById(id);
    }

    @PostMapping("/prescription/create")
    @ResponseBody
    public String createPrescription(@RequestParam Long recordId,
            @RequestParam String medicationName,
            @RequestParam(required = false) String dosage,
            @RequestParam(required = false) String frequency,
            @RequestParam(required = false) String duration,
            @RequestParam(required = false) String notes) {

        Prescriptions prescription = new Prescriptions();
        prescription.setRecordId(recordId);
        prescription.setMedicationName(medicationName);
        prescription.setDosage(dosage);
        prescription.setFrequency(frequency);
        prescription.setDuration(duration);
        prescription.setNotes(notes);

        medicalRecordsService.savePrescription(prescription);
        return "SUCCESS";
    }

    @PutMapping("/prescription/update")
    @ResponseBody
    public String updatePrescription(@RequestParam Long prescriptionId,
            @RequestParam String medicationName,
            @RequestParam(required = false) String dosage,
            @RequestParam(required = false) String frequency,
            @RequestParam(required = false) String duration,
            @RequestParam(required = false) String notes) {

        Prescriptions prescription = medicalRecordsService.getPrescriptionById(prescriptionId);
        if (prescription != null) {
            prescription.setMedicationName(medicationName);
            prescription.setDosage(dosage);
            prescription.setFrequency(frequency);
            prescription.setDuration(duration);
            prescription.setNotes(notes);

            medicalRecordsService.savePrescription(prescription);
            return "SUCCESS";
        }
        return "ERROR";
    }

    @DeleteMapping("/prescription/delete/{id}")
    @ResponseBody
    public String deletePrescription(@PathVariable Long id) {
        medicalRecordsService.deletePrescription(id);
        return "SUCCESS";
    }

    private void updatePrescriptions(Long recordId, List<String> medicationNames,
            List<String> dosages, List<String> frequencies,
            List<String> durations, List<String> notes) {
        // Xóa các prescription cũ
        medicalRecordsService.deletePrescriptionsByRecordId(recordId);

        // Thêm prescriptions mới
        for (int i = 0; i < medicationNames.size(); i++) {
            if (medicationNames.get(i) != null && !medicationNames.get(i).trim().isEmpty()) {
                Prescriptions prescription = new Prescriptions();
                prescription.setRecordId(recordId);
                prescription.setMedicationName(medicationNames.get(i).trim());

                if (dosages != null && i < dosages.size()) {
                    prescription.setDosage(dosages.get(i));
                }
                if (frequencies != null && i < frequencies.size()) {
                    prescription.setFrequency(frequencies.get(i));
                }
                if (durations != null && i < durations.size()) {
                    prescription.setDuration(durations.get(i));
                }
                if (notes != null && i < notes.size()) {
                    prescription.setNotes(notes.get(i));
                }

                medicalRecordsService.savePrescription(prescription);
            }
        }
    }

    @GetMapping("/medical-record/{id}/print")
    public void printMedicalRecord(@PathVariable Long id, HttpServletResponse response) {
        System.out.println("=== YÊU CẦU IN HỒ SƠ (iText) ===");

        try {
            MedicalRecords record = medicalRecordsService.getMedicalRecordById(id);
            if (record == null) {
                response.sendError(404, "Medical record not found");
                return;
            }

            Appointments appointment = appointmentService.getAppointmentById(record.getAppointmentId());
            Users patient = usersService.findById(appointment.getUserId());
            List<Prescriptions> prescriptions = medicalRecordsService.getPrescriptionsByRecordId(id);

            String username = getCurrentUsername();
            Users currentUser = usersService.findByUsername(username);

            byte[] pdfBytes = iTextReportService.generateMedicalRecordReport(record, patient, currentUser, prescriptions);

            // Thiết lập response
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=medical-record-" + id + ".pdf");
            response.setContentLength(pdfBytes.length);

            OutputStream out = response.getOutputStream();
            out.write(pdfBytes);
            out.flush();

            System.out.println("=== PDF iText ĐÃ GỬI THÀNH CÔNG ===");

        } catch (IOException e) {
            System.err.println("Lỗi iText controller: " + e.getMessage());
            try {
                response.sendError(500, "Error generating PDF: " + e.getMessage());
            } catch (IOException ex) {
                System.err.println("Lỗi gửi response lỗi: " + ex.getMessage());
            }
        }
    }

    @GetMapping("/medical-record/{id}/download")
    public void downloadMedicalRecord(@PathVariable Long id, HttpServletResponse response) {
        try {
            MedicalRecords record = medicalRecordsService.getMedicalRecordById(id);
            if (record == null) {
                response.sendError(404, "Medical record not found");
                return;
            }

            Appointments appointment = appointmentService.getAppointmentById(record.getAppointmentId());
            Users patient = usersService.findById(appointment.getUserId());
            List<Prescriptions> prescriptions = medicalRecordsService.getPrescriptionsByRecordId(id);

            String username = getCurrentUsername();
            Users currentUser = usersService.findByUsername(username);

            byte[] pdfBytes = iTextReportService.generateMedicalRecordReport(record, patient, currentUser, prescriptions);

            // Thiết lập response cho download
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=medical-record-" + id + ".pdf");
            response.setContentLength(pdfBytes.length);

            OutputStream out = response.getOutputStream();
            out.write(pdfBytes);
            out.flush();

        } catch (IOException e) {
            System.err.println("Lỗi download iText: " + e.getMessage());
            try {
                response.sendError(500, "Error generating PDF: " + e.getMessage());
            } catch (IOException ex) {
                System.err.println("Lỗi gửi response lỗi: " + ex.getMessage());
            }
        }
    }

    // Thêm vào DoctorController
    @GetMapping("/medical-record/edit/{id}")
    public String editMedicalRecordForm(@PathVariable Long id, Model model) {
        MedicalRecords record = medicalRecordsService.getMedicalRecordById(id);
        if (record == null) {
            return "redirect:/doctor/medical-records";
        }

        Appointments appointment = appointmentService.getAppointmentById(record.getAppointmentId());
        Users patient = usersService.findById(appointment.getUserId());
        List<Prescriptions> prescriptions = medicalRecordsService.getPrescriptionsByRecordId(id);

        model.addAttribute("medicalRecord", record);
        model.addAttribute("patient", patient);
        model.addAttribute("appointment", appointment);
        model.addAttribute("prescriptions", prescriptions);

        return "doctor/medical-record-edit";
    }

    @PostMapping("/medical-record/update")
    public String updateMedicalRecord(@ModelAttribute MedicalRecords medicalRecord,
            @RequestParam(required = false) List<String> medicationNames,
            @RequestParam(required = false) List<String> dosages,
            @RequestParam(required = false) List<String> frequencies,
            @RequestParam(required = false) List<String> durations,
            @RequestParam(required = false) List<String> notes) {

        // Cập nhật thông tin hồ sơ bệnh án
        medicalRecordsService.updateMedicalRecord(medicalRecord);

        // Cập nhật đơn thuốc nếu có
        if (medicationNames != null) {
            updatePrescriptions(medicalRecord.getRecordId(), medicationNames, dosages, frequencies, durations, notes);
        }

        return "redirect:/doctor/medical-record/" + medicalRecord.getRecordId();
    }

}
