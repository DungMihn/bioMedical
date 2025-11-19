/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.AppointmentSlots;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Admin
 */
@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlots, Long> {

    @Query("SELECT s FROM AppointmentSlots s WHERE s.doctorId = :doctorId")
    List<AppointmentSlots> findByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT s FROM AppointmentSlots s WHERE s.doctorId = :doctorId AND s.slotDate = :date")
    List<AppointmentSlots> findByDoctorIdAndSlotDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    @Query("SELECT s FROM AppointmentSlots s WHERE s.doctorId = :doctorId AND s.slotDate >= CURRENT_DATE")
    List<AppointmentSlots> findUpcomingSlotsByDoctorId(@Param("doctorId") Long doctorId);

    // ✅ Tìm slot theo trạng thái
    List<AppointmentSlots> findByStatus(String status);

    // ❌ SỬA LỖI: Thay thế method gây lỗi bằng 1 trong các cách sau:

    // Cách 1: Sử dụng @Query (ĐỀ XUẤT)
    @Query("SELECT s FROM AppointmentSlots s WHERE s.clinicId.clinicId = :clinicId AND s.status = :status")
    List<AppointmentSlots> findByClinicIdAndStatus(@Param("clinicId") Long clinicId, @Param("status") String status);

    // Cách 2: Sửa tên method theo đúng convention
    // List<AppointmentSlots> findByClinicIdClinicIdAndStatus(Long clinicId, String status);

    // Cách 3: Đổi tên property trong entity thành "clinic" rồi dùng:
    // List<AppointmentSlots> findByClinicClinicIdAndStatus(Long clinicId, String status);

    // ✅ Tìm slot của 1 chi nhánh theo ngày và trạng thái (mới - dùng để lọc đúng ngày)
    @Query("SELECT s FROM AppointmentSlots s "
            + "WHERE s.clinicId.clinicId = :clinicId "
            + "AND s.slotDate = :slotDate "
            + "AND s.status = :status")
    List<AppointmentSlots> findByClinicIdAndDateAndStatus(
            @Param("clinicId") Long clinicId,
            @Param("slotDate") Date slotDate,
            @Param("status") String status);

    // ✅ Thêm method tìm slot theo clinicId và slotDate (nếu cần)
    @Query("SELECT s FROM AppointmentSlots s WHERE s.clinicId.clinicId = :clinicId AND s.slotDate = :slotDate")
    List<AppointmentSlots> findByClinicIdAndSlotDate(@Param("clinicId") Long clinicId, @Param("slotDate") LocalDate slotDate);
}