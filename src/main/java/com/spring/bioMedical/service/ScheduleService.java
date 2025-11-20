package com.spring.bioMedical.service;


import com.spring.bioMedical.entity.AppointmentBooking;
import com.spring.bioMedical.entity.AppointmentSlots;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

 public interface ScheduleService {
     List<AppointmentSlots> listSlots(Long clinicId, LocalDate date);
     AppointmentBooking book(Long userId, Long slotId, String note);                // đặt lịch an toàn
    AppointmentBooking bookIntoClinic(Long userId, Long slotId, String note, Long clinicIdOfActor); // 🔒 chi nhánh
     void updateClinicHours(Long clinicId, LocalTime openTime, LocalTime closeTime, Integer slotMinutes);
     void cancel(Long appointmentId);                                        // huỷ và mở lại slot
     int generateSlotsForDateWithBreak(Long clinicId,
                                   java.time.LocalDate date,
                                   java.time.LocalTime breakStart,
                                   java.time.LocalTime breakEnd);
     int generateSlotsForDate(Long clinicId, LocalDate date, Long doctorId);
     AppointmentBooking bookByEmail(String email, Long slotId, String note);
 
 }
