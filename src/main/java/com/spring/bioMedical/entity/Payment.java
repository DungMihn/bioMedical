/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.bioMedical.entity;

import com.spring.bioMedical.entity.enums.PaymentStatus;
import lombok.Getter; import lombok.Setter;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@Entity
@Table(name = "Payments")
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "payment_id")               // <<< SỬA
  private Long paymentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_id", nullable = false)
  private Invoice invoice;

  @Column(name = "amount", nullable = false) // <<< SỬA (thêm name cho chắc ăn)
  private BigDecimal amount;

  @Column(name = "method", nullable = false) // <<< SỬA
  private String method;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false) // <<< SỬA
  private PaymentStatus status = PaymentStatus.PENDING;

  @Column(name = "paid_at")                  // <<< SỬA
  private LocalDateTime paidAt;

  @Column(name = "reference_no")             // <<< SỬA
  private String referenceNo;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();
}

