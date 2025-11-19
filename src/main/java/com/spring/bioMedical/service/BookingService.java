package com.spring.bioMedical.service;

import com.spring.bioMedical.dto.BookingRequest;
import com.spring.bioMedical.dto.GuestBookingForm;
import com.spring.bioMedical.entity.*;
import com.spring.bioMedical.repository.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    @Autowired
    private AppointmentRepository appointmentRepo;
    @Autowired
    private AppointmentSlotRepository slotRepo;
    @Autowired
    private UsersRepository usersRepo;
    @Autowired
    private EmailService emailService;

    /**
     * ✅ For logged-in users
     */
    public void createBooking(BookingRequest req, Users user) {
        AppointmentSlots slot = slotRepo.findById(req.getSlotId())
                .orElseThrow(() -> new RuntimeException("Time slot not found."));

        if (!"AVAILABLE".equalsIgnoreCase(slot.getStatus())) {
            throw new RuntimeException("This time slot has already been booked!");
        }

        Appointments appointment = new Appointments();
        appointment.setSlotId(slot);
        appointment.setUserId(user);
        appointment.setStatus("PENDING");
        appointment.setNotes(req.getNotes());
        appointment.setCreatedAt(new Date());

        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setOtpCode(otp);
        user.setOtpExpiry(new Date(System.currentTimeMillis() + 5 * 60 * 1000));
        usersRepo.save(user);
        appointmentRepo.save(appointment);

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(user.getEmail());
        mail.setSubject("Appointment Confirmation OTP");
        mail.setText("Hello " + user.getFullName() + ",\n\n"
                + "You have requested an appointment at Remedic.\n"
                + "Your OTP code is: " + otp + "\n"
                + "This code will expire in 5 minutes.\n\n"
                + "Appointment details:\n"
                + "- Date: " + slot.getSlotDate() + "\n"
                + "- Time: " + slot.getSlotTime() + "\n\n"
                + "Best regards,\nRemedic Team");
        mail.setFrom("dungminh2505@gmail.com");
        emailService.sendEmail(mail);
    }

    /**
     * ✅ For guest users
     */
    public void createGuestBooking(GuestBookingForm req) {
        try {
            Users existing = usersRepo.findByEmail(req.getEmail());
            Users guest;

            if (existing == null) {
                guest = new Users();
                guest.setUsername("guest_" + System.currentTimeMillis());
                guest.setEnabled(false);
                guest.setRole("PATIENT");
                guest.setCreatedAt(new Date());
            } else if (Boolean.TRUE.equals(existing.getEnabled())) {
                throw new RuntimeException("EMAIL_EXISTED");
            } else {
                guest = existing;
                guest.setUpdatedAt(new Date());
            }

            guest.setFullName(req.getFullName());
            guest.setEmail(req.getEmail());
            guest.setPhone(req.getPhone());
            guest.setGender(req.getGender());
            guest.setDateOfBirth(req.getDateOfBirth());
            guest.setPasswordHash(UUID.randomUUID().toString().substring(0, 8));

            String otp = String.format("%06d", new Random().nextInt(999999));
            guest.setOtpCode(otp);
            guest.setOtpExpiry(new Date(System.currentTimeMillis() + 5 * 60 * 1000));
            usersRepo.save(guest);

            AppointmentSlots slot = slotRepo.findById(req.getSlotId())
                    .orElseThrow(() -> new RuntimeException("Time slot not found."));

            Appointments app = new Appointments();
            app.setUserId(guest);
            app.setSlotId(slot);
            app.setStatus("PENDING");
            app.setNotes(req.getNotes());
            app.setCreatedAt(new Date());
            appointmentRepo.save(app);

            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(req.getEmail());
            mail.setSubject("Appointment Confirmation OTP");
            mail.setText("Hello " + req.getFullName() + ",\n\n"
                    + "You have requested an appointment at Remedic.\n"
                    + "Your OTP code is: " + otp + "\n"
                    + "This code will expire in 5 minutes.\n\n"
                    + "Appointment details:\n"
                    + "- Date: " + slot.getSlotDate() + "\n"
                    + "- Time: " + slot.getSlotTime() + "\n\n"
                    + "Best regards,\nRemedic Team");
            mail.setFrom("dungminh2505@gmail.com");
            emailService.sendEmail(mail);
        } catch (RuntimeException ex) {
            if ("EMAIL_EXISTED".equals(ex.getMessage())) {
                throw new RuntimeException("This email is already registered. Please log in to book an appointment.");
            }
            throw new RuntimeException("Unable to create appointment. Please check your information and try again.");
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred. Please try again later.");
        }
    }

    /**
     * ✅ Confirm OTP
     */
    public void confirmAppointment(String email, String otp) {
        Users user = usersRepo.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found.");
        }
        if (user.getOtpCode() == null || !user.getOtpCode().equals(otp)) {
            throw new RuntimeException("Invalid OTP code.");
        }
        if (user.getOtpExpiry() == null || user.getOtpExpiry().before(new Date())) {
            throw new RuntimeException("OTP code has expired.");
        }

        Appointments latest = appointmentRepo.findAll().stream()
                .filter(a -> a.getUserId().getUserId().equals(user.getUserId()))
                .filter(a -> "PENDING".equalsIgnoreCase(a.getStatus()))
                .max(Comparator.comparing(Appointments::getCreatedAt))
                .orElseThrow(() -> new RuntimeException("No pending appointment found."));

        // 🔥 Always reload the slot from DB to get the latest status
        AppointmentSlots slot = slotRepo.findById(latest.getSlotId().getSlotId())
                .orElseThrow(() -> new RuntimeException("Time slot not found or deleted."));

        // 🧠 If slot already booked, cancel this pending appointment
        if (!"AVAILABLE".equalsIgnoreCase(slot.getStatus())) {
            latest.setStatus("CANCELLED");
            appointmentRepo.save(latest);
            throw new RuntimeException("This time slot is no longer available and your appointment has been cancelled.");
        }

        // ✅ Confirm normally
        latest.setStatus("CONFIRMED");
        appointmentRepo.save(latest);

        slot.setStatus("BOOKED");
        slotRepo.save(slot);

        user.setOtpCode(null);
        user.setOtpExpiry(null);
        usersRepo.save(user);

        SimpleMailMessage successMail = new SimpleMailMessage();
        successMail.setTo(user.getEmail());
        successMail.setSubject("Appointment Confirmed");
        successMail.setText("Hello " + user.getFullName() + ",\n\n"
                + "Your appointment has been successfully confirmed!\n\n"
                + "Details:\n"
                + "- Date: " + slot.getSlotDate() + "\n"
                + "- Time: " + slot.getSlotTime() + "\n"
                + "- Notes: " + (latest.getNotes() != null ? latest.getNotes() : "(None)") + "\n\n"
                + "Thank you for choosing Remedic!\n\nRemedic Team");
        successMail.setFrom("dungminh2505@gmail.com");
        emailService.sendEmail(successMail);
    }

    public List<Appointments> getAllAppointments() {
        return appointmentRepo.findAll();
    }

    public Appointments cancelAppointment(Long appointmentId) {
        Appointments app = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found."));
        if (!app.getStatus().equalsIgnoreCase("PENDING")
                && !app.getStatus().equalsIgnoreCase("CONFIRMED")) {
            throw new RuntimeException("Only pending or confirmed appointments can be cancelled.");
        }
        app.setStatus("CANCELLED");
        appointmentRepo.save(app);

        AppointmentSlots slot = app.getSlotId();
        slot.setStatus("AVAILABLE");
        slotRepo.save(slot);
        return app;
    }

    public Appointments getLatestPendingAppointment(Users user) {
        return appointmentRepo.findAll().stream()
                .filter(a -> a.getUserId().getUserId().equals(user.getUserId()))
                .filter(a -> "PENDING".equalsIgnoreCase(a.getStatus()))
                .max(Comparator.comparing(Appointments::getCreatedAt))
                .orElse(null);
    }

    public void saveAppointment(Appointments app) {
        appointmentRepo.save(app);
    }

}
