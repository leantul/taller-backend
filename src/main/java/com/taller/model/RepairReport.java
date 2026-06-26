package com.taller.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@Entity
@Table(name = "repair_reports")
@AllArgsConstructor
@NoArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "id_repair_report"))
public class RepairReport extends BasicEntity {

    @Column(name = "repair_id", nullable = false)
    private String repairId;

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "client_last_name")
    private String clientLastName;

    @Column(name = "client_phone")
    private String clientPhone;

    @Column(name = "client_email")
    private String clientEmail;

    @Column(name = "device_type_name")
    private String deviceTypeName;

    @Column(name = "device_brand")
    private String deviceBrand;

    @Column(name = "device_model")
    private String deviceModel;

    @Column(name = "device_serial_number")
    private String deviceSerialNumber;

    @Column(name = "reported_issue", columnDefinition = "TEXT")
    private String reportedIssue;

    @Column(name = "work_performed", columnDefinition = "TEXT")
    private String workPerformed;

    @Column(name = "final_observations", columnDefinition = "TEXT")
    private String finalObservations;

    @Column(name = "show_part_prices", nullable = false)
    private Boolean showPartPrices;

    @Column(name = "final_amount")
    private BigDecimal finalAmount;
}
