package com.taller.model.repository;

import com.taller.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByReadedFalseOrderByEventDateDesc();
    Page<Notification> findByReadedFalse(Pageable pageable);
    long countByReadedFalse();
    List<Notification> findByEntityIdInAndType(Collection<String> entityIds, String type);
    List<Notification> findByEntityIdInAndTypeInAndEventDateBetween(
            Collection<String> entityIds,
            Collection<String> types,
            LocalDateTime from,
            LocalDateTime to);
    @Modifying
    @Query("""
            UPDATE Notification notification SET notification.readed = true
            WHERE notification.type = :type AND notification.readed = false
              AND notification.repairId NOT IN (SELECT repair.id FROM Repair repair
                  WHERE repair.status = com.taller.model.enums.RepairStatusEnum.RETIRADA_FALTA_COBRAR)
            """)
    int closeResolvedPaymentReminders(@Param("type") String type);

    @Modifying
    @Query("UPDATE Notification notification SET notification.readed = true " +
            "WHERE notification.type = :type AND notification.repairId = :repairId AND notification.readed = false")
    int markPaymentRemindersAsRead(@Param("type") String type, @Param("repairId") String repairId);
}
