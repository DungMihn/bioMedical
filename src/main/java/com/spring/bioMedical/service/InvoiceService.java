package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.*;
import com.spring.bioMedical.entity.enums.InvoiceStatus;
import com.spring.bioMedical.form.InvoiceCreateForm;
import com.spring.bioMedical.form.InvoiceItemForm;
import com.spring.bioMedical.repository.InvoiceItemRepository;
import com.spring.bioMedical.repository.InvoiceRepository;
import com.spring.bioMedical.repository.UsersRepository;
import com.spring.bioMedical.repository.ClinicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepo;
    private final InvoiceItemRepository itemRepo;

    // Giữ nguyên service bạn đang dùng
    private final AppointmentService appointmentService;
    private final ClinicRepository clinicRepo;
    private final UsersRepository usersRepo;

    /* =========================================================
       LIST cho BRANCH: luôn lọc theo clinicId
       ========================================================= */
    @Transactional(readOnly = true)
    public List<Invoice> searchForBranch(Long clinicId, String q) {
        if (clinicId == null) {
            return Collections.emptyList();
        }
        if (q != null && !q.isBlank()) {
            return invoiceRepo.findByClinic_ClinicIdAndInvoiceNoContainingIgnoreCase(clinicId, q.trim());
        }
        return invoiceRepo.findByClinic_ClinicId(clinicId);
    }

    /* =========================================================
       Tạo 1 invoice DRAFT từ appointment
       ========================================================= */
    @Transactional
    public Invoice initDraftFromAppointment(Long appointmentId) {
        Appointments appt = appointmentService.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy appointment: " + appointmentId));

        Invoice inv = new Invoice();
        inv.setAppointment(appt);

        // ✅ SỬA: Lấy clinic qua slot thay vì trực tiếp từ appointment
        if (appt.getSlot() != null && appt.getSlot().getClinic() != null) {
            inv.setClinic(appt.getSlot().getClinic());
        }

        inv.setInvoiceDate(LocalDateTime.now());
        inv.setSubtotal(BigDecimal.ZERO);
        inv.setDiscount(BigDecimal.ZERO);
        inv.setTaxAmount(BigDecimal.ZERO);
        inv.setTotalAmount(BigDecimal.ZERO);
        inv.setStatus(InvoiceStatus.DRAFT);
        inv.setItems(new ArrayList<>());

        return invoiceRepo.save(inv);
    }

    /* =========================================================
       Lưu lại DRAFT (tính lại tổng tiền)
       ========================================================= */
    @Transactional
    public void saveDraft(Invoice invoice) {
        // Tính lại line_total từng dòng và subtotal
        BigDecimal subtotal = BigDecimal.ZERO;

        if (invoice.getItems() != null) {
            for (InvoiceItem it : invoice.getItems()) {
                if (it.getQuantity() == null) {
                    it.setQuantity(BigDecimal.ONE);
                }
                if (it.getUnitPrice() == null) {
                    it.setUnitPrice(BigDecimal.ZERO);
                }
                it.setLineTotal(it.getUnitPrice().multiply(it.getQuantity()));
                it.setInvoice(invoice);
                subtotal = subtotal.add(it.getLineTotal());
            }
        }

        invoice.setSubtotal(subtotal);

        BigDecimal discount = nvl(invoice.getDiscount());
        BigDecimal tax = nvl(invoice.getTaxAmount());
        BigDecimal total = subtotal.subtract(discount).add(tax);

        invoice.setTotalAmount(total);

        invoiceRepo.save(invoice);
    }

    private static BigDecimal nvl(BigDecimal v) {
        return (v == null) ? BigDecimal.ZERO : v;
    }

    /* =========================================================
       Lấy chi tiết (không ép fetch các quan hệ — controller đã
       đọc items/payments trực tiếp qua repo)
       ========================================================= */
    @Transactional(readOnly = true)
    public Invoice getDetail(Long invoiceId) {
        return invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy invoice: " + invoiceId));
    }

    /* =========================================================
       Thêm/Xóa dòng khi DRAFT
       ========================================================= */
    @Transactional
    public void addItem(Long invoiceId, InvoiceItem item) {
        Invoice inv = getDetail(invoiceId);
        ensureDraft(inv);

        if (item.getQuantity() == null) {
            item.setQuantity(BigDecimal.ONE);
        }
        if (item.getUnitPrice() == null) {
            item.setUnitPrice(BigDecimal.ZERO);
        }
        item.setLineTotal(item.getUnitPrice().multiply(item.getQuantity()));
        item.setInvoice(inv);
        itemRepo.save(item);

        // Đổi tên method cho khớp với repository
        List<InvoiceItem> fresh = itemRepo.findByInvoice_InvoiceIdOrderByItemIdAsc(invoiceId);
        inv.getItems().clear();
        inv.getItems().addAll(fresh);

        saveDraft(inv);
    }

    @Transactional
    public void removeItem(Long invoiceId, Long itemId) {
        Invoice inv = getDetail(invoiceId);
        ensureDraft(inv);

        itemRepo.deleteById(itemId);

        // Đổi tên method cho khớp với repository
        List<InvoiceItem> fresh = itemRepo.findByInvoice_InvoiceIdOrderByItemIdAsc(invoiceId);
        inv.getItems().clear();
        inv.getItems().addAll(fresh);

        saveDraft(inv);
    }

    private void ensureDraft(Invoice inv) {
        if (inv.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Chỉ trạng thái DRAFT mới được phép thao tác dòng hàng.");
        }
    }

    /* =========================================================
       Issue hóa đơn (đóng DRAFT, phát hành số)
       ========================================================= */
    @Transactional
    public void issue(Long invoiceId) {
        Invoice inv = getDetail(invoiceId);
        if (inv.getStatus() != InvoiceStatus.DRAFT) {
            return;
        }

        // Số hóa đơn tối giản – nếu bạn có generator thì thay thế ở đây
        inv.setInvoiceNo("INV-" + invoiceId);
        inv.setStatus(InvoiceStatus.ISSUED);
        invoiceRepo.save(inv);
    }

    /* =========================================================
       Tạo invoice (cho Admin Super) + items
       ========================================================= */
    @Transactional
    public Invoice createInvoice(InvoiceCreateForm form) {
        Appointments appt = appointmentService.findById(form.getAppointmentId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy appointment: " + form.getAppointmentId()));

        Clinics clinic = clinicRepo.findById(form.getClinicId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy clinic: " + form.getClinicId()));

        Users patient = null;
        if (form.getPatientUserId() != null) {
            patient = usersRepo.findById(form.getPatientUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user bệnh nhân: " + form.getPatientUserId()));
        }

        Invoice inv = new Invoice();
        inv.setAppointment(appt);
        inv.setClinic(clinic);
        inv.setPatient(patient);

        inv.setInvoiceDate(LocalDateTime.now());
        inv.setSubtotal(nvl(form.getSubtotal()));
        inv.setDiscount(nvl(form.getDiscount()));
        inv.setTaxAmount(nvl(form.getTaxAmount()));

        BigDecimal total = inv.getSubtotal()
                .subtract(inv.getDiscount())
                .add(inv.getTaxAmount());
        inv.setTotalAmount(total);

        inv.setStatus(InvoiceStatus.ISSUED);
        invoiceRepo.save(inv);

        // Lưu items nếu có
        if (form.getItems() != null && !form.getItems().isEmpty()) {
            for (InvoiceItemForm itf : form.getItems()) {
                if (itf == null) {
                    continue;
                }
                InvoiceItem it = new InvoiceItem();
                it.setInvoice(inv);
                it.setItemName(itf.getItemName());
                it.setQuantity(itf.getQuantity() == null ? BigDecimal.ONE : itf.getQuantity());
                it.setUnitPrice(itf.getUnitPrice() == null ? BigDecimal.ZERO : itf.getUnitPrice());
                it.setLineTotal(it.getUnitPrice().multiply(it.getQuantity()));
                itemRepo.save(it);
            }
        }

        return inv;
    }
}
