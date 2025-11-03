package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.AppointmentSlots;
import java.sql.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlots, Long> {

    // ✅ Tìm slot theo trạng thái
    List<AppointmentSlots> findByStatus(String status);

    // ✅ Tìm tất cả slot của 1 chi nhánh theo trạng thái (cũ)
    List<AppointmentSlots> findByClinicId_ClinicIdAndStatus(Long clinicId, String status);

    // ✅ Tìm slot của 1 chi nhánh theo ngày và trạng thái (mới - dùng để lọc đúng ngày)
    @Query("SELECT s FROM AppointmentSlots s " +
           "WHERE s.clinicId.clinicId = :clinicId " +
           "AND s.slotDate = :slotDate " +
           "AND s.status = :status")
    List<AppointmentSlots> findByClinicIdAndDateAndStatus(
            @Param("clinicId") Long clinicId,
            @Param("slotDate") Date slotDate,
            @Param("status") String status);
}
