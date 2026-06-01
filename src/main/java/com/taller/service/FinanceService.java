package com.taller.service;

import com.taller.model.RepairPart;
import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.repository.RepairPartRepository;
import com.taller.model.repository.RepairRepository;
import com.taller.model.repository.projection.RepairListView;
import com.taller.resource.dto.DashboardSeriesItemDTO;
import com.taller.resource.dto.FinanceRowDTO;
import com.taller.resource.dto.FinanceSummaryDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FinanceService {

    private final RepairRepository repairRepository;
    private final RepairPartRepository repairPartRepository;

    public FinanceSummaryDTO getSummary(LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = to != null ? to.plusDays(1).atStartOfDay().minusNanos(1) : null;
        List<RepairListView> filteredRepairs = findFinanceRows(fromDateTime, toDateTime);
        List<RepairListView> deliveredRepairs = filteredRepairs.stream()
                .filter(repair -> repair.getStatus() == RepairStatusEnum.RETIRADA)
                .toList();
        Map<String, List<RepairPart>> partsByRepairId = deliveredRepairs.isEmpty()
                ? Map.of()
                : repairPartRepository.findByRepairIdIn(deliveredRepairs.stream().map(RepairListView::getId).toList()).stream()
                .collect(Collectors.groupingBy(RepairPart::getRepairId));

        List<FinanceRowDTO> rows = deliveredRepairs.stream()
                .map(repair -> toRowDto(repair, partsByRepairId.getOrDefault(repair.getId(), List.of())))
                .sorted(Comparator.comparing(FinanceRowDTO::getDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(FinanceRowDTO::getOrderNumber, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        BigDecimal totalIncome = rows.stream()
                .map(FinanceRowDTO::getIncome)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPartsCost = rows.stream()
                .map(FinanceRowDTO::getPartsCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalLabor = deliveredRepairs.stream()
                .map(repair -> safeMoney(repair.getLaborAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalQuoted = deliveredRepairs.stream()
                .map(repair -> safeMoney(repair.getQuotedAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netIncome = rows.stream()
                .map(FinanceRowDTO::getNet)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        FinanceSummaryDTO summary = new FinanceSummaryDTO();
        summary.setFrom(from);
        summary.setTo(to);
        summary.setRepairCount(rows.size());
        summary.setTotalIncome(totalIncome);
        summary.setTotalPartsCost(totalPartsCost);
        summary.setTotalLabor(totalLabor);
        summary.setTotalQuoted(totalQuoted);
        summary.setNetIncome(netIncome);
        BigDecimal currentAverageNet = rows.stream()
                .map(FinanceRowDTO::getNet)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setAverageNet(rows.isEmpty() ? BigDecimal.ZERO : currentAverageNet.divide(BigDecimal.valueOf(rows.size()), 2, java.math.RoundingMode.HALF_UP));
        summary.setDeliveredCount(rows.size());
        summary.setMonthlyNet(buildMonthlyNetSeries());
        summary.setRows(rows);
        return summary;
    }

    private List<DashboardSeriesItemDTO> buildMonthlyNetSeries() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(11);
        LocalDateTime from = firstMonth.atDay(1).atStartOfDay();

        List<RepairListView> repairs = findFinanceRows(from, null).stream()
                .filter(repair -> repair.getStatus() == RepairStatusEnum.RETIRADA)
                .toList();
        Map<String, List<RepairPart>> partsByRepairId = repairs.isEmpty()
                ? Map.of()
                : repairPartRepository.findByRepairIdIn(repairs.stream().map(RepairListView::getId).toList()).stream()
                .collect(Collectors.groupingBy(RepairPart::getRepairId));

        Map<YearMonth, BigDecimal> monthlyNet = new LinkedHashMap<>();
        YearMonth cursor = firstMonth;
        while (!cursor.isAfter(currentMonth)) {
            monthlyNet.put(cursor, BigDecimal.ZERO);
            cursor = cursor.plusMonths(1);
        }

        for (RepairListView repair : repairs) {
            LocalDateTime date = resolveFinanceDate(repair);
            if (date == null) {
                continue;
            }

            YearMonth month = YearMonth.from(date);
            if (!monthlyNet.containsKey(month)) {
                continue;
            }

            BigDecimal income = safeMoney(repair.getPrice());
            BigDecimal partsCost = sumPartsCost(partsByRepairId.getOrDefault(repair.getId(), List.of()));
            monthlyNet.put(month, monthlyNet.get(month).add(income.subtract(partsCost)));
        }

        return monthlyNet.entrySet().stream()
                .map(entry -> new DashboardSeriesItemDTO(formatMonth(entry.getKey()), entry.getValue()))
                .toList();
    }

    private FinanceRowDTO toRowDto(RepairListView repair, List<RepairPart> parts) {
        BigDecimal income = safeMoney(repair.getPrice());
        BigDecimal partsCost = sumPartsCost(parts);

        FinanceRowDTO row = new FinanceRowDTO();
        row.setRepairId(repair.getId());
        row.setOrderNumber(repair.getOrderNumber());
        row.setDate(resolveFinanceDate(repair));
        row.setStatus(repair.getStatus());
        row.setIncome(income);
        row.setPartsCost(partsCost);
        row.setNet(income.subtract(partsCost));
        return row;
    }

    private LocalDateTime resolveFinanceDate(RepairListView repair) {
        return repair.getReturnDateTime() != null ? repair.getReturnDateTime() : repair.getReceiveDateTime();
    }

    private BigDecimal sumPartsCost(List<RepairPart> parts) {
        return parts.stream()
                .map(part -> safeMoney(part.getCost()).multiply(BigDecimal.valueOf(part.getQuantity() != null ? part.getQuantity() : 1)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private List<RepairListView> findFinanceRows(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            return repairRepository.findFinanceRowsBetween(from, to);
        }

        if (from != null) {
            return repairRepository.findFinanceRowsFrom(from);
        }

        if (to != null) {
            return repairRepository.findFinanceRowsTo(to);
        }

        return repairRepository.findFinanceRowsAll();
    }

    private String formatMonth(YearMonth month) {
        String name = month.getMonth().name().substring(0, 1) + month.getMonth().name().substring(1).toLowerCase();
        return name + " " + month.getYear();
    }

    public FinanceService(RepairRepository repairRepository, RepairPartRepository repairPartRepository) {
        this.repairRepository = repairRepository;
        this.repairPartRepository = repairPartRepository;
    }
}
