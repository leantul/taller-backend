package com.taller.resource.dto;


public class DashboardSeriesItemDTO {
    private String label;
    private Number value;

    public DashboardSeriesItemDTO(String label, Number value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return this.label;
    }

    public Number getValue() {
        return this.value;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setValue(Number value) {
        this.value = value;
    }
}
