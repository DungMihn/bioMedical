//package com.spring.bioMedical;
//
//import com.spring.bioMedical.entity.Users;
//import com.spring.bioMedical.repository.UsersRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//public class DataLoader implements CommandLineRunner {
//
//    private final UsersRepository userRepository;
//
//    public DataLoader(UsersRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    @Override
//    public void run(String... args) {
//        // Nếu chưa có admin thì tạo mặc định
//        if (userRepository.findByEmail("admin@gmail.com") == null) {
//            Users admin = new Users();
//            admin.setEmail("admin@gmail.com");
//            admin.setPasswordHash("admin123");   // mật khẩu đơn giản (bạn có thể mã hóa BCrypt sau)
//            admin.setUsername("Admin");
//            admin.setEnabled(true);
//            admin.setRole("ADMIN");
//
//            userRepository.save(admin);
//            System.out.println("✅ Tạo user mặc định: admin@gmail.com / admin123");
//        }
//    }
//}
