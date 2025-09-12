package com.spring.bioMedical;

import com.spring.bioMedical.entity.User;
import com.spring.bioMedical.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataLoader(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        // Nếu chưa có admin thì tạo mặc định
        if (userRepository.findByEmail("admin@gmail.com") == null) {
            User admin = new User();
            admin.setEmail("admin@gmail.com");
            admin.setPassword("admin123");   // mật khẩu đơn giản (bạn có thể mã hóa BCrypt sau)
            admin.setFirstName("Default");
            admin.setLastName("Admin");
            admin.setEnabled(true);
            admin.setRole("ROLE_ADMIN");

            userRepository.save(admin);
            System.out.println("✅ Tạo user mặc định: admin@gmail.com / admin123");
        }
    }
}
