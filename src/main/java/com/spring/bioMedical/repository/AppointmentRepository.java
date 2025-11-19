package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.Appointments;
import com.spring.bioMedical.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointments, Long> {

    // ✅ Sửa lại query để phù hợp với SQL Server
    @Query(value = "SELECT a.* FROM Appointments a "
            + "WHERE a.slot_id IN (SELECT s.slot_id FROM AppointmentSlots s WHERE s.doctor_id = :doctorId) "
            + "ORDER BY a.created_at DESC", nativeQuery = true)
    List<Appointments> findByDoctorId(@Param("doctorId") Long doctorId);

    // ✅ Sửa query pending appointments
    @Query(value = "SELECT a.* FROM Appointments a "
            + "WHERE a.slot_id IN (SELECT s.slot_id FROM AppointmentSlots s WHERE s.doctor_id = :doctorId) "
            + "AND a.status = 'PENDING'", nativeQuery = true)
    List<Appointments> findPendingAppointmentsByDoctorId(@Param("doctorId") Long doctorId);

    // ✅ Sửa query today appointments - DÙNG GETDATE() thay vì CURRENT_DATE
    @Query(value = "SELECT a.* FROM Appointments a "
            + "WHERE a.slot_id IN (SELECT s.slot_id FROM AppointmentSlots s WHERE s.doctor_id = :doctorId "
            + "AND CAST(s.slot_date AS DATE) = CAST(GETDATE() AS DATE)) "
            + "ORDER BY a.slot_id", nativeQuery = true)
    List<Appointments> findTodayAppointmentsByDoctorId(@Param("doctorId") Long doctorId);

    // ✅ THÊM MỚI: Tìm appointments có slot_date trước ngày chỉ định và status cụ thể
    @Query(value = "SELECT a.* FROM Appointments a "
            + "INNER JOIN AppointmentSlots s ON a.slot_id = s.slot_id "
            + "WHERE CAST(s.slot_date AS DATE) < CAST(:date AS DATE) "
            + "AND a.status = :status", nativeQuery = true)
    List<Appointments> findBySlot_SlotDateBeforeAndStatus(@Param("date") Date date, @Param("status") String status);

    // ✅ THÊM MỚI: Tìm appointments theo patient (user_id)
    @Query(value = "SELECT a.* FROM Appointments a "
            + "WHERE a.user_id = :userId "
            + "ORDER BY a.created_at DESC", nativeQuery = true)
    List<Appointments> findByUserId(@Param("userId") Long userId);

    // ✅ THÊM MỚI: Tìm appointments theo slot_id
    @Query(value = "SELECT a.* FROM Appointments a "
            + "WHERE a.slot_id = :slotId", nativeQuery = true)
    List<Appointments> findBySlotId(@Param("slotId") Long slotId);

    // ✅ THÊM MỚI: Tìm appointments theo khoảng thời gian
    @Query(value = "SELECT a.* FROM Appointments a "
            + "INNER JOIN AppointmentSlots s ON a.slot_id = s.slot_id "
            + "WHERE CAST(s.slot_date AS DATE) BETWEEN CAST(:startDate AS DATE) AND CAST(:endDate AS DATE) "
            + "AND a.status = :status", nativeQuery = true)
    List<Appointments> findBySlotDateBetweenAndStatus(@Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("status") String status);

    // ✅ THÊM MỚI: Đếm số appointments theo status
    @Query(value = "SELECT COUNT(*) FROM Appointments a "
            + "WHERE a.status = :status", nativeQuery = true)
    Long countByStatus(@Param("status") String status);

    // ✅ THÊM MỚI: Tìm appointments sắp tới (trong 7 ngày tới)
    @Query(value = "SELECT a.* FROM Appointments a "
            + "INNER JOIN AppointmentSlots s ON a.slot_id = s.slot_id "
            + "WHERE CAST(s.slot_date AS DATE) BETWEEN CAST(GETDATE() AS DATE) AND CAST(DATEADD(day, 7, GETDATE()) AS DATE) "
            + "AND a.status IN ('PENDING', 'CONFIRMED') "
            + "ORDER BY s.slot_date, s.slot_time", nativeQuery = true)
    List<Appointments> findUpcomingAppointments();

    @Query("SELECT a FROM Appointments a WHERE a.status = 'PENDING' AND EXISTS "
            + "(SELECT s FROM AppointmentSlots s WHERE s.slotId = a.slotId AND s.slotDate < CURRENT_DATE)")
    List<Appointments> findExpiredPendingAppointments();

    // ✅ THÊM MỚI: Tìm appointments theo doctorId và status
    @Query(value = "SELECT a.* FROM Appointments a "
            + "WHERE a.slot_id IN (SELECT s.slot_id FROM AppointmentSlots s WHERE s.doctor_id = :doctorId) "
            + "AND a.status = :status "
            + "ORDER BY a.created_at DESC", nativeQuery = true)
    List<Appointments> findByDoctorIdAndStatus(@Param("doctorId") Long doctorId, @Param("status") String status);

    // ✅ THÊM MỚI: Đếm appointments theo status và doctorId
    @Query(value = "SELECT COUNT(*) FROM Appointments a "
            + "WHERE a.slot_id IN (SELECT s.slot_id FROM AppointmentSlots s WHERE s.doctor_id = :doctorId) "
            + "AND a.status = :status", nativeQuery = true)
    Long countByDoctorIdAndStatus(@Param("doctorId") Long doctorId, @Param("status") String status);

    // ✅ THÊM MỚI: Tìm appointments có medical record
    @Query(value = "SELECT a.* FROM Appointments a "
            + "WHERE a.appointment_id IN (SELECT mr.appointment_id FROM MedicalRecords mr) "
            + "AND a.slot_id IN (SELECT s.slot_id FROM AppointmentSlots s WHERE s.doctor_id = :doctorId)", nativeQuery = true)
    List<Appointments> findAppointmentsWithMedicalRecords(@Param("doctorId") Long doctorId);

    List<Appointments> findByUserId(Users user);
}
