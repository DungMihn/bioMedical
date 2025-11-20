package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.AppointmentSlots;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlots, Long> {

    // ✅ Derived queries với relationship
    List<AppointmentSlots> findByDoctorDoctorId(Long doctorId);
    List<AppointmentSlots> findByDoctorDoctorIdAndSlotDate(Long doctorId, LocalDate slotDate);
    List<AppointmentSlots> findByClinicClinicId(Long clinicId);
    List<AppointmentSlots> findByClinicClinicIdAndStatus(Long clinicId, String status);
    List<AppointmentSlots> findByClinicClinicIdAndSlotDate(Long clinicId, LocalDate slotDate);
    List<AppointmentSlots> findByStatus(String status);
    Optional<AppointmentSlots> findBySlotIdAndClinicClinicId(Long slotId, Long clinicId);

    // ✅ JPQL queries với relationship
    @Query("SELECT s FROM AppointmentSlots s WHERE s.doctor.doctorId = :doctorId AND s.slotDate >= CURRENT_DATE")
    List<AppointmentSlots> findUpcomingSlotsByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT s FROM AppointmentSlots s WHERE s.clinic.clinicId = :clinicId AND s.status = :status")
    List<AppointmentSlots> findByClinicIdAndStatus(@Param("clinicId") Long clinicId, @Param("status") String status);

    @Query("SELECT s FROM AppointmentSlots s WHERE s.clinic.clinicId = :clinicId AND s.slotDate = :slotDate AND s.status = :status")
    List<AppointmentSlots> findByClinicIdAndDateAndStatus(
            @Param("clinicId") Long clinicId,
            @Param("slotDate") LocalDate slotDate,
            @Param("status") String status);

    @Query("SELECT s FROM AppointmentSlots s WHERE s.status = 'AVAILABLE' AND s.slotDate >= CURRENT_DATE")
    List<AppointmentSlots> findAvailableSlots();

    @Query("SELECT s FROM AppointmentSlots s WHERE s.doctor.doctorId = :doctorId AND s.status = :status")
    List<AppointmentSlots> findByDoctorIdAndStatus(@Param("doctorId") Long doctorId, @Param("status") String status);

    @Query("SELECT s FROM AppointmentSlots s WHERE s.slotDate BETWEEN :startDate AND :endDate")
    List<AppointmentSlots> findBySlotDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM AppointmentSlots s WHERE s.doctor.doctorId = :doctorId AND s.slotDate = :slotDate AND s.status = 'AVAILABLE' ORDER BY s.slotTime")
    List<AppointmentSlots> findAvailableSlotsForBooking(
            @Param("doctorId") Long doctorId,
            @Param("slotDate") LocalDate slotDate);

    @Query("SELECT COUNT(s) > 0 FROM AppointmentSlots s WHERE s.slotId = :slotId AND s.status = 'AVAILABLE'")
    boolean isSlotAvailable(@Param("slotId") Long slotId);

    // ✅ Derived query cho method cũ
    List<AppointmentSlots> findByClinicClinicIdAndSlotDateOrderBySlotTimeAsc(Long clinicId, LocalDate date);

    // ✅ Native queries (giữ nguyên vì cần performance)
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM AppointmentSlots WHERE clinic_id = :clinicId AND slot_date = :slotDate AND status = 'AVAILABLE'", nativeQuery = true)
    int deleteAvailableByClinicAndDate(@Param("clinicId") Long clinicId, @Param("slotDate") LocalDate slotDate);

    @Query(value = "SELECT TOP 1 * FROM AppointmentSlots WHERE clinic_id = :clinicId AND slot_date = :slotDate AND CAST(:slotTime AS time) = CAST(slot_time AS time) AND doctor_id = :doctorId", nativeQuery = true)
    Optional<AppointmentSlots> findOneNative(
            @Param("clinicId") Long clinicId,
            @Param("slotDate") LocalDate slotDate,
            @Param("slotTime") String slotTime,
            @Param("doctorId") Long doctorId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE AppointmentSlots SET status = 'BOOKED' WHERE slot_id = :slotId AND status = 'AVAILABLE'", nativeQuery = true)
    int bookIfAvailable(@Param("slotId") Long slotId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE AppointmentSlots SET status = 'AVAILABLE' WHERE slot_id = :slotId AND status = 'BOOKED'", nativeQuery = true)
    int releaseIfBooked(@Param("slotId") Long slotId);

    // ✅ THÊM MỚI: Các methods hữu ích khác
    @Query("SELECT s FROM AppointmentSlots s WHERE s.clinic.clinicId = :clinicId AND s.doctor.doctorId = :doctorId AND s.slotDate = :slotDate")
    List<AppointmentSlots> findByClinicAndDoctorAndDate(
            @Param("clinicId") Long clinicId,
            @Param("doctorId") Long doctorId,
            @Param("slotDate") LocalDate slotDate);

    @Query("SELECT COUNT(s) FROM AppointmentSlots s WHERE s.clinic.clinicId = :clinicId AND s.slotDate = :slotDate AND s.status = 'AVAILABLE'")
    Long countAvailableSlotsByClinicAndDate(@Param("clinicId") Long clinicId, @Param("slotDate") LocalDate slotDate);

    @Query("SELECT s FROM AppointmentSlots s WHERE s.slotDate < CURRENT_DATE AND s.status = 'AVAILABLE'")
    List<AppointmentSlots> findExpiredAvailableSlots();

    // ✅ Method cho backward compatibility (nếu cần)
    @Query("SELECT s FROM AppointmentSlots s WHERE s.doctor.doctorId = :doctorId")
    List<AppointmentSlots> findByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT s FROM AppointmentSlots s WHERE s.doctor.doctorId = :doctorId AND s.slotDate = :date")
    List<AppointmentSlots> findByDoctorIdAndSlotDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
}