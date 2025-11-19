package com.spring.bioMedical.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.spring.bioMedical.entity.Users;
import java.awt.print.Pageable;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    // Tìm user theo username (login dùng username)
    Users findByUsername(String username);

    // Tìm user theo email
    Users findByEmail(String email);

    // Tìm user theo role (ADMIN, DOCTOR, PATIENT, GUEST)
    List<Users> findByRole(String role);

//    Optional<Users> findByEmail(String email);
//
//    Optional<Users> findByUsername(String username);
//    @Query("SELECT u FROM Users u WHERE u.role = :role")
//    List<Users> findByRole(@Param("role") String role);
    // Thêm vào UserRepository
    // Trong UsersRepository
// Trong UsersRepository - cách đơn giản nhất
    List<Users> findByUserIdInAndFullNameContainingIgnoreCase(Set<Long> patientIds, String fullName);

    List<Users> findByUserIdInAndPhoneContaining(Set<Long> patientIds, String phone);

    List<Users> findByUserIdInAndEmailContainingIgnoreCase(Set<Long> patientIds, String email);
    
    
    Users findByOtpCode(String otpCode);
}
