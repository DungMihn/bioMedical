// AdminSuperInvoiceController.java
package com.spring.bioMedical.Controller;

import com.spring.bioMedical.entity.Invoice;
import com.spring.bioMedical.entity.InvoiceItem;
import com.spring.bioMedical.entity.Payment;
import com.spring.bioMedical.form.InvoiceCreateForm;
import com.spring.bioMedical.form.PaymentCaptureForm;
import com.spring.bioMedical.repository.InvoiceItemRepository;
import com.spring.bioMedical.repository.InvoiceRepository;
import com.spring.bioMedical.repository.PaymentRepository;
import com.spring.bioMedical.service.InvoiceService;
import com.spring.bioMedical.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin_super/invoices") // dùng "_" cho đúng thư mục template
@RequiredArgsConstructor
public class AdminSuperInvoiceController {

  private final InvoiceRepository invoiceRepo;
  private final InvoiceItemRepository invoiceItemRepo;   // <— THÊM
  private final PaymentRepository paymentRepo;           // <— THÊM
  private final InvoiceService invoiceService;
  private final PaymentService paymentService;

  @GetMapping
  public String list(Model model){
    model.addAttribute("activePage","invoices");
    model.addAttribute("invoices", invoiceRepo.findAll());
    return "admin_super/invoices/list";
  }

@GetMapping("/{id}")
@Transactional(readOnly = true)
public String detail(@PathVariable Long id, Model model) {
    // 1) Lấy hóa đơn
    Invoice inv = invoiceRepo.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + id));

    // 2) Dòng hàng: đọc trực tiếp theo invoice_id
    //    (dùng native để loại trừ mọi vấn đề mapping)
    List<InvoiceItem> items = invoiceItemRepo.findNativeItemsByInvoiceId(id);
    long itemsCount = invoiceItemRepo.countNativeItemsByInvoiceId(id);

    // 3) Thanh toán: đọc trực tiếp theo invoice_id
    List<Payment> payments = paymentRepo.findByInvoice_InvoiceId(id);

    // 4) Appointment id (entity Appointment của bạn có int id)
    Long appointmentId = (inv.getAppointment() != null)
            ? (long) inv.getAppointment().getId()
            : null;

    // 5) Đổ dữ liệu ra view
    model.addAttribute("activePage", "invoices");
    model.addAttribute("invoice", inv);
    model.addAttribute("items", items);
    model.addAttribute("itemsCount", itemsCount);   // hiển thị debug trong header
    model.addAttribute("payments", payments);
    model.addAttribute("appointmentId", appointmentId);

    return "admin_super/invoices/detail";
}



  @PostMapping
  public String create(@ModelAttribute InvoiceCreateForm req){
    Invoice inv = invoiceService.createInvoice(req);
    return "redirect:/admin_super/invoices/" + inv.getInvoiceId();
  }

  @PostMapping("/{id}/capture")
  public String capture(@PathVariable Long id, @ModelAttribute PaymentCaptureForm req){
    req.setInvoiceId(id);
    paymentService.capture(req);
    return "redirect:/admin_super/invoices/" + id;
  }
}
