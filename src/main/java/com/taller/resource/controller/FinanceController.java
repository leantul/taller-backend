package com.taller.resource.controller;

import com.taller.resource.dto.FinanceSummaryDTO;
import com.taller.service.FinanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/finance")
public class FinanceController {

    private final FinanceService financeService;

    @GetMapping("/summary")
    public FinanceSummaryDTO getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return financeService.getSummary(from, to);
    }

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }
}
