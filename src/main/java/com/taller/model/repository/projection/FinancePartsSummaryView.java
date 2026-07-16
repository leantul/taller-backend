package com.taller.model.repository.projection;

import java.math.BigDecimal;

public interface FinancePartsSummaryView {
    BigDecimal getTotalPartsCost();
    BigDecimal getTotalPartsProfit();
}
