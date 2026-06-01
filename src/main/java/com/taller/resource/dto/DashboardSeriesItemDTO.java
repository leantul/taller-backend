package com.taller.resource.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DashboardSeriesItemDTO {
    private String label;
    private Number value;
}
