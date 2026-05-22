package com.taller.model.repository;

import com.taller.model.Repair;
import com.taller.model.repository.projection.DeviceLastRepairView;
import com.taller.model.repository.projection.MonthlyRevenueView;
import com.taller.model.repository.projection.RepairListView;
import com.taller.model.repository.projection.RepairStatusCountView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface RepairRepository extends JpaRepository<Repair, String> {
    @Query("""
            SELECT r.id AS id,
                   r.idDevice AS idDevice,
                   r.idClient AS idClient,
                   r.description AS description,
                   r.orderNumber AS orderNumber,
                   r.status AS status,
                   r.receiveDateTime AS receiveDateTime,
                   r.returnDateTime AS returnDateTime,
                   r.price AS price,
                   r.laborAmount AS laborAmount,
                   r.extraAmount AS extraAmount,
                   r.quotedAmount AS quotedAmount,
                   r.quoteNotes AS quoteNotes,
                   r.approved AS approved,
                   r.rejected AS rejected,
                   r.readyNotifiedAt AS readyNotifiedAt
            FROM Repair r
            ORDER BY r.creationDateTime DESC
            """)
    List<RepairListView> findListRows();

    @Query("""
            SELECT r.id AS id,
                   r.idDevice AS idDevice,
                   r.idClient AS idClient,
                   r.description AS description,
                   r.orderNumber AS orderNumber,
                   r.status AS status,
                   r.receiveDateTime AS receiveDateTime,
                   r.returnDateTime AS returnDateTime,
                   r.price AS price,
                   r.laborAmount AS laborAmount,
                   r.extraAmount AS extraAmount,
                   r.quotedAmount AS quotedAmount,
                   r.quoteNotes AS quoteNotes,
                   r.approved AS approved,
                   r.rejected AS rejected,
                   r.readyNotifiedAt AS readyNotifiedAt
            FROM Repair r
            WHERE lower(r.orderNumber) LIKE lower(concat('%', ?1, '%'))
               OR lower(r.description) LIKE lower(concat('%', ?1, '%'))
            ORDER BY r.creationDateTime DESC
            """)
    List<RepairListView> searchListRows(String term);

    @Query("""
            SELECT r.id AS id,
                   r.idDevice AS idDevice,
                   r.idClient AS idClient,
                   r.description AS description,
                   r.orderNumber AS orderNumber,
                   r.status AS status,
                   r.receiveDateTime AS receiveDateTime,
                   r.returnDateTime AS returnDateTime,
                   r.price AS price,
                   r.laborAmount AS laborAmount,
                   r.extraAmount AS extraAmount,
                   r.quotedAmount AS quotedAmount,
                   r.quoteNotes AS quoteNotes,
                   r.approved AS approved,
                   r.rejected AS rejected,
                   r.readyNotifiedAt AS readyNotifiedAt
            FROM Repair r
            WHERE COALESCE(r.returnDateTime, r.receiveDateTime) >= :from
              AND COALESCE(r.returnDateTime, r.receiveDateTime) <= :to
            ORDER BY COALESCE(r.returnDateTime, r.receiveDateTime) DESC, r.orderNumber DESC
            """)
    List<RepairListView> findFinanceRowsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT r.id AS id,
                   r.idDevice AS idDevice,
                   r.idClient AS idClient,
                   r.description AS description,
                   r.orderNumber AS orderNumber,
                   r.status AS status,
                   r.receiveDateTime AS receiveDateTime,
                   r.returnDateTime AS returnDateTime,
                   r.price AS price,
                   r.laborAmount AS laborAmount,
                   r.extraAmount AS extraAmount,
                   r.quotedAmount AS quotedAmount,
                   r.quoteNotes AS quoteNotes,
                   r.approved AS approved,
                   r.rejected AS rejected,
                   r.readyNotifiedAt AS readyNotifiedAt
            FROM Repair r
            WHERE COALESCE(r.returnDateTime, r.receiveDateTime) >= :from
            ORDER BY COALESCE(r.returnDateTime, r.receiveDateTime) DESC, r.orderNumber DESC
            """)
    List<RepairListView> findFinanceRowsFrom(@Param("from") LocalDateTime from);

    @Query("""
            SELECT r.id AS id,
                   r.idDevice AS idDevice,
                   r.idClient AS idClient,
                   r.description AS description,
                   r.orderNumber AS orderNumber,
                   r.status AS status,
                   r.receiveDateTime AS receiveDateTime,
                   r.returnDateTime AS returnDateTime,
                   r.price AS price,
                   r.laborAmount AS laborAmount,
                   r.extraAmount AS extraAmount,
                   r.quotedAmount AS quotedAmount,
                   r.quoteNotes AS quoteNotes,
                   r.approved AS approved,
                   r.rejected AS rejected,
                   r.readyNotifiedAt AS readyNotifiedAt
            FROM Repair r
            WHERE COALESCE(r.returnDateTime, r.receiveDateTime) <= :to
            ORDER BY COALESCE(r.returnDateTime, r.receiveDateTime) DESC, r.orderNumber DESC
            """)
    List<RepairListView> findFinanceRowsTo(@Param("to") LocalDateTime to);

    @Query("""
            SELECT r.id AS id,
                   r.idDevice AS idDevice,
                   r.idClient AS idClient,
                   r.description AS description,
                   r.orderNumber AS orderNumber,
                   r.status AS status,
                   r.receiveDateTime AS receiveDateTime,
                   r.returnDateTime AS returnDateTime,
                   r.price AS price,
                   r.laborAmount AS laborAmount,
                   r.extraAmount AS extraAmount,
                   r.quotedAmount AS quotedAmount,
                   r.quoteNotes AS quoteNotes,
                   r.approved AS approved,
                   r.rejected AS rejected,
                   r.readyNotifiedAt AS readyNotifiedAt
            FROM Repair r
            ORDER BY COALESCE(r.returnDateTime, r.receiveDateTime) DESC, r.orderNumber DESC
            """)
    List<RepairListView> findFinanceRowsAll();

    List<Repair> findByReturnDateTimeBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT r FROM Repair r ORDER BY r.creationDateTime DESC")
    List<Repair> findLatest(Pageable pageable);

    @Query("""
            SELECT r.id AS id,
                   r.idDevice AS idDevice,
                   r.idClient AS idClient,
                   r.description AS description,
                   r.orderNumber AS orderNumber,
                   r.status AS status,
                   r.receiveDateTime AS receiveDateTime,
                   r.returnDateTime AS returnDateTime,
                   r.price AS price,
                   r.laborAmount AS laborAmount,
                   r.extraAmount AS extraAmount,
                   r.quotedAmount AS quotedAmount,
                   r.quoteNotes AS quoteNotes,
                   r.approved AS approved,
                   r.rejected AS rejected,
                   r.readyNotifiedAt AS readyNotifiedAt
            FROM Repair r
            WHERE r.status = com.taller.model.enums.RepairStatusEnum.RETIRADA
            ORDER BY COALESCE(r.returnDateTime, r.receiveDateTime) DESC
            """)
    List<RepairListView> findLatestDeliveredRows(Pageable pageable);

    @Query("""
            SELECT r.status AS status, COUNT(r) AS total
            FROM Repair r
            GROUP BY r.status
            """)
    List<RepairStatusCountView> countByStatus();

    @Query("""
            SELECT r.receiveDateTime AS receiveDateTime, r.price AS price
            FROM Repair r
            WHERE r.receiveDateTime >= :from
            ORDER BY r.receiveDateTime ASC
            """)
    List<MonthlyRevenueView> findMonthlyRevenueSince(@Param("from") LocalDateTime from);

    @Query("""
            SELECT r.idDevice AS deviceId, MAX(r.receiveDateTime) AS lastRepairDate
            FROM Repair r
            WHERE r.idDevice IS NOT NULL
              AND r.receiveDateTime IS NOT NULL
            GROUP BY r.idDevice
            ORDER BY MAX(r.receiveDateTime) ASC
            """)
    List<DeviceLastRepairView> findOldestLastRepairByDevice(Pageable pageable);

    @Query(value = "SELECT nextval('repair_order_seq')", nativeQuery = true)
    Long nextOrderValue();
}
