/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.bioMedical.form;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReportFilterForm {
  private LocalDate from;
  private LocalDate to;
  private Long clinicId;  // optional
  private Long doctorId;  // optional
}

