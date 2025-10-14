package com.spring.bioMedical.Controller;

import com.spring.bioMedical.entity.Appointment;
import com.spring.bioMedical.entity.Doctor;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.service.AppointmentServiceImplementation;
import com.spring.bioMedical.service.DoctorService;
import com.spring.bioMedical.service.UsersService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    private final UsersService usersService;
    private final AppointmentServiceImplementation appointmentService;
    private final DoctorService doctorService;

    public DoctorController(UsersService usersService,
                            AppointmentServiceImplementation appointmentService,
                            DoctorService doctorService) {
        this.usersService = usersService;
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
    }

    @GetMapping("/index")
    public String index(Model model) {
        Users me = usersService.findByUsername(currentUsername());
        // cập nhật last-seen
        me.setUpdatedAt(LocalDateTime.now());
        usersService.save(me);

        // lấy thông tin bác sĩ (nếu user này là bác sĩ)
        Doctor doctor = doctorService.getDoctorByUserId(me.getUserId());

        // ưu tiên clinic của Doctor, nếu chưa có thì dùng clinic gán cho Users
        Long clinicId =
                (doctor != null && doctor.getClinic() != null) ? doctor.getClinic().getClinicId()
                : (me.getClinic() != null ? me.getClinic().getClinicId() : null);

        List<Appointment> apps = (clinicId != null)
                ? appointmentService.findAllByBranch(clinicId)    // ✅ chỉ lịch hẹn của chi nhánh
                : Collections.emptyList();

        model.addAttribute("name", me.getFullName());
        model.addAttribute("email", me.getEmail());
        model.addAttribute("bio", doctor != null ? doctor.getBio() : "");
        model.addAttribute("clinic",
                (doctor != null && doctor.getClinic() != null) ? doctor.getClinic().getName()
                        : (me.getClinic() != null ? me.getClinic().getName() : ""));
        model.addAttribute("specialty",
                (doctor != null && doctor.getSpecialty() != null) ? doctor.getSpecialty().getName() : "");
        model.addAttribute("app", apps);

        return "doctor/index";
    }

    /* helpers */
    private String currentUsername() {
        Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (p instanceof UserDetails) ? ((UserDetails) p).getUsername() : String.valueOf(p);
    }
}
