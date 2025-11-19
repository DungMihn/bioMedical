package com.spring.bioMedical.dto;

import java.time.LocalDate;

public class GuestBookingForm {

    private String fullName;
    private String email;
    private String phone;
    private String gender;
    private LocalDate dateOfBirth;
    private String notes;
    private Long clinicId;
    private Long slotId;

    // === GETTERS ===
    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getGender() {
        return gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getNotes() {
        return notes;
    }

    public Long getClinicId() {
        return clinicId;
    }

    public Long getSlotId() {
        return slotId;
    }

    // === SETTERS ===
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setClinicId(Long clinicId) {
        this.clinicId = clinicId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }
}