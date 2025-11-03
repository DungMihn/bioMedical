package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.Appointments;
import com.spring.bioMedical.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ✅ This scheduled job runs every midnight to auto-cancel
 * old appointments that were never completed.
 */
@Service
public class AppointmentScheduler {

    @Autowired
    private AppointmentRepository appointmentRepo;

    // ⏰ Runs every day at 00:00 (midnight)
    @Scheduled(cron = "0 0 0 * * ?")
//    @Scheduled(fixedRate = 60000) // chạy mỗi 60 giây

    public void autoCancelOldAppointments() {
        LocalDate today = LocalDate.now();
        Date todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Find all appointments before today that are still not completed or cancelled
        List<Appointments> outdatedAppointments = appointmentRepo.findAll().stream()
                .filter(a -> a.getSlotId() != null && a.getSlotId().getSlotDate().before(todayDate))
                .filter(a -> !a.getStatus().equalsIgnoreCase("COMPLETED"))
                .filter(a -> !a.getStatus().equalsIgnoreCase("CANCELLED"))
                .collect(Collectors.toList()); // ✅ Dùng collect() thay vì toList()

        if (!outdatedAppointments.isEmpty()) {
            outdatedAppointments.forEach(a -> a.setStatus("CANCELLED"));
            appointmentRepo.saveAll(outdatedAppointments);
            System.out.println("🕛 Auto-cancelled " + outdatedAppointments.size() + " outdated appointments.");
        } else {
            System.out.println("🕛 No outdated appointments found today.");
        }
    }
}
