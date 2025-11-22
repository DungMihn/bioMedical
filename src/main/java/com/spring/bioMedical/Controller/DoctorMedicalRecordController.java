package com.spring.bioMedical.Controller;

import com.spring.bioMedical.entity.MedicalRecords;
import com.spring.bioMedical.service.MedicalRecordsService;
import com.spring.bioMedical.service.AppointmentService;
import com.spring.bioMedical.service.BillingService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorMedicalRecordController {

    private final MedicalRecordsService medicalRecordsService;
    private final AppointmentService appointmentService;
    private final BillingService billingService;

    @PostMapping("/medical-record/create")
    public String createMedicalRecord(
            @ModelAttribute MedicalRecords record,
            RedirectAttributes ra) {

        // 1. Lưu hồ sơ (tự thiết lập appointment từ record.appointment.appointmentId)
        MedicalRecords saved = medicalRecordsService.saveRecord(record);

        // 2. Mark appointment COMPLETED
        appointmentService.markCompleted(saved.getAppointment().getAppointmentId());

        // 3. Tạo invoice nháp
        billingService.generateRandomInvoiceForRecord(saved.getRecordId());

        ra.addFlashAttribute("success", "Đã lưu hồ sơ và tạo hóa đơn nháp.");
        return "redirect:/doctor/medical-record/" + saved.getRecordId();
    }
}
