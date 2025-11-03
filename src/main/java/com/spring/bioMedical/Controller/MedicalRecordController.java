package com.spring.bioMedical.Controller;

import com.spring.bioMedical.entity.Medicalrecords;
import com.spring.bioMedical.repository.MedicalRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medical-record")
@CrossOrigin(origins = "*") // ✅ Cho phép gọi từ trình duyệt (AJAX)
public class MedicalRecordController {

    @Autowired
    private MedicalRecordRepository recordRepo;

    @GetMapping("/{appointmentId}")
    public Medicalrecords getRecordByAppointment(@PathVariable Long appointmentId) {
        return recordRepo.findByAppointmentId_AppointmentId(appointmentId);
    }
}
