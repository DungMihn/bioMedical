/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.bioMedical.form;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentCaptureForm {
  private Long invoiceId;
  private BigDecimal amount;
  private String method;      // CASH/CARD/TRANSFER
  private String referenceNo; // optional
}
