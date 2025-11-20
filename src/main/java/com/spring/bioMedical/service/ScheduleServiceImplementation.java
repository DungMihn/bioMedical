package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.AppointmentBooking;
import com.spring.bioMedical.entity.AppointmentSlots;
import com.spring.bioMedical.entity.Clinics;
import com.spring.bioMedical.entity.Doctors;
import com.spring.bioMedical.repository.AppointmentBookingRepository;
import com.spring.bioMedical.repository.AppointmentSlotRepository;
import com.spring.bioMedical.repository.ClinicRepository;
import com.spring.bioMedical.repository.DoctorRepository;
import com.spring.bioMedical.repository.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class ScheduleServiceImplementation implements ScheduleService {

    private final AppointmentSlotRepository slotRepo;
    private final AppointmentBookingRepository appointmentBookingRepo;
    private final ClinicRepository clinicRepo;
    private final DoctorRepository doctorRepo;
    private final UsersRepository userRepo;

    public ScheduleServiceImplementation(AppointmentSlotRepository slotRepo,
            AppointmentBookingRepository appointmentBookingRepo,
            ClinicRepository clinicRepo,
            DoctorRepository doctorRepo,
            UsersRepository userRepo) {
        this.slotRepo = slotRepo;
        this.appointmentBookingRepo = appointmentBookingRepo;
        this.clinicRepo = clinicRepo;
        this.doctorRepo = doctorRepo;
        this.userRepo = userRepo; // 👈 giữ nguyên
    }

    /**
     * Helper: hôm nay (nếu cần có thể đổi sang ZoneId.of("Asia/Ho_Chi_Minh")).
     */
    private static LocalDate today() {
        return LocalDate.now();
    }

    @Override
    public List<AppointmentSlots> listSlots(Long clinicId, LocalDate date) {
        return slotRepo.findByClinicClinicIdAndSlotDateOrderBySlotTimeAsc(clinicId, date);
    }

    @Override
    @Transactional
    public void updateClinicHours(Long clinicId, LocalTime openTime, LocalTime closeTime, Integer slotMinutes) {
        if (openTime == null || closeTime == null || !openTime.isBefore(closeTime)) {
            throw new IllegalArgumentException("Giờ mở/đóng không hợp lệ");
        }
        int minutes = (slotMinutes == null || slotMinutes <= 0) ? 60 : slotMinutes;

        Clinics c = clinicRepo.findById(clinicId).orElseThrow();
        c.setOpenTime(openTime);
        c.setCloseTime(closeTime);
        c.setSlotTime(minutes);
        clinicRepo.save(c);
    }

    /**
     * Lấy bác sĩ mặc định (doctor_id nhỏ nhất) của chi nhánh.
     */
    private Long resolveDefaultDoctorId(Long clinicId) {
        return doctorRepo.findTopByClinicClinicIdOrderByDoctorIdAsc(clinicId)
                .map(d -> d.getDoctorId())
                .orElseThrow(() -> new IllegalStateException(
                "Chi nhánh #" + clinicId + " chưa có bác sĩ nào. "
                + "Hãy tạo 1 bác sĩ (ví dụ 'Bác sĩ chung') để hệ thống gán slot."
        ));
    }

    // ================== SINH SLOT (UI không chọn bác sĩ) ==================
    /**
     * Sinh slot theo giờ clinic, bỏ qua khoảng nghỉ (vd 12:00–13:00). KHÔNG
     * truyền doctorId; hệ thống tự gán bác sĩ mặc định của chi nhánh.
     */
    @Transactional
    public int generateSlotsForDateWithBreak(Long clinicId,
            LocalDate date,
            LocalTime breakStart,
            LocalTime breakEnd) {
        Long doctorId = resolveDefaultDoctorId(clinicId);
        return generateSlotsForDateWithBreak(clinicId, date, doctorId, breakStart, breakEnd);
    }

    // ================== SINH SLOT (có doctorId) ==================
    /**
     * Sinh slot theo giờ clinic cho 1 bác sĩ cụ thể, có thể chỉ định giờ nghỉ.
     * Dùng native query + java.sql.Time để tránh lỗi TIME vs DATETIME.
     */
    @Transactional
    public int generateSlotsForDateWithBreak(Long clinicId,
            LocalDate date,
            Long doctorId,
            LocalTime breakStart,
            LocalTime breakEnd) {

        // ❗ Không cho tạo slot ở ngày quá khứ
        if (date.isBefore(today())) {
            throw new IllegalArgumentException("Không thể tạo slot cho ngày đã qua.");
        }

        Clinics clinic = clinicRepo.findById(clinicId).orElseThrow();

        // 👇 XOÁ AVAILABLE TRƯỚC
        slotRepo.deleteAvailableByClinicAndDate(clinicId, date);

        LocalTime open = clinic.getOpenTime();
        LocalTime close = clinic.getCloseTime();
        int minutes = (clinic.getSlotTime() == null) ? 60 : clinic.getSlotTime();

        int created = 0;
        for (LocalTime t = open; !t.plusMinutes(minutes).isAfter(close); t = t.plusMinutes(minutes)) {
            LocalTime tEnd = t.plusMinutes(minutes);
            boolean overlapBreak = (breakStart != null && breakEnd != null)
                    && (t.isBefore(breakEnd) && tEnd.isAfter(breakStart));
            if (overlapBreak) {
                continue;
            }

            boolean exists = slotRepo.findOneNative(
                    clinicId, date, t.toString() + ":00", doctorId
            ).isPresent();

            if (!exists) {
                AppointmentSlots s = new AppointmentSlots();

                // ✅ SỬA: Đổi tên biến
                Clinics clinicEntity = new Clinics();
                clinicEntity.setClinicId(clinicId);
                s.setClinic(clinicEntity);

                Doctors doctorEntity = new Doctors();
                doctorEntity.setDoctorId(doctorId);
                s.setDoctor(doctorEntity);

                s.setSlotDate(date);
                s.setSlotTime(t);
                s.setStatus("AVAILABLE");

                slotRepo.save(s);
                created++;
            }
        }
        return created;
    }

    /**
     * Sinh slot không có giờ nghỉ (giữ cho tương thích code cũ).
     */
    @Override
    @Transactional
    public int generateSlotsForDate(Long clinicId, LocalDate date, Long doctorId) {
        // ❗ Không cho tạo slot ở ngày quá khứ
        if (date.isBefore(today())) {
            throw new IllegalArgumentException("Không thể tạo slot cho ngày đã qua.");
        }
        return generateSlotsForDateWithBreak(clinicId, date, doctorId, null, null);
    }

    // ================== BOOK / CANCEL ==================
    /**
     * Book chuẩn: đánh dấu slot BOOKED nếu đang AVAILABLE, và tạo
     * AppointmentBooking.
     */
    @Override
    @Transactional
    public AppointmentBooking book(Long userId, Long slotId, String note) {
        // ❗ Không cho đặt slot quá khứ (hoặc đã qua giờ trong ngày hiện tại)
        AppointmentSlots slot = slotRepo.findById(slotId).orElseThrow();
        LocalDate d = slot.getSlotDate();
        LocalTime t = slot.getSlotTime();
        LocalDate today = today();
        boolean pastDay = d.isBefore(today);
        boolean pastTime = d.isEqual(today) && t.isBefore(LocalTime.now());
        if (pastDay || pastTime) {
            throw new IllegalStateException("Khung giờ đã qua, không thể đặt.");
        }

        int updated = slotRepo.bookIfAvailable(slotId);
        if (updated == 0) {
            throw new IllegalStateException("Khung giờ đã hết hoặc bị khóa.");
        }

        AppointmentBooking a = new AppointmentBooking();
        a.setUserId(userId);
        a.setSlotId(slotId);
        a.setStatus("PENDING"); // hoặc CONFIRMED theo flow của bạn
        a.setNotes(note);
        a.setCreatedAt(LocalDateTime.now());
        return appointmentBookingRepo.save(a);
    }

    /**
     * Book có kiểm tra chi nhánh: chỉ cho phép đặt khi slot thuộc đúng clinic
     * của actor.
     */
    @Override
    @Transactional
    public AppointmentBooking bookIntoClinic(Long userId, Long slotId, String note, Long clinicIdOfActor) {
        // Nếu không tìm thấy slot theo đúng chi nhánh => chặn ngay
        slotRepo.findBySlotIdAndClinicClinicId(slotId, clinicIdOfActor)
                .orElseThrow(() -> new IllegalStateException(
                "Slot không thuộc chi nhánh của bạn hoặc không tồn tại."
        ));

        // Tái sử dụng flow book chuẩn (đã có check AVAILABLE + kiểm tra quá khứ)
        return book(userId, slotId, note);
    }

    @Override
    @Transactional
    public void cancel(Long appointmentId) {
        AppointmentBooking a = appointmentBookingRepo.findById(appointmentId).orElseThrow();
        slotRepo.releaseIfBooked(a.getSlotId()); // trả slot về AVAILABLE
        a.setStatus("CANCELLED");
        // Xoá record (để bảng Appointments không còn hiển thị)
        appointmentBookingRepo.delete(a);
    }

    @Override
    @Transactional
    public AppointmentBooking bookByEmail(String email, Long slotId, String note) {
        if (email == null || !email.toLowerCase().endsWith("@gmail.com")) {
            throw new IllegalArgumentException("Vui lòng nhập Gmail hợp lệ (định dạng ...@gmail.com).");
        }

        Long userId = userRepo.findByEmailIgnoreCase(email.trim())
                .map(u -> u.getUserId())
                .orElseThrow(()
                        -> new IllegalArgumentException("Không tìm thấy người dùng với email: " + email));

        // tái sử dụng flow book cũ (giữ đúng logic set status, PENDING, vv.)
        return book(userId, slotId, note);
    }
}
