package com.taller.resource.dto;


public class DashboardRecentClientDTO {
    private String id;
    private String name;
    private String deviceType;

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDeviceType() {
        return this.deviceType;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
