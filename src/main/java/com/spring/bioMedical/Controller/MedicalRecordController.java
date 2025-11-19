package com.spring.bioMedical.Controller;

import com.spring.bioMedical.entity.MedicalRecords;
import com.spring.bioMedical.repository.MedicalRecordsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medical-record")
@CrossOrigin(origins = "*") // ✅ Cho phép gọi từ trình duyệt (AJAX)
public class MedicalRecordController {

    @Autowired
    private MedicalRecordsRepository recordRepo;

    @GetMapping("/{appointmentId}")
    public MedicalRecords getRecordByAppointment(@PathVariable Long appointmentId) {
        return recordRepo.findByAppointmentId(appointmentId);
    }
}