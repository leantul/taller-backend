package com.taller.service;

import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.repository.DeviceRepository;
import com.taller.model.repository.RepairRepository;
import com.taller.model.repository.projection.DeviceLastRepairView;
import com.taller.model.repository.projection.DashboardCountsView;
import com.taller.model.repository.projection.FinanceRepairView;
import com.taller.model.repository.projection.RepairMonthlyCountView;
import com.taller.model.repository.projection.RepairStatusCountView;
import com.taller.resource.dto.DashboardDTO;
import com.taller.resource.dto.DashboardInactiveDeviceDTO;
import com.taller.resource.dto.DashboardOverviewDTO;
import com.taller.resource.dto.DashboardSeriesItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RepairService repairService;
    private final DeviceRepository deviceRepository;
    private final RepairRepository repairRepository;
    private final Clock applicationClock;

    @Transactional(readOnly = true)
    public DashboardDTO monthSummary(int year, int month) {
        LocalDateTime from = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime to = from.plusMonths(1).minusSeconds(1);

        BigDecimal income = repairService.totalIncome(from, to);
        List<FinanceRepairView> repairs = repairRepository.findFinanceRowsBetween(from, to);
        BigDecimal costs = repairs.stream()
                .map(repair -> safeMoney(repair.getPartsCost()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardDTO.builder()
                .totalRecaudacion(income)
                .totalCostos(costs)
                .totalGanancia(income.subtract(costs))
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardOverviewDTO overview() {
        DashboardOverviewDTO dto = new DashboardOverviewDTO();
        DashboardCountsView counts = repairRepository.dashboardCounts();
        dto.setClientCount(counts.getClientCount());
        dto.setDeviceCount(counts.getDeviceCount());
        dto.setRepairCount(counts.getRepairCount());

        Map<RepairStatusEnum, Long> statusCounts = new EnumMap<>(RepairStatusEnum.class);
        for (RepairStatusCountView countView : repairRepository.countByStatus()) {
            statusCounts.put(countView.getStatus(), countView.getTotal());
        }
        dto.setWaitingPickupCount(statusCounts.getOrDefault(RepairStatusEnum.ESPERANDO_RETIRO, 0L)
                + statusCounts.getOrDefault(RepairStatusEnum.COBRADO_ESPERANDO_RETIRO, 0L));
        dto.setInProgressCount(
                statusCounts.getOrDefault(RepairStatusEnum.HACIENDO, 0L)
                        + statusCounts.getOrDefault(RepairStatusEnum.RECIBIDA, 0L)
        );
        dto.setQuotedPendingCount(statusCounts.getOrDefault(RepairStatusEnum.PRESUPUESTADA_ESPERANDO_RESPUESTA, 0L));
        LocalDate today = LocalDate.now(applicationClock);
        YearMonth currentMonth = YearMonth.from(today);
        YearMonth firstMonth = currentMonth.minusMonths(11);
        LocalDateTime from = firstMonth.atDay(1).atStartOfDay();
        LocalDateTime to = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
        Map<YearMonth, Long> monthlyCounts = new HashMap<>();
        for (RepairMonthlyCountView item : repairRepository.countReceivedByMonth(from, to)) {
            if (item.getMonth() != null) {
                monthlyCounts.put(YearMonth.from(item.getMonth()), item.getTotal() != null ? item.getTotal() : 0L);
            }
        }
        dto.setMonthlyReceivedRepairs(IntStream.range(0, 12)
                .mapToObj(firstMonth::plusMonths)
                .map(month -> new DashboardSeriesItemDTO(monthLabel(month), monthlyCounts.getOrDefault(month, 0L)))
                .toList());
        dto.setAverageTurnaroundDays(roundToOneDecimal(repairRepository.averageCompletedTurnaroundDays()));
        dto.setOverdueOpenRepairs(repairRepository.countOverdueOpenRepairs(today.minusDays(7).atStartOfDay()));

        List<DeviceLastRepairView> inactiveViews = repairRepository.findOldestLastRepairByDevice(PageRequest.of(0, 5));
        dto.setInactiveDevices(inactiveViews.stream().map(view -> {
            DashboardInactiveDeviceDTO item = new DashboardInactiveDeviceDTO();
            String deviceLabel = joinLabel(view.getDeviceTypeName(), view.getDeviceBrand(), view.getDeviceModel(), view.getDeviceId());
            String ownerLabel = joinLabel(view.getClientName(), view.getClientLastName(), "Cliente sin datos");
            item.setName(deviceLabel + " · " + ownerLabel);
            item.setLastRepair(view.getLastRepairDate() != null ? view.getLastRepairDate().toLocalDate().toString() : null);
            return item;
        }).toList());

        return dto;
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    private String joinLabel(String first, String second, String fallback) {
        String label = (nullSafe(first) + " " + nullSafe(second)).replaceAll("\\s+", " ").trim();
        return label.isBlank() ? fallback : label;
    }

    private String joinLabel(String first, String second, String third, String fallback) {
        String label = (nullSafe(first) + " " + nullSafe(second) + " " + nullSafe(third))
                .replaceAll("\\s+", " ").trim();
        return label.isBlank() ? fallback : label;
    }

    private String monthLabel(YearMonth month) {
        String name = month.getMonth().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es-AR"));
        return name.substring(0, 1).toUpperCase(Locale.forLanguageTag("es-AR")) + name.substring(1) + " " + month.getYear();
    }

    private double roundToOneDecimal(Double value) {
        if (value == null || !Double.isFinite(value)) {
            return 0;
        }
        return Math.round(value * 10.0) / 10.0;
    }
}
