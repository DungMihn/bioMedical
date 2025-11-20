package com.spring.bioMedical.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.spring.bioMedical.entity.Users;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    // ✅ GIỮ NGUYÊN: Các methods đúng
    Users findByUsername(String username);
    Users findByEmail(String email);
    List<Users> findByRole(String role);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Users findByOtpCode(String otpCode);
    Optional<Users> findByEmailIgnoreCase(String email);

    // ✅ SỬA: Dùng relationship clinic (đúng với entity)
    List<Users> findByRoleAndClinicClinicId(String role, Long clinicId);

    // ✅ GIỮ NGUYÊN: Các methods tìm kiếm theo Set
    List<Users> findByUserIdInAndFullNameContainingIgnoreCase(Set<Long> patientIds, String fullName);
    List<Users> findByUserIdInAndPhoneContaining(Set<Long> patientIds, String phone);
    List<Users> findByUserIdInAndEmailContainingIgnoreCase(Set<Long> patientIds, String email);

    // ✅ THÊM: Method với Optional
    @Query("SELECT u FROM Users u WHERE u.username = :username")
    Optional<Users> findOptionalByUsername(@Param("username") String username);

    // ✅ THÊM MỚI: Các methods hữu ích khác
    List<Users> findByEnabledTrue();
    List<Users> findByRoleAndEnabledTrue(String role);
    
    @Query("SELECT u FROM Users u WHERE u.role = 'PATIENT' AND LOWER(u.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Users> findPatientsByName(@Param("name") String name);
    
    @Query("SELECT COUNT(u) FROM Users u WHERE u.role = :role")
    Long countByRole(@Param("role") String role);
    
    List<Users> findByPhone(String phone);
    List<Users> findByFullNameContainingIgnoreCase(String fullName);
    
//    // ✅ THÊM MỚI: Tìm users theo clinic
//    List<Users> findByClinicClinicId(Long clinicId);
    
    // ✅ THÊM MỚI: Tìm users có clinic (không null)
    List<Users> findByClinicIsNotNull();
    
    // ✅ THÊM MỚI: Tìm users không có clinic
    List<Users> findByClinicIsNull();
    
    // ✅ THÊM MỚI: Tìm admin của clinic
    @Query("SELECT u FROM Users u WHERE u.role = 'ADMIN_BRANCH' AND u.clinic.clinicId = :clinicId")
    List<Users> findAdminByClinicId(@Param("clinicId") Long clinicId);
}