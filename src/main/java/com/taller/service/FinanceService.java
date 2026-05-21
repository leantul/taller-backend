package com.taller.service;

import com.taller.model.RepairPart;
import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.repository.RepairPartRepository;
import com.taller.model.repository.RepairRepository;
import com.taller.model.repository.projection.RepairListView;
import com.taller.resource.dto.FinanceRowDTO;
import com.taller.resource.dto.FinanceSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final RepairRepository repairRepository;
    private final RepairPartRepository repairPartRepository;

    public FinanceSummaryDTO getSummary(LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = to != null ? to.plusDays(1).atStartOfDay().minusNanos(1) : null;
        List<RepairListView> filteredRepairs = repairRepository.findFinanceRows(fromDateTime, toDateTime);
        Map<String, List<RepairPart>> partsByRepairId = filteredRepairs.isEmpty()
                ? Map.of()
                : repairPartRepository.findByRepairIdIn(filteredRepairs.stream().map(RepairListView::getId).toList()).stream()
                .collect(Collectors.groupingBy(RepairPart::getRepairId));

        List<FinanceRowDTO> rows = filteredRepairs.stream()
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
        BigDecimal totalLabor = filteredRepairs.stream()
                .map(repair -> safeMoney(repair.getLaborAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalQuoted = filteredRepairs.stream()
                .map(repair -> safeMoney(repair.getQuotedAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netIncome = totalIncome.subtract(totalPartsCost);

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
        summary.setDeliveredCount(rows.stream().filter(row -> row.getStatus() == RepairStatusEnum.RETIRADA).count());
        summary.setRows(rows);
        return summary;
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
}
