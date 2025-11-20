package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.Appointments;
import com.spring.bioMedical.entity.Invoice;
import com.spring.bioMedical.entity.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByClinic_ClinicId(Long clinicId);

    List<Invoice> findByClinic_ClinicIdAndInvoiceNoContainingIgnoreCase(Long clinicId, String invoiceNo);

    List<Invoice> findByStatus(InvoiceStatus status);

    List<Invoice> findByInvoiceNoContainingIgnoreCase(String q);

    // truy vấn theo quan hệ, không phụ thuộc tên field id của Appointment trong HQL
    Invoice findByAppointment(Appointments appointment);
    
    @EntityGraph(attributePaths = {"items","clinic","patient","appointment"})
  Optional<Invoice> findWithDetailByInvoiceId(@Param("id") Long id);
}
