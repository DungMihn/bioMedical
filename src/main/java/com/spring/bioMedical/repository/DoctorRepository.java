package com.spring.bioMedical.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.spring.bioMedical.entity.Doctor;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Doctor findByUser_UserId(Long userId);
    // Lấy 1 bác sĩ bất kỳ thuộc chi nhánh (ưu tiên id nhỏ nhất)
    Optional<Doctor> findTopByClinic_ClinicIdOrderByDoctorIdAsc(Long clinicId);

}
