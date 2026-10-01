package com.taller.model.repository;

import com.taller.model.Repair;
import com.taller.model.RepairPart;
import com.taller.model.RepairPayment;
import com.taller.model.enums.CurrencyEnum;
import com.taller.model.enums.RepairStatusEnum;
import com.taller.resource.dto.FinanceRowDTO;
import com.taller.service.FinanceService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(showSql = false, properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false"})
@Import(FinanceService.class)
abstract class FinancePeriodRepositoryTests {
    @Autowired TestEntityManager entities;
    @Autowired FinanceService finance;

    private static final LocalDate SEPTEMBER = LocalDate.of(2026, 9, 1);
    private static final LocalDate OCTOBER = LocalDate.of(2026, 10, 1);

    @Test
    void paidBeforePickupIsRecognizedOnlyInPaymentMonth() {
        Repair repair = repair(null, "100", "40", "60", "40");
        payment(repair, "100", LocalDateTime.of(2026, 9, 30, 23, 59, 59, 999999000));
        assertPeriod(SEPTEMBER, "100", "40", "60", "40", "20");

        repair.setStatus(RepairStatusEnum.RETIRADA);
        repair.setReturnDateTime(OCTOBER.atStartOfDay());
        entities.flush();
        assertPeriod(SEPTEMBER, "100", "40", "60", "40", "20");
        assertPeriod(OCTOBER, "0", "0", "0", "0", "0");
        assertEquals(0L, finance.getSummary(SEPTEMBER, OCTOBER.minusDays(1)).getDeliveredCount());
        assertEquals(0L, finance.getSummary(OCTOBER, OCTOBER.plusMonths(1).minusDays(1)).getPositiveFinalAmountCount());
        assertEquals(1L, finance.getSummary(OCTOBER, OCTOBER.plusMonths(1).minusDays(1)).getDeliveredCount());
        assertEquals(OCTOBER.atStartOfDay(), rows(OCTOBER, OCTOBER.plusMonths(1).minusDays(1)).get(0).getDate());
    }

    @Test
    void partialPaymentsUseEachMonthAndRecognizeCostsOnlyOnce() {
        Repair repair = repair(null, "100", "40", "60", "40");
        payment(repair, "30", SEPTEMBER.atTime(12, 0));
        payment(repair, "20", SEPTEMBER.plusDays(1).atTime(12, 0));
        payment(repair, "50", OCTOBER.atStartOfDay());
        assertPeriod(SEPTEMBER, "50", "40", "10", "40", "20");
        assertPeriod(OCTOBER, "50", "0", "50", "0", "0");
        assertMoney("60", finance.getSummary(null, null).getNetIncome());
        assertEquals(1, rows(SEPTEMBER, OCTOBER.plusMonths(1).minusDays(1)).size());
    }

    @Test
    void unpaidPickupRecognizesCostBeforeLaterPayment() {
        Repair repair = repair(SEPTEMBER.atTime(12, 0), "100", "40", "60", "40");
        repair.setStatus(RepairStatusEnum.RETIRADA_FALTA_COBRAR);
        assertPeriod(SEPTEMBER, "0", "40", "-40", "40", "20");
        payment(repair, "100", OCTOBER.atTime(12, 0));
        assertPeriod(SEPTEMBER, "0", "40", "-40", "40", "20");
        assertPeriod(OCTOBER, "100", "0", "100", "0", "0");
    }

    @Test
    void paymentAndPickupAtSameInstantRecognizeCostsOnce() {
        Repair repair = repair(SEPTEMBER.atStartOfDay(), "100", "40", "60", "40");
        payment(repair, "100", SEPTEMBER.atStartOfDay());
        assertPeriod(SEPTEMBER, "100", "40", "60", "40", "20");
        assertMoney("40", finance.getSummary(null, null).getTotalPartsCost());
    }

    @Test
    void openRangesDoNotIncludeEarlierPaymentsOrRecognizeCostsAgain() {
        Repair repair = repair(null, "100", "40", "60", "40");
        payment(repair, "30", SEPTEMBER.atStartOfDay());
        payment(repair, "70", OCTOBER.atStartOfDay());
        assertMoney("70", finance.getSummary(OCTOBER, null).getTotalIncome());
        assertMoney("0", finance.getSummary(OCTOBER, null).getTotalPartsCost());
        assertMoney("30", finance.getSummary(null, SEPTEMBER.plusMonths(1).minusDays(1)).getTotalIncome());
        assertMoney("100", finance.getSummary(null, null).getTotalIncome());
        assertMoney("70", rows(OCTOBER, null).get(0).getIncome());
        assertMoney("0", rows(OCTOBER, null).get(0).getPartsAmount());
    }

    @Test
    void paginationAndSortingUsePeriodAmountsNotAccumulatedAmounts() {
        Repair first = repair(null, "100", "40", "60", "40");
        payment(first, "90", SEPTEMBER.atStartOfDay());
        payment(first, "10", OCTOBER.atStartOfDay());
        Repair second = repair(null, "20", "5", "10", "10");
        payment(second, "20", OCTOBER.atStartOfDay());
        entities.flush();
        for (String sort : new String[]{"income", "net", "partsAmount"}) {
            var page = finance.getDetails(OCTOBER, OCTOBER.plusMonths(1).minusDays(1), 0, 1, sort, "desc");
            assertEquals(second.getId(), page.content().get(0).getRepairId());
            assertEquals(2L, page.totalElements());
            assertEquals(2, page.totalPages());
        }
        assertMoney("25", finance.getSummary(OCTOBER, OCTOBER.plusMonths(1).minusDays(1)).getNetIncome());
    }

    @Test
    void monthlyChartMatchesPeriodTotalsAndEmptyMonthsAreZero() {
        YearMonth month = YearMonth.now().minusMonths(1);
        Repair repair = repair(month.plusMonths(1).atDay(1).atStartOfDay(), "100", "40", "60", "40");
        payment(repair, "100", month.atEndOfMonth().atTime(23, 59, 59));
        entities.flush();
        var summary = finance.getSummary(month.atDay(1), month.atEndOfMonth());
        String label = month.getMonth().name().substring(0, 1) + month.getMonth().name().substring(1).toLowerCase() + " " + month.getYear();
        var chartValue = summary.getMonthlyNet().stream().filter(item -> label.equals(item.getLabel())).findFirst().orElseThrow().getValue();
        assertMoney("60", (BigDecimal) chartValue);
        assertEquals(12, summary.getMonthlyNet().size());
        assertMoney("0", (BigDecimal) summary.getMonthlyNet().get(11).getValue());
    }

    @Test
    void multiplePartsAndPaymentsAreNotMultipliedAndNullsUseDefaults() {
        Repair repair = repair(null, "100", "10", "15", "40");
        entities.persist(RepairPart.builder().repairId(repair.getId()).quantity(2)
                .cost(new BigDecimal("5")).salePrice(new BigDecimal("10")).build());
        entities.persist(RepairPart.builder().repairId(repair.getId())
                .cost(new BigDecimal("3")).build());
        entities.persist(RepairPart.builder().repairId(repair.getId())
                .salePrice(new BigDecimal("5")).build());
        payment(repair, "30", SEPTEMBER.atStartOfDay());
        payment(repair, "70", SEPTEMBER.atStartOfDay());
        assertPeriod(SEPTEMBER, "100", "23", "77", "40", "17");
    }

    @Test
    void noFinancialActivityAndMissingPartsProduceZeroTotals() {
        repair(null, "100", "0", "0", "40");
        assertEquals(0, rows(SEPTEMBER, SEPTEMBER.plusMonths(1).minusDays(1)).size());
        Repair paid = Repair.builder().status(RepairStatusEnum.COBRADO_ESPERANDO_RETIRO)
                .price(new BigDecimal("100")).laborAmount(BigDecimal.ZERO).build();
        entities.persist(paid);
        payment(paid, "100", SEPTEMBER.atStartOfDay());
        assertPeriod(SEPTEMBER, "100", "0", "100", "0", "0");
    }

    private Repair repair(LocalDateTime pickup, String price, String cost, String sale, String labor) {
        Repair repair = Repair.builder().status(pickup == null ? RepairStatusEnum.COBRADO_ESPERANDO_RETIRO : RepairStatusEnum.RETIRADA)
                .returnDateTime(pickup).price(new BigDecimal(price)).laborAmount(new BigDecimal(labor)).build();
        entities.persist(repair);
        entities.persist(RepairPart.builder().repairId(repair.getId()).quantity(1)
                .cost(new BigDecimal(cost)).salePrice(new BigDecimal(sale)).build());
        return repair;
    }

    private void payment(Repair repair, String amount, LocalDateTime date) {
        entities.persist(RepairPayment.builder().repairId(repair.getId()).amount(new BigDecimal(amount))
                .currency(CurrencyEnum.ARS).paymentDate(date).build());
        entities.flush();
    }

    private List<FinanceRowDTO> rows(LocalDate from, LocalDate to) {
        return finance.getDetails(from, to, 0, 100, "date", "desc").content();
    }

    private void assertPeriod(LocalDate start, String income, String cost, String net, String labor, String partsProfit) {
        entities.flush();
        LocalDate end = start.plusMonths(1).minusDays(1);
        var summary = finance.getSummary(start, end);
        assertMoney(income, summary.getTotalIncome());
        assertMoney(cost, summary.getTotalPartsCost());
        assertMoney(net, summary.getNetIncome());
        assertMoney(labor, summary.getTotalLabor());
        assertMoney(partsProfit, summary.getTotalPartsProfit());
        assertEquals(0, summary.getNetIncome().compareTo(summary.getTotalLabor().add(summary.getTotalPartsProfit()).add(summary.getTotalAdjustment())));
        var details = rows(start, end);
        assertEquals(summary.getRepairCount(), details.size());
        assertMoney(income, details.stream().map(FinanceRowDTO::getIncome).reduce(BigDecimal.ZERO, BigDecimal::add));
        assertMoney(cost, details.stream().map(FinanceRowDTO::getPartsAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        assertMoney(net, details.stream().map(FinanceRowDTO::getNet).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), () -> "expected " + expected + " but was " + actual);
    }
}
