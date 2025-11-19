package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.Appointments;
import com.spring.bioMedical.entity.AppointmentSlots;
import com.spring.bioMedical.repository.AppointmentRepository;
import com.spring.bioMedical.repository.AppointmentSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class AppointmentStatusService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentSlotRepository appointmentSlotRepository;

    @Autowired
    private AppointmentSlotService appointmentSlotService;

    /**
     * Tự động xử lý các lịch hẹn quá hạn
     * Chạy mỗi 5 phút
     */
    @Scheduled(fixedRate = 300000) // 5 phút
    public void autoProcessExpiredAppointments() {
        try {
            System.out.println("[AUTO-PROCESS] Starting auto appointment processing...");
            
            List<Appointments> allAppointments = appointmentRepository.findAll();
            int autoCancelled = 0;
            int markedNoShow = 0;
            
            for (Appointments appointment : allAppointments) {
                // Chỉ xử lý các appointment PENDING hoặc CONFIRMED
                if ("PENDING".equals(appointment.getStatus()) || "CONFIRMED".equals(appointment.getStatus())) {
                    
                    AppointmentSlots slot = appointmentSlotService.getSlotById(appointment.getSlotId());
                    if (slot == null) continue;
                    
                    LocalDateTime appointmentDateTime = LocalDateTime.of(slot.getSlotDate(), slot.getSlotTime());
                    LocalDateTime now = LocalDateTime.now();
                    
                    // PENDING appointments: tự động hủy nếu quá 1 giờ so với thời gian hẹn
                    if ("PENDING".equals(appointment.getStatus()) && appointmentDateTime.isBefore(now.minusHours(1))) {
                        appointment.setStatus("AUTO_CANCELLED");
                        appointment.setNotes("Tự động hủy: Quá hạn xác nhận");
                        appointmentRepository.save(appointment);
                        updateSlotStatusForCancellation(slot);
                        autoCancelled++;
                    }
                    // CONFIRMED appointments: đánh dấu NO_SHOW nếu quá 2 giờ so với thời gian hẹn
                    else if ("CONFIRMED".equals(appointment.getStatus()) && appointmentDateTime.isBefore(now.minusHours(2))) {
                        appointment.setStatus("NO_SHOW");
                        appointment.setNotes("Tự động đánh dấu: Bệnh nhân không đến khám");
                        appointmentRepository.save(appointment);
                        markedNoShow++;
                    }
                }
            }
            
            if (autoCancelled > 0 || markedNoShow > 0) {
                System.out.println("[AUTO-PROCESS] Completed - Auto cancelled: " + autoCancelled + 
                                 ", Marked no-show: " + markedNoShow);
            }
            
        } catch (Exception e) {
            System.err.println("[AUTO-PROCESS] Error: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra xem appointment có thể được hoàn thành không
     */
    public boolean canCompleteAppointment(Long appointmentId) {
        try {
            Appointments appointment = appointmentRepository.findById(appointmentId).orElse(null);
            if (appointment == null) return false;

            // Chỉ có thể hoàn thành lịch hẹn đã được xác nhận
            if (!"CONFIRMED".equals(appointment.getStatus())) {
                return false;
            }

            AppointmentSlots slot = appointmentSlotService.getSlotById(appointment.getSlotId());
            if (slot == null) return false;

            LocalDateTime appointmentDateTime = LocalDateTime.of(slot.getSlotDate(), slot.getSlotTime());
            LocalDateTime now = LocalDateTime.now();
            
            // Cho phép hoàn thành:
            // - Trong ngày hẹn (từ 0h ngày hôm đó)
            // - Hoặc sau thời gian hẹn (để xử lý trường hợp khám xong mới nhập hồ sơ)
            LocalDate appointmentDate = slot.getSlotDate();
            LocalDate today = LocalDate.now();
            
            return !appointmentDate.isAfter(today); // Ngày hẹn không sau hôm nay

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Cho phép đặt lại lịch hẹn đã hết hạn
     */
    public boolean rescheduleAppointment(Long appointmentId) {
        try {
            Appointments appointment = appointmentRepository.findById(appointmentId).orElse(null);
            if (appointment != null && 
                ("AUTO_CANCELLED".equals(appointment.getStatus()) || 
                 "NO_SHOW".equals(appointment.getStatus()))) {
                
                appointment.setStatus("PENDING");
                appointment.setNotes("Được đặt lại bởi bác sĩ");
                appointmentRepository.save(appointment);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Kiểm tra và cập nhật trạng thái expired cho các lịch hẹn của bác sĩ
     */
    public void checkAndUpdateExpiredAppointments(Long doctorId) {
        List<Appointments> doctorAppointments = appointmentRepository.findByDoctorId(doctorId);
        LocalDateTime now = LocalDateTime.now();

        for (Appointments appointment : doctorAppointments) {
            try {
                if ("PENDING".equals(appointment.getStatus()) || "CONFIRMED".equals(appointment.getStatus())) {
                    
                    AppointmentSlots slot = appointmentSlotService.getSlotById(appointment.getSlotId());
                    if (slot == null) continue;
                    
                    LocalDateTime appointmentDateTime = LocalDateTime.of(slot.getSlotDate(), slot.getSlotTime());
                    
                    if (appointmentDateTime.isBefore(now)) {
                        if ("PENDING".equals(appointment.getStatus())) {
                            appointment.setStatus("AUTO_CANCELLED");
                            appointment.setNotes("Tự động hủy do quá hạn xác nhận");
                        } else if ("CONFIRMED".equals(appointment.getStatus())) {
                            appointment.setStatus("NO_SHOW");
                            appointment.setNotes("Bệnh nhân không đến khám");
                        }
                        
                        appointmentRepository.save(appointment);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error checking expired appointment " + appointment.getAppointmentId() + ": " + e.getMessage());
            }
        }
    }

    private void updateSlotStatusForCancellation(AppointmentSlots slot) {
        try {
            if (slot != null && "BOOKED".equals(slot.getStatus())) {
                slot.setStatus("AVAILABLE");
                appointmentSlotRepository.save(slot);
            }
        } catch (Exception e) {
            System.err.println("Error updating slot status: " + e.getMessage());
        }
    }
}