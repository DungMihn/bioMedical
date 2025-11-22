package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

    // JPQL (để dùng bình thường)
    List<InvoiceItem> findByInvoice_InvoiceIdOrderByItemIdAsc(Long invoiceId);

    long countByInvoice_InvoiceId(Long invoiceId);

    // Native (để xác thực nhanh khi có nghi ngờ mapping)
    @Query(value
            = "SELECT * FROM InvoiceItems WHERE invoice_id = :invoiceId ORDER BY item_id",
            nativeQuery = true)
    List<InvoiceItem> findNativeItemsByInvoiceId(@Param("invoiceId") Long invoiceId);

    @Query(value
            = "SELECT COUNT(1) FROM InvoiceItems WHERE invoice_id = :invoiceId",
            nativeQuery = true)
    long countNativeItemsByInvoiceId(@Param("invoiceId") Long invoiceId);

    List<InvoiceItem> findByInvoice_InvoiceId(Long invoiceId);
}
