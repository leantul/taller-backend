package com.taller.model.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.lang.reflect.Method;
import java.util.Set;
import org.hibernate.query.hql.internal.HqlParseTreeBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

class RepairRepositoryFinanceQueryTest {

    private static final Set<String> FINANCE_QUERY_METHODS = Set.of(
            "findFinancePage",
            "summarizeFinanceRepairs",
            "summarizeFinanceParts",
            "summarizeMonthlyFinanceIncome",
            "summarizeMonthlyFinancePartsCost",
            "findFinanceActivityPage",
            "summarizeRetiredFinanceRepairs",
            "countFinanceActivityRepairs",
            "summarizeFinancePayments",
            "summarizeRecognizedFinanceParts",
            "sumPaymentIncomeBefore",
            "summarizeRecognizedFinancePartsBefore",
            "sumFinancePaymentIncomeBetween",
            "sumRecognizedPartsCostBetween");

    @Test
    void financeQueriesHaveValidHqlSyntax() {
        for (Method method : RepairRepository.class.getDeclaredMethods()) {
            if (!FINANCE_QUERY_METHODS.contains(method.getName())) continue;
            Query query = method.getAnnotation(Query.class);
            org.junit.jupiter.api.Assertions.assertFalse(query.nativeQuery(), () -> method.getName() + " must use JPQL/HQL, not native SQL");
            assertDoesNotThrow(
                    () -> parseHql(query.value()),
                    () -> method.getName() + " has invalid HQL");
            if (!query.countQuery().isBlank()) {
                assertDoesNotThrow(
                        () -> parseHql(query.countQuery()),
                        () -> method.getName() + " has invalid count HQL");
            }
        }
    }

    private void parseHql(String hql) {
        HqlParseTreeBuilder parserBuilder = HqlParseTreeBuilder.INSTANCE;
        parserBuilder.buildHqlParser(hql, parserBuilder.buildHqlLexer(hql)).statement();
    }
}
