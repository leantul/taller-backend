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
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RepairRepository extends JpaRepository<Repair, String> {
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
            WITH payment_totals AS (
              SELECT rp.repair_id, MAX(rp.payment_date) AS payment_date, SUM(rp.amount) AS income
              FROM repair_payments rp
              WHERE rp.payment_date >= COALESCE(:from, rp.payment_date)
                AND rp.payment_date <= COALESCE(:to, rp.payment_date)
              GROUP BY rp.repair_id
            ), first_payments AS (
              SELECT repair_id, MIN(payment_date) AS first_payment_date FROM repair_payments GROUP BY repair_id
            ), part_costs AS (
              SELECT repair_id, SUM(COALESCE(cost, 0) * COALESCE(quantity, 1)) AS parts_cost
              FROM repair_parts GROUP BY repair_id
            )
            SELECT r.id_repair AS "repairId",
                   CASE WHEN c.name IS NULL AND c.last_name IS NULL THEN '-'
                        ELSE trim(concat(COALESCE(c.name, ''), concat(' ', COALESCE(c.last_name, '')))) END AS "clientName",
                   pt.payment_date AS date, pt.income AS income,
                   CASE WHEN fp.first_payment_date >= COALESCE(:from, fp.first_payment_date)
                          AND fp.first_payment_date <= COALESCE(:to, fp.first_payment_date)
                        THEN COALESCE(pc.parts_cost, 0) ELSE 0 END AS "partsCost",
                   pt.income - CASE WHEN fp.first_payment_date >= COALESCE(:from, fp.first_payment_date)
                          AND fp.first_payment_date <= COALESCE(:to, fp.first_payment_date)
                        THEN COALESCE(pc.parts_cost, 0) ELSE 0 END AS net
            FROM payment_totals pt JOIN repairs r ON r.id_repair = pt.repair_id
            LEFT JOIN clients c ON c.id_client = r.id_client
            JOIN first_payments fp ON fp.repair_id = r.id_repair
            LEFT JOIN part_costs pc ON pc.repair_id = r.id_repair
            """,
            countQuery = """
            SELECT COUNT(DISTINCT rp.repair_id) FROM repair_payments rp
            WHERE rp.payment_date >= COALESCE(:from, rp.payment_date)
              AND rp.payment_date <= COALESCE(:to, rp.payment_date)
            """, nativeQuery = true)
    Page<FinanceRowView> findPaymentFinancePage(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    @Query(value = """
            WITH paid_repairs AS (
              SELECT DISTINCT repair_id FROM repair_payments
              WHERE payment_date >= COALESCE(:from, payment_date) AND payment_date <= COALESCE(:to, payment_date)
            )
            SELECT COUNT(*) AS "repairCount",
                   COALESCE((SELECT SUM(amount) FROM repair_payments WHERE payment_date >= COALESCE(:from, payment_date) AND payment_date <= COALESCE(:to, payment_date)), 0) AS "totalIncome",
                   COALESCE(SUM(COALESCE(r.labor_amount, 0)), 0) AS "totalLabor",
                   COALESCE(SUM(COALESCE(r.quoted_amount, 0)), 0) AS "totalQuoted",
                   COALESCE(SUM(CASE WHEN COALESCE(r.price, 0) = 0 THEN 1 ELSE 0 END), 0) AS "zeroFinalAmountCount",
                   COALESCE(SUM(CASE WHEN COALESCE(r.price, 0) > 0 THEN 1 ELSE 0 END), 0) AS "positiveFinalAmountCount"
            FROM paid_repairs pr JOIN repairs r ON r.id_repair = pr.repair_id
            """, nativeQuery = true)
    FinanceRepairSummaryView summarizePaymentFinanceRepairs(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = """
            WITH first_payments AS (SELECT repair_id, MIN(payment_date) AS payment_date FROM repair_payments GROUP BY repair_id)
            SELECT COALESCE(SUM(COALESCE(p.cost, 0) * COALESCE(p.quantity, 1)), 0) AS "totalPartsCost",
                   COALESCE(SUM((COALESCE(p.sale_price, 0) - COALESCE(p.cost, 0)) * COALESCE(p.quantity, 1)), 0) AS "totalPartsProfit"
            FROM first_payments fp JOIN repair_parts p ON p.repair_id = fp.repair_id
            WHERE fp.payment_date >= COALESCE(:from, fp.payment_date) AND fp.payment_date <= COALESCE(:to, fp.payment_date)
            """, nativeQuery = true)
    FinancePartsSummaryView summarizePaymentFinanceParts(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = """
            SELECT date_trunc('month', payment_date) AS month, COALESCE(SUM(amount), 0) AS value
            FROM repair_payments WHERE payment_date >= :from GROUP BY date_trunc('month', payment_date)
            """, nativeQuery = true)
    List<FinanceMonthlyView> summarizeMonthlyPaymentIncome(@Param("from") LocalDateTime from);

    @Query(value = """
            WITH first_payments AS (SELECT repair_id, MIN(payment_date) AS payment_date FROM repair_payments GROUP BY repair_id)
            SELECT date_trunc('month', fp.payment_date) AS month,
                   COALESCE(SUM(COALESCE(p.cost, 0) * COALESCE(p.quantity, 1)), 0) AS value
            FROM first_payments fp JOIN repair_parts p ON p.repair_id = fp.repair_id
            WHERE fp.payment_date >= :from GROUP BY date_trunc('month', fp.payment_date)
            """, nativeQuery = true)
    List<FinanceMonthlyView> summarizeMonthlyPaymentPartsCost(@Param("from") LocalDateTime from);

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
