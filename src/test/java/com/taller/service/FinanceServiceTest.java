package com.taller.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.repository.RepairRepository;
import com.taller.model.repository.projection.FinanceRepairView;
import com.taller.resource.dto.FinanceSummaryDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {

    @Mock
    private RepairRepository repairRepository;

    @Test
    void getSummary_countsDeliveredRepairsByFinalAmount() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);
        List<FinanceRepairView> repairs = List.of(
                financeRepair(RepairStatusEnum.RETIRADA, null),
                financeRepair(RepairStatusEnum.RETIRADA, BigDecimal.ZERO),
                financeRepair(RepairStatusEnum.RETIRADA, BigDecimal.valueOf(1500)),
                financeRepair(RepairStatusEnum.HACIENDO, BigDecimal.valueOf(2500))
        );
        when(repairRepository.findFinanceRowsBetween(
                LocalDateTime.of(2026, 6, 1, 0, 0),
                LocalDateTime.of(2026, 6, 30, 23, 59, 59, 999999999)))
                .thenReturn(repairs);
        when(repairRepository.findFinanceRowsFrom(any())).thenReturn(List.of());

        FinanceSummaryDTO summary = new FinanceService(repairRepository).getSummary(from, to);

        assertEquals(2L, summary.getZeroFinalAmountCount());
        assertEquals(1L, summary.getPositiveFinalAmountCount());
    }

    private FinanceRepairView financeRepair(RepairStatusEnum status, BigDecimal price) {
        FinanceRepairView repair = mock(FinanceRepairView.class);
        when(repair.getStatus()).thenReturn(status);
        if (status == RepairStatusEnum.RETIRADA) {
            when(repair.getPrice()).thenReturn(price);
            when(repair.getPartsCost()).thenReturn(BigDecimal.ZERO);
            when(repair.getLaborAmount()).thenReturn(BigDecimal.ZERO);
            when(repair.getQuotedAmount()).thenReturn(BigDecimal.ZERO);
            when(repair.getClientName()).thenReturn("Ada");
            when(repair.getClientLastName()).thenReturn("Lovelace");
            when(repair.getReturnDateTime()).thenReturn(LocalDateTime.of(2026, 6, 10, 10, 0));
        }
        return repair;
    }
}
