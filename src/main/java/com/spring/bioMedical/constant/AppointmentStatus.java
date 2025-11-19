package com.spring.bioMedical.constant;

public enum AppointmentStatus {
    PENDING,           // Chờ xác nhận
    CONFIRMED,         // Đã xác nhận
    COMPLETED,         // Đã hoàn thành (sau khi khám và có hồ sơ bệnh án)
    CANCELLED,         // Đã hủy (bởi bác sĩ hoặc bệnh nhân)
    AUTO_CANCELLED,    // Tự động hủy (quá hạn xác nhận)
    NO_SHOW,           // Bệnh nhân không đến
    EXPIRED           // Quá hạn (cho PENDING và CONFIRMED)
}