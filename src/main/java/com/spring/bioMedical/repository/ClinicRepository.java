package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.Clinics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicRepository extends JpaRepository<Clinics, Long> {
    
    // Tìm clinic theo tên (tìm kiếm gần đúng)
    @Query("SELECT c FROM Clinics c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Clinics> findByNameContainingIgnoreCase(@Param("name") String name);
    
    // Tìm clinic theo số điện thoại
    Optional<Clinics> findByPhone(String phone);
    
    // Lấy tất cả clinics sắp xếp theo tên
    List<Clinics> findAllByOrderByNameAsc();
    
    // Kiểm tra xem clinic có tồn tại không
    boolean existsByName(String name);
    
    // Đếm số lượng clinics
    @Query("SELECT COUNT(c) FROM Clinics c")
    long countClinics();
    
    // Tìm clinics có slot time cụ thể
    List<Clinics> findBySlotTime(Integer slotTime);
}