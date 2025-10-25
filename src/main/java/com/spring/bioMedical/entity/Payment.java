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

@Entity @Table(name="Payments")
@Getter @Setter
public class Payment {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long paymentId;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="invoice_id", nullable=false)
  private Invoice invoice;

  @Column(nullable=false) private BigDecimal amount;
  @Column(nullable=false) private String method;

  @Enumerated(EnumType.STRING)
  @Column(nullable=false) private PaymentStatus status = PaymentStatus.PENDING;

  private LocalDateTime paidAt;
  private String referenceNo;
  @Column(nullable=false) private LocalDateTime createdAt = LocalDateTime.now();
}
