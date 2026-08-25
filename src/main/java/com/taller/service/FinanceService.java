package com.taller.service;

import com.taller.model.repository.RepairRepository;
import com.taller.model.repository.projection.FinancePaymentSummaryView;
import com.taller.model.repository.projection.FinancePartsSummaryView;
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
    private static final Set<String> DETAIL_SORT_FIELDS = Set.of("clientName", "date", "income", "partsCost", "net");

    private final RepairRepository repairRepository;

    @Transactional(readOnly = true)
    public FinanceSummaryDTO getSummary(LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = startOfDay(from);
        LocalDateTime toDateTime = endOfDay(to);
        FinanceRepairSummaryView repairSummary = repairRepository.summarizePaymentFinanceRepairs(fromDateTime, toDateTime);
        FinancePaymentSummaryView paymentSummary = repairRepository.summarizePayments(fromDateTime, toDateTime);
        FinancePartsSummaryView partsSummary = repairRepository.summarizePaymentFinanceParts(fromDateTime, toDateTime);

        long repairCount = paymentSummary != null && paymentSummary.getRepairCount() != null
                ? paymentSummary.getRepairCount()
                : 0L;
        BigDecimal totalIncome = paymentSummary != null ? safeMoney(paymentSummary.getTotalIncome()) : BigDecimal.ZERO;
        BigDecimal totalLabor = repairSummary != null ? safeMoney(repairSummary.getTotalLabor()) : BigDecimal.ZERO;
        BigDecimal totalQuoted = repairSummary != null ? safeMoney(repairSummary.getTotalQuoted()) : BigDecimal.ZERO;
        BigDecimal totalPartsCost = partsSummary != null ? safeMoney(partsSummary.getTotalPartsCost()) : BigDecimal.ZERO;
        BigDecimal totalPartsProfit = partsSummary != null ? safeMoney(partsSummary.getTotalPartsProfit()) : BigDecimal.ZERO;
        BigDecimal netIncome = totalIncome.subtract(totalPartsCost);

        FinanceSummaryDTO summary = new FinanceSummaryDTO();
        summary.setFrom(from);
        summary.setTo(to);
        summary.setRepairCount(Math.toIntExact(repairCount));
        summary.setTotalIncome(totalIncome);
        summary.setTotalPartsCost(totalPartsCost);
        summary.setTotalLabor(totalLabor);
        summary.setTotalPartsProfit(totalPartsProfit);
        summary.setTotalQuoted(totalQuoted);
        summary.setZeroFinalAmountCount(repairSummary != null ? safeLong(repairSummary.getZeroFinalAmountCount()) : 0L);
        summary.setPositiveFinalAmountCount(repairSummary != null ? safeLong(repairSummary.getPositiveFinalAmountCount()) : 0L);
        summary.setNetIncome(netIncome);
        summary.setAverageNet(repairCount == 0
                ? BigDecimal.ZERO
                : netIncome.divide(BigDecimal.valueOf(repairCount), 2, RoundingMode.HALF_UP));
        summary.setDeliveredCount(repairCount);
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
        Page<FinanceRowView> result = repairRepository.findPaymentFinancePage(startOfDay(from), endOfDay(to), pageRequest);
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
            BigDecimal income = safeMoney(repairRepository.sumPaymentIncomeBetween(monthStart, nextMonth));
            BigDecimal partsCost = safeMoney(repairRepository.sumFirstPaymentPartsCostBetween(monthStart, nextMonth));
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
        dto.setIncome(safeMoney(row.getIncome()));
        dto.setPartsCost(safeMoney(row.getPartsCost()));
        dto.setNet(safeMoney(row.getNet()));
        return dto;
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
