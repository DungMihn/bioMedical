package com.spring.bioMedical.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.spring.bioMedical.entity.Appointment;
import java.util.Optional;

@Repository("appointmentRepository")
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    // Vì Clinic có PK là clinicId → dùng nested property Clinic_ClinicId
    List<Appointment> findByClinic_ClinicId(Long clinicId);
    
    // Dùng cho các thao tác xem/sửa/xoá theo id nhưng vẫn giới hạn chi nhánh
    Optional<Appointment> findByIdAndClinic_ClinicId(Integer id, Long clinicId);
}
