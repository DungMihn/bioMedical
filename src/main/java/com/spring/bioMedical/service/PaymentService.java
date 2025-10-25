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

  /** 1) Hàm controller có thể gọi trực tiếp */
  @Transactional
  public Payment capture(PaymentCaptureForm form){
    Invoice inv = invoiceRepo.findById(form.getInvoiceId())
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy invoice"));

    Payment p = new Payment();
    p.setInvoice(inv);
    p.setAmount(form.getAmount());
    p.setMethod(form.getMethod());
    p.setReferenceNo(form.getReferenceNo());
    p.setStatus(PaymentStatus.CAPTURED);
    p.setPaidAt(LocalDateTime.now());
    paymentRepo.save(p);

    BigDecimal captured = paymentRepo.sumCapturedByInvoice(inv.getInvoiceId());
    if (captured.compareTo(inv.getTotalAmount()) >= 0) {
      inv.setStatus(InvoiceStatus.PAID);
    }
    return p;
  }

  /** 2) Overload khớp với AdminBranchInvoiceController đang gọi */
  @Transactional
  public void capture(Long invoiceId, BigDecimal amount, String method, String referenceNo) {
    PaymentCaptureForm f = new PaymentCaptureForm();
    f.setInvoiceId(invoiceId);
    f.setAmount(amount);
    f.setMethod(method);
    f.setReferenceNo(referenceNo);
    capture(f);
  }

  /** Lấy danh sách payments theo invoice để render detail */
  @Transactional(readOnly = true)
  public List<Payment> findByInvoice(Long invoiceId) {
    return paymentRepo.findByInvoice_InvoiceId(invoiceId);
  }
}
