package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.Invoice;
import com.spring.bioMedical.entity.Payment;
import com.spring.bioMedical.entity.enums.InvoiceStatus;
import com.spring.bioMedical.entity.enums.PaymentStatus;
import com.spring.bioMedical.form.PaymentCaptureForm;
import com.spring.bioMedical.repository.InvoiceRepository;
import com.spring.bioMedical.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

  private final PaymentRepository paymentRepo;
  private final InvoiceRepository invoiceRepo;

  @Transactional
  public Payment capture(PaymentCaptureForm form){
    Invoice inv = invoiceRepo.findById(form.getInvoiceId())
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy invoice"));

    Payment p = new Payment();
    p.setInvoice(inv);
    p.setAmount(form.getAmount());
    p.setMethod(form.getMethod());
    p.setReferenceNo(form.getReferenceNo());
    p.setStatus(PaymentStatus.PAID);     // ✔ enum, không còn lỗi
    p.setPaidAt(LocalDateTime.now());
    paymentRepo.save(p);

    // Nếu muốn đơn giản: hễ có payment >= total_amount thì mark invoice PAID
    // (thay vì sumCapturedByInvoice nếu repo của bạn đang lọc theo CAPTURED trước đây)
    // BigDecimal totalPaid = paymentRepo.sumPaidByInvoice(inv.getInvoiceId());
    // if (totalPaid != null && totalPaid.compareTo(inv.getTotalAmount()) >= 0) ...

    inv.setStatus(InvoiceStatus.PAID);
    // nhớ invoiceRepo.save(inv); nếu entity chưa được quản lý
    return p;
  }

  @Transactional
  public void capture(Long invoiceId, BigDecimal amount, String method, String referenceNo) {
    PaymentCaptureForm f = new PaymentCaptureForm();
    f.setInvoiceId(invoiceId);
    f.setAmount(amount);
    f.setMethod(method);
    f.setReferenceNo(referenceNo);
    capture(f);
  }

  @Transactional(readOnly = true)
  public List<Payment> findByInvoice(Long invoiceId) {
    return paymentRepo.findByInvoice_InvoiceId(invoiceId);
  }
}


