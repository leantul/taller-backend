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
import com.taller.resource.dto.FinanceRowDTO;
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
        FinancePartsSummaryView beforePartsSummary = mock(FinancePartsSummaryView.class);
        FinancePaymentSummaryView paymentSummary = mock(FinancePaymentSummaryView.class);
        when(repairSummary.getRepairCount()).thenReturn(4L);
        when(paymentSummary.getRepairCount()).thenReturn(2L);
        when(paymentSummary.getTotalIncome()).thenReturn(BigDecimal.valueOf(2400));
        when(repairSummary.getTotalQuoted()).thenReturn(BigDecimal.valueOf(2700));
        when(repairSummary.getZeroFinalAmountCount()).thenReturn(1L);
        when(partsSummary.getTotalPartsCost()).thenReturn(BigDecimal.valueOf(600));
        when(partsSummary.getTotalPartsProfit()).thenReturn(BigDecimal.valueOf(500));
        when(repairRepository.summarizeRetiredFinanceRepairs(
                LocalDateTime.of(2026, 6, 1, 0, 0),
                LocalDateTime.of(2026, 6, 30, 23, 59, 59, 999999999)))
                .thenReturn(repairSummary);
        when(repairRepository.summarizeRecognizedFinanceParts(
                LocalDateTime.of(2026, 6, 1, 0, 0),
                LocalDateTime.of(2026, 6, 30, 23, 59, 59, 999999999)))
                .thenReturn(partsSummary);
        when(repairRepository.summarizeFinancePayments(
                LocalDateTime.of(2026, 6, 1, 0, 0),
                LocalDateTime.of(2026, 6, 30, 23, 59, 59, 999999999)))
                .thenReturn(paymentSummary);
        when(repairRepository.countFinanceActivityRepairs(
                LocalDateTime.of(2026, 6, 1, 0, 0),
                LocalDateTime.of(2026, 6, 30, 23, 59, 59, 999999999)))
                .thenReturn(4L);
        when(repairRepository.sumPaymentIncomeBefore(LocalDateTime.of(2026, 6, 1, 0, 0))).thenReturn(BigDecimal.ZERO);
        when(repairRepository.sumPaymentIncomeBefore(LocalDateTime.of(2026, 7, 1, 0, 0))).thenReturn(BigDecimal.valueOf(2400));
        when(repairRepository.summarizeRecognizedFinancePartsBefore(LocalDateTime.of(2026, 6, 1, 0, 0))).thenReturn(beforePartsSummary);
        when(repairRepository.summarizeRecognizedFinancePartsBefore(LocalDateTime.of(2026, 7, 1, 0, 0))).thenReturn(partsSummary);
        when(repairRepository.sumFinancePaymentIncomeBetween(any(), any())).thenReturn(BigDecimal.ZERO);
        when(repairRepository.sumRecognizedPartsCostBetween(any(), any())).thenReturn(BigDecimal.ZERO);

        FinanceSummaryDTO summary = new FinanceService(repairRepository).getSummary(from, to);

        assertEquals(4, summary.getRepairCount());
        assertEquals(1L, summary.getZeroFinalAmountCount());
        assertEquals(2L, summary.getPositiveFinalAmountCount());
        assertEquals(0, BigDecimal.valueOf(1800).compareTo(summary.getNetIncome()));
        assertEquals(0, BigDecimal.valueOf(500).compareTo(summary.getTotalPartsProfit()));
        assertEquals(0, BigDecimal.valueOf(1300).compareTo(summary.getTotalLabor()));
        assertEquals(0, BigDecimal.valueOf(450).compareTo(summary.getAverageNet()));
        assertEquals(12, summary.getMonthlyNet().size());
        assertEquals(true, summary.getMonthlyNet().stream()
                .allMatch(item -> BigDecimal.ZERO.compareTo((BigDecimal) item.getValue()) == 0));
    }

    @Test
    void getSummary_keepsPartialPaymentNegativeUntilPartsCostIsRecovered() {
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = LocalDate.of(2026, 8, 31);
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay().minusNanos(1);
        LocalDateTime nextMonth = to.plusDays(1).atStartOfDay();
        FinanceRepairSummaryView repairs = mock(FinanceRepairSummaryView.class);
        FinancePaymentSummaryView payments = mock(FinancePaymentSummaryView.class);
        FinancePartsSummaryView periodParts = mock(FinancePartsSummaryView.class);
        FinancePartsSummaryView beforeParts = mock(FinancePartsSummaryView.class);

        when(repairs.getRepairCount()).thenReturn(0L);
        when(payments.getRepairCount()).thenReturn(1L);
        when(payments.getTotalIncome()).thenReturn(BigDecimal.valueOf(40100));
        when(periodParts.getTotalPartsCost()).thenReturn(BigDecimal.valueOf(104101));
        when(periodParts.getTotalPartsProfit()).thenReturn(BigDecimal.valueOf(91799));
        when(repairRepository.summarizeRetiredFinanceRepairs(fromDateTime, toDateTime)).thenReturn(repairs);
        when(repairRepository.summarizeFinancePayments(fromDateTime, toDateTime)).thenReturn(payments);
        when(repairRepository.summarizeRecognizedFinanceParts(fromDateTime, toDateTime)).thenReturn(periodParts);
        when(repairRepository.countFinanceActivityRepairs(fromDateTime, toDateTime)).thenReturn(1L);
        when(repairRepository.sumPaymentIncomeBefore(fromDateTime)).thenReturn(BigDecimal.ZERO);
        when(repairRepository.sumPaymentIncomeBefore(nextMonth)).thenReturn(BigDecimal.valueOf(40100));
        when(repairRepository.summarizeRecognizedFinancePartsBefore(fromDateTime)).thenReturn(beforeParts);
        when(repairRepository.summarizeRecognizedFinancePartsBefore(nextMonth)).thenReturn(periodParts);
        when(repairRepository.sumFinancePaymentIncomeBetween(any(), any())).thenReturn(BigDecimal.ZERO);
        when(repairRepository.sumRecognizedPartsCostBetween(any(), any())).thenReturn(BigDecimal.ZERO);

        FinanceSummaryDTO summary = new FinanceService(repairRepository).getSummary(from, to);

        assertEquals(0, BigDecimal.valueOf(-64001).compareTo(summary.getNetIncome()));
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.getTotalPartsProfit()));
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.getTotalLabor()));
        assertEquals(1L, summary.getPositiveFinalAmountCount());
    }

    @Test
    void getDetails_clampsPaginationAndUsesWhitelistedBackendSort() {
        FinanceRowView row = mock(FinanceRowView.class);
        when(row.getRepairId()).thenReturn("repair-1");
        when(row.getClientName()).thenReturn("Ada Lovelace");
        when(row.getDate()).thenReturn(LocalDateTime.of(2026, 6, 10, 10, 0));
        when(row.getIncome()).thenReturn(BigDecimal.valueOf(1500));
        when(row.getPartsCost()).thenReturn(BigDecimal.valueOf(400));
        when(row.getPartsSale()).thenReturn(BigDecimal.valueOf(700));
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(repairRepository.findFinanceActivityPage(eq(null), eq(null), pageableCaptor.capture()))
                .thenReturn(new PageImpl<>(List.of(row), PageRequest.of(0, 100), 101));

        PageDTO<FinanceRowDTO> result = new FinanceService(repairRepository)
                .getDetails(null, null, -4, 1000, "unsupported", "sideways");

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(100, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("date").getDirection());
        assertEquals(101, result.totalElements());
        assertEquals(1, result.content().size());
        assertEquals(0, BigDecimal.valueOf(700).compareTo(result.content().getFirst().getPartsAmount()));
        assertEquals(0, BigDecimal.valueOf(1100).compareTo(result.content().getFirst().getNet()));
    }

    @Test
    void getDetails_forwardsEveryAllowedSortToTheRepository() {
        List<String> sortFields = List.of("clientName", "date", "income", "partsAmount", "net");
        when(repairRepository.findFinanceActivityPage(any(), any(), any(Pageable.class)))
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
        verify(repairRepository, times(sortFields.size())).findFinanceActivityPage(
                eq(LocalDateTime.of(2026, 6, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 6, 30, 23, 59, 59, 999999999)),
                pageableCaptor.capture());
        for (int index = 0; index < sortFields.size(); index++) {
            Sort.Order order = pageableCaptor.getAllValues().get(index).getSort().getOrderFor(sortFields.get(index));
            assertEquals(Sort.Direction.ASC, order.getDirection());
        }
    }

    @Test
    void recognizedPartsAmount_usesCostUntilAccumulatedIncomeReachesSale() {
        FinanceService service = new FinanceService(repairRepository);
        BigDecimal cost = new BigDecimal("92149.03");
        BigDecimal sale = new BigDecimal("159900.00");

        assertEquals(0, cost.compareTo(service.recognizedPartsAmount(new BigDecimal("120000.00"), cost, sale)));
        assertEquals(0, new BigDecimal("27850.97").compareTo(service.currentNet(new BigDecimal("120000.00"), cost)));
        assertEquals(0, sale.compareTo(service.recognizedPartsAmount(new BigDecimal("159900.00"), cost, sale)));
        assertEquals(0, new BigDecimal("67750.97").compareTo(service.currentNet(new BigDecimal("159900.00"), cost)));
        assertEquals(0, sale.compareTo(service.recognizedPartsAmount(new BigDecimal("200000.00"), cost, sale)));
        assertEquals(0, new BigDecimal("107850.97").compareTo(service.currentNet(new BigDecimal("200000.00"), cost)));
    }

    @Test
    void recognizedPartsAmount_treatsMissingPartsAsZero() {
        FinanceService service = new FinanceService(repairRepository);

        assertEquals(0, BigDecimal.ZERO.compareTo(service.recognizedPartsAmount(BigDecimal.ZERO, null, null)));
        assertEquals(0, BigDecimal.TEN.compareTo(service.recognizedPartsAmount(BigDecimal.ZERO, BigDecimal.TEN, null)));
        assertEquals(0, BigDecimal.TEN.compareTo(service.currentNet(BigDecimal.TEN, null)));
    }
}
