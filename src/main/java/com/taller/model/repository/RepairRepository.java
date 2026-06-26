package com.taller.model.repository;

import com.taller.model.Repair;
import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.repository.projection.DeviceLastRepairView;
import com.taller.model.repository.projection.ClientRepairHistoryView;
import com.taller.model.repository.projection.DeliveryReportSourceView;
import com.taller.model.repository.projection.FinanceRepairView;
import com.taller.model.repository.projection.RepairListView;
import com.taller.model.repository.projection.RepairStatusCountView;
import com.taller.model.repository.projection.StatusBoardRepairView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RepairRepository extends JpaRepository<Repair, String> {
    @Query("""
            SELECT r.id AS id,
                   r.idDevice AS idDevice,
                   r.idClient AS idClient,
                   r.orderNumber AS orderNumber,
                   r.description AS description,
                   r.status AS status,
                   r.receiveDateTime AS receiveDateTime,
                   r.returnDateTime AS returnDateTime,
                   r.price AS price,
                   r.quotedAmount AS quotedAmount,
                   c.name AS clientName,
                   c.lastName AS clientLastName,
                   d.deviceType.name AS deviceTypeName,
                   d.brand AS deviceBrand,
                   d.model AS deviceModel
            FROM Repair r
            LEFT JOIN r.client c
            LEFT JOIN r.device d
            WHERE r.status <> com.taller.model.enums.RepairStatusEnum.RETIRADA
            ORDER BY r.creationDateTime DESC
            """)
    List<StatusBoardRepairView> findStatusBoardRows();

    @Query(value = """
            SELECT r.id AS id,
                   r.orderNumber AS orderNumber,
                   r.status AS status,
                   d.brand AS deviceBrand,
                   d.model AS deviceModel,
                   r.receiveDateTime AS receiveDateTime,
                   r.returnDateTime AS returnDateTime
            FROM Repair r
            LEFT JOIN r.device d
            WHERE r.idClient = :clientId
            ORDER BY CASE WHEN r.receiveDateTime IS NULL THEN 1 ELSE 0 END,
                     r.receiveDateTime DESC,
                     r.orderNumber DESC
            """,
            countQuery = "SELECT COUNT(r) FROM Repair r WHERE r.idClient = :clientId")
    Page<ClientRepairHistoryView> findClientHistory(@Param("clientId") String clientId, Pageable pageable);

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
                   r.readyNotifiedAt AS readyNotifiedAt,
                   c.name AS clientName,
                   c.lastName AS clientLastName,
                   c.phone AS clientPhone,
                   d.deviceType.name AS deviceTypeName,
                   d.brand AS deviceBrand,
                   d.model AS deviceModel
            FROM Repair r
            LEFT JOIN r.client c
            LEFT JOIN r.device d
            ORDER BY r.creationDateTime DESC
            """)
    List<RepairListView> findListRows();

    @Query(value = """
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
                   r.readyNotifiedAt AS readyNotifiedAt,
                   c.name AS clientName,
                   c.lastName AS clientLastName,
                   c.phone AS clientPhone,
                   d.deviceType.name AS deviceTypeName,
                   d.brand AS deviceBrand,
                   d.model AS deviceModel
            FROM Repair r
            LEFT JOIN r.client c
            LEFT JOIN r.device d
            WHERE (:term = ''
               OR lower(r.orderNumber) LIKE lower(concat('%', :term, '%'))
               OR lower(r.description) LIKE lower(concat('%', :term, '%'))
               OR lower(c.name) LIKE lower(concat('%', :term, '%'))
               OR lower(c.lastName) LIKE lower(concat('%', :term, '%'))
               OR lower(d.brand) LIKE lower(concat('%', :term, '%'))
               OR lower(d.model) LIKE lower(concat('%', :term, '%'))
               OR lower(d.deviceType.name) LIKE lower(concat('%', :term, '%')))
            """,
            countQuery = """
            SELECT COUNT(r)
            FROM Repair r
            LEFT JOIN r.client c
            LEFT JOIN r.device d
            WHERE (:term = ''
               OR lower(r.orderNumber) LIKE lower(concat('%', :term, '%'))
               OR lower(r.description) LIKE lower(concat('%', :term, '%'))
               OR lower(c.name) LIKE lower(concat('%', :term, '%'))
               OR lower(c.lastName) LIKE lower(concat('%', :term, '%'))
               OR lower(d.brand) LIKE lower(concat('%', :term, '%'))
               OR lower(d.model) LIKE lower(concat('%', :term, '%'))
               OR lower(d.deviceType.name) LIKE lower(concat('%', :term, '%')))
            """)
    Page<RepairListView> findPage(
            @Param("term") String term,
            Pageable pageable);

    @Query(value = """
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
                   r.readyNotifiedAt AS readyNotifiedAt,
                   c.name AS clientName,
                   c.lastName AS clientLastName,
                   c.phone AS clientPhone,
                   d.deviceType.name AS deviceTypeName,
                   d.brand AS deviceBrand,
                   d.model AS deviceModel
            FROM Repair r
            LEFT JOIN r.client c
            LEFT JOIN r.device d
            WHERE (:term = ''
               OR lower(r.orderNumber) LIKE lower(concat('%', :term, '%'))
               OR lower(r.description) LIKE lower(concat('%', :term, '%'))
               OR lower(c.name) LIKE lower(concat('%', :term, '%'))
               OR lower(c.lastName) LIKE lower(concat('%', :term, '%'))
               OR lower(d.brand) LIKE lower(concat('%', :term, '%'))
               OR lower(d.model) LIKE lower(concat('%', :term, '%'))
               OR lower(d.deviceType.name) LIKE lower(concat('%', :term, '%')))
              AND r.receiveDateTime >= :from
            """,
            countQuery = """
            SELECT COUNT(r)
            FROM Repair r
            LEFT JOIN r.client c
            LEFT JOIN r.device d
            WHERE (:term = ''
               OR lower(r.orderNumber) LIKE lower(concat('%', :term, '%'))
               OR lower(r.description) LIKE lower(concat('%', :term, '%'))
               OR lower(c.name) LIKE lower(concat('%', :term, '%'))
               OR lower(c.lastName) LIKE lower(concat('%', :term, '%'))
               OR lower(d.brand) LIKE lower(concat('%', :term, '%'))
               OR lower(d.model) LIKE lower(concat('%', :term, '%'))
               OR lower(d.deviceType.name) LIKE lower(concat('%', :term, '%')))
              AND r.receiveDateTime >= :from
            """)
    Page<RepairListView> findPageFrom(
            @Param("term") String term,
            @Param("from") LocalDateTime from,
            Pageable pageable);

    @Query(value = """
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
                   r.readyNotifiedAt AS readyNotifiedAt,
                   c.name AS clientName,
                   c.lastName AS clientLastName,
                   c.phone AS clientPhone,
                   d.deviceType.name AS deviceTypeName,
                   d.brand AS deviceBrand,
                   d.model AS deviceModel
            FROM Repair r
            LEFT JOIN r.client c
            LEFT JOIN r.device d
            WHERE (:term = ''
               OR lower(r.orderNumber) LIKE lower(concat('%', :term, '%'))
               OR lower(r.description) LIKE lower(concat('%', :term, '%'))
               OR lower(c.name) LIKE lower(concat('%', :term, '%'))
               OR lower(c.lastName) LIKE lower(concat('%', :term, '%'))
               OR lower(d.brand) LIKE lower(concat('%', :term, '%'))
               OR lower(d.model) LIKE lower(concat('%', :term, '%'))
               OR lower(d.deviceType.name) LIKE lower(concat('%', :term, '%')))
              AND r.receiveDateTime <= :to
            """,
            countQuery = """
            SELECT COUNT(r)
            FROM Repair r
            LEFT JOIN r.client c
            LEFT JOIN r.device d
            WHERE (:term = ''
               OR lower(r.orderNumber) LIKE lower(concat('%', :term, '%'))
               OR lower(r.description) LIKE lower(concat('%', :term, '%'))
               OR lower(c.name) LIKE lower(concat('%', :term, '%'))
               OR lower(c.lastName) LIKE lower(concat('%', :term, '%'))
               OR lower(d.brand) LIKE lower(concat('%', :term, '%'))
               OR lower(d.model) LIKE lower(concat('%', :term, '%'))
               OR lower(d.deviceType.name) LIKE lower(concat('%', :term, '%')))
              AND r.receiveDateTime <= :to
            """)
    Page<RepairListView> findPageTo(
            @Param("term") String term,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    @Query(value = """
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
                   r.readyNotifiedAt AS readyNotifiedAt,
                   c.name AS clientName,
                   c.lastName AS clientLastName,
                   c.phone AS clientPhone,
                   d.deviceType.name AS deviceTypeName,
                   d.brand AS deviceBrand,
                   d.model AS deviceModel
            FROM Repair r
            LEFT JOIN r.client c
            LEFT JOIN r.device d
            WHERE (:term = ''
               OR lower(r.orderNumber) LIKE lower(concat('%', :term, '%'))
               OR lower(r.description) LIKE lower(concat('%', :term, '%'))
               OR lower(c.name) LIKE lower(concat('%', :term, '%'))
               OR lower(c.lastName) LIKE lower(concat('%', :term, '%'))
               OR lower(d.brand) LIKE lower(concat('%', :term, '%'))
               OR lower(d.model) LIKE lower(concat('%', :term, '%'))
               OR lower(d.deviceType.name) LIKE lower(concat('%', :term, '%')))
              AND r.receiveDateTime >= :from
              AND r.receiveDateTime <= :to
            """,
            countQuery = """
            SELECT COUNT(r)
            FROM Repair r
            LEFT JOIN r.client c
            LEFT JOIN r.device d
            WHERE (:term = ''
               OR lower(r.orderNumber) LIKE lower(concat('%', :term, '%'))
               OR lower(r.description) LIKE lower(concat('%', :term, '%'))
               OR lower(c.name) LIKE lower(concat('%', :term, '%'))
               OR lower(c.lastName) LIKE lower(concat('%', :term, '%'))
               OR lower(d.brand) LIKE lower(concat('%', :term, '%'))
               OR lower(d.model) LIKE lower(concat('%', :term, '%'))
               OR lower(d.deviceType.name) LIKE lower(concat('%', :term, '%')))
              AND r.receiveDateTime >= :from
              AND r.receiveDateTime <= :to
            """)
    Page<RepairListView> findPageBetween(
            @Param("term") String term,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

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
                   r.readyNotifiedAt AS readyNotifiedAt,
                   c.name AS clientName,
                   c.lastName AS clientLastName,
                   c.phone AS clientPhone,
                   d.deviceType.name AS deviceTypeName,
                   d.brand AS deviceBrand,
                   d.model AS deviceModel
            FROM Repair r
            LEFT JOIN r.client c
            LEFT JOIN r.device d
            ORDER BY r.creationDateTime DESC
            """)
    List<RepairListView> findLatestRows(Pageable pageable);

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
            SELECT r.id AS repairId,
                   r.orderNumber AS orderNumber,
                   c.name AS clientName,
                   c.lastName AS clientLastName,
                   c.phone AS clientPhone,
                   c.email AS clientEmail,
                   d.deviceType.name AS deviceTypeName,
                   d.brand AS deviceBrand,
                   d.model AS deviceModel,
                   d.serialNumber AS deviceSerialNumber,
                   r.description AS reportedIssue,
                   r.quoteNotes AS workPerformed,
                   r.price AS finalAmount
            FROM Repair r
            LEFT JOIN r.client c
            LEFT JOIN r.device d
            WHERE r.id = :repairId
            """)
    java.util.Optional<DeliveryReportSourceView> findDeliveryReportSourceById(@Param("repairId") String repairId);

    @Query("""
            SELECT r.id AS id,
                   c.name AS clientName,
                   c.lastName AS clientLastName,
                   r.status AS status,
                   r.receiveDateTime AS receiveDateTime,
                   r.returnDateTime AS returnDateTime,
                   r.price AS price,
                   r.laborAmount AS laborAmount,
                   r.quotedAmount AS quotedAmount,
                   COALESCE(SUM(COALESCE(p.cost, 0) * COALESCE(p.quantity, 1)), 0) AS partsCost,
                   COALESCE(SUM((COALESCE(p.salePrice, 0) - COALESCE(p.cost, 0)) * COALESCE(p.quantity, 1)), 0) AS partsProfit
            FROM Repair r
            LEFT JOIN r.client c
            LEFT JOIN r.parts p
            WHERE COALESCE(r.returnDateTime, r.receiveDateTime) >= :from
              AND COALESCE(r.returnDateTime, r.receiveDateTime) <= :to
            GROUP BY r.id, c.name, c.lastName, r.status, r.receiveDateTime, r.returnDateTime, r.price, r.laborAmount, r.quotedAmount
            ORDER BY COALESCE(r.returnDateTime, r.receiveDateTime) DESC
            """)
    List<FinanceRepairView> findFinanceRowsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT r.id AS id,
                   c.name AS clientName,
                   c.lastName AS clientLastName,
                   r.status AS status,
                   r.receiveDateTime AS receiveDateTime,
                   r.returnDateTime AS returnDateTime,
                   r.price AS price,
                   r.laborAmount AS laborAmount,
                   r.quotedAmount AS quotedAmount,
                   COALESCE(SUM(COALESCE(p.cost, 0) * COALESCE(p.quantity, 1)), 0) AS partsCost,
                   COALESCE(SUM((COALESCE(p.salePrice, 0) - COALESCE(p.cost, 0)) * COALESCE(p.quantity, 1)), 0) AS partsProfit
            FROM Repair r
            LEFT JOIN r.client c
            LEFT JOIN r.parts p
            WHERE COALESCE(r.returnDateTime, r.receiveDateTime) >= :from
            GROUP BY r.id, c.name, c.lastName, r.status, r.receiveDateTime, r.returnDateTime, r.price, r.laborAmount, r.quotedAmount
            ORDER BY COALESCE(r.returnDateTime, r.receiveDateTime) DESC
            """)
    List<FinanceRepairView> findFinanceRowsFrom(@Param("from") LocalDateTime from);

    @Query("""
            SELECT r.id AS id,
                   c.name AS clientName,
                   c.lastName AS clientLastName,
                   r.status AS status,
                   r.receiveDateTime AS receiveDateTime,
                   r.returnDateTime AS returnDateTime,
                   r.price AS price,
                   r.laborAmount AS laborAmount,
                   r.quotedAmount AS quotedAmount,
                   COALESCE(SUM(COALESCE(p.cost, 0) * COALESCE(p.quantity, 1)), 0) AS partsCost,
                   COALESCE(SUM((COALESCE(p.salePrice, 0) - COALESCE(p.cost, 0)) * COALESCE(p.quantity, 1)), 0) AS partsProfit
            FROM Repair r
            LEFT JOIN r.client c
            LEFT JOIN r.parts p
            WHERE COALESCE(r.returnDateTime, r.receiveDateTime) <= :to
            GROUP BY r.id, c.name, c.lastName, r.status, r.receiveDateTime, r.returnDateTime, r.price, r.laborAmount, r.quotedAmount
            ORDER BY COALESCE(r.returnDateTime, r.receiveDateTime) DESC
            """)
    List<FinanceRepairView> findFinanceRowsTo(@Param("to") LocalDateTime to);

    @Query("""
            SELECT r.id AS id,
                   c.name AS clientName,
                   c.lastName AS clientLastName,
                   r.status AS status,
                   r.receiveDateTime AS receiveDateTime,
                   r.returnDateTime AS returnDateTime,
                   r.price AS price,
                   r.laborAmount AS laborAmount,
                   r.quotedAmount AS quotedAmount,
                   COALESCE(SUM(COALESCE(p.cost, 0) * COALESCE(p.quantity, 1)), 0) AS partsCost,
                   COALESCE(SUM((COALESCE(p.salePrice, 0) - COALESCE(p.cost, 0)) * COALESCE(p.quantity, 1)), 0) AS partsProfit
            FROM Repair r
            LEFT JOIN r.client c
            LEFT JOIN r.parts p
            GROUP BY r.id, c.name, c.lastName, r.status, r.receiveDateTime, r.returnDateTime, r.price, r.laborAmount, r.quotedAmount
            ORDER BY COALESCE(r.returnDateTime, r.receiveDateTime) DESC
            """)
    List<FinanceRepairView> findFinanceRowsAll();

    List<Repair> findByStatusAndReturnDateTimeIsNotNullOrderByReturnDateTimeDesc(RepairStatusEnum status);

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
