package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.Users;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByUsername(String username); // <- trả về Optional
    Users findByEmail(String email);
    List<Users> findByRole(String role);

    // Dùng Clinic_ClinicId vì field là clinicId trong entity Clinic
    List<Users> findByRoleAndClinic_ClinicId(String role, Long clinicId);
}
