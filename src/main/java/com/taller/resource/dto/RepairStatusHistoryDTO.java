package com.taller.resource.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taller.model.enums.RepairStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RepairStatusHistoryDTO {
    private String id;
    private String repairId;
    private RepairStatusEnum status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime changedAt;
}
