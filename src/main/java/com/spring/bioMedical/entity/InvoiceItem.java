package com.spring.bioMedical.entity;

import lombok.Getter; import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "InvoiceItems")
@Getter @Setter
public class InvoiceItem {
 @Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "item_id")
private Long itemId;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "invoice_id", nullable = false)
private Invoice invoice;

@Column(name = "item_name", nullable = false)
private String itemName;

@Column(name = "quantity", nullable = false)
private BigDecimal quantity = BigDecimal.ONE;

@Column(name = "unit_price", nullable = false)
private BigDecimal unitPrice = BigDecimal.ZERO;

@Column(name = "line_total", nullable = false)
private BigDecimal lineTotal = BigDecimal.ZERO;

}
