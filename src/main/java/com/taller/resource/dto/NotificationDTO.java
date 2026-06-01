package com.taller.resource.dto;


import com.taller.model.enums.DeviceTypeEnum;
import com.taller.model.enums.RepairStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class NotificationDTO {
    private String id;
    private String title;
    private String message;
    private Boolean readed;
    private LocalDateTime eventDate;
    private String type;
    private String entityId;
    private String repairId;
    private String deviceId;
    private String clientId;
    private String clientName;
    private String clientLastName;
    private String clientPhone;
    private String clientEmail;
    private DeviceTypeEnum deviceType;
    private String deviceBrand;
    private String deviceModel;
    private String deviceSerialNumber;
    private String orderNumber;
    private String repairDescription;
    private RepairStatusEnum status;
    private LocalDateTime receiveDateTime;
    private LocalDateTime returnDateTime;
    private BigDecimal quotedAmount;
    private BigDecimal price;
    private String quoteNotes;
    private List<RepairPartDTO> parts;
    private String observationId;
    private String observationNote;
    private LocalDateTime observationObservedAt;
    private LocalDateTime observationFollowUpAt;

    public String getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getMessage() {
        return this.message;
    }

    public Boolean getReaded() {
        return this.readed;
    }

    public LocalDateTime getEventDate() {
        return this.eventDate;
    }

    public String getType() {
        return this.type;
    }

    public String getEntityId() {
        return this.entityId;
    }

    public String getRepairId() {
        return this.repairId;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getClientName() {
        return this.clientName;
    }

    public String getClientLastName() {
        return this.clientLastName;
    }

    public String getClientPhone() {
        return this.clientPhone;
    }

    public String getClientEmail() {
        return this.clientEmail;
    }

    public DeviceTypeEnum getDeviceType() {
        return this.deviceType;
    }

    public String getDeviceBrand() {
        return this.deviceBrand;
    }

    public String getDeviceModel() {
        return this.deviceModel;
    }

    public String getDeviceSerialNumber() {
        return this.deviceSerialNumber;
    }

    public String getOrderNumber() {
        return this.orderNumber;
    }

    public String getRepairDescription() {
        return this.repairDescription;
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

    public BigDecimal getQuotedAmount() {
        return this.quotedAmount;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public String getQuoteNotes() {
        return this.quoteNotes;
    }

    public List<RepairPartDTO> getParts() {
        return this.parts;
    }

    public String getObservationId() {
        return this.observationId;
    }

    public String getObservationNote() {
        return this.observationNote;
    }

    public LocalDateTime getObservationObservedAt() {
        return this.observationObservedAt;
    }

    public LocalDateTime getObservationFollowUpAt() {
        return this.observationFollowUpAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setReaded(Boolean readed) {
        this.readed = readed;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public void setRepairId(String repairId) {
        this.repairId = repairId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setClientLastName(String clientLastName) {
        this.clientLastName = clientLastName;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public void setDeviceType(DeviceTypeEnum deviceType) {
        this.deviceType = deviceType;
    }

    public void setDeviceBrand(String deviceBrand) {
        this.deviceBrand = deviceBrand;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public void setDeviceSerialNumber(String deviceSerialNumber) {
        this.deviceSerialNumber = deviceSerialNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setRepairDescription(String repairDescription) {
        this.repairDescription = repairDescription;
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

    public void setQuotedAmount(BigDecimal quotedAmount) {
        this.quotedAmount = quotedAmount;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setQuoteNotes(String quoteNotes) {
        this.quoteNotes = quoteNotes;
    }

    public void setParts(List<RepairPartDTO> parts) {
        this.parts = parts;
    }

    public void setObservationId(String observationId) {
        this.observationId = observationId;
    }

    public void setObservationNote(String observationNote) {
        this.observationNote = observationNote;
    }

    public void setObservationObservedAt(LocalDateTime observationObservedAt) {
        this.observationObservedAt = observationObservedAt;
    }

    public void setObservationFollowUpAt(LocalDateTime observationFollowUpAt) {
        this.observationFollowUpAt = observationFollowUpAt;
    }
}
