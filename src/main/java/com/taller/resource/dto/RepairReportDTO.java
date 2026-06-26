package com.taller.resource.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepairReportDTO {
    private String id;
    private String repairId;
    private String orderNumber;
    private LocalDateTime issuedAt;
    private String clientName;
    private String clientLastName;
    private String clientPhone;
    private String clientEmail;
    private String deviceTypeName;
    private String deviceBrand;
    private String deviceModel;
    private String deviceSerialNumber;
    private String reportedIssue;
    private String workPerformed;
    private String finalObservations;
    private Boolean showPartPrices;
    private BigDecimal finalAmount;
    private List<RepairReportHardwareItemDTO> hardwareItems;
    private List<RepairReportSoftwareItemDTO> softwareItems;
}
