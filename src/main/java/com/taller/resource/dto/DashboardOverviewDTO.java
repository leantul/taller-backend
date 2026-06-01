package com.taller.resource.dto;


import java.util.List;

public class DashboardOverviewDTO {
    private long clientCount;
    private long deviceCount;
    private long repairCount;
    private long waitingPickupCount;
    private long inProgressCount;
    private long quotedPendingCount;
    private List<DashboardSeriesItemDTO> deviceTypes;
    private List<DashboardSeriesItemDTO> repairStatuses;
    private List<DashboardRecentClientDTO> recentClients;
    private List<DashboardRecentDeviceDTO> recentDevices;
    private List<DashboardRecentRepairDTO> recentRepairs;
    private List<DashboardInactiveDeviceDTO> inactiveDevices;

    public long getClientCount() {
        return this.clientCount;
    }

    public long getDeviceCount() {
        return this.deviceCount;
    }

    public long getRepairCount() {
        return this.repairCount;
    }

    public long getWaitingPickupCount() {
        return this.waitingPickupCount;
    }

    public long getInProgressCount() {
        return this.inProgressCount;
    }

    public long getQuotedPendingCount() {
        return this.quotedPendingCount;
    }

    public List<DashboardSeriesItemDTO> getDeviceTypes() {
        return this.deviceTypes;
    }

    public List<DashboardSeriesItemDTO> getRepairStatuses() {
        return this.repairStatuses;
    }

    public List<DashboardRecentClientDTO> getRecentClients() {
        return this.recentClients;
    }

    public List<DashboardRecentDeviceDTO> getRecentDevices() {
        return this.recentDevices;
    }

    public List<DashboardRecentRepairDTO> getRecentRepairs() {
        return this.recentRepairs;
    }

    public List<DashboardInactiveDeviceDTO> getInactiveDevices() {
        return this.inactiveDevices;
    }

    public void setClientCount(long clientCount) {
        this.clientCount = clientCount;
    }

    public void setDeviceCount(long deviceCount) {
        this.deviceCount = deviceCount;
    }

    public void setRepairCount(long repairCount) {
        this.repairCount = repairCount;
    }

    public void setWaitingPickupCount(long waitingPickupCount) {
        this.waitingPickupCount = waitingPickupCount;
    }

    public void setInProgressCount(long inProgressCount) {
        this.inProgressCount = inProgressCount;
    }

    public void setQuotedPendingCount(long quotedPendingCount) {
        this.quotedPendingCount = quotedPendingCount;
    }

    public void setDeviceTypes(List<DashboardSeriesItemDTO> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    public void setRepairStatuses(List<DashboardSeriesItemDTO> repairStatuses) {
        this.repairStatuses = repairStatuses;
    }

    public void setRecentClients(List<DashboardRecentClientDTO> recentClients) {
        this.recentClients = recentClients;
    }

    public void setRecentDevices(List<DashboardRecentDeviceDTO> recentDevices) {
        this.recentDevices = recentDevices;
    }

    public void setRecentRepairs(List<DashboardRecentRepairDTO> recentRepairs) {
        this.recentRepairs = recentRepairs;
    }

    public void setInactiveDevices(List<DashboardInactiveDeviceDTO> inactiveDevices) {
        this.inactiveDevices = inactiveDevices;
    }
}
