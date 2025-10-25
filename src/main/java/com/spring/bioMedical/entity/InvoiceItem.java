package com.spring.bioMedical.entity;

import lombok.Getter; import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "InvoiceItems")
@Getter @Setter
public class InvoiceItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long itemId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_id", nullable = false)
  private Invoice invoice;

  @Column(nullable = false)
  private String itemName;

  @Column(nullable = false)
  private BigDecimal quantity = BigDecimal.ONE;

  @Column(nullable = false)
  private BigDecimal unitPrice = BigDecimal.ZERO;

  // CHO PHÉP service set line_total
  @Column(name = "line_total", nullable = false)
  private BigDecimal lineTotal = BigDecimal.ZERO;
}
