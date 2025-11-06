package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.AppointmentSlot;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {

    List<AppointmentSlot> findByClinicIdAndSlotDateOrderBySlotTimeAsc(Long clinicId, LocalDate date);

    Optional<AppointmentSlot> findBySlotIdAndClinicId(Long slotId, Long clinicId);

    @Query(value = ""
        + "SELECT TOP 1 * "
        + "FROM AppointmentSlots "
        + "WHERE clinic_id = :clinicId "
        + "  AND slot_date = :slotDate "
        + "  AND CAST(:slotTime AS time) = CAST(slot_time AS time) "
        + "  AND doctor_id = :doctorId",
        nativeQuery = true)
    Optional<AppointmentSlot> findOneNative(
            @Param("clinicId") Long clinicId,
            @Param("slotDate") LocalDate slotDate,
            @Param("slotTime") String slotTime,
            @Param("doctorId") Long doctorId);

   
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM AppointmentSlots WHERE clinic_id=:clinicId AND slot_date=:slotDate AND status='AVAILABLE'", nativeQuery = true)
    int deleteAvailableByClinicAndDate(@Param("clinicId") Long clinicId,
                                       @Param("slotDate") LocalDate slotDate);

    @Modifying
    @Query(value = "UPDATE AppointmentSlots SET status='BOOKED' WHERE slot_id=:slotId AND status='AVAILABLE'", nativeQuery = true)
    int bookIfAvailable(@Param("slotId") Long slotId);

    @Modifying
    @Query(value = "UPDATE AppointmentSlots SET status='AVAILABLE' WHERE slot_id=:slotId AND status='BOOKED'", nativeQuery = true)
    int releaseIfBooked(@Param("slotId") Long slotId);

}
