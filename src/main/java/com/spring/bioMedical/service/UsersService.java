package com.spring.bioMedical.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.repository.UsersRepository;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    
       @Autowired
    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public Users findByUsername(String username) {
        return usersRepository.findByUsername(username);
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
     public Users findByOtpCode(String otpCode) {
        return usersRepository.findByOtpCode(otpCode);
    }

    public Users findById(Long userId) {
        return usersRepository.findById(userId).orElse(null);
    }

    public List<Users> searchPatients(String query, int limit) {
        try {
            // Lấy tất cả patients
            List<Users> allPatients = usersRepository.findByRole("PATIENT");

            if (allPatients == null || allPatients.isEmpty()) {
                return new ArrayList<>();
            }

            String searchLower = query.toLowerCase().trim();

            return allPatients.stream()
                    .filter(patient -> {
                        try {
                            // Kiểm tra patient không null
                            if (patient == null) {
                                return false;
                            }

                            // Kiểm tra tên
                            boolean nameMatch = false;
                            if (patient.getFullName() != null && !patient.getFullName().trim().isEmpty()) {
                                nameMatch = patient.getFullName().toLowerCase().contains(searchLower);
                            }

                            // Kiểm tra số điện thoại
                            boolean phoneMatch = false;
                            if (patient.getPhone() != null && !patient.getPhone().trim().isEmpty()) {
                                phoneMatch = patient.getPhone().contains(query);
                            }

                            // Kiểm tra email (tùy chọn)
                            boolean emailMatch = false;
                            if (patient.getEmail() != null && !patient.getEmail().trim().isEmpty()) {
                                emailMatch = patient.getEmail().toLowerCase().contains(searchLower);
                            }

                            return nameMatch || phoneMatch || emailMatch;

                        } catch (Exception e) {
                            // Bỏ qua patient bị lỗi
                            System.err.println("Error processing patient: " + e.getMessage());
                            return false;
                        }
                    })
                    .sorted((p1, p2) -> {
                        // Ưu tiên kết quả trùng tên hơn trùng số điện thoại
                        boolean p1NameMatch = p1.getFullName() != null
                                && p1.getFullName().toLowerCase().contains(searchLower);
                        boolean p2NameMatch = p2.getFullName() != null
                                && p2.getFullName().toLowerCase().contains(searchLower);

                        if (p1NameMatch && !p2NameMatch) {
                            return -1;
                        }
                        if (!p1NameMatch && p2NameMatch) {
                            return 1;
                        }
                        return 0;
                    })
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error in searchPatients: " + e.getMessage());
            return new ArrayList<>();
        }
    }

}
