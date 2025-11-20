package com.spring.bioMedical.repository;

import com.spring.bioMedical.form.AppointmentForm;
import com.spring.bioMedical.entity.AppointmentBooking;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentBookingRepository extends JpaRepository<AppointmentBooking, Long> {
    
      @Query("SELECT b FROM AppointmentBooking b WHERE b.user.userId = :userId")
    List<AppointmentBooking> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT b FROM AppointmentBooking b WHERE b.slot.slotId = :slotId")
    List<AppointmentBooking> findBySlotId(@Param("slotId") Long slotId);
    
    @Query("SELECT b FROM AppointmentBooking b WHERE b.user.userId = :userId AND b.status = :status")
    List<AppointmentBooking> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Query(
      "select new com.spring.bioMedical.form.AppointmentForm(" +
      "  b.appointmentId, " +
      "  coalesce(u.fullName, u.username), " +
      "  u.email, " +
      "  s.slotDate, " +
      "  s.slotTime, " +
      "  b.notes, " +
      "  b.createdAt" +
      ") " +
      "from AppointmentBooking b " +
      "join b.user u " +
      "join b.slot s " +
      "where s.clinic.clinicId = :clinicId " +
      "order by s.slotDate asc, s.slotTime asc"
    )
    List<AppointmentForm> findRowsForClinic(@Param("clinicId") Long clinicId);

    @Query(
      "select new com.spring.bioMedical.form.AppointmentForm(" +
      "  b.appointmentId, " +
      "  coalesce(u.fullName, u.username), " +
      "  u.email, " +
      "  s.slotDate, " +
      "  s.slotTime, " +
      "  b.notes, " +
      "  b.createdAt" +
      ") " +
      "from AppointmentBooking b " +
      "join b.user u " +  // ✅ SỬA
      "join b.slot s " +  // ✅ SỬA
      "where s.clinic.clinicId = :clinicId " +  // ✅ SỬA
      "  and s.slotDate = :date " +
      "order by s.slotDate asc, s.slotTime asc"
    )
    List<AppointmentForm> findRowsForClinicAndDate(@Param("clinicId") Long clinicId,
                                                   @Param("date") LocalDate date);
    
    @Query(
      "select new com.spring.bioMedical.form.AppointmentForm(" +
      "  b.appointmentId, " +
      "  coalesce(u.fullName, u.username), " +
      "  u.email, " +
      "  s.slotDate, " +
      "  s.slotTime, " +
      "  b.notes, " +
      "  b.createdAt" +
      ") " +
      "from AppointmentBooking b " +
      "join b.user u " +  // ✅ SỬA
      "join b.slot s " +  // ✅ SỬA
      "where s.clinic.clinicId = :clinicId " +  // ✅ SỬA
      "  and s.slotDate >= :today " +
      "order by s.slotDate asc, s.slotTime asc"
    )
    List<AppointmentForm> findUpcomingRowsForClinic(@Param("clinicId") Long clinicId,
                                                    @Param("today") LocalDate today);

    // ✅ THÊM MỚI: Derived queries
   List<AppointmentBooking> findByUserUserId(Long userId);
     List<AppointmentBooking> findBySlotSlotId(Long slotId);
    List<AppointmentBooking> findByStatus(String status);
    List<AppointmentBooking> findByUserUserIdAndStatus(Long userId, String status);
    
    // ✅ THÊM MỚI: Tìm appointments theo slot và status
    @Query("SELECT b FROM AppointmentBooking b WHERE b.slot.slotId = :slotId AND b.status = :status")
    List<AppointmentBooking> findBySlotIdAndStatus(@Param("slotId") Long slotId, @Param("status") String status);
    
    // ✅ THÊM MỚI: Tìm appointments sắp tới của user
    @Query(
      "SELECT b FROM AppointmentBooking b " +
      "JOIN b.slot s " +
      "WHERE b.user.userId = :userId " +
      "AND s.slotDate >= CURRENT_DATE " +
      "ORDER BY s.slotDate ASC, s.slotTime ASC"
    )
    List<AppointmentBooking> findUpcomingAppointmentsByUser(@Param("userId") Long userId);
    
    // ✅ THÊM MỚI: Đếm appointments theo status
    @Query("SELECT COUNT(b) FROM AppointmentBooking b WHERE b.status = :status")
    Long countByStatus(@Param("status") String status);
    
    // ✅ THÊM MỚI: Tìm appointments cần xử lý (pending)
    @Query(
      "SELECT b FROM AppointmentBooking b " +
      "WHERE b.status = 'PENDING' " +
      "AND b.createdAt < :beforeDate"
    )
    List<AppointmentBooking> findPendingAppointmentsOlderThan(@Param("beforeDate") LocalDateTime beforeDate);
}