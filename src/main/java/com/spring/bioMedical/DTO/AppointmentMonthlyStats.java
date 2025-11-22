package com.spring.bioMedical.dto;

public class AppointmentMonthlyStats {

    private Integer month;               // tháng (1–12)
    private Long totalCount;             // tổng lịch hẹn
    private Long completedCount;         // hoàn thành
    private Long cancelledAndAutoCount;  // CANCELLED + AUTO_CANCELLED
    private Long noShowCount;            // NO_SHOW

    public AppointmentMonthlyStats(Integer month,
                                   Long totalCount,
                                   Long completedCount,
                                   Long cancelledAndAutoCount,
                                   Long noShowCount) {
        this.month = month;
        this.totalCount = totalCount;
        this.completedCount = completedCount;
        this.cancelledAndAutoCount = cancelledAndAutoCount;
        this.noShowCount = noShowCount;
    }

    public Integer getMonth() {
        return month;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public Long getCompletedCount() {
        return completedCount;
    }

    public Long getCancelledAndAutoCount() {
        return cancelledAndAutoCount;
    }

    public Long getNoShowCount() {
        return noShowCount;
    }

    // tiện cho Thymeleaf
    public double getCompletionRate() {
        if (totalCount == null || totalCount == 0) return 0.0;
        return (completedCount * 100.0) / totalCount;
    }

    public double getNoShowRate() {
        if (totalCount == null || totalCount == 0) return 0.0;
        return (noShowCount * 100.0) / totalCount;
    }
}
