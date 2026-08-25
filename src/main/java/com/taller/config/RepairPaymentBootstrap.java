package com.taller.config;

import com.taller.model.RepairPayment;
import com.taller.model.enums.CurrencyEnum;
import com.taller.model.repository.RepairPaymentRepository;
import com.taller.model.repository.RepairRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RepairPaymentBootstrap implements ApplicationRunner {
    private final RepairRepository repairRepository;
    private final RepairPaymentRepository paymentRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        var payments = repairRepository.findLegacyRetiredWithoutPayments().stream()
                .filter(repair -> repair.getPrice() != null && repair.getPrice().signum() > 0)
                .filter(repair -> repair.getReturnDateTime() != null || repair.getReceiveDateTime() != null)
                .map(repair -> RepairPayment.builder()
                        .repairId(repair.getId())
                        .amount(repair.getPrice())
                        .currency(CurrencyEnum.ARS)
                        .paymentDate(repair.getReturnDateTime() != null ? repair.getReturnDateTime() : repair.getReceiveDateTime())
                        .notes("Cobro histórico migrado")
                        .build())
                .toList();
        paymentRepository.saveAll(payments);
    }
}
