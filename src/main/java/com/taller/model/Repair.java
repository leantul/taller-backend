package com.taller.model;

import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.enums.converter.RepairStatusEnumConverter;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "repairs")
@Entity
@AttributeOverride(name = "id", column = @Column(name = "id_repair"))
public class Repair extends BasicEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_device", insertable = false, updatable = false)
    private Device device;

    @Column(name = "id_device")
    private String idDevice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_client", insertable = false, updatable = false)
    private Client client;

    @Column(name = "id_client")
    private String idClient;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "status")
    @Convert(converter = RepairStatusEnumConverter.class)
    private RepairStatusEnum status;

    @Column(name = "receive_date_time")
    private LocalDateTime receiveDateTime;

    @Column(name = "return_date_time")
    private LocalDateTime returnDateTime;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "labor_amount")
    private BigDecimal laborAmount;

    @Column(name = "extra_amount")
    private BigDecimal extraAmount;

    @Column(name = "quoted_amount")
    private BigDecimal quotedAmount;

    @Column(name = "quote_notes", columnDefinition = "TEXT")
    private String quoteNotes;

    @Column(name = "approved")
    private Boolean approved;

    @Column(name = "rejected")
    private Boolean rejected;

    @Column(name = "ready_notified_at")
    private LocalDateTime readyNotifiedAt;

    @OneToMany(mappedBy = "repair", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepairPart> parts = new ArrayList<>();

    @OneToMany(mappedBy = "repair", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepairPayment> payments = new ArrayList<>();

    public Repair() {
    }

    public Repair(Device device, String idDevice, Client client, String idClient, String description, String orderNumber, RepairStatusEnum status, LocalDateTime receiveDateTime, LocalDateTime returnDateTime, BigDecimal price, BigDecimal laborAmount, BigDecimal extraAmount, BigDecimal quotedAmount, String quoteNotes, Boolean approved, Boolean rejected, LocalDateTime readyNotifiedAt) {
        this.device = device;
        this.idDevice = idDevice;
        this.client = client;
        this.idClient = idClient;
        this.description = description;
        this.orderNumber = orderNumber;
        this.status = status;
        this.receiveDateTime = receiveDateTime;
        this.returnDateTime = returnDateTime;
        this.price = price;
        this.laborAmount = laborAmount;
        this.extraAmount = extraAmount;
        this.quotedAmount = quotedAmount;
        this.quoteNotes = quoteNotes;
        this.approved = approved;
        this.rejected = rejected;
        this.readyNotifiedAt = readyNotifiedAt;
    }

    public Device getDevice() {
        return this.device;
    }

    public String getIdDevice() {
        return this.idDevice;
    }

    public Client getClient() {
        return this.client;
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

    public void setDevice(Device device) {
        this.device = device;
    }

    public void setIdDevice(String idDevice) {
        this.idDevice = idDevice;
    }

    public void setClient(Client client) {
        this.client = client;
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

    public static RepairBuilder builder() {
        return new RepairBuilder();
    }

    public static class RepairBuilder {
        private Device device;
        private String idDevice;
        private Client client;
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

        public RepairBuilder device(Device device) {
            this.device = device;
            return this;
        }

        public RepairBuilder idDevice(String idDevice) {
            this.idDevice = idDevice;
            return this;
        }

        public RepairBuilder client(Client client) {
            this.client = client;
            return this;
        }

        public RepairBuilder idClient(String idClient) {
            this.idClient = idClient;
            return this;
        }

        public RepairBuilder description(String description) {
            this.description = description;
            return this;
        }

        public RepairBuilder orderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
            return this;
        }

        public RepairBuilder status(RepairStatusEnum status) {
            this.status = status;
            return this;
        }

        public RepairBuilder receiveDateTime(LocalDateTime receiveDateTime) {
            this.receiveDateTime = receiveDateTime;
            return this;
        }

        public RepairBuilder returnDateTime(LocalDateTime returnDateTime) {
            this.returnDateTime = returnDateTime;
            return this;
        }

        public RepairBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public RepairBuilder laborAmount(BigDecimal laborAmount) {
            this.laborAmount = laborAmount;
            return this;
        }

        public RepairBuilder extraAmount(BigDecimal extraAmount) {
            this.extraAmount = extraAmount;
            return this;
        }

        public RepairBuilder quotedAmount(BigDecimal quotedAmount) {
            this.quotedAmount = quotedAmount;
            return this;
        }

        public RepairBuilder quoteNotes(String quoteNotes) {
            this.quoteNotes = quoteNotes;
            return this;
        }

        public RepairBuilder approved(Boolean approved) {
            this.approved = approved;
            return this;
        }

        public RepairBuilder rejected(Boolean rejected) {
            this.rejected = rejected;
            return this;
        }

        public RepairBuilder readyNotifiedAt(LocalDateTime readyNotifiedAt) {
            this.readyNotifiedAt = readyNotifiedAt;
            return this;
        }

        public Repair build() {
            return new Repair(device, idDevice, client, idClient, description, orderNumber, status, receiveDateTime, returnDateTime, price, laborAmount, extraAmount, quotedAmount, quoteNotes, approved, rejected, readyNotifiedAt);
        }
    }
}
