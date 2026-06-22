package com.taller.model.repository;

import com.taller.model.RepairReportHardwareItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepairReportHardwareItemRepository extends JpaRepository<RepairReportHardwareItem, String> {

    List<RepairReportHardwareItem> findByRepairReportIdOrderByCreationDateTimeAsc(String repairReportId);

    void deleteAllByRepairReportId(String repairReportId);
}
