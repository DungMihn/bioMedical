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

    // ✅ SỬA: Dùng JPQL với relationship thay vì native query
    @Query("SELECT a FROM Appointments a "
            + "WHERE a.slot.doctor.doctorId = :doctorId "
            + "ORDER BY a.createdAt DESC")
    List<Appointments> findByDoctorId(@Param("doctorId") Long doctorId);

    // ✅ SỬA: Dùng JPQL cho query today appointments
    @Query("SELECT a FROM Appointments a "
            + "WHERE a.slot.doctor.doctorId = :doctorId "
            + "AND a.slot.slotDate = CURRENT_DATE "
            + // ← SỬA Ở ĐÂY
            "ORDER BY a.slot.slotTime ASC")
    List<Appointments> findTodayAppointmentsByDoctorId(@Param("doctorId") Long doctorId);

    // ✅ SỬA: Dùng JPQL cho pending appointments
    @Query("SELECT a FROM Appointments a "
            + "WHERE a.slot.doctor.doctorId = :doctorId "
            + "AND a.status = 'PENDING' "
            + "ORDER BY a.createdAt DESC")
    List<Appointments> findPendingAppointmentsByDoctorId(@Param("doctorId") Long doctorId);

    // ✅ SỬA: Dùng JPQL cho confirmed appointments
    @Query("SELECT a FROM Appointments a "
            + "WHERE a.slot.doctor.doctorId = :doctorId "
            + "AND a.status = 'CONFIRMED' "
            + "ORDER BY a.slot.slotDate ASC, a.slot.slotTime ASC")
    List<Appointments> findConfirmedAppointmentsByDoctorId(@Param("doctorId") Long doctorId);

    // ✅ SỬA: Dùng JPQL cho completed appointments
    @Query("SELECT a FROM Appointments a "
            + "WHERE a.slot.doctor.doctorId = :doctorId "
            + "AND a.status = 'COMPLETED' "
            + "ORDER BY a.createdAt DESC")
    List<Appointments> findCompletedAppointmentsByDoctorId(@Param("doctorId") Long doctorId);

    // ✅ SỬA: Dùng JPQL cho slot date before và status
    @Query("SELECT a FROM Appointments a "
            + "WHERE a.slot.slotDate < :date "
            + "AND a.status = :status")
    List<Appointments> findBySlotDateBeforeAndStatus(@Param("date") Date date, @Param("status") String status);

    // ✅ SỬA: Dùng relationship user thay vì user_id
    @Query("SELECT a FROM Appointments a "
            + "WHERE a.user.userId = :userId "
            + "ORDER BY a.createdAt DESC")
    List<Appointments> findByUserId(@Param("userId") Long userId);

    // ✅ SỬA: Dùng relationship slot thay vì slot_id
    @Query("SELECT a FROM Appointments a "
            + "WHERE a.slot.slotId = :slotId")
    List<Appointments> findBySlotId(@Param("slotId") Long slotId);

    // ✅ SỬA: Dùng JPQL cho slot date between
    @Query("SELECT a FROM Appointments a "
            + "WHERE a.slot.slotDate BETWEEN :startDate AND :endDate "
            + "AND a.status = :status")
    List<Appointments> findBySlotDateBetweenAndStatus(@Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("status") String status);

    // ✅ SỬA: Dùng JPQL cho count by status
    @Query("SELECT COUNT(a) FROM Appointments a WHERE a.status = :status")
    Long countByStatus(@Param("status") String status);

    // ✅ SỬA: Dùng JPQL cho upcoming appointments
    @Query("SELECT a FROM Appointments a "
            + "WHERE a.slot.slotDate BETWEEN CURRENT_DATE AND FUNCTION('DATEADD', day, 7, CURRENT_DATE) "
            + "AND a.status IN ('PENDING', 'CONFIRMED') "
            + "ORDER BY a.slot.slotDate, a.slot.slotTime")
    List<Appointments> findUpcomingAppointments();

    // ✅ SỬA: Dùng JPQL cho expired pending appointments
    @Query("SELECT a FROM Appointments a "
            + "WHERE a.status = 'PENDING' AND a.slot.slotDate < CURRENT_DATE")
    List<Appointments> findExpiredPendingAppointments();

    // ✅ SỬA: Dùng JPQL cho doctorId và status
    @Query("SELECT a FROM Appointments a "
            + "WHERE a.slot.doctor.doctorId = :doctorId "
            + "AND a.status = :status "
            + "ORDER BY a.createdAt DESC")
    List<Appointments> findByDoctorIdAndStatus(@Param("doctorId") Long doctorId, @Param("status") String status);

    // ✅ SỬA: Dùng JPQL cho count by doctorId và status
    @Query("SELECT COUNT(a) FROM Appointments a "
            + "WHERE a.slot.doctor.doctorId = :doctorId "
            + "AND a.status = :status")
    Long countByDoctorIdAndStatus(@Param("doctorId") Long doctorId, @Param("status") String status);

    // ✅ SỬA: Dùng JPQL cho appointments với medical records
    @Query("SELECT a FROM Appointments a "
            + "WHERE EXISTS (SELECT mr FROM MedicalRecords mr WHERE mr.appointment = a) "
            + "AND a.slot.doctor.doctorId = :doctorId")
    List<Appointments> findAppointmentsWithMedicalRecords(@Param("doctorId") Long doctorId);

    // ✅ THÊM MỚI: Tìm appointments theo user entity (dùng trong service)
    List<Appointments> findByUserAndStatusOrderByCreatedAtDesc(Users user, String status);

    // ✅ THÊM MỚI: Tìm appointments theo status
    List<Appointments> findByStatusOrderByCreatedAtDesc(String status);

    // ✅ THÊM MỚI: Tìm appointments theo user
    List<Appointments> findByUserOrderByCreatedAtDesc(Users user);

    // ✅ THÊM MỚI: Tìm first appointment theo user và status
    default Appointments findFirstByUserAndStatusOrderByCreatedAtDesc(Users user, String status) {
        List<Appointments> appointments = findByUserAndStatusOrderByCreatedAtDesc(user, status);
        return appointments.isEmpty() ? null : appointments.get(0);
    }
}
