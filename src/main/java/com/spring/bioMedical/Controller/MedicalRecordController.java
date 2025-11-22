package com.spring.bioMedical.Controller;


import com.spring.bioMedical.dto.MedicalRecordDto;
import com.spring.bioMedical.dto.PrescriptionDto;
import com.spring.bioMedical.entity.MedicalRecords;
import com.spring.bioMedical.repository.MedicalRecordsRepository;
import com.spring.bioMedical.service.MedicalRecordsService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/medical-record")
@CrossOrigin(origins = "*") // ✅ Cho phép gọi từ trình duyệt (AJAX)
public class MedicalRecordController {

    @Autowired
    private MedicalRecordsRepository recordRepo;
    
    @Autowired
    private MedicalRecordsService medicalRecordService;


    @GetMapping("/{appointmentId}")
@ResponseBody
public ResponseEntity<MedicalRecordDto> getRecordByAppointment(@PathVariable Long appointmentId) {
    MedicalRecords record = medicalRecordService.getMedicalRecordByAppointmentId(appointmentId);
    if (record == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    MedicalRecordDto dto = new MedicalRecordDto();
    dto.setRecordId(record.getRecordId());
    dto.setAppointmentId(record.getAppointment().getAppointmentId());
    dto.setDiagnosis(record.getDiagnosis());
    dto.setSymptoms(record.getSymptoms());
    dto.setTreatment(record.getTreatment());
    dto.setNotes(record.getNotes());

    // ✅ map prescriptions -> prescriptionsCollection (đúng tên JS đang dùng)
    if (record.getPrescriptions() != null) {
        List<PrescriptionDto> presDtos = record.getPrescriptions()
                .stream()
                .map(p -> {
                    PrescriptionDto pd = new PrescriptionDto();
                    pd.setPrescriptionId(p.getPrescriptionId());    // tên getter theo entity của bạn
                    pd.setMedicationName(p.getMedicationName());
                    pd.setDosage(p.getDosage());
                    pd.setFrequency(p.getFrequency());
                    pd.setDuration(p.getDuration());
                    pd.setNotes(p.getNotes());
                    return pd;
                })
                .collect(Collectors.toList());

        dto.setPrescriptionsCollection(presDtos);
    }
System.out.println("Pres count = " + 
    (record.getPrescriptions() == null ? null : record.getPrescriptions().size()));

    return ResponseEntity.ok(dto);
    
}

}
