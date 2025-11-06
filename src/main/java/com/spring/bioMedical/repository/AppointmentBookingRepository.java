package com.spring.bioMedical.repository;

import com.spring.bioMedical.form.AppointmentForm;
import com.spring.bioMedical.entity.AppointmentBooking;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface AppointmentBookingRepository extends JpaRepository<AppointmentBooking, Long> {

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
      "join Users u on u.userId = b.userId " +
      "join AppointmentSlot s on s.slotId = b.slotId " +
      "where s.clinicId = :clinicId " +
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
      "join Users u on u.userId = b.userId " +
      "join AppointmentSlot s on s.slotId = b.slotId " +
      "where s.clinicId = :clinicId " +
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
  "join Users u on u.userId = b.userId " +
  "join AppointmentSlot s on s.slotId = b.slotId " +
  "where s.clinicId = :clinicId " +
  "  and s.slotDate >= :today " +     // ✅ lọc lịch từ hôm nay trở đi
  "order by s.slotDate asc, s.slotTime asc"
)
List<AppointmentForm> findUpcomingRowsForClinic(@Param("clinicId") Long clinicId,
                                                @Param("today") LocalDate today);

}
