package com.taller.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@Entity
@Table(name = "repair_report_software_items")
@AllArgsConstructor
@NoArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "id_repair_report_software_item"))
public class RepairReportSoftwareItem extends BasicEntity {

    @Column(name = "repair_report_id", nullable = false)
    private String repairReportId;

    @Column(name = "software_name", nullable = false)
    private String softwareName;

    @Column(name = "detail")
    private String detail;
}
