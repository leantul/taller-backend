package com.taller.resource.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class DashboardOverviewDTO {
    private long clientCount;
    private long deviceCount;
    private long repairCount;
    private BigDecimal totalRevenue;
    private BigDecimal monthRevenue;
    private long waitingPickupCount;
    private long inProgressCount;
    private long quotedPendingCount;
    private List<DashboardSeriesItemDTO> monthlyRevenue;
    private List<DashboardSeriesItemDTO> deviceTypes;
    private List<DashboardSeriesItemDTO> repairStatuses;
    private List<DashboardRecentClientDTO> recentClients;
    private List<DashboardRecentDeviceDTO> recentDevices;
    private List<DashboardRecentRepairDTO> recentRepairs;
    private List<DashboardInactiveDeviceDTO> inactiveDevices;
}
