
package com.spring.bioMedical.form;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


public class AppointmentForm {
    private Long id;
    private String name;
    private String email;
    private LocalDate date;
    private LocalTime time;
    private String description;
    private LocalDateTime regtime;

    public AppointmentForm(Long id, String name, String email,
                          LocalDate date, LocalTime time,
                          String description, LocalDateTime regtime) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.date = date;
        this.time = time;
        this.description = description;
        this.regtime = regtime;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public String getDescription() { return description; }
    public LocalDateTime getRegtime() { return regtime; }
}
