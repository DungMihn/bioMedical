package com.spring.bioMedical.Controller;

import com.spring.bioMedical.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Tuple;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin-super/statistics")
@RequiredArgsConstructor
public class AdminSuperReportsController {
  private final ReportService reportService;

  @GetMapping({"", "/"})
  public String dashboard(
      @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      Model model
  ){
    LocalDate f = (from == null) ? LocalDate.now().withDayOfMonth(1) : from;
    LocalDate t = (to   == null) ? LocalDate.now()                    : to;

    // Lấy dữ liệu daily theo Y/M/D và convert thành 2 list cho Chart.js
    List<Tuple> dailyTuples = reportService.revenueByDay(f, t);
    List<String> dailyLabels = dailyTuples.stream()
        .map(tp -> {
          int y = ((Number) tp.get("y")).intValue();
          int m = ((Number) tp.get("m")).intValue();
          int d = ((Number) tp.get("d")).intValue();
          return LocalDate.of(y, m, d).toString(); // yyyy-MM-dd
        })
        .collect(Collectors.toList());

    List<BigDecimal> dailyValues = dailyTuples.stream()
        .map(tp -> (BigDecimal) tp.get("revenue"))
        .collect(Collectors.toList());

    model.addAttribute("from", f.toString());
    model.addAttribute("to",   t.toString());
    model.addAttribute("byClinic", reportService.revenueByClinic(f, t));
    model.addAttribute("activePage", "statistics");


    // Thêm 2 biến mới cho chart
    model.addAttribute("dailyLabels", dailyLabels);
    model.addAttribute("dailyValues", dailyValues);

    return "admin_super/reports/statistics";
  }
}
