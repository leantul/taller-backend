package com.taller.model.repository;

import com.taller.model.RepairPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RepairPaymentRepository extends JpaRepository<RepairPayment, String> {
    List<RepairPayment> findByRepairId(String repairId);
    List<RepairPayment> findByRepairIdIn(Collection<String> repairIds);
}
