package com.taller.model.repository;

import com.taller.model.Repair;
import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.repository.projection.DeviceLastRepairView;
import com.taller.model.repository.projection.DeviceRepairHistoryView;
import com.taller.model.repository.projection.DashboardCountsView;
import com.taller.model.repository.projection.ClientRepairHistoryView;
import com.taller.model.repository.projection.DeliveryReportSourceView;
import com.taller.model.repository.projection.FinanceRepairView;
import com.taller.model.repository.projection.FinanceMonthlyView;
import com.taller.model.repository.projection.FinancePaymentSummaryView;
import com.taller.model.repository.projection.FinancePartsSummaryView;
import com.taller.model.repository.projection.FinanceRepairSummaryView;
import com.taller.model.repository.projection.FinanceRowView;
import com.taller.model.repository.projection.RepairListView;
import com.taller.model.repository.projection.RepairStatusCountView;
import com.taller.model.repository.projection.StatusBoardRepairView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RepairRepository extends JpaRepository<Repair, String> {
    List<Repair> findByStatusInAndReturnDateTimeIsNotNullOrderByReturnDateTimeDesc(Collection<RepairStatusEnum> statuses);
    @Query("SELECT r FROM Repair r WHERE r.status = com.taller.model.enums.RepairStatusEnum.RETIRADA AND NOT EXISTS (SELECT p.id FROM RepairPayment p WHERE p.repairId = r.id)")
    List<Repair> findLegacyRetiredWithoutPayments();
    @Query(value = """
            SELECT (SELECT COUNT(*) FROM clients) AS "clientCount",
                   (SELECT COUNT(*) FROM devices) AS "deviceCount",
                   (SELECT COUNT(*) FROM repairs) AS "repairCount"
            """, nativeQuery = true)
    DashboardCountsView dashboardCounts();
    String REPAIR_PAGE_SELECT = """
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
            """;
    String REPAIR_PAGE_COUNT = """
            SELECT COUNT(r)
            FROM Repair r
            LEFT JOIN r.client c
            LEFT JOIN r.device d
            """;
    String REPAIR_PAGE_TERM_FILTER = """
            (:term = ''
               OR lower(r.orderNumber) LIKE lower(concat('%', :term, '%'))
               OR lower(r.description) LIKE lower(concat('%', :term, '%'))
               OR lower(c.name) LIKE lower(concat('%', :term, '%'))
               OR lower(c.lastName) LIKE lower(concat('%', :term, '%'))
               OR lower(d.brand) LIKE lower(concat('%', :term, '%'))
               OR lower(d.model) LIKE lower(concat('%', :term, '%'))
               OR lower(d.deviceType.name) LIKE lower(concat('%', :term, '%')))
            """;

    @Query(value = """
            SELECT r.id AS repairId,
                   CASE
                       WHEN c.name IS NULL AND c.lastName IS NULL THEN '-'
                       ELSE trim(concat(coalesce(c.name, ''), concat(' ', coalesce(c.lastName, ''))))
                   END AS clientName,
                   COALESCE(r.returnDateTime, r.receiveDateTime) AS date,
                   COALESCE(r.price, 0) AS income,
                   COALESCE(SUM(COALESCE(p.cost, 0) * COALESCE(p.quantity, 1)), 0) AS partsCost,
                   COALESCE(r.price, 0) - COALESCE(SUM(COALESCE(p.cost, 0) * COALESCE(p.quantity, 1)), 0) AS net
            FROM Repair r
            LEFT JOIN r.client c
            LEFT JOIN r.parts p
            WHERE r.status = com.taller.model.enums.RepairStatusEnum.RETIRADA
              AND COALESCE(r.returnDateTime, r.receiveDateTime) >= COALESCE(:from, COALESCE(r.returnDateTime, r.receiveDateTime))
              AND COALESCE(r.returnDateTime, r.receiveDateTime) <= COALESCE(:to, COALESCE(r.returnDateTime, r.receiveDateTime))
            GROUP BY r.id, c.name, c.lastName, r.returnDateTime, r.receiveDateTime, r.price
            """,
            countQuery = """
            SELECT COUNT(r)
            FROM Repair r
            WHERE r.status = com.taller.model.enums.RepairStatusEnum.RETIRADA
              AND COALESCE(r.returnDateTime, r.receiveDateTime) >= COALESCE(:from, COALESCE(r.returnDateTime, r.receiveDateTime))
              AND COALESCE(r.returnDateTime, r.receiveDateTime) <= COALESCE(:to, COALESCE(r.returnDateTime, r.receiveDateTime))
            """)
    Page<FinanceRowView> findFinancePage(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    @Query("""
            SELECT COUNT(r) AS repairCount,
                   COALESCE(SUM(COALESCE(r.price, 0)), 0) AS totalIncome,
                   COALESCE(SUM(COALESCE(r.laborAmount, 0)), 0) AS totalLabor,
                   COALESCE(SUM(COALESCE(r.quotedAmount, 0)), 0) AS totalQuoted,
                   COALESCE(SUM(CASE WHEN COALESCE(r.price, 0) = 0 THEN 1 ELSE 0 END), 0) AS zeroFinalAmountCount,
                   COALESCE(SUM(CASE WHEN COALESCE(r.price, 0) > 0 THEN 1 ELSE 0 END), 0) AS positiveFinalAmountCount
            FROM Repair r
            WHERE r.status = com.taller.model.enums.RepairStatusEnum.RETIRADA
              AND COALESCE(r.returnDateTime, r.receiveDateTime) >= COALESCE(:from, COALESCE(r.returnDateTime, r.receiveDateTime))
              AND COALESCE(r.returnDateTime, r.receiveDateTime) <= COALESCE(:to, COALESCE(r.returnDateTime, r.receiveDateTime))
            """)
    FinanceRepairSummaryView summarizeFinanceRepairs(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(SUM(COALESCE(p.cost, 0) * COALESCE(p.quantity, 1)), 0) AS totalPartsCost,
                   COALESCE(SUM((COALESCE(p.salePrice, 0) - COALESCE(p.cost, 0)) * COALESCE(p.quantity, 1)), 0) AS totalPartsProfit
            FROM RepairPart p
            JOIN p.repair r
            WHERE r.status = com.taller.model.enums.RepairStatusEnum.RETIRADA
              AND COALESCE(r.returnDateTime, r.receiveDateTime) >= COALESCE(:from, COALESCE(r.returnDateTime, r.receiveDateTime))
              AND COALESCE(r.returnDateTime, r.receiveDateTime) <= COALESCE(:to, COALESCE(r.returnDateTime, r.receiveDateTime))
            """)
    FinancePartsSummaryView summarizeFinanceParts(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("""
            SELECT FUNCTION('date_trunc', 'month', COALESCE(r.returnDateTime, r.receiveDateTime)) AS month,
                   COALESCE(SUM(COALESCE(r.price, 0)), 0) AS value
            FROM Repair r
            WHERE r.status = com.taller.model.enums.RepairStatusEnum.RETIRADA
              AND COALESCE(r.returnDateTime, r.receiveDateTime) >= :from
            GROUP BY FUNCTION('date_trunc', 'month', COALESCE(r.returnDateTime, r.receiveDateTime))
            """)
    List<FinanceMonthlyView> summarizeMonthlyFinanceIncome(@Param("from") LocalDateTime from);

    @Query("""
            SELECT FUNCTION('date_trunc', 'month', COALESCE(r.returnDateTime, r.receiveDateTime)) AS month,
                   COALESCE(SUM(COALESCE(p.cost, 0) * COALESCE(p.quantity, 1)), 0) AS value
            FROM RepairPart p
            JOIN p.repair r
            WHERE r.status = com.taller.model.enums.RepairStatusEnum.RETIRADA
              AND COALESCE(r.returnDateTime, r.receiveDateTime) >= :from
            GROUP BY FUNCTION('date_trunc', 'month', COALESCE(r.returnDateTime, r.receiveDateTime))
            """)
    List<FinanceMonthlyView> summarizeMonthlyFinancePartsCost(@Param("from") LocalDateTime from);

    @Query(value = """
            SELECT r.id AS repairId,
                   CASE WHEN c.name IS NULL AND c.lastName IS NULL THEN '-'
                        ELSE trim(concat(COALESCE(c.name, ''), concat(' ', COALESCE(c.lastName, '')))) END AS clientName,
                   CASE
                       WHEN MAX(CASE WHEN payment.paymentDate >= COALESCE(:from, payment.paymentDate)
                                          AND payment.paymentDate <= COALESCE(:to, payment.paymentDate)
                                     THEN payment.paymentDate ELSE NULL END) IS NULL THEN r.returnDateTime
                       WHEN r.returnDateTime IS NULL OR r.returnDateTime < COALESCE(:from, r.returnDateTime)
                            OR r.returnDateTime > COALESCE(:to, r.returnDateTime)
                            OR MAX(CASE WHEN payment.paymentDate >= COALESCE(:from, payment.paymentDate)
                                             AND payment.paymentDate <= COALESCE(:to, payment.paymentDate)
                                        THEN payment.paymentDate ELSE NULL END) >= r.returnDateTime
                           THEN MAX(CASE WHEN payment.paymentDate >= COALESCE(:from, payment.paymentDate)
                                              AND payment.paymentDate <= COALESCE(:to, payment.paymentDate)
                                         THEN payment.paymentDate ELSE NULL END)
                       ELSE r.returnDateTime
                   END AS date,
                   COALESCE(SUM(CASE WHEN payment.paymentDate <= COALESCE(:to, payment.paymentDate)
                                     THEN COALESCE(payment.amount, 0) ELSE 0 END), 0) AS income,
                   (SELECT COALESCE(SUM(COALESCE(part.cost, 0) * COALESCE(part.quantity, 1)), 0)
                    FROM RepairPart part WHERE part.repairId = r.id) AS partsCost,
                   (SELECT COALESCE(SUM(COALESCE(part.salePrice, 0) * COALESCE(part.quantity, 1)), 0)
                    FROM RepairPart part WHERE part.repairId = r.id) AS partsSale,
                   CASE WHEN (SELECT COALESCE(SUM(COALESCE(part.salePrice, 0) * COALESCE(part.quantity, 1)), 0)
                              FROM RepairPart part WHERE part.repairId = r.id) > 0
                                  AND COALESCE(SUM(CASE WHEN payment.paymentDate <= COALESCE(:to, payment.paymentDate)
                                               THEN COALESCE(payment.amount, 0) ELSE 0 END), 0)
                                  >= (SELECT COALESCE(SUM(COALESCE(part.salePrice, 0) * COALESCE(part.quantity, 1)), 0)
                                      FROM RepairPart part WHERE part.repairId = r.id)
                        THEN (SELECT COALESCE(SUM(COALESCE(part.salePrice, 0) * COALESCE(part.quantity, 1)), 0)
                              FROM RepairPart part WHERE part.repairId = r.id)
                        ELSE (SELECT COALESCE(SUM(COALESCE(part.cost, 0) * COALESCE(part.quantity, 1)), 0)
                              FROM RepairPart part WHERE part.repairId = r.id)
                   END AS partsAmount,
                   COALESCE(SUM(CASE WHEN payment.paymentDate <= COALESCE(:to, payment.paymentDate)
                                     THEN COALESCE(payment.amount, 0) ELSE 0 END), 0)
                   - (SELECT COALESCE(SUM(COALESCE(part.cost, 0) * COALESCE(part.quantity, 1)), 0)
                      FROM RepairPart part WHERE part.repairId = r.id) AS net
            FROM Repair r
            LEFT JOIN r.client c
            LEFT JOIN RepairPayment payment ON payment.repairId = r.id
            GROUP BY r.id, c.name, c.lastName, r.returnDateTime
            HAVING MAX(CASE WHEN payment.paymentDate >= COALESCE(:from, payment.paymentDate)
                                 AND payment.paymentDate <= COALESCE(:to, payment.paymentDate)
                            THEN payment.paymentDate ELSE NULL END) IS NOT NULL
                OR (r.returnDateTime >= COALESCE(:from, r.returnDateTime)
                    AND r.returnDateTime <= COALESCE(:to, r.returnDateTime))
            """,
            countQuery = """
            SELECT COUNT(r) FROM Repair r
            WHERE EXISTS (SELECT payment.id FROM RepairPayment payment
                          WHERE payment.repairId = r.id
                            AND payment.paymentDate >= COALESCE(:from, payment.paymentDate)
                            AND payment.paymentDate <= COALESCE(:to, payment.paymentDate))
               OR (r.returnDateTime >= COALESCE(:from, r.returnDateTime)
                   AND r.returnDateTime <= COALESCE(:to, r.returnDateTime))
            """)
    Page<FinanceRowView> findFinanceActivityPage(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    @Query("""
            SELECT COUNT(r) AS repairCount, 0 AS totalIncome,
                   COALESCE(SUM(COALESCE(r.laborAmount, 0)), 0) AS totalLabor,
                   COALESCE(SUM(COALESCE(r.quotedAmount, 0)), 0) AS totalQuoted,
                   COALESCE(SUM(CASE WHEN COALESCE(r.price, 0) = 0 THEN 1 ELSE 0 END), 0) AS zeroFinalAmountCount,
                   0 AS positiveFinalAmountCount
            FROM Repair r
            WHERE (r.status = com.taller.model.enums.RepairStatusEnum.RETIRADA
                   OR r.status = com.taller.model.enums.RepairStatusEnum.RETIRADA_FALTA_COBRAR)
              AND r.returnDateTime >= COALESCE(:from, r.returnDateTime)
              AND r.returnDateTime <= COALESCE(:to, r.returnDateTime)
            """)
    FinanceRepairSummaryView summarizeRetiredFinanceRepairs(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COUNT(r)
            FROM Repair r
            WHERE EXISTS (SELECT payment.id FROM RepairPayment payment
                          WHERE payment.repairId = r.id
                            AND payment.paymentDate >= COALESCE(:from, payment.paymentDate)
                            AND payment.paymentDate <= COALESCE(:to, payment.paymentDate))
               OR (r.returnDateTime >= COALESCE(:from, r.returnDateTime)
                   AND r.returnDateTime <= COALESCE(:to, r.returnDateTime))
            """)
    Long countFinanceActivityRepairs(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COUNT(DISTINCT payment.repairId) AS repairCount,
                   COALESCE(SUM(payment.amount), 0) AS totalIncome
            FROM RepairPayment payment
            WHERE payment.paymentDate >= COALESCE(:from, payment.paymentDate)
              AND payment.paymentDate <= COALESCE(:to, payment.paymentDate)
            """)
    FinancePaymentSummaryView summarizeFinancePayments(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(SUM(COALESCE(part.cost, 0) * COALESCE(part.quantity, 1)), 0) AS totalPartsCost,
                   COALESCE(SUM((COALESCE(part.salePrice, 0) - COALESCE(part.cost, 0)) * COALESCE(part.quantity, 1)), 0) AS totalPartsProfit
            FROM RepairPart part JOIN part.repair r
            WHERE (((SELECT MIN(payment.paymentDate) FROM RepairPayment payment WHERE payment.repairId = r.id)
                         >= COALESCE(:from, (SELECT MIN(payment.paymentDate) FROM RepairPayment payment WHERE payment.repairId = r.id))
                     AND (SELECT MIN(payment.paymentDate) FROM RepairPayment payment WHERE payment.repairId = r.id)
                         <= COALESCE(:to, (SELECT MIN(payment.paymentDate) FROM RepairPayment payment WHERE payment.repairId = r.id))
                     AND (r.returnDateTime IS NULL OR (SELECT MIN(payment.paymentDate) FROM RepairPayment payment WHERE payment.repairId = r.id) <= r.returnDateTime))
                    OR (r.returnDateTime >= COALESCE(:from, r.returnDateTime)
                        AND r.returnDateTime <= COALESCE(:to, r.returnDateTime)
                        AND ((SELECT MIN(payment.paymentDate) FROM RepairPayment payment WHERE payment.repairId = r.id) IS NULL
                             OR r.returnDateTime < (SELECT MIN(payment.paymentDate) FROM RepairPayment payment WHERE payment.repairId = r.id))))
            """)
    FinancePartsSummaryView summarizeRecognizedFinanceParts(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(SUM(payment.amount), 0)
            FROM RepairPayment payment
            WHERE payment.paymentDate < :cutoff
            """)
    BigDecimal sumPaymentIncomeBefore(@Param("cutoff") LocalDateTime cutoff);

    @Query("""
            SELECT COALESCE(SUM(COALESCE(part.cost, 0) * COALESCE(part.quantity, 1)), 0) AS totalPartsCost,
                   COALESCE(SUM((COALESCE(part.salePrice, 0) - COALESCE(part.cost, 0)) * COALESCE(part.quantity, 1)), 0) AS totalPartsProfit
            FROM RepairPart part JOIN part.repair r
            WHERE EXISTS (SELECT payment.id FROM RepairPayment payment WHERE payment.repairId = r.id AND payment.paymentDate < :cutoff)
               OR r.returnDateTime < :cutoff
            """)
    FinancePartsSummaryView summarizeRecognizedFinancePartsBefore(@Param("cutoff") LocalDateTime cutoff);

    @Query("""
            SELECT COALESCE(SUM(payment.amount), 0)
            FROM RepairPayment payment
            WHERE payment.paymentDate >= :from AND payment.paymentDate < :to
            """)
    BigDecimal sumFinancePaymentIncomeBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(SUM(COALESCE(part.cost, 0) * COALESCE(part.quantity, 1)), 0)
            FROM RepairPart part JOIN part.repair r
            WHERE (((SELECT MIN(payment.paymentDate) FROM RepairPayment payment WHERE payment.repairId = r.id) >= :from
                     AND (SELECT MIN(payment.paymentDate) FROM RepairPayment payment WHERE payment.repairId = r.id) < :to
                     AND (r.returnDateTime IS NULL OR (SELECT MIN(payment.paymentDate) FROM RepairPayment payment WHERE payment.repairId = r.id) <= r.returnDateTime))
                    OR (r.returnDateTime >= :from AND r.returnDateTime < :to
                        AND ((SELECT MIN(payment.paymentDate) FROM RepairPayment payment WHERE payment.repairId = r.id) IS NULL
                             OR r.returnDateTime < (SELECT MIN(payment.paymentDate) FROM RepairPayment payment WHERE payment.repairId = r.id))))
            """)
    BigDecimal sumRecognizedPartsCostBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

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
            SELECT r.id AS id, r.idDevice AS idDevice, r.idClient AS idClient,
                   r.orderNumber AS orderNumber, r.description AS description, r.status AS status,
                   r.receiveDateTime AS receiveDateTime, r.returnDateTime AS returnDateTime,
                   r.price AS price, r.quotedAmount AS quotedAmount,
                   c.name AS clientName, c.lastName AS clientLastName,
                   d.deviceType.name AS deviceTypeName, d.brand AS deviceBrand, d.model AS deviceModel
            FROM Repair r LEFT JOIN r.client c LEFT JOIN r.device d
            WHERE r.status = :status
            """,
            countQuery = "SELECT COUNT(r) FROM Repair r WHERE r.status = :status")
    Page<StatusBoardRepairView> findStatusBoardPage(@Param("status") RepairStatusEnum status, Pageable pageable);

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

    @Query(value = """
            SELECT r.id AS id,
                   r.orderNumber AS orderNumber,
                   r.status AS status,
                   r.description AS description,
                   r.receiveDateTime AS receiveDateTime,
                   r.returnDateTime AS returnDateTime
            FROM Repair r
            WHERE r.idDevice = :deviceId
            ORDER BY CASE WHEN r.receiveDateTime IS NULL THEN 1 ELSE 0 END,
                     r.receiveDateTime DESC,
                     r.orderNumber DESC
            """,
            countQuery = "SELECT COUNT(r) FROM Repair r WHERE r.idDevice = :deviceId")
    Page<DeviceRepairHistoryView> findDeviceHistory(@Param("deviceId") String deviceId, Pageable pageable);

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

    @Query(
            value = REPAIR_PAGE_SELECT
                    + "WHERE " + REPAIR_PAGE_TERM_FILTER
                    + "  AND r.status = :status",
            countQuery = REPAIR_PAGE_COUNT
                    + "WHERE " + REPAIR_PAGE_TERM_FILTER
                    + "  AND r.status = :status")
    Page<RepairListView> findPageByStatus(
            @Param("term") String term,
            @Param("status") RepairStatusEnum status,
            Pageable pageable);

    @Query(
            value = REPAIR_PAGE_SELECT
                    + "WHERE " + REPAIR_PAGE_TERM_FILTER
                    + "  AND r.status = :status"
                    + "  AND r.receiveDateTime >= :from",
            countQuery = REPAIR_PAGE_COUNT
                    + "WHERE " + REPAIR_PAGE_TERM_FILTER
                    + "  AND r.status = :status"
                    + "  AND r.receiveDateTime >= :from")
    Page<RepairListView> findPageByStatusFrom(
            @Param("term") String term,
            @Param("status") RepairStatusEnum status,
            @Param("from") LocalDateTime from,
            Pageable pageable);

    @Query(
            value = REPAIR_PAGE_SELECT
                    + "WHERE " + REPAIR_PAGE_TERM_FILTER
                    + "  AND r.status = :status"
                    + "  AND r.receiveDateTime <= :to",
            countQuery = REPAIR_PAGE_COUNT
                    + "WHERE " + REPAIR_PAGE_TERM_FILTER
                    + "  AND r.status = :status"
                    + "  AND r.receiveDateTime <= :to")
    Page<RepairListView> findPageByStatusTo(
            @Param("term") String term,
            @Param("status") RepairStatusEnum status,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    @Query(
            value = REPAIR_PAGE_SELECT
                    + "WHERE " + REPAIR_PAGE_TERM_FILTER
                    + "  AND r.status = :status"
                    + "  AND r.receiveDateTime >= :from"
                    + "  AND r.receiveDateTime <= :to",
            countQuery = REPAIR_PAGE_COUNT
                    + "WHERE " + REPAIR_PAGE_TERM_FILTER
                    + "  AND r.status = :status"
                    + "  AND r.receiveDateTime >= :from"
                    + "  AND r.receiveDateTime <= :to")
    Page<RepairListView> findPageByStatusBetween(
            @Param("term") String term,
            @Param("status") RepairStatusEnum status,
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
                   r.readyNotifiedAt AS readyNotifiedAt,
                   c.name AS clientName,
                   c.lastName AS clientLastName
            FROM Repair r
            LEFT JOIN r.client c
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
            SELECT r.idDevice AS deviceId,
                   MAX(r.receiveDateTime) AS lastRepairDate,
                   dt.name AS deviceTypeName,
                   d.brand AS deviceBrand,
                   d.model AS deviceModel,
                   c.name AS clientName,
                   c.lastName AS clientLastName
            FROM Repair r
            JOIN r.device d
            LEFT JOIN d.deviceType dt
            LEFT JOIN d.client c
            WHERE r.idDevice IS NOT NULL
              AND r.receiveDateTime IS NOT NULL
            GROUP BY r.idDevice, dt.name, d.brand, d.model, c.name, c.lastName
            ORDER BY MAX(r.receiveDateTime) ASC
            """)
    List<DeviceLastRepairView> findOldestLastRepairByDevice(Pageable pageable);

    @Query(value = "SELECT nextval('repair_order_seq')", nativeQuery = true)
    Long nextOrderValue();
}
