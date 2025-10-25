package com.spring.bioMedical.entity;

import com.spring.bioMedical.entity.enums.InvoiceStatus;
import lombok.Getter; import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList; import java.util.List;

@Entity
@Table(name = "Invoices")
@Getter @Setter
public class Invoice {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long invoiceId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "appointment_id", nullable = false)
  private Appointment appointment;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "clinic_id", nullable = false)
  private Clinic clinic;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "patient_user_id", nullable = false)
  private Users patient;

  @Column(unique = true)
  private String invoiceNo;

  @Column(nullable = false)
  private LocalDateTime invoiceDate = LocalDateTime.now();

  @Column(nullable = false)
  private BigDecimal subtotal = BigDecimal.ZERO;

  @Column(nullable = false)
  private BigDecimal discount = BigDecimal.ZERO;

  @Column(nullable = false)
  private BigDecimal taxAmount = BigDecimal.ZERO;

  // CHO PHÉP service set tổng tiền
  @Column(name = "total_amount", nullable = false)
  private BigDecimal totalAmount = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private InvoiceStatus status = InvoiceStatus.DRAFT;

  @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<InvoiceItem> items = new ArrayList<>();

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();
}
