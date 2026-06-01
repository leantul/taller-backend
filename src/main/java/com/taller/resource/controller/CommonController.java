package com.taller.resource.controller;

import com.taller.service.CommonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/common")
public class CommonController {

    private final CommonService commonService;

    @GetMapping("/repairStatus")
    public Map<Integer, String> getRepairStatus() {
        return commonService.getRepairStatus();
    }

    public CommonController(CommonService commonService) {
        this.commonService = commonService;
    }
}
