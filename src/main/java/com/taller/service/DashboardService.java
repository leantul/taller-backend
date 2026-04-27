package com.taller.service;

import com.taller.model.RepairPart;
import com.taller.model.repository.RepairPartRepository;
import com.taller.resource.dto.DashboardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RepairService repairService;
    private final RepairPartRepository repairPartRepository;

    public DashboardDTO monthSummary(int year, int month) {
        LocalDateTime from = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime to = from.plusMonths(1).minusSeconds(1);

        BigDecimal income = repairService.totalIncome(from, to);
        BigDecimal costs = repairPartRepository.findAll().stream()
                .map(RepairPart::getCost)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardDTO.builder()
                .totalRecaudacion(income)
                .totalCostos(costs)
                .totalGanancia(income.subtract(costs))
                .build();
    }
}
