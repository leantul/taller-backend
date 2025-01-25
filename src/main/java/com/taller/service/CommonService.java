package com.taller.service;

import com.taller.model.enums.RepairStatusEnum;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CommonService {

    public Map<Integer, String> getRepairStatus() {
        Map<Integer, String> repairStatus = new java.util.LinkedHashMap<>();
        for (RepairStatusEnum repairStatusEnum : RepairStatusEnum.values()) {
            repairStatus.put(repairStatusEnum.getCode(), repairStatusEnum.getStatus());
        }
        return repairStatus;
    }
}
