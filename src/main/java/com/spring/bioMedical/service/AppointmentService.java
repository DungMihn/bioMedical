package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.Appointments;
import com.spring.bioMedical.entity.AppointmentSlots;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.repository.AppointmentRepository;
import com.spring.bioMedical.repository.AppointmentSlotRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
    
     // Thêm phương thức này để xử lý confirm appointment
      // Phương thức confirm appointment - Đảm bảo đã có @Transactional
    public boolean confirmAppointment(Long appointmentId) {
        try {
            Appointments appointment = getAppointmentById(appointmentId);
            System.out.println("[DEBUG] Confirming appointment: " + appointmentId + ", current status: " + 
                (appointment != null ? appointment.getStatus() : "null"));
            
            if (appointment != null && "PENDING".equals(appointment.getStatus())) {
                // Kiểm tra xem slot còn available không
                AppointmentSlots slot = appointmentSlotService.getSlotById(appointment.getSlotId());
                System.out.println("[DEBUG] Slot status: " + (slot != null ? slot.getStatus() : "null"));
                
                if (slot != null && "AVAILABLE".equals(slot.getStatus())) {
                    // Cập nhật status appointment
                    appointment.setStatus("CONFIRMED");
                    appointmentRepository.save(appointment);
                    
                    // Cập nhật status slot thành BOOKED
                    slot.setStatus("BOOKED");
                    appointmentSlotRepository.save(slot); // Sử dụng repository trực tiếp
                    
                    System.out.println("[DEBUG] Appointment confirmed successfully");
                    return true;
                } else {
                    System.out.println("[DEBUG] Slot not available for confirmation");
                }
            } else {
                System.out.println("[DEBUG] Appointment not found or not PENDING status");
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error confirming appointment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Sửa phương thức updateAppointmentStatus
    public boolean updateAppointmentStatus(Long appointmentId, String status) {
        try {
            Appointments appointment = getAppointmentById(appointmentId);
            System.out.println("[DEBUG] Updating appointment " + appointmentId + " to status: " + status);
            
            if (appointment != null) {
                // Kiểm tra chuyển đổi trạng thái hợp lệ
                if (!isValidStatusTransition(appointment.getStatus(), status)) {
                    System.out.println("[DEBUG] Invalid status transition from " + appointment.getStatus() + " to " + status);
                    return false;
                }
                
                String oldStatus = appointment.getStatus();
                appointment.setStatus(status);
                appointmentRepository.save(appointment);

                // Cập nhật slot status nếu cần
                updateSlotStatus(appointment.getSlotId(), status, oldStatus);
                
                System.out.println("[DEBUG] Appointment status updated successfully");
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error updating appointment status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Sửa phương thức updateSlotStatus
    private void updateSlotStatus(Long slotId, String appointmentStatus, String oldAppointmentStatus) {
        try {
            AppointmentSlots slot = appointmentSlotRepository.findById(slotId).orElse(null);
            if (slot != null) {
                if ("CANCELLED".equals(appointmentStatus) && "CONFIRMED".equals(oldAppointmentStatus)) {
                    // Chỉ set về AVAILABLE nếu cancel từ CONFIRMED
                    slot.setStatus("AVAILABLE");
                    appointmentSlotRepository.save(slot);
                    System.out.println("[DEBUG] Slot " + slotId + " set to AVAILABLE");
                } else if ("CONFIRMED".equals(appointmentStatus) && "PENDING".equals(oldAppointmentStatus)) {
                    // Set to BOOKED khi confirm từ PENDING
                    slot.setStatus("BOOKED");
                    appointmentSlotRepository.save(slot);
                    System.out.println("[DEBUG] Slot " + slotId + " set to BOOKED");
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating slot status: " + e.getMessage());
        }
    }

   private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        // Logic validation cho status transition
        if ("COMPLETED".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
            return false; // Không thể thay đổi từ COMPLETED hoặc CANCELLED
        }
        
        if ("PENDING".equals(currentStatus) && ("CONFIRMED".equals(newStatus) || "CANCELLED".equals(newStatus))) {
            return true;
        }
        
        if ("CONFIRMED".equals(currentStatus) && ("COMPLETED".equals(newStatus) || "CANCELLED".equals(newStatus))) {
            return true;
        }
        
        return false;
    }



    public List<Appointments> getAppointmentsByDoctorId(Long doctorId) {
        try {
            List<Appointments> appointments = appointmentRepository.findByDoctorId(doctorId);
            appointments.forEach(this::enrichAppointmentData);
            appointments.sort((a1, a2) -> {
                // Sort by date and time descending
                return compareAppointments(a2, a1);
            });
            return appointments;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<Appointments> getTodayAppointmentsByDoctorId(Long doctorId) {
        try {
            List<Appointments> appointments = appointmentRepository.findTodayAppointmentsByDoctorId(doctorId);
            appointments.forEach(this::enrichAppointmentData);

            // Sort by time ascending for today's appointments
            appointments.sort(this::compareAppointmentsByTime);
            return appointments;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<Appointments> getPendingAppointmentsByDoctorId(Long doctorId) {
        try {
            List<Appointments> appointments = appointmentRepository.findPendingAppointmentsByDoctorId(doctorId);
            appointments.forEach(this::enrichAppointmentData);
            return appointments;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // Helper method để so sánh appointments cho sorting
    private int compareAppointments(Appointments a1, Appointments a2) {
        try {
            // So sánh theo date trước, rồi đến time
            LocalDate date1 = LocalDate.parse(a1.getAppointmentDate());
            LocalDate date2 = LocalDate.parse(a2.getAppointmentDate());

            int dateCompare = date2.compareTo(date1); // Descending
            if (dateCompare != 0) {
                return dateCompare;
            }

            // Nếu cùng ngày, so sánh giờ
            LocalTime time1 = LocalTime.parse(a1.getAppointmentTime());
            LocalTime time2 = LocalTime.parse(a2.getAppointmentTime());
            return time2.compareTo(time1); // Descending
        } catch (Exception e) {
            return 0;
        }
    }

    // Helper method để sort today's appointments by time ascending
    private int compareAppointmentsByTime(Appointments a1, Appointments a2) {
        try {
            LocalTime time1 = LocalTime.parse(a1.getAppointmentTime());
            LocalTime time2 = LocalTime.parse(a2.getAppointmentTime());
            return time1.compareTo(time2); // Ascending
        } catch (Exception e) {
            return 0;
        }
    }

    // Phương thức enrichAppointmentData giữ nguyên
    private void enrichAppointmentData(Appointments appointment) {
        if (appointment == null) {
            return;
        }

        try {
            // Lấy thông tin slot
            AppointmentSlots slot = appointmentSlotService.getSlotById(appointment.getSlotId());
            if (slot != null) {
                appointment.setAppointmentDate(slot.getSlotDate() != null ? slot.getSlotDate().toString() : "N/A");
                appointment.setAppointmentTime(slot.getSlotTime() != null ? slot.getSlotTime().toString() : "N/A");
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

    public Appointments getAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId).orElse(null);
    }
    
    public Appointments saveAppointment(Appointments appointment) {
        return appointmentRepository.save(appointment);
    }
    
    
    private void updateSlotStatus(Long slotId, String appointmentStatus) {
        try {
            AppointmentSlots slot = appointmentSlotRepository.findById(slotId).orElse(null);
            if (slot != null) {
                if ("CANCELLED".equals(appointmentStatus) && !"COMPLETED".equals(slot.getStatus())) {
                    slot.setStatus("AVAILABLE");
                    appointmentSlotRepository.save(slot);
                } else if ("CONFIRMED".equals(appointmentStatus)) {
                    slot.setStatus("BOOKED");
                    appointmentSlotRepository.save(slot);
                }
            }
        } catch (Exception e) {
               System.out.println("Error updating slot status: {}"+ e.getMessage());
        }
    }
    
    
    
      // ✅ Sửa lại phương thức autoCancelExpiredAppointments để dùng repository method mới
    @Scheduled(cron = "0 0 2 * * ?") // Chạy mỗi ngày lúc 2:00 AM
    @Transactional
    public void autoCancelExpiredAppointments() {
        try {
            // Lấy ngày hôm qua
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            Date yesterday = cal.getTime();
            
            System.out.println("Auto-cancelling expired appointments before: {}"+ yesterday);
            
            // Sử dụng phương thức repository mới
            List<Appointments> expiredAppointments = appointmentRepository
                .findBySlot_SlotDateBeforeAndStatus(yesterday, "PENDING");
            
            System.out.println("Found {} expired appointments to cancel"+ expiredAppointments.size());
            
            for (Appointments appointment : expiredAppointments) {
                appointment.setStatus("CANCELLED");
                updateSlotStatus(appointment.getSlotId(), "CANCELLED");
                System.out.println("Cancelled appointment ID: {}"+ appointment.getAppointmentId());
            }
            
            appointmentRepository.saveAll(expiredAppointments);
            System.out.println("Successfully auto-cancelled {} expired appointments"+ expiredAppointments.size());
            
        } catch (Exception e) {
            System.out.println("Error auto-cancelling expired appointments: {}"+ e.getMessage()+ e);
        }
    }
    
    // ✅ THÊM MỚI: Lấy appointments sắp tới
    public List<Appointments> getUpcomingAppointments() {
        return appointmentRepository.findUpcomingAppointments();
    }
    
    // ✅ THÊM MỚI: Lấy appointments theo patient
    public List<Appointments> getAppointmentsByPatient(Long userId) {
        return appointmentRepository.findByUserId(userId);
    }
    
    // ✅ THÊM MỚI: Thống kê appointments theo status
    public Map<String, Long> getAppointmentStats() {
        Map<String, Long> stats = new HashMap<>();
        String[] statuses = {"PENDING", "CONFIRMED", "COMPLETED", "CANCELLED"};
        
        for (String status : statuses) {
            Long count = appointmentRepository.countByStatus(status);
            stats.put(status, count != null ? count : 0L);
        }
        
        return stats;
    }
    
    // ✅ THÊM MỚI: Tìm appointments theo khoảng thời gian
    public List<Appointments> getAppointmentsByDateRange(Date startDate, Date endDate, String status) {
        return appointmentRepository.findBySlotDateBetweenAndStatus(startDate, endDate, status);
    }
    
    

}
