package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.MedicalRecords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordsRepository extends JpaRepository<MedicalRecords, Long> {

    // ✅ SỬA: Dùng relationship thay vì subquery phức tạp
    @Query("SELECT mr FROM MedicalRecords mr "
            + "WHERE mr.appointment.slot.doctor.doctorId = :doctorId")
    List<MedicalRecords> findByDoctorId(@Param("doctorId") Long doctorId);

    // ✅ SỬA: Dùng relationship trực tiếp
    @Query("SELECT mr FROM MedicalRecords mr "
            + "WHERE mr.appointment.user.userId = :patientId")
    List<MedicalRecords> findByPatientId(@Param("patientId") Long patientId);

    // ✅ SỬA: Dùng relationship appointment thay vì appointmentId
    @Query("SELECT mr FROM MedicalRecords mr WHERE mr.appointment.appointmentId = :appointmentId")
    MedicalRecords findByAppointmentId(@Param("appointmentId") Long appointmentId);

    // ✅ SỬA: Dùng relationship trong derived query
    List<MedicalRecords> findByAppointmentAppointmentIdIn(List<Long> appointmentIds);

    // ✅ THÊM MỚI: Tìm medical records theo appointment
    List<MedicalRecords> findByAppointmentAppointmentId(Long appointmentId);

    // ✅ THÊM MỚI: Tìm medical records có prescriptions
    @Query("SELECT mr FROM MedicalRecords mr WHERE SIZE(mr.prescriptions) > 0")
    List<MedicalRecords> findRecordsWithPrescriptions();

    // ✅ THÊM MỚI: Tìm medical records theo doctor và có diagnosis
    @Query("SELECT mr FROM MedicalRecords mr "
            + "WHERE mr.appointment.slot.doctor.doctorId = :doctorId "
            + "AND mr.diagnosis IS NOT NULL")
    List<MedicalRecords> findByDoctorIdWithDiagnosis(@Param("doctorId") Long doctorId);

    // ✅ THÊM MỚI: Tìm medical records gần đây
    @Query("SELECT mr FROM MedicalRecords mr ORDER BY mr.createdAt DESC")
    List<MedicalRecords> findRecentRecords();

    // ✅ THÊM MỚI: Đếm số medical records theo patient
    @Query("SELECT COUNT(mr) FROM MedicalRecords mr WHERE mr.appointment.user.userId = :patientId")
    Long countByPatientId(@Param("patientId") Long patientId);
}
