package com.taller.resource.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taller.model.enums.RepairStatusEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class RepairDTO {
    private String id;
    private DeviceDTO device;
    private ClientDTO client;
    private String idDevice;
    private String idClient;
    private String description;
    private String orderNumber;
    private RepairStatusEnum status;
    private LocalDateTime receiveDateTime;
    private LocalDateTime returnDateTime;
    private BigDecimal price;
    @NotNull(message = "Completá la mano de obra. Si no corresponde, ingresá $0")
    @DecimalMin(value = "0.0", message = "La mano de obra no puede ser negativa")
    private BigDecimal laborAmount;
    private BigDecimal extraAmount;
    private BigDecimal quotedAmount;
    private String quoteNotes;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String repairNotes;
    private Boolean approved;
    private Boolean rejected;
    private LocalDateTime readyNotifiedAt;
    private List<RepairPartDTO> parts;
    private List<RepairPaymentDTO> payments;
    private BigDecimal totalPaid;
    private BigDecimal outstandingBalance;
    private RepairStatusUpdateDTO.PaymentType paymentType;
    private BigDecimal paymentAmount;
    private List<DeviceObservationDTO> observations;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<RepairStatusHistoryDTO> statusHistory;
    private String clientName;
    private String clientPhone;
    private String deviceLabel;
}
