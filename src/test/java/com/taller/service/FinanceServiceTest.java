package com.taller.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taller.model.repository.RepairRepository;
import com.taller.model.repository.projection.FinancePartsSummaryView;
import com.taller.model.repository.projection.FinancePaymentSummaryView;
import com.taller.model.repository.projection.FinanceRepairSummaryView;
import com.taller.model.repository.projection.FinanceRowView;
import com.taller.resource.dto.FinanceSummaryDTO;
import com.taller.resource.dto.PageDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {

    @Mock
    private RepairRepository repairRepository;

    @Test
    void getSummary_usesDatabaseAggregatesAndTreatsNullPartsAsZero() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);
        FinanceRepairSummaryView repairSummary = mock(FinanceRepairSummaryView.class);
        FinancePartsSummaryView partsSummary = mock(FinancePartsSummaryView.class);
        FinancePaymentSummaryView paymentSummary = mock(FinancePaymentSummaryView.class);
        when(paymentSummary.getRepairCount()).thenReturn(3L);
        when(paymentSummary.getTotalIncome()).thenReturn(BigDecimal.valueOf(2400));
        when(repairSummary.getTotalLabor()).thenReturn(BigDecimal.valueOf(900));
        when(repairSummary.getTotalQuoted()).thenReturn(BigDecimal.valueOf(2700));
        when(repairSummary.getZeroFinalAmountCount()).thenReturn(1L);
        when(repairSummary.getPositiveFinalAmountCount()).thenReturn(2L);
        when(partsSummary.getTotalPartsCost()).thenReturn(BigDecimal.valueOf(600));
        when(partsSummary.getTotalPartsProfit()).thenReturn(null);
        when(repairRepository.summarizePaymentFinanceRepairs(
                LocalDateTime.of(2026, 6, 1, 0, 0),
                LocalDateTime.of(2026, 6, 30, 23, 59, 59, 999999999)))
                .thenReturn(repairSummary);
        when(repairRepository.summarizePaymentFinanceParts(
                LocalDateTime.of(2026, 6, 1, 0, 0),
                LocalDateTime.of(2026, 6, 30, 23, 59, 59, 999999999)))
                .thenReturn(partsSummary);
        when(repairRepository.summarizePayments(
                LocalDateTime.of(2026, 6, 1, 0, 0),
                LocalDateTime.of(2026, 6, 30, 23, 59, 59, 999999999)))
                .thenReturn(paymentSummary);
        when(repairRepository.sumPaymentIncomeBetween(any(), any())).thenReturn(BigDecimal.ZERO);
        when(repairRepository.sumFirstPaymentPartsCostBetween(any(), any())).thenReturn(BigDecimal.ZERO);

        FinanceSummaryDTO summary = new FinanceService(repairRepository).getSummary(from, to);

        assertEquals(3, summary.getRepairCount());
        assertEquals(1L, summary.getZeroFinalAmountCount());
        assertEquals(2L, summary.getPositiveFinalAmountCount());
        assertEquals(0, BigDecimal.valueOf(1800).compareTo(summary.getNetIncome()));
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.getTotalPartsProfit()));
        assertEquals(0, BigDecimal.valueOf(600).compareTo(summary.getAverageNet()));
        assertEquals(12, summary.getMonthlyNet().size());
        assertEquals(true, summary.getMonthlyNet().stream()
                .allMatch(item -> BigDecimal.ZERO.compareTo((BigDecimal) item.getValue()) == 0));
    }

    @Test
    void getDetails_clampsPaginationAndUsesWhitelistedBackendSort() {
        FinanceRowView row = mock(FinanceRowView.class);
        when(row.getRepairId()).thenReturn("repair-1");
        when(row.getClientName()).thenReturn("Ada Lovelace");
        when(row.getDate()).thenReturn(LocalDateTime.of(2026, 6, 10, 10, 0));
        when(row.getIncome()).thenReturn(BigDecimal.valueOf(1500));
        when(row.getPartsCost()).thenReturn(BigDecimal.valueOf(400));
        when(row.getNet()).thenReturn(BigDecimal.valueOf(1100));
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(repairRepository.findPaymentFinancePage(eq(null), eq(null), pageableCaptor.capture()))
                .thenReturn(new PageImpl<>(List.of(row), PageRequest.of(0, 100), 101));

        PageDTO<?> result = new FinanceService(repairRepository)
                .getDetails(null, null, -4, 1000, "unsupported", "sideways");

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(100, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("date").getDirection());
        assertEquals(101, result.totalElements());
        assertEquals(1, result.content().size());
    }

    @Test
    void getDetails_forwardsEveryAllowedSortToTheRepository() {
        List<String> sortFields = List.of("clientName", "date", "income", "partsCost", "net");
        when(repairRepository.findPaymentFinancePage(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        FinanceService service = new FinanceService(repairRepository);
        for (String sortField : sortFields) {
            service.getDetails(
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 6, 30),
                    0,
                    10,
                    sortField,
                    "asc");
        }

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repairRepository, times(sortFields.size())).findPaymentFinancePage(
                eq(LocalDateTime.of(2026, 6, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 6, 30, 23, 59, 59, 999999999)),
                pageableCaptor.capture());
        for (int index = 0; index < sortFields.size(); index++) {
            Sort.Order order = pageableCaptor.getAllValues().get(index).getSort().getOrderFor(sortFields.get(index));
            assertEquals(Sort.Direction.ASC, order.getDirection());
        }
    }
}
