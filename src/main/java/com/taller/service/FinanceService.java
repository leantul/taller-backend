package com.taller.service;

import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.repository.RepairRepository;
import com.taller.model.repository.projection.FinanceRepairView;
import com.taller.resource.dto.DashboardSeriesItemDTO;
import com.taller.resource.dto.FinanceRowDTO;
import com.taller.resource.dto.FinanceSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final RepairRepository repairRepository;

    @Transactional(readOnly = true)
    public FinanceSummaryDTO getSummary(LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = to != null ? to.plusDays(1).atStartOfDay().minusNanos(1) : null;
        List<FinanceRepairView> filteredRepairs = findFinanceRows(fromDateTime, toDateTime);
        List<FinanceRepairView> deliveredRepairs = filteredRepairs.stream()
                .filter(repair -> repair.getStatus() == RepairStatusEnum.RETIRADA)
                .toList();

        List<FinanceRowDTO> rows = deliveredRepairs.stream()
                .map(this::toRowDto)
                .sorted(Comparator.comparing(FinanceRowDTO::getDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(FinanceRowDTO::getClientName, Comparator.nullsLast(String::compareToIgnoreCase)))
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
        summary.setAverageNet(rows.isEmpty() ? BigDecimal.ZERO : netIncome.divide(BigDecimal.valueOf(rows.size()), 2, java.math.RoundingMode.HALF_UP));
        summary.setDeliveredCount(rows.size());
        summary.setMonthlyNet(buildMonthlyNetSeries());
        summary.setRows(rows);
        return summary;
    }

    private List<DashboardSeriesItemDTO> buildMonthlyNetSeries() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(11);
        LocalDateTime from = firstMonth.atDay(1).atStartOfDay();

        List<FinanceRepairView> repairs = findFinanceRows(from, null).stream()
                .filter(repair -> repair.getStatus() == RepairStatusEnum.RETIRADA)
                .toList();

        Map<YearMonth, BigDecimal> monthlyNet = new LinkedHashMap<>();
        YearMonth cursor = firstMonth;
        while (!cursor.isAfter(currentMonth)) {
            monthlyNet.put(cursor, BigDecimal.ZERO);
            cursor = cursor.plusMonths(1);
        }

        for (FinanceRepairView repair : repairs) {
            LocalDateTime date = resolveFinanceDate(repair);
            if (date == null) {
                continue;
            }

            YearMonth month = YearMonth.from(date);
            if (!monthlyNet.containsKey(month)) {
                continue;
            }

            BigDecimal income = safeMoney(repair.getPrice());
            BigDecimal partsCost = safeMoney(repair.getPartsCost());
            monthlyNet.put(month, monthlyNet.get(month).add(income.subtract(partsCost)));
        }

        return monthlyNet.entrySet().stream()
                .map(entry -> new DashboardSeriesItemDTO(formatMonth(entry.getKey()), entry.getValue()))
                .toList();
    }

    private FinanceRowDTO toRowDto(FinanceRepairView repair) {
        BigDecimal income = safeMoney(repair.getPrice());
        BigDecimal partsCost = safeMoney(repair.getPartsCost());

        FinanceRowDTO row = new FinanceRowDTO();
        row.setRepairId(repair.getId());
        row.setClientName(formatClientName(repair));
        row.setDate(resolveFinanceDate(repair));
        row.setIncome(income);
        row.setPartsCost(partsCost);
        row.setNet(income.subtract(partsCost));
        return row;
    }

    private LocalDateTime resolveFinanceDate(FinanceRepairView repair) {
        return repair.getReturnDateTime() != null ? repair.getReturnDateTime() : repair.getReceiveDateTime();
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private List<FinanceRepairView> findFinanceRows(LocalDateTime from, LocalDateTime to) {
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

    private String formatClientName(FinanceRepairView repair) {
        String fullName = Stream.of(repair.getClientName(), repair.getClientLastName())
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" "));
        return fullName.isBlank() ? "-" : fullName;
    }

    private String formatMonth(YearMonth month) {
        String name = month.getMonth().name().substring(0, 1) + month.getMonth().name().substring(1).toLowerCase();
        return name + " " + month.getYear();
    }
}
