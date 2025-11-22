package com.spring.bioMedical.Controller;

import com.spring.bioMedical.entity.Invoice;
import com.spring.bioMedical.entity.InvoiceItem;
import com.spring.bioMedical.entity.Payment;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.service.InvoiceService;
import com.spring.bioMedical.service.PaymentService;
import com.spring.bioMedical.service.UsersService;
import com.spring.bioMedical.repository.InvoiceItemRepository;
import com.spring.bioMedical.repository.InvoiceRepository;
import com.spring.bioMedical.repository.PaymentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/admin-branch/invoices")
public class AdminBranchInvoiceController {

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final UsersService usersService;
    private final InvoiceItemRepository invoiceItemRepo;
    private final InvoiceRepository invoiceRepo;
    private final PaymentRepository paymentRepo;

    public AdminBranchInvoiceController(
            InvoiceService invoiceService,
            PaymentService paymentService,
            UsersService usersService,
            InvoiceItemRepository invoiceItemRepo,
            InvoiceRepository invoiceRepo,
            PaymentRepository paymentRepo
    ) {
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
        this.usersService = usersService;
        this.invoiceItemRepo = invoiceItemRepo;
        this.invoiceRepo = invoiceRepo;
        this.paymentRepo = paymentRepo;
    }

    @ModelAttribute
    public void setActiveNav(Model model) {
        model.addAttribute("activePage", "invoices");
    }

    // Lấy clinicId từ user đăng nhập
    private Long getCurrentClinicId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        Users u = usersService.findByUsername(auth.getName());
        return (u.getClinic() != null) ? u.getClinic().getClinicId() : null;
    }

    private boolean sameClinic(Invoice inv, Long clinicId) {
        return inv != null
                && inv.getClinic() != null
                && Objects.equals(inv.getClinic().getClinicId(), clinicId);
    }

    // Danh sách hóa đơn
    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        Long clinicId = getCurrentClinicId();
        List<Invoice> invoices = invoiceService.searchForBranch(clinicId, q);

        model.addAttribute("invoices", invoices);
        model.addAttribute("clinicId", clinicId);
        model.addAttribute("q", q);
        return "admin_branch/invoices/list";
    }

    // Tạo mới (Draft) từ appointment
    @GetMapping("/new")
    public String newFromAppointment(@RequestParam Long appointmentId, Model model) {
        Invoice inv = invoiceService.initDraftFromAppointment(appointmentId);

        if (!sameClinic(inv, getCurrentClinicId())) {
            return "redirect:/access-denied";
        }

        model.addAttribute("invoice", inv);
        return "admin_branch/invoices/form";
    }

    // Lưu Draft
    @PostMapping("/save")
    public String save(@ModelAttribute("invoice") Invoice invoice, BindingResult br) {
        if (br.hasErrors()) {
            return "admin_branch/invoices/form";
        }

        if (!sameClinic(invoice, getCurrentClinicId())) {
            return "redirect:/access-denied";
        }

        invoiceService.saveDraft(invoice);
        return "redirect:/admin-branch/invoices/" + invoice.getInvoiceId();
    }

    // Chi tiết hóa đơn
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String detail(@PathVariable Long id, Model model) {

        Invoice inv = invoiceService.getDetail(id);

        if (!sameClinic(inv, getCurrentClinicId())) {
            return "redirect:/access-denied";
        }

        List<InvoiceItem> items = invoiceItemRepo.findNativeItemsByInvoiceId(id);
        long count = invoiceItemRepo.countNativeItemsByInvoiceId(id);
        List<Payment> payments = paymentService.findByInvoice(id);

        Long apptId = (inv.getAppointment() != null) ? inv.getAppointment().getAppointmentId() : null;

        model.addAttribute("invoice", inv);
        model.addAttribute("items", items);
        model.addAttribute("itemsCount", count);
        model.addAttribute("payments", payments);
        model.addAttribute("appointmentId", apptId);

        return "admin_branch/invoices/detail";
    }

    // Thêm dòng
    @PostMapping("/{id}/items")
    public String addItem(@PathVariable Long id,
            @ModelAttribute("newItem") InvoiceItem item) {
        Invoice inv = invoiceService.getDetail(id);
        if (!sameClinic(inv, getCurrentClinicId())) {
            return "redirect:/access-denied";
        }

        invoiceService.addItem(id, item);
        return "redirect:/admin-branch/invoices/" + id;
    }

    // Xóa dòng
    @PostMapping("/{id}/items/{itemId}/delete")
    public String removeItem(@PathVariable Long id, @PathVariable Long itemId) {
        Invoice inv = invoiceService.getDetail(id);
        if (!sameClinic(inv, getCurrentClinicId())) {
            return "redirect:/access-denied";
        }

        invoiceService.removeItem(id, itemId);
        return "redirect:/admin-branch/invoices/" + id;
    }

    // Issue hóa đơn (từ DRAFT -> ISSUED)
    @PostMapping("/{id}/issue")
    public String issue(@PathVariable Long id) {
        Invoice inv = invoiceService.getDetail(id);
        if (!sameClinic(inv, getCurrentClinicId())) {
            return "redirect:/access-denied";
        }

        invoiceService.issue(id);
        return "redirect:/admin-branch/invoices/" + id;
    }

    // THU TIỀN (thanh toán) - dùng POST /{id}/pay
    @PostMapping("/{id}/pay")
    @Transactional
    public String pay(@PathVariable("id") Long invoiceId,
            RedirectAttributes ra) {

        Invoice inv = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn"));

        if (!sameClinic(inv, getCurrentClinicId())) {
            return "redirect:/access-denied";
        }

        paymentService.capture(invoiceId, inv.getTotalAmount(), "CASH", "MANUAL-" + invoiceId);

        ra.addFlashAttribute("msg", "Đã ghi nhận thanh toán hóa đơn #" + inv.getInvoiceNo());
        return "redirect:/admin-branch/invoices";   // ⭐ TRẢ VỀ LIST
    }

    // Print
    @GetMapping("/{id}/print")
    public String print(@PathVariable Long id, Model model) {
        Invoice inv = invoiceService.getDetail(id);
        if (!sameClinic(inv, getCurrentClinicId())) {
            return "redirect:/access-denied";
        }

        model.addAttribute("invoice", inv);
        return "admin_branch/invoices/print";
    }
}
