package com.taller.model.repository;

import com.taller.model.RepairStatusHistory;
import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.repository.projection.RepairStatusHistoryView;
import com.taller.model.repository.projection.OverdueRepairPaymentView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepairStatusHistoryRepository extends JpaRepository<RepairStatusHistory, String> {
    List<RepairStatusHistoryView> findByRepairIdOrderByChangedAtAscCreationDateTimeAsc(String repairId);
    @Query("""
            SELECT history.repairId AS repairId, history.changedAt AS statusChangedAt
            FROM RepairStatusHistory history JOIN history.repair repair
            WHERE repair.status = com.taller.model.enums.RepairStatusEnum.RETIRADA_FALTA_COBRAR
              AND history.status = com.taller.model.enums.RepairStatusEnum.RETIRADA_FALTA_COBRAR
              AND history.changedAt <= :threshold
              AND history.changedAt = (SELECT MAX(latest.changedAt) FROM RepairStatusHistory latest
                  WHERE latest.repairId = history.repairId)
            """)
    Page<OverdueRepairPaymentView> findOverduePaymentRepairs(@Param("threshold") LocalDateTime threshold, Pageable pageable);
    boolean existsByRepairIdAndStatusAndChangedAt(String repairId, RepairStatusEnum status, LocalDateTime changedAt);
    void deleteByRepairId(String repairId);
}
