package com.taller.model.repository;

import com.taller.model.RepairReport;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepairReportRepository extends JpaRepository<RepairReport, String> {

    Optional<RepairReport> findByRepairId(String repairId);
}
