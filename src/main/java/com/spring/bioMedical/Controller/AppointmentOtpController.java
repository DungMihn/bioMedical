package com.spring.bioMedical.Controller;

import com.spring.bioMedical.entity.AppointmentSlots;
import com.spring.bioMedical.entity.Appointments;
import com.spring.bioMedical.service.BookingService;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.repository.UsersRepository;
import com.spring.bioMedical.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.Random;

@Controller
public class AppointmentOtpController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UsersRepository usersRepo;

    @Autowired
    private EmailService emailService;

    // ✅ Display OTP input form
    @GetMapping("/verify-appointment-otp")
    public ModelAndView showOtpForm(@RequestParam("email") String email) {
        ModelAndView mv = new ModelAndView("verify-appointment-otp");
        mv.addObject("email", email);
        return mv;
    }

    // ✅ Verify OTP
    @PostMapping("/verify-appointment-otp")
    public ModelAndView verifyAppointmentOtp(@RequestParam("email") String email,
                                             @RequestParam("otp") String otp) {
        ModelAndView mv = new ModelAndView("verify-appointment-otp");
        try {
            bookingService.confirmAppointment(email, otp);
            mv.addObject("success", true);
            mv.addObject("email", email);
        } catch (Exception e) {
            mv.addObject("errorMessage", e.getMessage());
            mv.addObject("email", email);
        }
        return mv;
    }

    // ✅ Resend OTP (for appointment booking)
@GetMapping("/resend-otp")
public ModelAndView resendOtp(@RequestParam("email") String email) {
    ModelAndView mv = new ModelAndView("verify-appointment-otp");

    try {
        Users user = usersRepo.findByEmail(email);
        if (user == null) throw new RuntimeException("User email not found");

        // 🔍 Find latest pending appointment
        Appointments pendingApp = bookingService.getLatestPendingAppointment(user);
        if (pendingApp == null) {
            throw new RuntimeException("No pending appointment found to resend OTP.");
        }

        AppointmentSlots slot = pendingApp.getSlot();
        if (!"AVAILABLE".equalsIgnoreCase(slot.getStatus())) {
            // If slot already booked by another, cancel appointment and stop
            pendingApp.setStatus("CANCELLED");
            bookingService.saveAppointment(pendingApp);
            throw new RuntimeException("This time slot is already booked by another patient. Your pending appointment has been cancelled.");
        }

        // ✅ Generate new OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setOtpCode(otp);
        user.setOtpExpiry(new Date(System.currentTimeMillis() + 5 * 60 * 1000)); // 5 minutes
        usersRepo.save(user);

        // ✅ Send OTP email
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(user.getEmail());
        mail.setSubject("Appointment OTP Code (Resent)");
        mail.setText("Hello " + user.getFullName() + ",\n\n"
                + "Your new OTP code is: " + otp
                + "\nIt will expire in 5 minutes.\n\nRemedic Team");
        mail.setFrom("dungminh2505@gmail.com");
        emailService.sendEmail(mail);

        mv.addObject("email", email);
        mv.addObject("successMessage", "A new OTP has been sent to your email.");
    } catch (Exception e) {
        mv.addObject("email", email);
        mv.addObject("errorMessage", "Failed to resend OTP: " + e.getMessage());
    }

    return mv;
}
}