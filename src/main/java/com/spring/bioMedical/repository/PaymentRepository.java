package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

  @Query("select coalesce(sum(p.amount),0) from Payment p " +
         "where p.invoice.invoiceId = :invoiceId and p.status = 'CAPTURED'")
  BigDecimal sumCapturedByInvoice(Long invoiceId);

  List<Payment> findByInvoice_InvoiceId(Long invoiceId);
}
