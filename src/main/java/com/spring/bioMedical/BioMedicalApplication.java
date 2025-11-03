package com.spring.bioMedical;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling; // ✅ Thêm dòng này

@SpringBootApplication
@EnableAsync
@EnableScheduling // ✅ Bật tính năng chạy tác vụ theo lịch
public class BioMedicalApplication {

	public static void main(String[] args) {
		SpringApplication.run(BioMedicalApplication.class, args);
	}
}
