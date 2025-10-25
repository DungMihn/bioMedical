package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.MedicalRecords;
import com.spring.bioMedical.entity.Prescriptions;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.repository.MedicalRecordsRepository;
import com.spring.bioMedical.repository.PrescriptionsRepository;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        List<MedicalRecords> records = medicalRecordsRepository.findByDoctorId(doctorId);
        records.forEach(this::enrichMedicalRecordData);
        return records;
    }

    public List<MedicalRecords> searchMedicalRecords(String keyword, Long doctorId) {
        List<MedicalRecords> records;

        if (keyword.matches("\\d+")) {
            // Search by phone number
            records = medicalRecordsRepository.findByDoctorId(doctorId).stream()
                    .filter(record -> {
                        Users patient = userService.findById(record.getAppointment().getUserId());
                        return patient != null && patient.getPhone() != null
                                && patient.getPhone().contains(keyword);
                    })
                    .toList();
        } else {
            // Search by patient name
            records = medicalRecordsRepository.findByDoctorId(doctorId).stream()
                    .filter(record -> {
                        Users patient = userService.findById(record.getAppointment().getUserId());
                        return patient != null && patient.getFullName() != null
                                && patient.getFullName().toLowerCase().contains(keyword.toLowerCase());
                    })
                    .toList();
        }

        records.forEach(this::enrichMedicalRecordData);
        return records;
    }

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
