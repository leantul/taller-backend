package com.taller.resource.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DashboardOverviewDTO {
    private long clientCount;
    private long deviceCount;
    private long repairCount;
    private long waitingPickupCount;
    private long inProgressCount;
    private long quotedPendingCount;
    private List<DashboardSeriesItemDTO> monthlyReceivedRepairs;
    private double averageTurnaroundDays;
    private long overdueOpenRepairs;
    private List<DashboardInactiveDeviceDTO> inactiveDevices;
}
