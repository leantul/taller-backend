package com.taller.model.repository;

import com.taller.model.RepairReportHardwareItem;
import com.taller.model.repository.projection.RepairReportHardwareItemView;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepairReportHardwareItemRepository extends JpaRepository<RepairReportHardwareItem, String> {

    @Query("""
            SELECT i.id AS id,
                   i.partName AS partName,
                   i.quantity AS quantity,
                   i.detail AS detail,
                   i.unitPrice AS unitPrice,
                   i.includePrice AS includePrice
            FROM RepairReportHardwareItem i
            WHERE i.repairReportId = :repairReportId
            ORDER BY i.creationDateTime ASC
            """)
    List<RepairReportHardwareItemView> findViewByRepairReportIdOrderByCreationDateTimeAsc(@Param("repairReportId") String repairReportId);

    void deleteAllByRepairReportId(String repairReportId);
}
