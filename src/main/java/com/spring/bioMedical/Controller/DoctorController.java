package com.spring.bioMedical.Controller;

import com.spring.bioMedical.entity.*;
import com.spring.bioMedical.service.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentSlotService appointmentSlotService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private MedicalRecordsService medicalRecordsService;

    @Autowired
    private ITextReportService iTextReportService;

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

    @GetMapping("/index")
    public String doctorIndex(Model model) {
        try {
            // Lấy username hiện tại
            String username = getCurrentUsername();

            // Lấy user từ DB
            Users currentUser = usersService.findByUsername(username);
            Users user = usersService.findByUsername(username);
            System.out.println("[DEBUG] Current user = " + user);

            // Khởi tạo mặc định
            List<Appointments> todayAppointments = new ArrayList<>();
            List<Appointments> pendingAppointments = new ArrayList<>();

            // Cập nhật last seen
            if (currentUser != null) {
                currentUser.setUpdatedAt(new Date());
                usersService.save(currentUser);
            }

            if (pendingAppointments != null && !pendingAppointments.isEmpty()) {
                model.addAttribute("hasPendingAppointments", true);
            } else {
                model.addAttribute("hasPendingAppointments", false);
            }

            // Nếu có user thì lấy doctor và appointments
            if (user != null) {
                Optional<Doctors> doctorOpt = doctorService.getDoctorByUserId(user.getUserId());
                if (doctorOpt.isPresent()) {
                    Doctors doctor = doctorOpt.get();
                    todayAppointments = appointmentService.getTodayAppointmentsByDoctorId(doctor.getDoctorId());
                    System.out.println("[DEBUG] todayAppointments = " + todayAppointments);
                    pendingAppointments = appointmentService.getPendingAppointmentsByDoctorId(doctor.getDoctorId());
                    System.out.println("[DEBUG] pendingAppointments = " + pendingAppointments);
                    model.addAttribute("doctor", doctor);
                }
            }

            model.addAttribute("todayAppointments", todayAppointments);
            model.addAttribute("pendingAppointments", pendingAppointments);
            model.addAttribute("name", currentUser != null ? currentUser.getFullName() : "Doctor");
            model.addAttribute("email", currentUser != null ? currentUser.getEmail() : "");
            model.addAttribute("user", currentUser != null ? currentUser.getFullName() : "Doctor");

            return "doctor/index";

        } catch (Exception e) {
            // Log lỗi và trả về trang index với dữ liệu rỗng
            System.err.println("Error in doctorIndex: " + e.getMessage());

            model.addAttribute("todayAppointments", new ArrayList<>());
            model.addAttribute("pendingAppointments", new ArrayList<>());
            model.addAttribute("name", "Doctor");
            model.addAttribute("user", "Doctor");
            model.addAttribute("email", "");
            model.addAttribute("doctor", new Doctors());

            return "doctor/index";
        }
    }

//    private List<Appointments> safeGetTodayAppointments(Long doctorId) {
//        try {
//            return appointmentService.getTodayAppointmentsByDoctorId(doctorId);
//        } catch (Exception e) {
//            return Collections.emptyList();
//        }
//    }
//
//    private List<Appointments> safeGetPendingAppointments(Long doctorId) {
//        try {
//            return appointmentService.getPendingAppointmentsByDoctorId(doctorId);
//        } catch (Exception e) {
//            return Collections.emptyList();
//        }
//    }
    // Quản lý appointments
    @GetMapping("/appointments")
    public String appointments(Model model) {
        String username = getCurrentUsername();
        Users user = usersService.findByUsername(username);

        if (user != null) {
            Optional<Doctors> doctorOpt = doctorService.getDoctorByUserId(user.getUserId());
            if (doctorOpt.isPresent()) {
                Doctors doctor = doctorOpt.get();
                List<Appointments> appointments = appointmentService.getAppointmentsByDoctorId(doctor.getDoctorId());
                model.addAttribute("appointments", appointments);
                model.addAttribute("todayAppointments", appointments);
                System.out.println("[DEBUG] Appoint = " + appointments);
                model.addAttribute("doctor", doctor);
            }
        } else {
            System.out.println("[DEBUG] USERRRRRRRRR " + user);
        }

        return "doctor/appointments";
    }

    // Xem chi tiết appointment
    @GetMapping("/appointment/{id}")
    public String viewAppointment(@PathVariable Long id, Model model) {
        Appointments appointment = appointmentService.getAppointmentById(id);
        System.out.println("[DEBUG] APPPPPPP = " + appointment);
        if (appointment != null) {
            Users patient = usersService.findById(appointment.getUserId());
            System.out.println("[DEBUG] PATIENTTT = " + patient);
            model.addAttribute("appointment", appointment);
            model.addAttribute("patient", patient);
        }
        return "doctor/appointment-detail";
    }

    // Tra cứu hồ sơ bệnh án
    @GetMapping("/medical-records")
    public String medicalRecords(@RequestParam(required = false) String search, Model model) {
        String username = getCurrentUsername();
        Users user = usersService.findByUsername(username);

        if (user != null) {
            Optional<Doctors> doctorOpt = doctorService.getDoctorByUserId(user.getUserId());
            if (doctorOpt.isPresent()) {
                Doctors doctor = doctorOpt.get();

                List<MedicalRecords> records;
                if (search != null && !search.trim().isEmpty()) {
                    records = medicalRecordsService.searchMedicalRecords(search.trim(), doctor.getDoctorId());
                } else {
                    records = medicalRecordsService.getMedicalRecordsByDoctorId(doctor.getDoctorId());
                }
                System.out.println("[DEBUG] records = " + records + "search" + search + "Doctor" + doctor);
                model.addAttribute("records", records);
                model.addAttribute("search", search);
                model.addAttribute("doctor", doctor);
            }
        }

        return "doctor/medical-records";
    }

    // Tạo hồ sơ bệnh án mới
    @GetMapping("/medical-record/create")
    public String createMedicalRecordForm(@RequestParam Long appointmentId, Model model) {
        Appointments appointment = appointmentService.getAppointmentById(appointmentId);
        if (appointment != null) {
            Users patient = usersService.findById(appointment.getUserId());
            model.addAttribute("appointment", appointment);
            model.addAttribute("patient", patient);
            model.addAttribute("medicalRecord", new MedicalRecords());
        }
        return "doctor/medical-record-form";
    }

    @GetMapping("/medical-record/{id}")
    public String viewMedicalRecord(@PathVariable Long id, Model model) {
        MedicalRecords record = medicalRecordsService.getMedicalRecordById(id);
        if (record != null) {
            // Lấy thông tin appointment
            Appointments appointment = appointmentService.getAppointmentById(record.getAppointmentId());

            // Lấy thông tin bệnh nhân
            Users patient = usersService.findById(appointment.getUserId());

            // Lấy thông tin bác sĩ từ slot
            AppointmentSlots slot = appointmentSlotService.getSlotById(appointment.getSlotId());
            Optional<Doctors> doctorOpt = Optional.empty();
            if (slot != null) {
                doctorOpt = doctorService.getDoctorByIdOp(slot.getDoctorId());
            }

            // Lấy đơn thuốc
            List<Prescriptions> prescriptions = medicalRecordsService.getPrescriptionsByRecordId(id);

            // Thêm các thuộc tính vào model
            model.addAttribute("medicalRecord", record);
            model.addAttribute("appointment", appointment);
            model.addAttribute("patient", patient);
            model.addAttribute("prescriptions", prescriptions);

            if (doctorOpt.isPresent()) {
                model.addAttribute("doctor", doctorOpt.get());
            }
        }
        return "doctor/medical-record-detail";
    }

    @PostMapping("/appointment/{id}/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, String>> confirmAppointment(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean success = appointmentService.confirmAppointment(id);
            if (success) {
                response.put("status", "SUCCESS");
                response.put("message", "Appointment confirmed successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "ERROR");
                response.put("message", "Cannot confirm appointment. Slot may be already booked.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // Sửa ở đây
            }
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Error confirming appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // Và ở đây
        }
    }

    @PostMapping("/appointment/{id}/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, String>> cancelAppointment(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean success = appointmentService.updateAppointmentStatus(id, "CANCELLED");
            if (success) {
                response.put("status", "SUCCESS");
                response.put("message", "Appointment cancelled successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "ERROR");
                response.put("message", "Cannot cancel appointment");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // Sửa ở đây
            }
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Error cancelling appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // Và ở đây
        }
    }

    @PostMapping("/appointment/{id}/complete")
    @ResponseBody
    public ResponseEntity<Map<String, String>> completeAppointment(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean success = appointmentService.updateAppointmentStatus(id, "COMPLETED");
            if (success) {
                response.put("status", "SUCCESS");
                response.put("message", "Appointment completed successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "ERROR");
                response.put("message", "Cannot complete appointment");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // Sửa ở đây
            }
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Error completing appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // Và ở đây
        }
    }

    @PostMapping("/medical-record/save")
    public String saveMedicalRecord(@ModelAttribute MedicalRecords medicalRecord,
            @RequestParam List<String> medicationNames,
            @RequestParam List<String> dosages,
            @RequestParam List<String> frequencies,
            @RequestParam List<String> durations,
            @RequestParam List<String> notes) {

        // Tạo danh sách prescriptions từ form data
        List<Prescriptions> prescriptions = createPrescriptionsList(medicationNames, dosages, frequencies, durations, notes);
        medicalRecordsService.saveMedicalRecord(medicalRecord, prescriptions);

        return "redirect:/doctor/medical-records";
    }

    private List<Prescriptions> createPrescriptionsList(List<String> medicationNames,
            List<String> dosages,
            List<String> frequencies,
            List<String> durations,
            List<String> notes) {
        List<Prescriptions> prescriptions = new ArrayList<>();

        for (int i = 0; i < medicationNames.size(); i++) {
            String medicationName = medicationNames.get(i);

            // Chỉ tạo prescription nếu medication name không rỗng
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

        return prescriptions;
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    // Trong DoctorController
// Hiển thị form tạo hồ sơ bệnh án cho bệnh nhân mới
    @GetMapping("/medical-record/create-new")
    public String createMedicalRecordForNewPatientForm(Model model) {
        model.addAttribute("medicalRecord", new MedicalRecords());
        return "doctor/medical-record-new-patient-form";
    }

// Xử lý tạo hồ sơ bệnh án cho bệnh nhân mới
//    @PostMapping("/medical-record/create-new")
//    public String createMedicalRecordForNewPatient(
//            @ModelAttribute MedicalRecords medicalRecord,
//            @RequestParam String fullName,
//            @RequestParam String phone,
//            @RequestParam(required = false) String email,
//            @RequestParam(required = false) Date dob,
//            @RequestParam(required = false) String address,
//            @RequestParam List<String> medicationNames,
//            @RequestParam List<String> dosages,
//            @RequestParam List<String> frequencies,
//            @RequestParam List<String> durations,
//            @RequestParam List<String> notes) {
//
//        // Tạo user mới cho bệnh nhân
//        Users newPatient = new Users();
//        newPatient.setFullName(fullName);
//        newPatient.setPhone(phone);
//        newPatient.setEmail(email != null ? email : "");
//        newPatient.setDob(dob);
//        newPatient.setAddress(address != null ? address : "");
//        newPatient.setRole("PATIENT");
//        newPatient.setCreatedAt(new Date());
//        newPatient.setUpdatedAt(new Date());
//
//        // Lưu user mới
//        Users savedPatient = usersService.save(newPatient);
//
//        // Thiết lập thông tin cho hồ sơ bệnh án
//        String username = getCurrentUsername();
//        Users currentUser = usersService.findByUsername(username);
//        Optional<Doctors> doctorOpt = doctorService.getDoctorByUserId(currentUser.getUserId());
//
//        if (doctorOpt.isPresent()) {
//            medicalRecord.setDoctorId(doctorOpt.get().getDoctorId());
//        }
//
//        medicalRecord.setUserId(savedPatient.getUserId());
//        medicalRecord.setCreatedAt(new Date());
//
//        // Tạo danh sách prescriptions từ form data
//        List<Prescriptions> prescriptions = createPrescriptionsList(medicationNames, dosages, frequencies, durations, notes);
//
//        // Lưu hồ sơ bệnh án
//        medicalRecordsService.saveMedicalRecord(medicalRecord, prescriptions);
//
//        return "redirect:/doctor/medical-records";
//    }
}
