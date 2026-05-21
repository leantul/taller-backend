package com.taller.model.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface MonthlyRevenueView {
    LocalDateTime getReceiveDateTime();
    BigDecimal getPrice();
}
