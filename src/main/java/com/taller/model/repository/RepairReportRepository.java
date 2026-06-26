package com.taller.model.repository;

import com.taller.model.RepairReport;
import com.taller.model.repository.projection.RepairReportIdView;
import com.taller.model.repository.projection.RepairReportView;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepairReportRepository extends JpaRepository<RepairReport, String> {

    @Query("""
            SELECT r.id AS id,
                   r.repairId AS repairId,
                   r.orderNumber AS orderNumber,
                   r.issuedAt AS issuedAt,
                   r.clientName AS clientName,
                   r.clientLastName AS clientLastName,
                   r.clientPhone AS clientPhone,
                   r.clientEmail AS clientEmail,
                   r.deviceTypeName AS deviceTypeName,
                   r.deviceBrand AS deviceBrand,
                   r.deviceModel AS deviceModel,
                   r.deviceSerialNumber AS deviceSerialNumber,
                   r.reportedIssue AS reportedIssue,
                   r.workPerformed AS workPerformed,
                   r.finalObservations AS finalObservations,
                   r.showPartPrices AS showPartPrices,
                   r.finalAmount AS finalAmount
            FROM RepairReport r
            WHERE r.repairId = :repairId
            """)
    Optional<RepairReportView> findViewByRepairId(@Param("repairId") String repairId);

    @Query("""
            SELECT r.id AS id
            FROM RepairReport r
            WHERE r.repairId = :repairId
            """)
    Optional<RepairReportIdView> findIdViewByRepairId(@Param("repairId") String repairId);
}
