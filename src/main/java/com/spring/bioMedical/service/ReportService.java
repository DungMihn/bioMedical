package com.spring.bioMedical.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
  private final EntityManager em;

  private static LocalDateTime startOfDay(LocalDate d) { return d.atStartOfDay(); }
  private static LocalDateTime endExclusive(LocalDate d) { return d.plusDays(1).atStartOfDay(); }

  /**
   * TRƯỚC ĐÂY: dùng DATE(p.paidAt) / function('date',...) => lỗi
   * NAY: dùng year()/month()/day() để portable.
   *
   * Trả về List<Tuple> với cột:
   *   y = year, m = month, d = day, revenue = sum(amount)
   */
  public List<Tuple> revenueByDay(LocalDate from, LocalDate to){
    return em.createQuery(
      "select year(p.paidAt) as y, month(p.paidAt) as m, day(p.paidAt) as d, " +
      "       coalesce(sum(p.amount), 0) as revenue " +
      "from Payment p " +
      "where p.status = 'CAPTURED' and p.paidAt between :f and :t " +
      "group by year(p.paidAt), month(p.paidAt), day(p.paidAt) " +
      "order by year(p.paidAt), month(p.paidAt), day(p.paidAt)", Tuple.class)
      .setParameter("f", startOfDay(from))
      .setParameter("t", endExclusive(to))
      .getResultList();
  }

  public List<Tuple> revenueByClinic(LocalDate from, LocalDate to){
    return em.createQuery(
      "select i.clinic.clinicId as clinicId, i.clinic.name as name, " +
      "       coalesce(sum(p.amount), 0) as revenue " +
      "from Payment p join p.invoice i " +
      "where p.status='CAPTURED' and p.paidAt between :f and :t " +
      "group by i.clinic.clinicId, i.clinic.name " +
      "order by revenue desc", Tuple.class)
      .setParameter("f", startOfDay(from))
      .setParameter("t", endExclusive(to))
      .getResultList();
  }
}
