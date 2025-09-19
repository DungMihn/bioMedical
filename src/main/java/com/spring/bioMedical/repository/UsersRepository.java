package com.spring.bioMedical.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.spring.bioMedical.entity.Users;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {
    
    // Tìm user theo username (login dùng username)
    Users findByUsername(String username);

    // Tìm user theo email
    Users findByEmail(String email);

    // Tìm user theo role (ADMIN, DOCTOR, PATIENT, GUEST)
    List<Users> findByRole(String role);
}
