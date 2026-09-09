package com.taller.service;

import com.taller.model.repository.RepairRepository;
import com.taller.model.repository.projection.FinanceActivitySummaryView;
import com.taller.model.repository.projection.FinancePaymentSummaryView;
import com.taller.model.repository.projection.FinanceRepairSummaryView;
import com.taller.model.repository.projection.FinanceRowView;
import com.taller.resource.dto.DashboardSeriesItemDTO;
import com.taller.resource.dto.FinanceRowDTO;
import com.taller.resource.dto.FinanceSummaryDTO;
import com.taller.resource.dto.PageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private static final int MAXIMUM_PAGE_SIZE = 100;
    private static final Set<String> DETAIL_SORT_FIELDS = Set.of("clientName", "date", "income", "partsAmount", "net");

    private final RepairRepository repairRepository;

    @Transactional(readOnly = true)
    public FinanceSummaryDTO getSummary(LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = startOfDay(from);
        LocalDateTime toDateTime = endOfDay(to);
        FinanceRepairSummaryView repairSummary = repairRepository.summarizeRetiredFinanceRepairs(fromDateTime, toDateTime);
        FinanceActivitySummaryView activitySummary = repairRepository.summarizeFinanceActivity(fromDateTime, toDateTime);
        FinancePaymentSummaryView paymentSummary = repairRepository.summarizeFinancePayments(fromDateTime, toDateTime);

        long repairCount = safeLong(repairRepository.countFinanceActivityRepairs(fromDateTime, toDateTime));
        long paidRepairCount = paymentSummary != null ? safeLong(paymentSummary.getRepairCount()) : 0L;
        BigDecimal totalIncome = activitySummary != null ? safeMoney(activitySummary.getTotalIncome()) : BigDecimal.ZERO;
        BigDecimal totalQuoted = repairSummary != null ? safeMoney(repairSummary.getTotalQuoted()) : BigDecimal.ZERO;
        BigDecimal totalPartsCost = activitySummary != null ? safeMoney(activitySummary.getTotalPartsCost()) : BigDecimal.ZERO;
        BigDecimal totalLabor = activitySummary != null ? safeMoney(activitySummary.getTotalLabor()) : BigDecimal.ZERO;
        BigDecimal totalPartsProfit = activitySummary != null ? safeMoney(activitySummary.getTotalPartsProfit()) : BigDecimal.ZERO;
        BigDecimal netIncome = totalIncome.subtract(totalPartsCost);
        BigDecimal totalAdjustment = netIncome.subtract(totalLabor).subtract(totalPartsProfit);

        FinanceSummaryDTO summary = new FinanceSummaryDTO();
        summary.setFrom(from);
        summary.setTo(to);
        summary.setRepairCount(Math.toIntExact(repairCount));
        summary.setTotalIncome(totalIncome);
        summary.setTotalPartsCost(totalPartsCost);
        summary.setTotalLabor(totalLabor);
        summary.setTotalPartsProfit(totalPartsProfit);
        summary.setTotalAdjustment(totalAdjustment);
        summary.setTotalQuoted(totalQuoted);
        summary.setZeroFinalAmountCount(repairSummary != null ? safeLong(repairSummary.getZeroFinalAmountCount()) : 0L);
        summary.setPositiveFinalAmountCount(paidRepairCount);
        summary.setNetIncome(netIncome);
        summary.setAverageNet(repairCount == 0
                ? BigDecimal.ZERO
                : netIncome.divide(BigDecimal.valueOf(repairCount), 2, RoundingMode.HALF_UP));
        summary.setDeliveredCount(repairSummary != null ? safeLong(repairSummary.getRepairCount()) : 0L);
        summary.setMonthlyNet(buildMonthlyNetSeries());
        return summary;
    }

    @Transactional(readOnly = true)
    public PageDTO<FinanceRowDTO> getDetails(
            LocalDate from,
            LocalDate to,
            int page,
            int size,
            String sortBy,
            String sortDir) {
        String safeSortBy = DETAIL_SORT_FIELDS.contains(sortBy) ? sortBy : "date";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), MAXIMUM_PAGE_SIZE),
                Sort.by(new Sort.Order(direction, safeSortBy), new Sort.Order(Sort.Direction.ASC, "repairId")));
        Page<FinanceRowView> result = repairRepository.findFinanceActivityPage(startOfDay(from), endOfDay(to), pageRequest);
        List<FinanceRowDTO> content = result.getContent().stream().map(this::toRowDto).toList();
        return new PageDTO<>(content, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    private List<DashboardSeriesItemDTO> buildMonthlyNetSeries() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(11);
        LocalDateTime from = firstMonth.atDay(1).atStartOfDay();
        Map<YearMonth, BigDecimal> monthlyNet = new LinkedHashMap<>();

        YearMonth cursor = firstMonth;
        while (!cursor.isAfter(currentMonth)) {
            monthlyNet.put(cursor, BigDecimal.ZERO);
            cursor = cursor.plusMonths(1);
        }

        for (YearMonth month : monthlyNet.keySet()) {
            LocalDateTime monthStart = month.atDay(1).atStartOfDay();
            LocalDateTime nextMonth = month.plusMonths(1).atDay(1).atStartOfDay();
            FinanceActivitySummaryView activity = repairRepository.summarizeFinanceActivity(
                    monthStart,
                    nextMonth.minusNanos(1));
            BigDecimal income = activity != null ? safeMoney(activity.getTotalIncome()) : BigDecimal.ZERO;
            BigDecimal partsCost = activity != null ? safeMoney(activity.getTotalPartsCost()) : BigDecimal.ZERO;
            monthlyNet.put(month, income.subtract(partsCost));
        }

        return monthlyNet.entrySet().stream()
                .map(entry -> new DashboardSeriesItemDTO(formatMonth(entry.getKey()), entry.getValue()))
                .toList();
    }

    private FinanceRowDTO toRowDto(FinanceRowView row) {
        FinanceRowDTO dto = new FinanceRowDTO();
        dto.setRepairId(row.getRepairId());
        dto.setClientName(row.getClientName());
        dto.setDate(row.getDate());
        BigDecimal income = safeMoney(row.getIncome());
        BigDecimal partsAmount = recognizedPartsAmount(income, row.getPartsCost(), row.getPartsSale());
        dto.setIncome(income);
        dto.setPartsAmount(partsAmount);
        dto.setNet(currentNet(income, row.getPartsCost()));
        return dto;
    }

    BigDecimal recognizedPartsAmount(BigDecimal income, BigDecimal partsCost, BigDecimal partsSale) {
        return safeMoney(partsCost);
    }

    BigDecimal currentNet(BigDecimal income, BigDecimal partsCost) {
        return safeMoney(income).subtract(safeMoney(partsCost));
    }

    private LocalDateTime startOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    private LocalDateTime endOfDay(LocalDate date) {
        return date != null ? date.plusDays(1).atStartOfDay().minusNanos(1) : null;
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private long safeLong(Long value) {
        return value != null ? value : 0L;
    }

    private String formatMonth(YearMonth month) {
        String name = month.getMonth().name().substring(0, 1) + month.getMonth().name().substring(1).toLowerCase();
        return name + " " + month.getYear();
    }
}
