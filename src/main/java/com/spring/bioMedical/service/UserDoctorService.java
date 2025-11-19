/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.bioMedical.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.repository.UsersRepository;
import java.awt.print.Pageable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;

/**
 *
 * @author Admin
 */
@Service
public class UserDoctorService {

    private final UsersRepository usersRepository;

    @Autowired
    public UserDoctorService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }
    // Thêm vào UsersService
// Trong UsersService

    public List<Users> searchPatients(String query, Set<Long> patientIds, int limit) {
        try {
            if (patientIds == null || patientIds.isEmpty() || query == null || query.trim().isEmpty()) {
                return new ArrayList<>();
            }

            String searchTerm = query.trim();
            Set<Users> results = new HashSet<>();

            // Tìm theo tên
            results.addAll(usersRepository.findByUserIdInAndFullNameContainingIgnoreCase(patientIds, searchTerm));

            // Tìm theo số điện thoại
            results.addAll(usersRepository.findByUserIdInAndPhoneContaining(patientIds, searchTerm));

            // Tìm theo email
            results.addAll(usersRepository.findByUserIdInAndEmailContainingIgnoreCase(patientIds, searchTerm));

            return results.stream()
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error searching patients: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
