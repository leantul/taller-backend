package com.taller.resource.controller;

import com.taller.resource.dto.DashboardDTO;
import com.taller.resource.dto.DashboardOverviewDTO;
import com.taller.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/month")
    public DashboardDTO monthSummary(@RequestParam(required = false) Integer year,
                                     @RequestParam(required = false) Integer month) {
        LocalDate now = LocalDate.now();
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();
        return dashboardService.monthSummary(targetYear, targetMonth);
    }

    @GetMapping("/overview")
    public DashboardOverviewDTO overview() {
        return dashboardService.overview();
    }

}
