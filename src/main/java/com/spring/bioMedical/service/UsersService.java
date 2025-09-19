package com.spring.bioMedical.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.repository.UsersRepository;

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
}
