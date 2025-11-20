/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author ADMIN
 */
package com.spring.bioMedical.form;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class InvoiceCreateForm {
  private Long appointmentId;
  private Long clinicId;
  private Long patientUserId;
  private BigDecimal subtotal;
  private BigDecimal discount = BigDecimal.ZERO;
  private BigDecimal taxAmount = BigDecimal.ZERO;
  private List<InvoiceItemForm> items;
}

