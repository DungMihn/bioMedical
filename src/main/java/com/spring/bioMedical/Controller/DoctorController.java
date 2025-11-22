package com.spring.bioMedical.Controller;

import com.spring.bioMedical.DTO.AppointmentHistoryDTO;
import com.spring.bioMedical.DTO.ConfirmedAppointmentDTO;
import com.spring.bioMedical.entity.*;
import com.spring.bioMedical.repository.DoctorRepository;
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
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    // ==================== DEPENDENCIES ====================
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private DoctorRepository doctorRepo;
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
    @Autowired
    private SpecialtyService specialtyService;

    @Autowired
    private BillingService billingService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // ==================== DASHBOARD / INDEX ====================
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

                    // Đọc tất cả appointment của bác sĩ
                    List<Appointments> allAppointments = appointmentService.getAppointmentsByDoctorId(doctorId);
                    System.out.println("[DEBUG] Total appointments found: " + allAppointments.size());

                    for (Appointments appt : allAppointments) {
                        System.out.println("[DEBUG] Appointment " + appt.getAppointmentId()
                                + " - Status: " + appt.getStatus()
                                + " - Slot ID: " + appt.getSlotId()
                                + " - User ID: " + appt.getUserId());
                    }

                    // Các danh sách con
                    List<Appointments> todayAppointments = getTodayAppointmentsWithExpiredCheck(allAppointments);
                    List<Appointments> confirmedAppointments = getConfirmedAppointments(allAppointments);
                    List<Appointments> completedAppointments = getCompletedAppointments(allAppointments);
                    List<Appointments> expiredAppointments = getExpiredAppointments(allAppointments);

                    // Thông báo / auto-actions
                    List<Appointments> upcomingNotifications
                            = appointmentService.getUpcomingAppointmentsForNotifications(doctorId);
                    List<Appointments> recentAutoActions = getRecentAutoActions(doctorId);

                    // Appointment đang "active" trong hôm nay (±2h)
                    LocalDateTime now = LocalDateTime.now();
                    List<Appointments> activeAppointments = allAppointments.stream()
                            .filter(apt -> isActiveAppointment(apt, now))
                            .collect(Collectors.toList());

                    // Bệnh nhân gần đây (dựa trên hồ sơ)
                    List<Users> recentPatients = getRecentPatients(doctorId);

                    // Thống kê lịch sử 30 ngày
                    Map<String, Object> historyStats30 = calculateHistoryStats(allAppointments, 30);

                    // Đếm tổng hợp
                    Map<String, Long> appointmentCounts = calculateAppointmentCounts(allAppointments);

                    // Stats tổng hợp cho dashboard
                    Map<String, Object> stats = getAccurateAppointmentStats(allAppointments);

                    // Gán model
                    model.addAttribute("doctor", doctor);
                    model.addAttribute("specialtyName", getDoctorSpecialtyName(doctor));

                    model.addAttribute("todayAppointments", todayAppointments);
                    model.addAttribute("confirmedAppointments", confirmedAppointments);
                    model.addAttribute("completedAppointments", completedAppointments);
                    model.addAttribute("expiredAppointments", expiredAppointments);

                    model.addAttribute("upcomingNotifications", upcomingNotifications);
                    model.addAttribute("recentAutoActions", recentAutoActions);
                    model.addAttribute("activeAppointments", activeAppointments);
                    model.addAttribute("activeAppointmentsCount", activeAppointments.size());
                    model.addAttribute("recentPatients", recentPatients);
                    model.addAttribute("historyStats30", historyStats30);
                    model.addAttribute("appointmentCounts", appointmentCounts);

                    model.addAttribute("todayAppointmentsCount", todayAppointments.size());
                    model.addAttribute("confirmedAppointmentsCount", confirmedAppointments.size());
                    model.addAttribute("completedAppointmentsCount", completedAppointments.size());
                    model.addAttribute("expiredAppointmentsCount", expiredAppointments.size());

                    model.addAttribute("hasExpiredAppointments", !expiredAppointments.isEmpty());
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
            return handleError(model, "Dashboard");
        }
    }

    private boolean isActiveAppointment(Appointments appointment, LocalDateTime now) {
        LocalDateTime aptDateTime = createAppointmentDateTime(appointment);
        if (aptDateTime == null) {
            return false;
        }
        if (!aptDateTime.toLocalDate().equals(now.toLocalDate())) {
            return false;
        }

        long diffHours = ChronoUnit.HOURS.between(aptDateTime, now);
        return diffHours >= 0 && diffHours <= 2;
    }

    // ==================== NHÓM HELPER FILTER CHUNG ====================
    private LocalDateTime createAppointmentDateTime(Appointments appointment) {
        try {
            if (appointment.getAppointmentDate() == null || appointment.getAppointmentTime() == null) {
                return null;
            }
            LocalDate date = LocalDate.parse(appointment.getAppointmentDate(), DATE_FMT);
            LocalTime time = LocalTime.parse(appointment.getAppointmentTime(), TIME_FMT);
            return LocalDateTime.of(date, time);
        } catch (Exception e) {
            return null;
        }
    }

    private List<Appointments> getTodayAppointmentsWithExpiredCheck(List<Appointments> allAppointments) {
        LocalDate today = LocalDate.now();
        return allAppointments.stream()
                .filter(apt -> {
                    try {
                        if (apt.getAppointmentDate() == null) {
                            return false;
                        }
                        LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), DATE_FMT);
                        boolean isToday = aptDate.equals(today);
                        // CHỈ CONFIRMED mới được coi là lịch hợp lệ
                        boolean isValidStatus = "CONFIRMED".equals(apt.getStatus());
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
                .filter(apt -> !appointmentService.isAppointmentExpired(apt))
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
                    boolean isConfirmed = "CONFIRMED".equals(apt.getStatus());
                    return isConfirmed && appointmentService.isAppointmentExpired(apt);
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> getAccurateAppointmentStats(List<Appointments> allAppointments) {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();

        try {
            long todayCount = allAppointments.stream()
                    .filter(apt -> {
                        try {
                            if (apt.getAppointmentDate() == null) {
                                return false;
                            }
                            LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), DATE_FMT);
                            boolean isToday = aptDate.equals(today);
                            boolean isValidStatus = "CONFIRMED".equals(apt.getStatus());
                            boolean isNotExpired = !appointmentService.isAppointmentExpired(apt);
                            return isToday && isValidStatus && isNotExpired;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .count();

            long upcomingCount = allAppointments.stream()
                    .filter(apt -> "CONFIRMED".equals(apt.getStatus()))
                    .filter(apt -> {
                        try {
                            if (apt.getAppointmentDate() == null) {
                                return false;
                            }
                            LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), DATE_FMT);
                            return !aptDate.isBefore(today) && aptDate.isBefore(today.plusDays(7));
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .count();

            long recentAutoActions = allAppointments.stream()
                    .filter(apt -> "AUTO_CANCELLED".equals(apt.getStatus())
                    || "NO_SHOW".equals(apt.getStatus()))
                    .filter(apt -> apt.getCreatedAt() != null
                    && apt.getCreatedAt().toInstant()
                            .isAfter(Instant.now().minus(24, ChronoUnit.HOURS)))
                    .count();

            long expiredCount = allAppointments.stream()
                    .filter(apt -> {
                        boolean isConfirmed = "CONFIRMED".equals(apt.getStatus());
                        return isConfirmed && appointmentService.isAppointmentExpired(apt);

                    })
                    .count();

            LocalDateTime now = LocalDateTime.now();
            long activeCount = allAppointments.stream()
                    .filter(apt -> isActiveAppointment(apt, now))
                    .count();

            stats.put("todayCount", todayCount);
            stats.put("upcomingCount", upcomingCount);
            stats.put("recentAutoActions", recentAutoActions);
            stats.put("expiredCount", expiredCount);
            stats.put("activeCount", activeCount);
            stats.put("totalAppointments", (long) allAppointments.size());
        } catch (Exception e) {
            stats.put("todayCount", 0L);
            stats.put("upcomingCount", 0L);
            stats.put("recentAutoActions", 0L);
            stats.put("expiredCount", 0L);
            stats.put("activeCount", 0L);
            stats.put("totalAppointments", 0L);
        }
        return stats;
    }

    // ==================== CONFIRMED APPOINTMENTS LIST ====================
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

                    List<Appointments> allConfirmedAppointments
                            = appointmentService.getConfirmedAppointmentsByDoctorId(doctorId);

                    List<ConfirmedAppointmentDTO> filteredAppointments
                            = processConfirmedAppointments(allConfirmedAppointments, period);

                    Map<String, Long> periodCounts
                            = calculateConfirmedPeriodCounts(allConfirmedAppointments);

                    model.addAttribute("appointments", filteredAppointments);
                    model.addAttribute("doctor", doctor);
                    model.addAttribute("period", period);
                    model.addAttribute("periodCounts", periodCounts);
                    model.addAttribute("totalCount", allConfirmedAppointments.size());
                    model.addAttribute("filteredCount", filteredAppointments.size());
                    model.addAttribute("upcomingAppointmentsCount", periodCounts.get("upcoming"));
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

    private List<ConfirmedAppointmentDTO> processConfirmedAppointments(List<Appointments> appointments, String period) {
        if (appointments == null || appointments.isEmpty()) {
            return new ArrayList<>();
        }

        LocalDateTime now = LocalDateTime.now();

        return appointments.stream()
                .filter(apt -> {
                    try {
                        LocalDateTime aptDateTime = createAppointmentDateTime(apt);
                        if (aptDateTime == null) {
                            return false;
                        }

                        switch (period) {
                            case "today":
                                return aptDateTime.toLocalDate().equals(now.toLocalDate());
                            case "tomorrow":
                                return aptDateTime.toLocalDate()
                                        .equals(now.toLocalDate().plusDays(1));
                            case "week":
                                LocalDate weekEnd = now.toLocalDate().plusDays(7);
                                return !aptDateTime.toLocalDate().isBefore(now.toLocalDate())
                                        && aptDateTime.toLocalDate().isBefore(weekEnd);
                            case "upcoming":
                                return aptDateTime.isAfter(now);
                            case "past":
                                return aptDateTime.isBefore(now);
                            case "active":
                                return isActiveAppointment(apt, now);
                            default:
                                return true;
                        }
                    } catch (Exception e) {
                        return false;
                    }
                })
                .sorted(Comparator.comparing(this::createAppointmentDateTime)) // tăng dần theo thời gian
                .map(apt -> {
                    LocalDateTime aptDateTime = createAppointmentDateTime(apt);
                    String timeIndicatorClass = calculateTimeIndicatorClass(aptDateTime);
                    String timeIndicatorText = calculateTimeIndicatorText(aptDateTime);
                    boolean todayOrPast = isAppointmentTodayOrPast(aptDateTime);
                    boolean past = isAppointmentPast(aptDateTime);
                    boolean canCreateMedicalRecord = appointmentService.canCreateMedicalRecord(apt);

                    return new ConfirmedAppointmentDTO(
                            apt,
                            timeIndicatorClass,
                            timeIndicatorText,
                            todayOrPast,
                            past,
                            canCreateMedicalRecord
                    );
                })
                .collect(Collectors.toList());
    }

    private String calculateTimeIndicatorClass(LocalDateTime aptDateTime) {
        if (aptDateTime == null) {
            return "time-past";
        }

        LocalDateTime now = LocalDateTime.now();
        if (aptDateTime.isBefore(now)) {
            return "time-past";
        }
        if (aptDateTime.toLocalDate().equals(now.toLocalDate())) {
            return "time-today";
        }
        return "time-upcoming";
    }

    private String calculateTimeIndicatorText(LocalDateTime aptDateTime) {
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
                return minutesUntil + " PHÚT";
            } else {
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

    private boolean isAppointmentTodayOrPast(LocalDateTime aptDateTime) {
        if (aptDateTime == null) {
            return true;
        }
        LocalDateTime now = LocalDateTime.now();
        return !aptDateTime.isAfter(now);
    }

    private boolean isAppointmentPast(LocalDateTime aptDateTime) {
        if (aptDateTime == null) {
            return true;
        }
        LocalDateTime now = LocalDateTime.now();
        return aptDateTime.isBefore(now);
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
                    LocalDateTime dt = createAppointmentDateTime(apt);
                    return dt != null && dt.toLocalDate().equals(now.toLocalDate());
                })
                .count());

        counts.put("tomorrow", appointments.stream()
                .filter(apt -> {
                    LocalDateTime dt = createAppointmentDateTime(apt);
                    return dt != null && dt.toLocalDate().equals(now.toLocalDate().plusDays(1));
                })
                .count());

        counts.put("week", appointments.stream()
                .filter(apt -> {
                    LocalDateTime dt = createAppointmentDateTime(apt);
                    if (dt == null) {
                        return false;
                    }
                    LocalDate weekEnd = now.toLocalDate().plusDays(7);
                    return !dt.toLocalDate().isBefore(now.toLocalDate())
                            && dt.toLocalDate().isBefore(weekEnd);
                })
                .count());

        counts.put("upcoming", appointments.stream()
                .filter(apt -> {
                    LocalDateTime dt = createAppointmentDateTime(apt);
                    return dt != null && dt.isAfter(now);
                })
                .count());

        counts.put("past", appointments.stream()
                .filter(apt -> {
                    LocalDateTime dt = createAppointmentDateTime(apt);
                    return dt != null && dt.isBefore(now);
                })
                .count());

        counts.put("active", appointments.stream()
                .filter(apt -> isActiveAppointment(apt, now))
                .count());

        counts.put("all", (long) appointments.size());
        return counts;
    }

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
            default:
                return "sắp diễn ra";
        }
    }

    // ==================== APPOINTMENT HISTORY ====================
    @GetMapping("/appointments/history")
    public String appointmentHistory(@RequestParam(defaultValue = "30") int days,
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

                    List<Appointments> allAppointments
                            = appointmentService.getAppointmentsByDoctorId(doctorId);

                    List<AppointmentHistoryDTO> historyAppointments
                            = filterHistoryAppointments(allAppointments, days, status, search);

                    Map<String, Object> stats = calculateHistoryStats(allAppointments, days);

                    model.addAttribute("appointments", historyAppointments);
                    model.addAttribute("doctor", doctor);
                    model.addAttribute("days", days);
                    model.addAttribute("status", status);
                    model.addAttribute("search", search);
                    model.addAttribute("stats", stats);
                    model.addAttribute("totalCount", historyAppointments.size());

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
            int days,
            String status,
            String search) {
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
                        LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), DATE_FMT);
                        boolean withinDateRange = !aptDate.isBefore(startDate);

                        boolean statusMatch = "all".equals(status) || status.equals(apt.getStatus());

                        boolean searchMatch = true;
                        if (search != null && !search.trim().isEmpty()) {
                            String searchLower = search.toLowerCase().trim();
                            String patientName = apt.getPatientName() != null
                                    ? apt.getPatientName().toLowerCase() : "";
                            String patientEmail = apt.getPatientEmail() != null
                                    ? apt.getPatientEmail().toLowerCase() : "";
                            searchMatch = patientName.contains(searchLower)
                                    || patientEmail.contains(searchLower);
                        }

                        return withinDateRange && statusMatch && searchMatch;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .sorted((a1, a2) -> {
                    try {
                        if (a1.getCreatedAt() == null || a2.getCreatedAt() == null) {
                            return 0;
                        }
                        return a2.getCreatedAt().compareTo(a1.getCreatedAt()); // mới nhất lên đầu
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .map(apt -> {
                    String timeAgo = calculateTimeAgo(apt.getCreatedAt());
                    boolean canViewMedicalRecord = "COMPLETED".equals(apt.getStatus());
                    boolean canCreateMedicalRecord = appointmentService.canCreateMedicalRecord(apt);
                    String statusBadgeClass = getStatusBadgeClass(apt.getStatus());
                    return new AppointmentHistoryDTO(apt, timeAgo,
                            canViewMedicalRecord, canCreateMedicalRecord, statusBadgeClass);
                })
                .collect(Collectors.toList());
    }

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
                            LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), DATE_FMT);
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
                            LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), DATE_FMT);
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
                            LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), DATE_FMT);
                            return !aptDate.isBefore(startDate);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .count();

            stats.put("total", total);
            stats.put("completed", completed);
            stats.put("cancelled", cancelled);
            stats.put("completionRate", total > 0
                    ? Math.round((completed * 100.0) / total) : 0);
        } catch (Exception e) {
            stats.put("total", 0L);
            stats.put("completed", 0L);
            stats.put("cancelled", 0L);
            stats.put("completionRate", 0L);
        }
        return stats;
    }

    private List<String> getStatusOptions() {
        return Arrays.asList("all", "COMPLETED", "CONFIRMED", "CANCELLED",
                "NO_SHOW", "AUTO_CANCELLED", "EXPIRED", "PENDING");
    }

    // ==================== AUTO ACTIONS LOG ====================
    @GetMapping("/appointments/auto-actions")
    public String autoActionsLog(@RequestParam(defaultValue = "7") int days, Model model) {
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

    private List<Appointments> getRecentAutoActions(Long doctorId) {
        try {
            List<Appointments> allAppointments
                    = appointmentService.getAppointmentsByDoctorId(doctorId);

            return allAppointments.stream()
                    .filter(apt -> "AUTO_CANCELLED".equals(apt.getStatus())
                    || "NO_SHOW".equals(apt.getStatus()))
                    .filter(apt -> apt.getCreatedAt() != null
                    && apt.getCreatedAt().toInstant()
                            .isAfter(Instant.now().minus(24, ChronoUnit.HOURS)))
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

    private List<Appointments> getAutoActions(Long doctorId, int days) {
        try {
            List<Appointments> allAppointments
                    = appointmentService.getAppointmentsByDoctorId(doctorId);
            LocalDate startDate = LocalDate.now().minusDays(days);

            return allAppointments.stream()
                    .filter(apt -> "AUTO_CANCELLED".equals(apt.getStatus())
                    || "NO_SHOW".equals(apt.getStatus()))
                    .filter(apt -> {
                        try {
                            if (apt.getAppointmentDate() == null) {
                                return false;
                            }
                            LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), DATE_FMT);
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

    // ==================== QUẢN LÝ APPOINTMENTS – LIST / FILTER ====================
    @GetMapping("/appointments")
    public String appointments(@RequestParam(defaultValue = "all") String filter,
            @RequestParam(required = false) String search,
            Model model) {
        try {
            String username = getCurrentUsername();
            Users user = usersService.findByUsername(username);

            if (user != null) {
                Optional<Doctors> doctorOpt = doctorService.getDoctorByUserId(user.getUserId());
                if (doctorOpt.isPresent()) {
                    Doctors doctor = doctorOpt.get();
                    Long doctorId = doctor.getDoctorId();

                    List<Appointments> allAppointments
                            = appointmentService.getAppointmentsByDoctorId(doctorId);

                    System.out.println("[APPOINTMENTS] Total: " + allAppointments.size());
                    System.out.println("[APPOINTMENTS] Filter = " + filter + ", search = " + search);

                    List<Appointments> filteredAppointments
                            = filterAppointments(allAppointments, filter, search);

                    // Sắp xếp: mới nhất (thời gian gần hiện tại nhất) lên TRÊN
                    filteredAppointments.sort((a1, a2) -> {
                        try {
                            LocalDateTime d1 = createAppointmentDateTime(a1);
                            LocalDateTime d2 = createAppointmentDateTime(a2);
                            if (d1 == null && d2 == null) {
                                return 0;
                            }
                            if (d1 == null) {
                                return 1;
                            }
                            if (d2 == null) {
                                return -1;
                            }
                            return d2.compareTo(d1); // GIẢM dần => mới nhất trước
                        } catch (Exception e) {
                            return 0;
                        }
                    });

                    int MAX_SHOW = 50;
                    boolean truncated = filteredAppointments.size() > MAX_SHOW;
                    if (truncated) {
                        filteredAppointments = filteredAppointments.subList(0, MAX_SHOW);
                    }

                    Map<String, Long> counts = calculateAppointmentCounts(allAppointments);

                    model.addAttribute("appointments", filteredAppointments);
                    model.addAttribute("filter", filter);
                    model.addAttribute("search", search);
                    model.addAttribute("appointmentCounts", counts);
                    model.addAttribute("doctor", doctor);
                    model.addAttribute("listTruncated", truncated);
                    model.addAttribute("totalFilteredCount", filteredAppointments.size());

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

    // 1 method filter duy nhất (filter + search)
    private List<Appointments> filterAppointments(List<Appointments> appointments,
            String filter,
            String search) {
        if (appointments == null || appointments.isEmpty()) {
            return new ArrayList<>();
        }

        String searchText = (search == null) ? "" : search.trim().toLowerCase();

        List<Appointments> baseList;
        LocalDate today = LocalDate.now();

        if (filter == null || filter.isEmpty() || "all".equals(filter)) {
            baseList = new ArrayList<>(appointments);
        } else {
            System.out.println("[APPOINTMENTS] Filtering with: " + filter);
            switch (filter) {
                case "today":
                    baseList = appointments.stream()
                            .filter(apt -> {
                                try {
                                    if (apt.getAppointmentDate() == null) {
                                        return false;
                                    }
                                    LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), DATE_FMT);
                                    boolean isToday = aptDate.equals(today);
                                    boolean isValidStatus = "CONFIRMED".equals(apt.getStatus())
                                            || "PENDING".equals(apt.getStatus());
                                    return isToday && isValidStatus;
                                } catch (Exception e) {
                                    return false;
                                }
                            })
                            .collect(Collectors.toList());
                    break;
                case "pending":
                    baseList = appointments.stream()
                            .filter(apt -> "PENDING".equals(apt.getStatus()))
                            .collect(Collectors.toList());
                    break;
                case "confirmed":
                    baseList = appointments.stream()
                            .filter(apt -> "CONFIRMED".equals(apt.getStatus()))
                            .collect(Collectors.toList());
                    break;
                case "completed":
                    baseList = appointments.stream()
                            .filter(apt -> "COMPLETED".equals(apt.getStatus()))
                            .collect(Collectors.toList());
                    break;
                case "cancelled":
                    baseList = appointments.stream()
                            .filter(apt -> "CANCELLED".equals(apt.getStatus())
                            || "AUTO_CANCELLED".equals(apt.getStatus()))
                            .collect(Collectors.toList());
                    break;
                case "expired":
                    baseList = appointments.stream()
                            .filter(apt -> {
                                boolean isPendingOrConfirmed
                                        = "PENDING".equals(apt.getStatus()) || "CONFIRMED".equals(apt.getStatus());
                                return isPendingOrConfirmed && appointmentService.isAppointmentExpired(apt);
                            })
                            .collect(Collectors.toList());
                    break;
                default:
                    baseList = new ArrayList<>(appointments);
            }
        }

        if (searchText.isEmpty()) {
            System.out.println("[APPOINTMENTS] No search, result size = " + baseList.size());
            return baseList;
        }

        List<Appointments> result = baseList.stream()
                .filter(apt -> {
                    String name = apt.getPatientName() != null
                            ? apt.getPatientName().toLowerCase() : "";
                    String email = apt.getPatientEmail() != null
                            ? apt.getPatientEmail().toLowerCase() : "";
                    String phone = "";
                    try {
                        if (apt.getUserId() != null) {
                            Users patient = usersService.findById(apt.getUserId());
                            if (patient != null && patient.getPhone() != null) {
                                phone = patient.getPhone().toLowerCase();
                            }
                        }
                    } catch (Exception ex) {
                        // ignore
                    }
                    String date = apt.getAppointmentDate() != null
                            ? apt.getAppointmentDate().toLowerCase() : "";
                    String time = apt.getAppointmentTime() != null
                            ? apt.getAppointmentTime().toLowerCase() : "";

                    return name.contains(searchText)
                            || email.contains(searchText)
                            || phone.contains(searchText)
                            || date.contains(searchText)
                            || time.contains(searchText);
                })
                .collect(Collectors.toList());

        System.out.println("[APPOINTMENTS] After search filter, result size = " + result.size());
        return result;
    }

    private Map<String, Long> calculateAppointmentCounts(List<Appointments> appointments) {
        Map<String, Long> counts = new HashMap<>();
        if (appointments == null) {
            appointments = new ArrayList<>();
        }

        counts.put("all", (long) appointments.size());

        long todayCount = appointments.stream()
                .filter(apt -> {
                    try {
                        if (apt.getAppointmentDate() == null) {
                            return false;
                        }
                        LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), DATE_FMT);
                        LocalDate today = LocalDate.now();
                        boolean isToday = aptDate.equals(today);
                        boolean isValidStatus = "CONFIRMED".equals(apt.getStatus());
                        return isToday && isValidStatus;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();
        counts.put("today", todayCount);

        counts.put("pending", appointments.stream()
                .filter(apt -> "PENDING".equals(apt.getStatus()))
                .count());

        counts.put("confirmed", appointments.stream()
                .filter(apt -> "CONFIRMED".equals(apt.getStatus()))
                .count());

        counts.put("completed", appointments.stream()
                .filter(apt -> "COMPLETED".equals(apt.getStatus()))
                .count());

        counts.put("cancelled", appointments.stream()
                .filter(apt -> "CANCELLED".equals(apt.getStatus())
                || "AUTO_CANCELLED".equals(apt.getStatus()))
                .count());

        long expiredCount = appointments.stream()
                .filter(apt -> {
                    boolean isConfirmed = "CONFIRMED".equals(apt.getStatus());
                    return isConfirmed && appointmentService.isAppointmentExpired(apt);
                })
                .count();
        counts.put("expired", expiredCount);

        System.out.println("[DEBUG] Calculated counts: " + counts);
        return counts;
    }

    // ==================== ACTIONS: CONFIRM / COMPLETE / CANCEL / EXPIRED ====================
    @PostMapping("/appointments/{id}/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, String>> confirmAppointment(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        try {
            Appointments appointment = appointmentService.getAppointmentById(id);
            if (appointment == null) {
                response.put("status", "ERROR");
                response.put("message", "Không tìm thấy lịch hẹn");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Chỉ cho phép confirm từ trạng thái PENDING
            if (!"PENDING".equals(appointment.getStatus())) {
                response.put("status", "ERROR");
                response.put("message", "Chỉ có thể xác nhận lịch hẹn đang ở trạng thái CHỜ XÁC NHẬN");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Không cho confirm nếu đã hết hạn
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
            Appointments appointment = appointmentService.getAppointmentById(id);
            if (appointment == null) {
                response.put("status", "ERROR");
                response.put("message", "Không tìm thấy lịch hẹn");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Chỉ cho phép hoàn thành từ trạng thái CONFIRMED
            if (!"CONFIRMED".equals(appointment.getStatus())) {
                response.put("status", "ERROR");
                response.put("message", "Chỉ có thể hoàn thành lịch hẹn đã được xác nhận");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            boolean success = appointmentService.completeAppointment(id);
            if (success) {
                response.put("status", "SUCCESS");
                response.put("message", "Đã hoàn thành lịch hẹn và có thể tạo hồ sơ bệnh án");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "ERROR");
                response.put("message", "Không thể hoàn thành lịch hẹn");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/appointments/{id}/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, String>> cancelAppointment(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean success = appointmentService.updateAppointmentStatus(id, "CANCELLED");
            if (success) {
                response.put("status", "SUCCESS");
                response.put("message", "Đã hủy lịch hẹn");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "ERROR");
                response.put("message", "Không thể hủy lịch hẹn");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Danh sách lịch expired
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

                    List<Appointments> allAppointments
                            = appointmentService.getAppointmentsByDoctorId(doctorId);
                    List<Appointments> expiredAppointments = getExpiredAppointments(allAppointments);

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

    // ==================== NO-SHOW ====================
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

    // ==================== XEM CHI TIẾT APPOINTMENT ====================
    @GetMapping("/appointments/{id}")
    public String viewAppointment(@PathVariable Long id, Model model) {
        try {
            String username = getCurrentUsername();
            Users currentUser = usersService.findByUsername(username);

            if (currentUser != null) {
                Optional<Doctors> doctorOpt = doctorService.getDoctorByUserId(currentUser.getUserId());
                doctorOpt.ifPresent(doctor -> model.addAttribute("doctor", doctor));
            }

            Appointments appointment = appointmentService.getAppointmentById(id);
            if (appointment == null) {
                return "redirect:/doctor/appointments?error=Appointment not found";
            }

            Users patient = usersService.findById(appointment.getUserId());

            appointmentService.enrichAppointmentDataPub(appointment);

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

    // ==================== MEDICAL RECORDS – LIST / SEARCH ====================
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
                        records = medicalRecordsService.searchMedicalRecords(
                                search.trim(), doctor.getDoctorId());
                    } else {
                        records = medicalRecordsService.getMedicalRecordsByDoctorId(doctor.getDoctorId());
                    }

                    model.addAttribute("records", records);
                    model.addAttribute("search", search);
                    model.addAttribute("doctor", doctor);

                    System.out.println("[DEBUG] Search term: " + search);
                    System.out.println("[DEBUG] Found records: " + records.size());
                    records.forEach(record -> {
                        if (record.getAppointment() != null) {
                            Users patient
                                    = usersService.findById(record.getAppointment().getUserId());
                            System.out.println("[DEBUG] Patient: "
                                    + (patient != null ? patient.getFullName() : "null"));
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
            List<MedicalRecords> allRecords
                    = medicalRecordsService.getMedicalRecordsByDoctorId(doctorId);

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
                                return usersService.findById(record.getAppointment().getUserId());
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
                List<Prescriptions> prescriptions
                        = medicalRecordsService.getPrescriptionsByRecordId(id);

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

    // ==================== MEDICAL RECORD – CREATE / EDIT ====================
    @GetMapping("/medical-record/create")
    public String createMedicalRecordForm(@RequestParam(required = false) Long appointmentId,
            Model model) {
        try {
            String username = getCurrentUsername();
            Users user = usersService.findByUsername(username);

            if (user != null) {
                Optional<Doctors> doctorOpt = doctorService.getDoctorByUserId(user.getUserId());
                doctorOpt.ifPresent(doctor -> model.addAttribute("doctor", doctor));
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
    public String saveMedicalRecord(
            @ModelAttribute MedicalRecords medicalRecord,
            @RequestParam(required = false) List<String> medicationNames,
            @RequestParam(required = false) List<String> dosages,
            @RequestParam(required = false) List<String> frequencies,
            @RequestParam(required = false) List<String> durations,
            @RequestParam(required = false) List<String> notes,
            RedirectAttributes ra) {

        try {
            if (medicalRecord.getCreatedAt() == null) {
                medicalRecord.setCreatedAt(new Date());
            }

            if (medicalRecord.getAppointment() == null
                    || medicalRecord.getAppointment().getAppointmentId() == null) {
                ra.addFlashAttribute("error", "Thiếu thông tin lịch hẹn.");
                return "redirect:/doctor/medical-record/create";
            }

            // Tạo danh sách đơn thuốc
            List<Prescriptions> prescriptions
                    = createPrescriptionsList(medicationNames, dosages, frequencies, durations, notes);

            // 1. Lưu hồ sơ + đơn thuốc
            MedicalRecords savedRecord
                    = medicalRecordsService.saveMedicalRecord(medicalRecord, prescriptions);

            Long appointmentId = savedRecord.getAppointment().getAppointmentId();

            // 2. Đánh dấu appointment = COMPLETED
            appointmentService.markCompleted(appointmentId);

            // 3. Tạo hóa đơn draft
            billingService.generateInvoiceForRecord(savedRecord.getRecordId());

            ra.addFlashAttribute("success", "Đã lưu hồ sơ, đơn thuốc và tạo hóa đơn nháp.");
            return "redirect:/doctor/medical-record/" + savedRecord.getRecordId();

        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi khi lưu hồ sơ: " + e.getMessage());
            return "redirect:/doctor/medical-record/create";
        }
    }

    @GetMapping("/medical-record/edit/{id}")
    public String editMedicalRecordForm(@PathVariable Long id, Model model) {
        MedicalRecords record = medicalRecordsService.getMedicalRecordById(id);
        if (record == null) {
            return "redirect:/doctor/medical-records";
        }

        Appointments appointment = appointmentService.getAppointmentById(record.getAppointmentId());
        Users patient = usersService.findById(appointment.getUserId());
        List<Prescriptions> prescriptions
                = medicalRecordsService.getPrescriptionsByRecordId(id);

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
        medicalRecordsService.updateMedicalRecord(medicalRecord);

        if (medicationNames != null) {
            updatePrescriptions(medicalRecord.getRecordId(), medicationNames, dosages, frequencies, durations, notes);
        }

        return "redirect:/doctor/medical-record/" + medicalRecord.getRecordId();
    }

    // ==================== MEDICAL RECORD – CHỌN APPOINTMENT ====================
    @GetMapping("/medical-record/select-appointment")
    public String selectAppointmentForMedicalRecord(Model model) {
        try {
            String username = getCurrentUsername();
            Users user = usersService.findByUsername(username);

            if (user != null) {
                Optional<Doctors> doctorOpt = doctorService.getDoctorByUserId(user.getUserId());
                if (doctorOpt.isPresent()) {
                    Doctors doctor = doctorOpt.get();

                    List<Appointments> eligibleAppointments
                            = appointmentService.getAppointmentsByDoctorId(doctor.getDoctorId())
                                    .stream()
                                    .filter(appointmentService::canCreateMedicalRecord)
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
    public String viewMedicalRecordByAppointment(@PathVariable Long appointmentId,
            Model model) {
        try {
            MedicalRecords record
                    = medicalRecordsService.getMedicalRecordByAppointmentId(appointmentId);
            if (record != null) {
                Appointments appointment = appointmentService.getAppointmentById(appointmentId);
                Users patient = usersService.findById(appointment.getUserId());
                List<Prescriptions> prescriptions
                        = medicalRecordsService.getPrescriptionsByRecordId(record.getRecordId());

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

    // ==================== PRESCRIPTION APIs ====================
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

    private List<Prescriptions> createPrescriptionsList(List<String> medicationNames,
            List<String> dosages,
            List<String> frequencies,
            List<String> durations,
            List<String> notes) {
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

    private void updatePrescriptions(Long recordId,
            List<String> medicationNames,
            List<String> dosages,
            List<String> frequencies,
            List<String> durations,
            List<String> notes) {
        medicalRecordsService.deletePrescriptionsByRecordId(recordId);

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

    // ==================== PDF REPORT (iText) ====================
    @GetMapping("/medical-record/{id}/print")
    public void printMedicalRecord(@PathVariable Long id, HttpServletResponse response) {
        System.out.println("=== YÊU CẦU IN HỒ SƠ (iText) ===");
        generatePdfReport(id, response, "inline");
    }

    @GetMapping("/medical-record/{id}/download")
    public void downloadMedicalRecord(@PathVariable Long id, HttpServletResponse response) {
        generatePdfReport(id, response, "attachment");
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
            List<Prescriptions> prescriptions
                    = medicalRecordsService.getPrescriptionsByRecordId(id);

            String username = getCurrentUsername();
            Users currentUser = usersService.findByUsername(username);

            byte[] pdfBytes = iTextReportService.generateMedicalRecordReport(
                    record, patient, currentUser, prescriptions);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    disposition + "; filename=medical-record-" + id + ".pdf");
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

    // ==================== SEARCH SUGGESTIONS ====================
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

                    List<MedicalRecords> records
                            = medicalRecordsService.getMedicalRecordsByDoctorId(doctor.getDoctorId());

                    Set<Long> processedPatientIds = new HashSet<>();

                    for (MedicalRecords record : records) {
                        if (record.getAppointment() != null) {
                            Users patient = usersService.findById(
                                    record.getAppointment().getUserId());
                            if (patient != null
                                    && !processedPatientIds.contains(patient.getUserId())) {

                                String lowerQuery = query.toLowerCase();
                                boolean matchByName = patient.getFullName() != null
                                        && patient.getFullName().toLowerCase().contains(lowerQuery);
                                boolean matchByPhone = patient.getPhone() != null
                                        && patient.getPhone().contains(query);

                                if (matchByName || matchByPhone) {
                                    processedPatientIds.add(patient.getUserId());
                                    Map<String, String> suggestion = new HashMap<>();
                                    suggestion.put("name", patient.getFullName());
                                    suggestion.put("phone", patient.getPhone() != null ? patient.getPhone() : "");
                                    suggestion.put("type", "Bệnh nhân");
                                    suggestion.put("value", patient.getFullName());
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

    // ==================== COMMON HELPERS ====================
    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    private String handleError(Model model, String page) {
        model.addAttribute("error", "Có lỗi xảy ra khi tải trang " + page);
        return "doctor/error";
    }

    private String getDoctorSpecialtyName(Doctors doctor) {
        if (doctor != null && doctor.getSpecialtyId() != null) {
            try {
                Specialties specialty
                        = specialtyService.getSpecialtyById(doctor.getSpecialtyId());
                return specialty != null ? specialty.getName() : "Đa khoa";
            } catch (Exception e) {
                return "Đa khoa";
            }
        }
        return "Đa khoa";
    }

}
