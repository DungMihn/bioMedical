package com.spring.bioMedical.Controller;

import com.spring.bioMedical.entity.Invoice;
import com.spring.bioMedical.entity.InvoiceItem;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.repository.InvoiceItemRepository;
import com.spring.bioMedical.service.InvoiceService;
import com.spring.bioMedical.service.PaymentService;
import com.spring.bioMedical.service.UsersService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/admin-branch/invoices") // PATH dùng dấu gạch ngang (-)
public class AdminBranchInvoiceController {

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final UsersService usersService;
    private final InvoiceItemRepository invoiceItemRepo;

    public AdminBranchInvoiceController(InvoiceService invoiceService,
                                        PaymentService paymentService,
                                        UsersService usersService,
                                        InvoiceItemRepository invoiceItemRepo) {
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
        this.usersService = usersService;
        this.invoiceItemRepo = invoiceItemRepo;
    }
    @ModelAttribute
    public void setActiveNav(Model model) {
        model.addAttribute("activePage", "invoices");
    }
    
    /* ===== helper lấy clinicId của user đăng nhập ===== */
    private Long getCurrentClinicId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Users u = usersService.findByUsername(auth.getName());
        return (u.getClinic() != null) ? u.getClinic().getClinicId() : null;
    }

    private boolean sameClinic(Invoice inv, Long clinicId) {
        return inv != null && inv.getClinic() != null
                && Objects.equals(inv.getClinic().getClinicId(), clinicId);
    }

    /** LIST hóa đơn theo chi nhánh của user đăng nhập */
    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        Long clinicId = getCurrentClinicId();
        List<Invoice> invoices = invoiceService.searchForBranch(clinicId, q);
        model.addAttribute("invoices", invoices);
        model.addAttribute("clinicId", clinicId);
        model.addAttribute("q", q);
        return "admin_branch/invoices/list"; // folder template dùng dấu _
    }

    /** Tạo hóa đơn từ appointment (nếu bạn dùng chức năng này cho BRANCH) */
    @GetMapping("/new")
    public String newFromAppointment(@RequestParam Long appointmentId, Model model) {
        Invoice inv = invoiceService.initDraftFromAppointment(appointmentId);

        Long myClinic = getCurrentClinicId();
        if (!sameClinic(inv, myClinic)) return "redirect:/access-denied";

        model.addAttribute("invoice", inv);
        return "admin_branch/invoices/form";
    }

    /** Lưu (DRAFT) */
    @PostMapping("/save")
    public String save(@ModelAttribute("invoice") Invoice invoice, BindingResult br) {
        if (br.hasErrors()) return "admin_branch/invoices/form";
        Long myClinic = getCurrentClinicId();
        if (!sameClinic(invoice, myClinic)) return "redirect:/access-denied";

        invoiceService.saveDraft(invoice);
        // redirect theo PATH có dấu gạch ngang (-)
        return "redirect:/admin-branch/invoices/" + invoice.getInvoiceId();
    }

    /** Chi tiết hóa đơn (items + payments) */
@GetMapping("/{id}")
@Transactional(readOnly = true)
public String detail(@PathVariable Long id, Model model) {
    Invoice inv = invoiceService.getDetail(id);

    Long myClinic = getCurrentClinicId();
    if (!sameClinic(inv, myClinic)) return "redirect:/access-denied";

    // DÙNG NATIVE để chắc chắn đọc đúng dữ liệu bảng InvoiceItems
    List<InvoiceItem> items   = invoiceItemRepo.findNativeItemsByInvoiceId(id);
    long itemsCount           = invoiceItemRepo.countNativeItemsByInvoiceId(id);

    // Thanh toán như cũ
    List<com.spring.bioMedical.entity.Payment> payments = paymentService.findByInvoice(id);

    Long appointmentId = (inv.getAppointment() != null) ? inv.getAppointment().getAppointmentId() : null;

    model.addAttribute("invoice", inv);
    model.addAttribute("items", items);
    model.addAttribute("itemsCount", itemsCount);
    model.addAttribute("payments", payments);
    model.addAttribute("appointmentId", appointmentId);

    return "admin_branch/invoices/detail";
}




    /** Thêm dòng hàng (khi còn DRAFT) */
    @PostMapping("/{id}/items")
    public String addItem(@PathVariable Long id,
                          @ModelAttribute("newItem") InvoiceItem item) {
        Invoice inv = invoiceService.getDetail(id);
        if (!sameClinic(inv, getCurrentClinicId())) return "redirect:/access-denied";

        invoiceService.addItem(id, item);
        return "redirect:/admin-branch/invoices/" + id;
    }

    /** Xóa dòng hàng */
    @PostMapping("/{id}/items/{itemId}/delete")
    public String removeItem(@PathVariable Long id, @PathVariable Long itemId) {
        Invoice inv = invoiceService.getDetail(id);
        if (!sameClinic(inv, getCurrentClinicId())) return "redirect:/access-denied";

        invoiceService.removeItem(id, itemId);
        return "redirect:/admin-branch/invoices/" + id;
    }

    /** Issue hóa đơn (đóng DRAFT, phát hành số) */
    @PostMapping("/{id}/issue")
    public String issue(@PathVariable Long id) {
        Invoice inv = invoiceService.getDetail(id);
        if (!sameClinic(inv, getCurrentClinicId())) return "redirect:/access-denied";

        invoiceService.issue(id);
        return "redirect:/admin-branch/invoices/" + id;
    }

    /** Thu tiền (capture) */
    @PostMapping("/{id}/payments")
    public String capture(@PathVariable Long id,
                          @RequestParam BigDecimal amount,
                          @RequestParam String method) {
        Invoice inv = invoiceService.getDetail(id);
        if (!sameClinic(inv, getCurrentClinicId())) return "redirect:/access-denied";

        // chữ ký bạn đang dùng: capture(Long invoiceId, BigDecimal amount, String method, String referenceNo)
        paymentService.capture(id, amount, method, null);
        return "redirect:/admin-branch/invoices/" + id;
    }

    /** In (print) */
    @GetMapping("/{id}/print")
    public String print(@PathVariable Long id, Model model) {
        Invoice inv = invoiceService.getDetail(id);
        if (!sameClinic(inv, getCurrentClinicId())) return "redirect:/access-denied";

        model.addAttribute("invoice", inv);
        return "admin_branch/invoices/print";
    }
}
