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
  @Column(name = "invoice_id")
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

  @Column(name = "invoice_no", unique = true)        // <<< SỬA
  private String invoiceNo;

  @Column(name = "invoice_date", nullable = false)   // <<< SỬA
  private LocalDateTime invoiceDate = LocalDateTime.now();

  @Column(nullable = false)
  private BigDecimal subtotal = BigDecimal.ZERO;

  @Column(nullable = false)
  private BigDecimal discount = BigDecimal.ZERO;

  @Column(name = "tax_amount", nullable = false)     // <<< SỬA
  private BigDecimal taxAmount = BigDecimal.ZERO;

  @Column(name = "total_amount", nullable = false)
  private BigDecimal totalAmount = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private InvoiceStatus status = InvoiceStatus.DRAFT;

  @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<InvoiceItem> items = new ArrayList<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();
}
