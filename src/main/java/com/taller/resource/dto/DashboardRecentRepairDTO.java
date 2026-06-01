package com.taller.resource.dto;


import java.math.BigDecimal;

public class DashboardRecentRepairDTO {
    private String repairId;
    private String date;
    private String client;
    private BigDecimal price;

    public String getRepairId() {
        return this.repairId;
    }

    public String getDate() {
        return this.date;
    }

    public String getClient() {
        return this.client;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public void setRepairId(String repairId) {
        this.repairId = repairId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
