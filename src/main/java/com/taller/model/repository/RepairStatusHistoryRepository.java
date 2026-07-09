package com.taller.model.repository;

import com.taller.model.RepairStatusHistory;
import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.repository.projection.RepairStatusHistoryView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RepairStatusHistoryRepository extends JpaRepository<RepairStatusHistory, String> {
    List<RepairStatusHistoryView> findByRepairIdOrderByChangedAtAscCreationDateTimeAsc(String repairId);
    boolean existsByRepairIdAndStatusAndChangedAt(String repairId, RepairStatusEnum status, LocalDateTime changedAt);
    void deleteByRepairId(String repairId);
}
