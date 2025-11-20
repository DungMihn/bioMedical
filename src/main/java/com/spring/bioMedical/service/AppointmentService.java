package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.Appointments;
import com.spring.bioMedical.entity.AppointmentSlots;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.repository.AppointmentRepository;
import com.spring.bioMedical.repository.AppointmentSlotRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentSlotService appointmentSlotService;

    @Autowired
    private UsersService userService;

    @Autowired
    private AppointmentSlotRepository appointmentSlotRepository;

    // ==================== STATUS MANAGEMENT ====================
    // Định nghĩa các trạng thái hợp lệ
    public enum AppointmentStatus {
        PENDING, // Đang chờ xác nhận
        CONFIRMED, // Đã xác nhận
        COMPLETED, // Đã hoàn thành (sau khi khám và có hồ sơ bệnh án)
        CANCELLED, // Đã hủy (chủ động)
        AUTO_CANCELLED, // Tự động hủy (quá hạn)
        NO_SHOW, // Bệnh nhân không đến
        EXPIRED        // Quá hạn (cho PENDING)
    }

    // ==================== CORE BUSINESS LOGIC ====================
    /**
     * Xác nhận appointment - với validation mạnh mẽ
     */
    // Trong AppointmentService - thêm method này
    public List<AppointmentSlots> getSlotsByDoctorId(Long doctorId) {
        try {
            return appointmentSlotRepository.findByDoctorId(doctorId);
        } catch (Exception e) {
            System.err.println("❌ Error getting slots by doctor ID: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Transactional
    public boolean confirmAppointment(Long appointmentId) {
        try {
            Appointments appointment = getAppointmentById(appointmentId);
            System.out.println("[DEBUG] Confirming appointment: " + appointmentId + ", current status: "
                    + (appointment != null ? appointment.getStatus() : "null"));

            if (appointment == null) {
                System.out.println("[DEBUG] Appointment not found");
                return false;
            }

            // Kiểm tra trạng thái hiện tại
            if (!AppointmentStatus.PENDING.name().equals(appointment.getStatus())) {
                System.out.println("[DEBUG] Appointment is not PENDING, current status: " + appointment.getStatus());
                return false;
            }

            // Kiểm tra xem appointment có quá hạn không
            if (isAppointmentExpired(appointment)) {
                System.out.println("[DEBUG] Cannot confirm expired appointment");
                appointment.setStatus(AppointmentStatus.EXPIRED.name());
                appointmentRepository.save(appointment);
                return false;
            }

            // Kiểm tra slot availability
            AppointmentSlots slot = appointmentSlotService.getSlotById(appointment.getSlotId());
            if (slot == null) {
                System.out.println("[DEBUG] Slot not found");
                return false;
            }

            if (!"AVAILABLE".equals(slot.getStatus())) {
                System.out.println("[DEBUG] Slot is not available, current status: " + slot.getStatus());
                return false;
            }

            // Thực hiện confirm
            appointment.setStatus(AppointmentStatus.CONFIRMED.name());
            appointmentRepository.save(appointment);

            // Cập nhật slot status
            slot.setStatus("BOOKED");
            appointmentSlotRepository.save(slot);

            System.out.println("[DEBUG] Appointment confirmed successfully");
            return true;

        } catch (Exception e) {
            System.err.println("Error confirming appointment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Trong AppointmentService - thêm method test
    public void testRepositoryMethods(Long doctorId) {
        try {
            System.out.println("=== TESTING REPOSITORY METHODS ===");

            // Test method chính
            List<Appointments> appointments = appointmentRepository.findByDoctorId(doctorId);
            System.out.println("findByDoctorId: " + appointments.size() + " appointments");

            // Test native query
            List<Appointments> todayApps = appointmentRepository.findTodayAppointmentsByDoctorId(doctorId);
            System.out.println("findTodayAppointmentsByDoctorId: " + todayApps.size() + " appointments");

            // Test pending
            List<Appointments> pendingApps = appointmentRepository.findPendingAppointmentsByDoctorId(doctorId);
            System.out.println("findPendingAppointmentsByDoctorId: " + pendingApps.size() + " appointments");

            System.out.println("=== END TEST ===");

        } catch (Exception e) {
            System.err.println("Repository test error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hoàn thành appointment - chỉ khi đã CONFIRMED và có thể tạo hồ sơ bệnh án
     */
    @Transactional
    public boolean completeAppointment(Long appointmentId) {
        try {
            Appointments appointment = getAppointmentById(appointmentId);
            System.out.println("[DEBUG] Completing appointment: " + appointmentId + ", current status: "
                    + (appointment != null ? appointment.getStatus() : "null"));

            if (appointment == null) {
                return false;
            }

            // Chỉ có thể complete từ trạng thái CONFIRMED
            if (!AppointmentStatus.CONFIRMED.name().equals(appointment.getStatus())) {
                System.out.println("[DEBUG] Cannot complete appointment with status: " + appointment.getStatus());
                return false;
            }

            // Kiểm tra xem appointment đã diễn ra chưa (cho phép complete ngay cả khi quá giờ)
            AppointmentSlots slot = appointmentSlotService.getSlotById(appointment.getSlotId());
            if (slot != null) {
                LocalDateTime appointmentDateTime = LocalDateTime.of(slot.getSlotDate(), slot.getSlotTime());
                LocalDateTime now = LocalDateTime.now();

                // Cho phép complete nếu đã qua thời gian hẹn (bác sĩ có thể complete sau khi khám)
                if (appointmentDateTime.isAfter(now)) {
                    System.out.println("[DEBUG] Appointment time hasn't arrived yet");
                    // Vẫn cho phép complete sớm nếu cần, hoặc return false nếu muốn nghiêm ngặt
                }
            }

            appointment.setStatus(AppointmentStatus.COMPLETED.name());
            appointment.setNotes("Hoàn thành: Đã khám và tạo hồ sơ bệnh án.");
            appointmentRepository.save(appointment);

            System.out.println("[DEBUG] Appointment completed successfully");
            return true;

        } catch (Exception e) {
            System.err.println("Error completing appointment: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật trạng thái appointment với validation đầy đủ
     */
    @Transactional
    public boolean updateAppointmentStatus(Long appointmentId, String status) {
        try {
            Appointments appointment = getAppointmentById(appointmentId);
            System.out.println("[DEBUG] Updating appointment " + appointmentId + " from "
                    + (appointment != null ? appointment.getStatus() : "null") + " to: " + status);

            if (appointment == null) {
                return false;
            }

            String currentStatus = appointment.getStatus();
            String newStatus = status.toUpperCase();

            // Validation chuyển đổi trạng thái
            if (!isValidStatusTransition(currentStatus, newStatus)) {
                System.out.println("[DEBUG] Invalid status transition from " + currentStatus + " to " + newStatus);
                return false;
            }

            // Xử lý đặc biệt cho trường hợp confirm appointment quá hạn
            if (AppointmentStatus.CONFIRMED.name().equals(newStatus) && isAppointmentExpired(appointment)) {
                System.out.println("[DEBUG] Cannot confirm expired appointment");
                return false;
            }

            String oldStatus = appointment.getStatus();
            appointment.setStatus(newStatus);
            appointmentRepository.save(appointment);

            // Cập nhật slot status nếu cần
            updateSlotStatus(appointment.getSlotId(), newStatus, oldStatus);

            System.out.println("[DEBUG] Appointment status updated successfully");
            return true;

        } catch (Exception e) {
            System.err.println("Error updating appointment status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ==================== EXPIRATION MANAGEMENT ====================
    /**
     * Kiểm tra appointment đã expired chưa
     */
    public boolean isAppointmentExpired(Appointments appointment) {
        try {
            if (appointment == null) {
                return false;
            }

            AppointmentSlots slot = appointmentSlotService.getSlotById(appointment.getSlotId());
            if (slot == null) {
                return false;
            }

            LocalDate appointmentDate = slot.getSlotDate();
            LocalTime appointmentTime = slot.getSlotTime();

            if (appointmentDate == null || appointmentTime == null) {
                return false;
            }

            LocalDateTime appointmentDateTime = LocalDateTime.of(appointmentDate, appointmentTime);
            return appointmentDateTime.isBefore(LocalDateTime.now());

        } catch (Exception e) {
            System.err.println("Error checking appointment expiration: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tự động quản lý appointments - chạy định kỳ
     */
    @Scheduled(fixedRate = 300000) // 5 phút
    @Transactional
    public void autoManageAppointments() {
        try {
            System.out.println("[AUTO-MANAGER] Starting auto appointment management...");

            List<Appointments> allAppointments = appointmentRepository.findAll();
            int autoCancelled = 0;
            int markedExpired = 0;
            int markedNoShow = 0;

            for (Appointments appointment : allAppointments) {
                // PENDING -> EXPIRED nếu quá thời gian hẹn
                if (AppointmentStatus.PENDING.name().equals(appointment.getStatus())
                        && isAppointmentExpired(appointment)) {
                    appointment.setStatus(AppointmentStatus.EXPIRED.name());
                    appointment.setNotes("Tự động đánh dấu: Quá hạn xác nhận");
                    appointmentRepository.save(appointment);
                    updateSlotStatusForExpired(appointment.getSlotId());
                    markedExpired++;
                } // CONFIRMED -> NO_SHOW nếu quá 1 giờ so với thời gian hẹn
                else if (AppointmentStatus.CONFIRMED.name().equals(appointment.getStatus())
                        && shouldMarkAsNoShow(appointment)) {
                    appointment.setStatus(AppointmentStatus.NO_SHOW.name());
                    appointment.setNotes("Tự động đánh dấu: Bệnh nhân không đến khám");
                    appointmentRepository.save(appointment);
                    markedNoShow++;
                }
            }

            if (markedExpired > 0 || markedNoShow > 0) {
                System.out.println("[AUTO-MANAGER] Completed - Marked expired: " + markedExpired
                        + ", Marked no-show: " + markedNoShow);
            }

        } catch (Exception e) {
            System.err.println("[AUTO-MANAGER] Error: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra appointment CONFIRMED có nên đánh dấu NO_SHOW
     */
    private boolean shouldMarkAsNoShow(Appointments appointment) {
        if (!AppointmentStatus.CONFIRMED.name().equals(appointment.getStatus())) {
            return false;
        }

        try {
            AppointmentSlots slot = appointmentSlotService.getSlotById(appointment.getSlotId());
            if (slot == null) {
                return false;
            }

            LocalDateTime appointmentDateTime = LocalDateTime.of(slot.getSlotDate(), slot.getSlotTime());
            LocalDateTime now = LocalDateTime.now();

            // Đánh dấu NO_SHOW nếu quá 1 giờ so với thời gian hẹn
            return appointmentDateTime.isBefore(now.minusHours(1));

        } catch (Exception e) {
            return false;
        }
    }

    // ==================== VALIDATION LOGIC ====================
    /**
     * Kiểm tra chuyển đổi trạng thái hợp lệ
     */
    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        String current = currentStatus.toUpperCase();
        String newStat = newStatus.toUpperCase();

        // Không thể thay đổi từ các trạng thái cuối
        if (Arrays.asList("COMPLETED", "CANCELLED", "NO_SHOW", "AUTO_CANCELLED", "EXPIRED").contains(current)) {
            return false;
        }

        Map<String, List<String>> validTransitions = new HashMap<>();
        validTransitions.put("PENDING", Arrays.asList("CONFIRMED", "CANCELLED"));
        validTransitions.put("CONFIRMED", Arrays.asList("COMPLETED", "CANCELLED", "NO_SHOW"));
        validTransitions.put("EXPIRED", Arrays.asList()); // Không thể chuyển từ EXPIRED

        List<String> allowed = validTransitions.get(current);
        return allowed != null && allowed.contains(newStat);
    }

    // ==================== SLOT MANAGEMENT ====================
    /**
     * Cập nhật slot status khi appointment thay đổi
     */
    private void updateSlotStatus(Long slotId, String appointmentStatus, String oldAppointmentStatus) {
        try {
            AppointmentSlots slot = appointmentSlotRepository.findById(slotId).orElse(null);
            if (slot != null) {
                if ("CANCELLED".equals(appointmentStatus) && "CONFIRMED".equals(oldAppointmentStatus)) {
                    slot.setStatus("AVAILABLE");
                    appointmentSlotRepository.save(slot);
                    System.out.println("[DEBUG] Slot " + slotId + " set to AVAILABLE due to cancellation");
                } else if ("CONFIRMED".equals(appointmentStatus) && "PENDING".equals(oldAppointmentStatus)) {
                    slot.setStatus("BOOKED");
                    appointmentSlotRepository.save(slot);
                    System.out.println("[DEBUG] Slot " + slotId + " set to BOOKED");
                } else if ("COMPLETED".equals(appointmentStatus)) {
                    // Có thể giữ nguyên BOOKED hoặc chuyển sang DONE tùy requirement
                    // slot.setStatus("DONE");
                    System.out.println("[DEBUG] Appointment completed, slot remains BOOKED");
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating slot status: " + e.getMessage());
        }
    }

    private void updateSlotStatusForExpired(Long slotId) {
        try {
            AppointmentSlots slot = appointmentSlotRepository.findById(slotId).orElse(null);
            if (slot != null && "BOOKED".equals(slot.getStatus())) {
                slot.setStatus("AVAILABLE");
                appointmentSlotRepository.save(slot);
                System.out.println("[DEBUG] Slot " + slotId + " set to AVAILABLE due to expiration");
            }
        } catch (Exception e) {
            System.err.println("Error updating slot status for expired appointment: " + e.getMessage());
        }
    }

    // ==================== QUERY METHODS ====================
    /**
     * Lấy appointments với trạng thái expired được tính toán động
     */
    public List<Appointments> getAppointmentsByDoctorId(Long doctorId) {
        try {
            List<Appointments> appointments = appointmentRepository.findByDoctorId(doctorId);

            // Tính toán trạng thái expired cho mỗi appointment
            appointments.forEach(apt -> {
                if (("PENDING".equals(apt.getStatus()) || "CONFIRMED".equals(apt.getStatus()))
                        && isAppointmentExpired(apt)) {
                    apt.setStatus("EXPIRED");
                }
            });

            appointments.forEach(this::enrichAppointmentData);
            appointments.sort((a1, a2) -> compareAppointments(a2, a1));
            return appointments;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Lấy appointments hôm nay với expired check
     */
    public List<Appointments> getTodayAppointmentsByDoctorId(Long doctorId) {
        try {
            List<Appointments> appointments = appointmentRepository.findTodayAppointmentsByDoctorId(doctorId);

            // Đánh dấu expired appointments
            appointments.forEach(apt -> {
                if (("PENDING".equals(apt.getStatus()) || "CONFIRMED".equals(apt.getStatus()))
                        && isAppointmentExpired(apt)) {
                    apt.setStatus("EXPIRED");
                }
            });

            appointments.forEach(this::enrichAppointmentData);
            appointments.sort(this::compareAppointmentsByTime);
            return appointments;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

// Trong AppointmentService - sửa method enrichAppointmentData
    private void enrichAppointmentData(Appointments appointment) {
        if (appointment == null) {
            return;
        }

        try {
            // Lấy thông tin slot từ repository
            AppointmentSlots slot = appointmentSlotRepository.findById(appointment.getSlotId()).orElse(null);
            if (slot != null) {
                // Format date và time từ slot thực tế
                if (slot.getSlotDate() != null) {
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    appointment.setAppointmentDate(slot.getSlotDate().format(dateFormatter));
                }
                if (slot.getSlotTime() != null) {
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    appointment.setAppointmentTime(slot.getSlotTime().format(timeFormatter));
                }
            }

            // Lấy thông tin patient
            Users patient = userService.findById(appointment.getUserId());
            if (patient != null) {
                appointment.setPatientName(patient.getFullName() != null ? patient.getFullName() : "Bệnh nhân");
                appointment.setPatientEmail(patient.getEmail() != null ? patient.getEmail() : "Không có email");
            } else {
                appointment.setPatientName("Bệnh nhân ẩn danh");
                appointment.setPatientEmail("Không có email");
            }

        } catch (Exception e) {
            System.err.println("❌ Error enriching appointment " + appointment.getAppointmentId() + ": " + e.getMessage());
        }
    }

    // Các methods utility khác giữ nguyên...
    private int compareAppointmentsByTime(Appointments a1, Appointments a2) {
        try {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime time1 = LocalTime.parse(a1.getAppointmentTime(), timeFormatter);
            LocalTime time2 = LocalTime.parse(a2.getAppointmentTime(), timeFormatter);
            return time1.compareTo(time2);
        } catch (Exception e) {
            return 0;
        }
    }

    private int compareAppointments(Appointments a1, Appointments a2) {
        try {
            LocalDate date1 = LocalDate.parse(a1.getAppointmentDate());
            LocalDate date2 = LocalDate.parse(a2.getAppointmentDate());
            int dateCompare = date2.compareTo(date1);
            if (dateCompare != 0) {
                return dateCompare;
            }

            LocalTime time1 = LocalTime.parse(a1.getAppointmentTime());
            LocalTime time2 = LocalTime.parse(a2.getAppointmentTime());
            return time2.compareTo(time1);
        } catch (Exception e) {
            return 0;
        }
    }

    // Các methods repository delegate giữ nguyên...
    public List<Appointments> getPendingAppointmentsByDoctorId(Long doctorId) {
        try {
            List<Appointments> appointments = appointmentRepository.findPendingAppointmentsByDoctorId(doctorId);
            appointments.forEach(this::enrichAppointmentData);
            return appointments;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<Appointments> getConfirmedAppointmentsByDoctorId(Long doctorId) {
        List<Appointments> appointments = appointmentRepository.findByDoctorIdAndStatus(doctorId, "CONFIRMED");
        appointments.forEach(this::enrichAppointmentData);
        return appointments;
    }

    public List<Appointments> getCompletedAppointmentsByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorIdAndStatus(doctorId, "COMPLETED");
    }

    public Appointments getAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId).orElse(null);
    }

    @Transactional
    public boolean markAsNoShow(Long appointmentId) {
        try {
            Appointments appointment = getAppointmentById(appointmentId);
            if (appointment != null && "CONFIRMED".equals(appointment.getStatus())) {
                appointment.setStatus("NO_SHOW");
                appointment.setNotes("Bệnh nhân không đến khám");
                appointmentRepository.save(appointment);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Appointments> getUpcomingAppointmentsForNotifications(Long doctorId) {
        try {
            List<Appointments> allAppointments = appointmentRepository.findByDoctorId(doctorId);
            LocalDateTime now = LocalDateTime.now();

            return allAppointments.stream()
                    .filter(apt -> "CONFIRMED".equals(apt.getStatus()))
                    .filter(apt -> {
                        try {
                            AppointmentSlots slot = appointmentSlotService.getSlotById(apt.getSlotId());
                            if (slot == null) {
                                return false;
                            }

                            LocalDateTime aptDateTime = LocalDateTime.of(slot.getSlotDate(), slot.getSlotTime());
                            // Lấy appointments trong vòng 2 giờ tới
                            return aptDateTime.isAfter(now)
                                    && aptDateTime.isBefore(now.plusHours(2));

                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .sorted((a1, a2) -> {
                        try {
                            AppointmentSlots slot1 = appointmentSlotService.getSlotById(a1.getSlotId());
                            AppointmentSlots slot2 = appointmentSlotService.getSlotById(a2.getSlotId());
                            LocalDateTime dt1 = LocalDateTime.of(slot1.getSlotDate(), slot1.getSlotTime());
                            LocalDateTime dt2 = LocalDateTime.of(slot2.getSlotDate(), slot2.getSlotTime());
                            return dt1.compareTo(dt2);
                        } catch (Exception e) {
                            return 0;
                        }
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public Map<String, Object> getAppointmentStats(Long doctorId) {
        Map<String, Object> stats = new HashMap<>();

        try {
            List<Appointments> allAppointments = appointmentRepository.findByDoctorId(doctorId);
            LocalDate today = LocalDate.now();

            // Today's appointments
            long todayCount = allAppointments.stream()
                    .filter(apt -> {
                        try {
                            if (apt.getAppointmentDate() == null) {
                                return false;
                            }
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            LocalDate aptDate = LocalDate.parse(apt.getAppointmentDate(), formatter);
                            return aptDate.equals(today)
                                    && ("CONFIRMED".equals(apt.getStatus()) || "PENDING".equals(apt.getStatus()));
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .count();

            // Upcoming appointments (next 7 days)
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

            stats.put("todayCount", todayCount);
            stats.put("upcomingCount", upcomingCount);
            stats.put("recentAutoActions", recentAutoActions);
            stats.put("totalAppointments", allAppointments.size());

        } catch (Exception e) {
            stats.put("todayCount", 0L);
            stats.put("upcomingCount", 0L);
            stats.put("recentAutoActions", 0L);
            stats.put("totalAppointments", 0L);
        }

        return stats;
    }

    // Thêm các methods này vào AppointmentService
    /**
     * Kiểm tra có thể tạo hồ sơ bệnh án không
     */
    /**
     * Lấy class CSS cho time status
     */
    public String getTimeStatusClass(Appointments appointment) {
        if (appointment == null) {
            return "time-past";
        }

        try {
            LocalDateTime aptDateTime = getAppointmentDateTime(appointment);
            if (aptDateTime == null) {
                return "time-past";
            }

            LocalDateTime now = LocalDateTime.now();

            if (aptDateTime.isBefore(now)) {
                return "time-past";
            } else if (aptDateTime.toLocalDate().equals(now.toLocalDate())) {
                return "time-today";
            } else {
                return "time-upcoming";
            }
        } catch (Exception e) {
            return "time-past";
        }
    }

    /**
     * Lấy text cho time status
     */
    public String getTimeStatusText(Appointments appointment) {
        if (appointment == null) {
            return "ĐÃ QUA";
        }

        try {
            LocalDateTime aptDateTime = getAppointmentDateTime(appointment);
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
                return daysBetween + " NGÀY";
            }
        } catch (Exception e) {
            return "ĐÃ QUA";
        }
    }

    /**
     * Lấy LocalDateTime từ appointment
     */
    private LocalDateTime getAppointmentDateTime(Appointments appointment) {
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

    /**
     * Làm giàu dữ liệu appointment với thông tin bổ sung
     */
    public void enrichAppointmentDataPub(Appointments appointment) {
        if (appointment == null) {
            return;
        }

        try {
            // Lấy thông tin slot
            AppointmentSlots slot = appointmentSlotService.getSlotById(appointment.getSlotId());
            if (slot != null) {
                // Format LocalDate và LocalTime đúng cách
                if (slot.getSlotDate() != null) {
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    appointment.setAppointmentDate(slot.getSlotDate().format(dateFormatter));
                } else {
                    appointment.setAppointmentDate("N/A");
                }

                if (slot.getSlotTime() != null) {
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    appointment.setAppointmentTime(slot.getSlotTime().format(timeFormatter));
                } else {
                    appointment.setAppointmentTime("N/A");
                }
            }
        } catch (Exception e) {
            appointment.setAppointmentDate("N/A");
            appointment.setAppointmentTime("N/A");
        }

        try {
            // Lấy thông tin patient
            Users patient = userService.findById(appointment.getUserId());
            if (patient != null) {
                appointment.setPatientName(patient.getFullName() != null ? patient.getFullName() : "Unknown Patient");
                appointment.setPatientEmail(patient.getEmail() != null ? patient.getEmail() : "No email");
            } else {
                appointment.setPatientName("Unknown Patient");
                appointment.setPatientEmail("No email");
            }
        } catch (Exception e) {
            appointment.setPatientName("Unknown Patient");
            appointment.setPatientEmail("No email");
        }
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

    //LONG
    /**
     * LẤY APPOINTMENT THEO ID
     *
     * @param id
     * @return
     */
    public Optional<Appointments> findById(Long id) {
        return appointmentRepository.findById(id); // JpaRepository đã có sẵn
    }

    @Transactional
    public void save(Appointments app) {
        appointmentRepository.save(app);
    }

}
