package com.taller.resource.dto;

import com.taller.model.enums.RepairStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private BigDecimal laborAmount;
    private BigDecimal extraAmount;
    private BigDecimal quotedAmount;
    private String quoteNotes;
    private Boolean approved;
    private Boolean rejected;
    private LocalDateTime readyNotifiedAt;
    private List<RepairPartDTO> parts;
    private List<RepairPaymentDTO> payments;
    private List<DeviceObservationDTO> observations;

    public String getId() {
        return this.id;
    }

    public DeviceDTO getDevice() {
        return this.device;
    }

    public ClientDTO getClient() {
        return this.client;
    }

    public String getIdDevice() {
        return this.idDevice;
    }

    public String getIdClient() {
        return this.idClient;
    }

    public String getDescription() {
        return this.description;
    }

    public String getOrderNumber() {
        return this.orderNumber;
    }

    public RepairStatusEnum getStatus() {
        return this.status;
    }

    public LocalDateTime getReceiveDateTime() {
        return this.receiveDateTime;
    }

    public LocalDateTime getReturnDateTime() {
        return this.returnDateTime;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public BigDecimal getLaborAmount() {
        return this.laborAmount;
    }

    public BigDecimal getExtraAmount() {
        return this.extraAmount;
    }

    public BigDecimal getQuotedAmount() {
        return this.quotedAmount;
    }

    public String getQuoteNotes() {
        return this.quoteNotes;
    }

    public Boolean getApproved() {
        return this.approved;
    }

    public Boolean getRejected() {
        return this.rejected;
    }

    public LocalDateTime getReadyNotifiedAt() {
        return this.readyNotifiedAt;
    }

    public List<RepairPartDTO> getParts() {
        return this.parts;
    }

    public List<RepairPaymentDTO> getPayments() {
        return this.payments;
    }

    public List<DeviceObservationDTO> getObservations() {
        return this.observations;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDevice(DeviceDTO device) {
        this.device = device;
    }

    public void setClient(ClientDTO client) {
        this.client = client;
    }

    public void setIdDevice(String idDevice) {
        this.idDevice = idDevice;
    }

    public void setIdClient(String idClient) {
        this.idClient = idClient;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setStatus(RepairStatusEnum status) {
        this.status = status;
    }

    public void setReceiveDateTime(LocalDateTime receiveDateTime) {
        this.receiveDateTime = receiveDateTime;
    }

    public void setReturnDateTime(LocalDateTime returnDateTime) {
        this.returnDateTime = returnDateTime;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setLaborAmount(BigDecimal laborAmount) {
        this.laborAmount = laborAmount;
    }

    public void setExtraAmount(BigDecimal extraAmount) {
        this.extraAmount = extraAmount;
    }

    public void setQuotedAmount(BigDecimal quotedAmount) {
        this.quotedAmount = quotedAmount;
    }

    public void setQuoteNotes(String quoteNotes) {
        this.quoteNotes = quoteNotes;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public void setRejected(Boolean rejected) {
        this.rejected = rejected;
    }

    public void setReadyNotifiedAt(LocalDateTime readyNotifiedAt) {
        this.readyNotifiedAt = readyNotifiedAt;
    }

    public void setParts(List<RepairPartDTO> parts) {
        this.parts = parts;
    }

    public void setPayments(List<RepairPaymentDTO> payments) {
        this.payments = payments;
    }

    public void setObservations(List<DeviceObservationDTO> observations) {
        this.observations = observations;
    }
}
