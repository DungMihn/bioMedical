///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.spring.bioMedical.Controller;
//
//import com.spring.bioMedical.entity.*;
//import com.spring.bioMedical.service.*;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
///**
// *
// * @author Admin
// */
//@Controller
//@RequestMapping("/api/appointments")
//public class AppointmentApiController {
//
//    @Autowired
//    private AppointmentService appointmentService;
//
//    // ✅ API lấy appointments sắp tới
//    @GetMapping("/upcoming")
//    public ResponseEntity<List<Appointments>> getUpcomingAppointments() {
//        try {
//            List<Appointments> appointments = appointmentService.getUpcomingAppointments();
//            return ResponseEntity.ok(appointments);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // ✅ API lấy appointments theo patient
//    @GetMapping("/patient/{userId}")
//    public ResponseEntity<List<Appointments>> getPatientAppointments(@PathVariable Long userId) {
//        try {
//            List<Appointments> appointments = appointmentService.getAppointmentsByPatient(userId);
//            return ResponseEntity.ok(appointments);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // ✅ API thống kê appointments
//    @GetMapping("/stats")
//    public ResponseEntity<Map<String, Long>> getAppointmentStats() {
//        try {
//            Map<String, Long> stats = appointmentService.getAppointmentStats();
//            return ResponseEntity.ok(stats);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // ✅ API tìm appointments theo khoảng thời gian
//    @GetMapping("/search")
//    public ResponseEntity<List<Appointments>> searchAppointments(
//            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
//            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
//            @RequestParam(required = false) String status) {
//        try {
//            List<Appointments> appointments = appointmentService.getAppointmentsByDateRange(startDate, endDate, status);
//            return ResponseEntity.ok(appointments);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//}
