package com.taller.model.repository;

import com.taller.model.RepairPart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RepairPartRepository extends JpaRepository<RepairPart, String> {
    List<RepairPart> findByRepairId(String repairId);
    List<RepairPart> findByRepairIdIn(Collection<String> repairIds);
}
