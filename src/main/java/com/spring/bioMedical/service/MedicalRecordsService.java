package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.Appointments;
import com.spring.bioMedical.entity.MedicalRecords;
import com.spring.bioMedical.entity.Prescriptions;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.repository.MedicalRecordsRepository;
import com.spring.bioMedical.repository.PrescriptionsRepository;
import java.awt.print.Pageable;
import java.util.ArrayList;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;

@Service
public class MedicalRecordsService {

    @Autowired
    private MedicalRecordsRepository medicalRecordsRepository;

    @Autowired
    private PrescriptionsRepository prescriptionsRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UsersService userService;

    public List<MedicalRecords> getMedicalRecordsByDoctorId(Long doctorId) {
        try {
            // Lấy tất cả appointments của doctor
            List<Appointments> doctorAppointments = appointmentService.getAppointmentsByDoctorId(doctorId);
            List<Long> appointmentIds = doctorAppointments.stream()
                    .map(Appointments::getAppointmentId)
                    .collect(Collectors.toList());

            if (appointmentIds.isEmpty()) {
                return new ArrayList<>();
            }

            // ✅ SỬA: Dùng method mới findByAppointmentAppointmentIdIn
            List<MedicalRecords> records = medicalRecordsRepository.findByAppointmentAppointmentIdIn(appointmentIds);
            records.forEach(this::enrichMedicalRecordData);
            return records;

        } catch (Exception e) {
            System.err.println("Error getting medical records by doctor ID: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<MedicalRecords> searchMedicalRecords(String keyword, Long doctorId) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return getMedicalRecordsByDoctorId(doctorId);
            }

            List<MedicalRecords> allRecords = getMedicalRecordsByDoctorId(doctorId);
            String searchTerm = keyword.trim().toLowerCase();

            return allRecords.stream()
                    .filter(record -> {
                        try {
                            if (record.getAppointment() == null) {
                                return false;
                            }

                            Users patient = userService.findById(record.getAppointment().getUserId());
                            if (patient == null) {
                                return false;
                            }

                            // Tìm kiếm đơn giản - chỉ cần match bất kỳ field nào
                            boolean patientNameMatch = patient.getFullName() != null
                                    && patient.getFullName().toLowerCase().contains(searchTerm);

                            boolean phoneMatch = patient.getPhone() != null
                                    && patient.getPhone().contains(searchTerm);

                            boolean diagnosisMatch = record.getDiagnosis() != null
                                    && record.getDiagnosis().toLowerCase().contains(searchTerm);

                            boolean symptomsMatch = record.getSymptoms() != null
                                    && record.getSymptoms().toLowerCase().contains(searchTerm);

                            return patientNameMatch || phoneMatch || diagnosisMatch || symptomsMatch;

                        } catch (Exception e) {
                            System.err.println("Error searching record: " + e.getMessage());
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error in searchMedicalRecords: " + e.getMessage());
            return new ArrayList<>();
        }
    }

//    private boolean matchesSearch(Users patient, String keyword) {
//        if (keyword == null || keyword.trim().isEmpty()) {
//            return true;
//        }
//
//        String searchLower = keyword.toLowerCase().trim();
//        boolean nameMatch = patient.getFullName() != null
//                && patient.getFullName().toLowerCase().contains(searchLower);
//        boolean phoneMatch = patient.getPhone() != null
//                && patient.getPhone().contains(keyword);
//        boolean emailMatch = patient.getEmail() != null
//                && patient.getEmail().toLowerCase().contains(searchLower);
//
//        return nameMatch || phoneMatch || emailMatch;
//    }
    public void updateMedicalRecord(MedicalRecords medicalRecord) {
        MedicalRecords existingRecord = medicalRecordsRepository.findById(medicalRecord.getRecordId()).orElse(null);
        if (existingRecord != null) {
            existingRecord.setSymptoms(medicalRecord.getSymptoms());
            existingRecord.setDiagnosis(medicalRecord.getDiagnosis());
            existingRecord.setTreatment(medicalRecord.getTreatment());
            existingRecord.setNotes(medicalRecord.getNotes());
            existingRecord.setCreatedAt(new Date()); // Cập nhật thời gian

            medicalRecordsRepository.save(existingRecord);
        }
    }

    public Prescriptions getPrescriptionById(Long prescriptionId) {
        return prescriptionsRepository.findById(prescriptionId).orElse(null);
    }

    public void savePrescription(Prescriptions prescription) {
        prescriptionsRepository.save(prescription);
    }

    public void deletePrescription(Long prescriptionId) {
        prescriptionsRepository.deleteById(prescriptionId);
    }

    public void deletePrescriptionsByRecordId(Long recordId) {
        List<Prescriptions> prescriptions = prescriptionsRepository.findByRecordId(recordId);
        prescriptionsRepository.deleteAll(prescriptions);
    }

    public MedicalRecords getMedicalRecordByAppointmentId(Long appointmentId) {
        return medicalRecordsRepository.findByAppointmentId(appointmentId);
    }

    @Transactional
    public MedicalRecords saveMedicalRecord(MedicalRecords medicalRecord, List<Prescriptions> prescriptions) {
        MedicalRecords savedRecord = medicalRecordsRepository.save(medicalRecord);

        // Save prescriptions
        if (prescriptions != null) {
            prescriptions.forEach(p -> p.setRecordId(savedRecord.getRecordId()));
            prescriptionsRepository.saveAll(prescriptions);
        }

        return savedRecord;
    }

    @Transactional
    public void updateMedicalRecord(Long recordId, MedicalRecords medicalRecord, List<Prescriptions> prescriptions) {
        medicalRecord.setRecordId(recordId);
        medicalRecordsRepository.save(medicalRecord);

        // Delete existing prescriptions and save new ones
        prescriptionsRepository.deleteByRecordId(recordId);
        if (prescriptions != null) {
            prescriptions.forEach(p -> p.setRecordId(recordId));
            prescriptionsRepository.saveAll(prescriptions);
        }
    }

    public MedicalRecords getMedicalRecordById(Long recordId) {
        MedicalRecords record = medicalRecordsRepository.findById(recordId).orElse(null);
        if (record != null) {
            enrichMedicalRecordData(record);
        }
        return record;
    }

    private void enrichMedicalRecordData(MedicalRecords record) {
        // Get appointment details
        record.setAppointment(appointmentService.getAppointmentById(record.getAppointmentId()));

        if (record.getAppointment() != null) {
            // Get patient details
            record.setPatient(userService.findById(record.getAppointment().getUserId()));
        }

        // Get prescriptions
        List<Prescriptions> prescriptions = prescriptionsRepository.findByRecordId(record.getRecordId());
        record.setPrescriptions(prescriptions);
    }

    public List<Prescriptions> getPrescriptionsByRecordId(Long recordId) {
        return prescriptionsRepository.findByRecordId(recordId);
    }

}
