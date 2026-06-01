package com.taller.resource.dto;


public class DashboardInactiveDeviceDTO {
    private String name;
    private String lastRepair;

    public String getName() {
        return this.name;
    }

    public String getLastRepair() {
        return this.lastRepair;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastRepair(String lastRepair) {
        this.lastRepair = lastRepair;
    }
}
