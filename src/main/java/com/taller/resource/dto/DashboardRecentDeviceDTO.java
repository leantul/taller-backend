package com.taller.resource.dto;

import com.taller.model.enums.DeviceTypeEnum;

public class DashboardRecentDeviceDTO {
    private String id;
    private DeviceTypeEnum deviceType;
    private String brand;
    private String model;

    public String getId() {
        return this.id;
    }

    public DeviceTypeEnum getDeviceType() {
        return this.deviceType;
    }

    public String getBrand() {
        return this.brand;
    }

    public String getModel() {
        return this.model;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDeviceType(DeviceTypeEnum deviceType) {
        this.deviceType = deviceType;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
