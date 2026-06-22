package com.taller.model.repository;

import com.taller.model.RepairReportSoftwareItem;
import com.taller.model.repository.projection.RepairReportSoftwareItemView;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepairReportSoftwareItemRepository extends JpaRepository<RepairReportSoftwareItem, String> {

    @Query("""
            SELECT i.id AS id,
                   i.softwareName AS softwareName,
                   i.detail AS detail
            FROM RepairReportSoftwareItem i
            WHERE i.repairReportId = :repairReportId
            ORDER BY i.creationDateTime ASC
            """)
    List<RepairReportSoftwareItemView> findViewByRepairReportIdOrderByCreationDateTimeAsc(@Param("repairReportId") String repairReportId);

    void deleteAllByRepairReportId(String repairReportId);
}
