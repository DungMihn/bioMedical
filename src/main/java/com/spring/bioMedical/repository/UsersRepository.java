package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.Users;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByUsername(String username);
    Users findByEmail(String email);
    List<Users> findByRole(String role);
    List<Users> findByRoleAndClinic_ClinicId(String role, Long clinicId);

    // NEW
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Nếu muốn không phân biệt hoa/thường:
    // boolean existsByUsernameIgnoreCase(String username);
    // boolean existsByEmailIgnoreCase(String email);
}
