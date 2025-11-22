package com.spring.bioMedical.entity;

import com.spring.bioMedical.entity.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@Entity
@Table(name = "Payments")
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "payment_id")
  private Long paymentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_id", nullable = false)
  private Invoice invoice;

  @Column(name = "amount", nullable = false)
  private BigDecimal amount;

  @Column(name = "method", nullable = false)
  private String method;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private PaymentStatus status = PaymentStatus.PENDING;   // ✔ enum

  @Column(name = "paid_at")               // ✔ map đúng với cột paid_at trong DB
  private LocalDateTime paidAt;

  @Column(name = "reference_no")
  private String referenceNo;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();
}
