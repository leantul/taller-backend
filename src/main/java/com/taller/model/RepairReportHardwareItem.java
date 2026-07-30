package com.taller.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@Entity
@Table(name = "repair_report_hardware_items", indexes = @Index(name = "idx_repair_report_hardware_items_report_id", columnList = "repair_report_id"))
@AllArgsConstructor
@NoArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "id_repair_report_hardware_item"))
public class RepairReportHardwareItem extends BasicEntity {

    @Column(name = "repair_report_id", nullable = false)
    private String repairReportId;

    @Column(name = "part_name", nullable = false)
    private String partName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "detail")
    private String detail;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "include_price", nullable = false)
    private Boolean includePrice;
}
