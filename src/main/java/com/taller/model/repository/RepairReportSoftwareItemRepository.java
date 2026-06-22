package com.taller.model.repository;

import com.taller.model.RepairReportSoftwareItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepairReportSoftwareItemRepository extends JpaRepository<RepairReportSoftwareItem, String> {

    List<RepairReportSoftwareItem> findByRepairReportIdOrderByCreationDateTimeAsc(String repairReportId);

    void deleteAllByRepairReportId(String repairReportId);
}
