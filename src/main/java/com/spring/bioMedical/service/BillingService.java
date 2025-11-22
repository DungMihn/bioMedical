/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.AppointmentSlots;
import com.spring.bioMedical.entity.Appointments;
import com.spring.bioMedical.entity.Clinics;
import com.spring.bioMedical.entity.Invoice;
import com.spring.bioMedical.entity.InvoiceItem;
import com.spring.bioMedical.entity.MedicalRecords;
import com.spring.bioMedical.entity.Prescriptions;
import com.spring.bioMedical.entity.PriceList;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.entity.enums.InvoiceStatus;
import com.spring.bioMedical.repository.InvoiceItemRepository;
import com.spring.bioMedical.repository.InvoiceRepository;
import com.spring.bioMedical.repository.MedicalRecordsRepository;
import com.spring.bioMedical.repository.PrescriptionsRepository;
import com.spring.bioMedical.repository.PriceListRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Admin
 */
@Service
@RequiredArgsConstructor
public class BillingService {

    private final MedicalRecordsRepository medicalRecordsRepo;
    private final PrescriptionsRepository prescriptionsRepo;
    private final InvoiceRepository invoiceRepo;
    private final InvoiceItemRepository invoiceItemRepo;
    private final PriceListRepository priceListRepo;

    @Transactional
    public Invoice generateInvoiceForRecord(Long recordId) {

        System.out.println("\n===============================");
        System.out.println("📘 [BILLING] BẮT ĐẦU TẠO INVOICE cho recordId = " + recordId);
        System.out.println("===============================\n");

        // 1. Load MedicalRecord
        MedicalRecords record = medicalRecordsRepo.findById(recordId).orElse(null);
        if (record == null) {
            System.err.println("❌ [BILLING] MedicalRecord NOT FOUND: " + recordId);
            throw new IllegalArgumentException("MedicalRecord not found");
        }
        System.out.println("✔ MedicalRecord loaded: recordId=" + record.getRecordId());

        Appointments appt = record.getAppointment();
        if (appt == null) {
            System.err.println("❌ [BILLING] Appointment = NULL trong MedicalRecord!");
            throw new IllegalStateException("Record has no appointment");
        }
        System.out.println("✔ Appointment loaded: appointmentId=" + appt.getAppointmentId());

        Users patient = appt.getUser();
        if (patient == null) {
            System.err.println("❌ [BILLING] Patient (Users) = NULL trong Appointment!");
            throw new IllegalStateException("Appointment has no patient");
        }
        System.out.println("✔ Patient loaded: " + patient.getFullName() + " (userId=" + patient.getUserId() + ")");

        AppointmentSlots slot = appt.getSlot();
        if (slot == null) {
            System.err.println("❌ [BILLING] Slot = NULL trong Appointment!");
            throw new IllegalStateException("Appointment has no slot");
        }
        System.out.println("✔ Slot loaded: slotId=" + slot.getSlotId());

        Clinics clinic = slot.getClinic();
        if (clinic == null) {
            System.err.println("❌ [BILLING] Clinic = NULL trong Slot!");
            throw new IllegalStateException("Slot has no clinic");
        }
        System.out.println("✔ Clinic loaded: clinicId=" + clinic.getClinicId());

        // 2. Check existing invoice
        Invoice existing = invoiceRepo.findByAppointment_AppointmentId(appt.getAppointmentId());
        if (existing != null) {
            System.out.println("⚠️ [BILLING] Invoice đã tồn tại → invoiceId: " + existing.getInvoiceId());
            return existing;
        }

        System.out.println("➡ Chưa có invoice → Tiến hành tạo invoice mới");

        // 3. Create invoice
        Invoice invoice = new Invoice();
        invoice.setAppointment(appt);
        invoice.setClinic(clinic);
        invoice.setPatient(patient);
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setInvoiceDate(LocalDateTime.now());

        BigDecimal subtotal = BigDecimal.ZERO;

        // ===========================
        // 4. Add CONSULTATION FEE
        // ===========================
        System.out.println("🔎 Tìm giá dịch vụ khám (CONSULTATION)");

        PriceList consultation = priceListRepo
                .findFirstByItemTypeAndItemNameIgnoreCase("SERVICE", "CONSULTATION")
                .orElse(null);

        if (consultation != null) {
            InvoiceItem consultItem = new InvoiceItem();
            consultItem.setInvoice(invoice);
            consultItem.setItemName(consultation.getItemName());
            consultItem.setQuantity(BigDecimal.ONE);
            consultItem.setUnitPrice(consultation.getDefaultPrice());
            consultItem.setLineTotal(consultation.getDefaultPrice());

            invoice.getItems().add(consultItem);
            subtotal = subtotal.add(consultItem.getLineTotal());

            System.out.println("✔ Thêm phí khám: " + consultation.getDefaultPrice());
        } else {
            System.err.println("⚠️ WARNING: Không tìm thấy giá CONSULTATION trong PriceList!");
        }

        // ===========================
        // 5. Add MEDICINES
        // ===========================
        System.out.println("🔎 Lấy danh sách thuốc từ đơn thuốc (Prescriptions)…");

        List<Prescriptions> presList = prescriptionsRepo.findByMedicalRecordRecordId(recordId);

        if (presList == null || presList.isEmpty()) {
            System.out.println("⚠️ Không có thuốc trong đơn – bỏ qua phần thuốc");
        } else {
            for (Prescriptions p : presList) {
                System.out.println("➡ Thuốc: " + p.getMedicationName());

                PriceList price = priceListRepo
                        .findFirstByItemTypeAndItemNameIgnoreCase("MEDICINE", p.getMedicationName())
                        .orElse(null);

                if (price == null) {
                    System.err.println("⚠️ Không tìm thấy giá thuốc: " + p.getMedicationName());
                    continue;
                }

                BigDecimal qty = BigDecimal.ONE;
                BigDecimal unitPrice = price.getDefaultPrice();
                BigDecimal lineTotal = unitPrice.multiply(qty);

                InvoiceItem medItem = new InvoiceItem();
                medItem.setInvoice(invoice);
                medItem.setItemName(p.getMedicationName());
                medItem.setQuantity(qty);
                medItem.setUnitPrice(unitPrice);
                medItem.setLineTotal(lineTotal);

                invoice.getItems().add(medItem);
                subtotal = subtotal.add(lineTotal);

                System.out.println("✔ Thêm thuốc " + p.getMedicationName()
                        + " | Giá: " + unitPrice
                        + " | Thành tiền: " + lineTotal);
            }
        }

        // ===========================
        // 6. Total Calculation
        // ===========================
        invoice.setSubtotal(subtotal);
        invoice.setDiscount(BigDecimal.ZERO);
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setTotalAmount(subtotal);

        System.out.println("\n📌 Tổng tiền tạm tính (subtotal): " + subtotal);
        System.out.println("📌 Tổng tiền cuối: " + subtotal);

        // 7. Save Invoice
        Invoice saved = invoiceRepo.save(invoice);

        System.out.println("===============================");
        System.out.println("✅ [BILLING] TẠO INVOICE THÀNH CÔNG!");
        System.out.println("➡ invoiceId = " + saved.getInvoiceId());
        System.out.println("➡ items = " + saved.getItems().size());
        System.out.println("===============================\n");

        return saved;
    }

    @Transactional
    public Invoice generateRandomInvoiceForRecord(Long recordId) {

        System.out.println("\n===============================");
        System.out.println("🧪 [TEST BILLING] TẠO INVOICE RANDOM cho recordId = " + recordId);
        System.out.println("===============================\n");

        // 1. Load MedicalRecord
        MedicalRecords record = medicalRecordsRepo.findById(recordId).orElse(null);
        if (record == null) {
            throw new IllegalArgumentException("MedicalRecord not found: " + recordId);
        }

        Appointments appt = record.getAppointment();
        if (appt == null) {
            throw new IllegalStateException("Record has no appointment");
        }

        Users patient = appt.getUser();
        if (patient == null) {
            throw new IllegalStateException("Appointment has no patient");
        }

        AppointmentSlots slot = appt.getSlot();
        if (slot == null) {
            throw new IllegalStateException("Appointment has no slot");
        }

        Clinics clinic = slot.getClinic();
        if (clinic == null) {
            throw new IllegalStateException("Slot has no clinic");
        }

        System.out.println("✔ RecordId=" + recordId
                + " | AppointmentId=" + appt.getAppointmentId()
                + " | Patient=" + patient.getFullName()
                + " | ClinicId=" + clinic.getClinicId());

        // 2. Nếu đã có invoice rồi thì khỏi tạo nữa
        Invoice existing = invoiceRepo.findByAppointment_AppointmentId(appt.getAppointmentId());
        if (existing != null) {
            System.out.println("⚠️ [TEST BILLING] Invoice đã tồn tại, dùng lại invoiceId=" + existing.getInvoiceId());
            return existing;
        }

        // 3. Tạo invoice DRAFT (chưa có tổng tiền)
        Invoice invoice = new Invoice();
        invoice.setAppointment(appt);
        invoice.setClinic(clinic);
        invoice.setPatient(patient);
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setInvoiceDate(LocalDateTime.now());
        invoice.setCreatedAt(LocalDateTime.now()); // nếu entity có trường này

        // Lưu trước để có invoiceId cho InvoiceItems
        invoice = invoiceRepo.save(invoice);

        // 4. Tạo một số item TEST với giá random
        List<String> testItemNames = Arrays.asList(
                "Khám tổng quát",
                "Xét nghiệm máu",
                "Thuốc giảm đau",
                "Thuốc hạ sốt",
                "Thuốc kháng sinh"
        );

        Random random = new Random();
        BigDecimal subtotal = BigDecimal.ZERO;

        int itemCount = 1 + random.nextInt(3); // 1 đến 3 item
        System.out.println("🧾 Sinh " + itemCount + " InvoiceItems test...");

        for (int i = 0; i < itemCount; i++) {
            String itemName = testItemNames.get(random.nextInt(testItemNames.size()));

            BigDecimal quantity = BigDecimal.valueOf(1 + random.nextInt(3)); // 1–3
            BigDecimal unitPrice = BigDecimal.valueOf(50000 + random.nextInt(200000)); // 50k–250k
            BigDecimal lineTotal = unitPrice.multiply(quantity);

            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoice);
            item.setItemName(itemName);
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            item.setLineTotal(lineTotal);

            // nếu Invoice có List<InvoiceItem> items;
            if (invoice.getItems() != null) {
                invoice.getItems().add(item);
            }

            invoiceItemRepo.save(item); // lưu từng item
            subtotal = subtotal.add(lineTotal);

            System.out.println("  ➕ Item: " + itemName
                    + " | qty=" + quantity
                    + " | unitPrice=" + unitPrice
                    + " | lineTotal=" + lineTotal);
        }

        // 5. Cập nhật lại tổng tiền vào Invoice
        invoice.setSubtotal(subtotal);
        invoice.setDiscount(BigDecimal.ZERO);
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setTotalAmount(subtotal);

        invoice = invoiceRepo.save(invoice);

        System.out.println("📌 Subtotal (TEST) = " + subtotal);
        System.out.println("✅ [TEST BILLING] TẠO INVOICE RANDOM THÀNH CÔNG, invoiceId=" + invoice.getInvoiceId());
        System.out.println("===============================\n");

        return invoice;
    }

}
