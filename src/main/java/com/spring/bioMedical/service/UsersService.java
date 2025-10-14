package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.Clinic;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.repository.UsersRepository;
import java.util.List;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;

    public Users findById(Long id) {
        return usersRepository.findById(id).orElse(null);
    }

    public Users findByUsername(String username) {
        return usersRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    public Users findByEmail(String email) {
        return usersRepository.findByEmail(email);
    }

    public List<Users> findByRole(String role) {
        return usersRepository.findByRole(role);
    }

    public void save(Users user) {
        usersRepository.save(user);
    }

    public List<Users> findAll() {
        return usersRepository.findAll();
    }

    public List<Users> findDoctorsByClinic(Long clinicId) {
        if (clinicId == null) {
            return usersRepository.findByRole("DOCTOR");
        }
        return usersRepository.findByRoleAndClinic_ClinicId("DOCTOR", clinicId);
    }

    public void assignClinic(Users user, Clinic clinic) {
        user.setClinic(clinic);
    }

    @Transactional
    public void assignClinic(Long userId, Clinic clinic) {
        Users u = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        u.setClinic(clinic); // dirty checking
    }
}
