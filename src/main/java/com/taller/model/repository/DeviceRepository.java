package com.taller.model.repository;

import com.taller.model.Device;
import com.taller.model.repository.projection.DeviceBasicView;
import com.taller.model.repository.projection.DeviceListView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeviceRepository extends JpaRepository<Device, String> {
    @Query("""
            SELECT d.id AS id,
                   d.brand AS brand,
                   d.model AS model,
                   d.serialNumber AS serialNumber,
                   d.deviceTypeId AS deviceTypeId,
                   d.deviceType.name AS deviceTypeName,
                   d.clientId AS clientId,
                   c.name AS clientName,
                   c.lastName AS clientLastName,
                   (SELECT h.passwordValue FROM DevicePasswordHistory h WHERE h.deviceId = d.id AND h.isCurrent = true) AS currentPassword
            FROM Device d
            LEFT JOIN d.client c
            ORDER BY d.creationDateTime DESC
            """)
    List<DeviceListView> findListRows();

    @Query("""
            SELECT d.id AS id,
                   d.brand AS brand,
                   d.model AS model,
                   d.serialNumber AS serialNumber,
                   d.deviceTypeId AS deviceTypeId,
                   d.deviceType.name AS deviceTypeName,
                   d.clientId AS clientId,
                   c.name AS clientName,
                   c.lastName AS clientLastName,
                   (SELECT h.passwordValue FROM DevicePasswordHistory h WHERE h.deviceId = d.id AND h.isCurrent = true) AS currentPassword
            FROM Device d
            LEFT JOIN d.client c
            WHERE d.clientId = ?1
            ORDER BY d.creationDateTime DESC
            """)
    List<DeviceListView> findListRowsByClientId(String clientId);

    @Query(value = """
            SELECT d.id AS id,
                   d.brand AS brand,
                   d.model AS model,
                   d.serialNumber AS serialNumber,
                   d.deviceTypeId AS deviceTypeId,
                   d.deviceType.name AS deviceTypeName,
                   d.clientId AS clientId,
                   c.name AS clientName,
                   c.lastName AS clientLastName,
                   (SELECT h.passwordValue FROM DevicePasswordHistory h WHERE h.deviceId = d.id AND h.isCurrent = true) AS currentPassword
            FROM Device d
            LEFT JOIN d.client c
            WHERE (:term = ''
               OR lower(d.brand) LIKE lower(concat('%', :term, '%'))
               OR lower(d.model) LIKE lower(concat('%', :term, '%'))
               OR lower(d.serialNumber) LIKE lower(concat('%', :term, '%'))
               OR lower(d.clientId) LIKE lower(concat('%', :term, '%'))
               OR lower(d.deviceType.name) LIKE lower(concat('%', :term, '%'))
               OR lower(c.name) LIKE lower(concat('%', :term, '%'))
               OR lower(c.lastName) LIKE lower(concat('%', :term, '%'))
               OR EXISTS (
                    SELECT 1 FROM DeviceObservation o
                    WHERE o.deviceId = d.id
                      AND o.resolvedAt IS NULL
                      AND lower(o.note) LIKE lower(concat('%', :term, '%'))
               ))
              AND (:clientId = '' OR d.clientId = :clientId)
              AND (:clientTerm = ''
               OR lower(c.name) LIKE lower(concat('%', :clientTerm, '%'))
               OR lower(c.lastName) LIKE lower(concat('%', :clientTerm, '%')))
            ORDER BY
              CASE WHEN :sortBy = 'deviceType' AND :sortDir = 'asc' THEN lower(d.deviceType.name) END ASC,
              CASE WHEN :sortBy = 'deviceType' AND :sortDir = 'desc' THEN lower(d.deviceType.name) END DESC,
              CASE WHEN :sortBy = 'brand' AND :sortDir = 'asc' THEN lower(d.brand) END ASC,
              CASE WHEN :sortBy = 'brand' AND :sortDir = 'desc' THEN lower(d.brand) END DESC,
              CASE WHEN :sortBy = 'model' AND :sortDir = 'asc' THEN lower(d.model) END ASC,
              CASE WHEN :sortBy = 'model' AND :sortDir = 'desc' THEN lower(d.model) END DESC,
              CASE WHEN :sortBy = 'client' AND :sortDir = 'asc' THEN lower(concat(c.name, ' ', c.lastName)) END ASC,
              CASE WHEN :sortBy = 'client' AND :sortDir = 'desc' THEN lower(concat(c.name, ' ', c.lastName)) END DESC,
              CASE WHEN :sortBy = 'observations' AND :sortDir = 'asc' THEN (
                  SELECT COUNT(o.id) FROM DeviceObservation o WHERE o.deviceId = d.id AND o.resolvedAt IS NULL
              ) END ASC,
              CASE WHEN :sortBy = 'observations' AND :sortDir = 'desc' THEN (
                  SELECT COUNT(o.id) FROM DeviceObservation o WHERE o.deviceId = d.id AND o.resolvedAt IS NULL
              ) END DESC,
              CASE WHEN :sortBy = 'password' AND :sortDir = 'asc' THEN (
                  SELECT h.passwordValue FROM DevicePasswordHistory h WHERE h.deviceId = d.id AND h.isCurrent = true
              ) END ASC,
              CASE WHEN :sortBy = 'password' AND :sortDir = 'desc' THEN (
                  SELECT h.passwordValue FROM DevicePasswordHistory h WHERE h.deviceId = d.id AND h.isCurrent = true
              ) END DESC,
              d.creationDateTime DESC
            """,
            countQuery = """
            SELECT COUNT(d)
            FROM Device d
            LEFT JOIN d.client c
            WHERE (:term = ''
               OR lower(d.brand) LIKE lower(concat('%', :term, '%'))
               OR lower(d.model) LIKE lower(concat('%', :term, '%'))
               OR lower(d.serialNumber) LIKE lower(concat('%', :term, '%'))
               OR lower(d.clientId) LIKE lower(concat('%', :term, '%'))
               OR lower(d.deviceType.name) LIKE lower(concat('%', :term, '%'))
               OR lower(c.name) LIKE lower(concat('%', :term, '%'))
               OR lower(c.lastName) LIKE lower(concat('%', :term, '%'))
               OR EXISTS (
                    SELECT 1 FROM DeviceObservation o
                    WHERE o.deviceId = d.id
                      AND o.resolvedAt IS NULL
                      AND lower(o.note) LIKE lower(concat('%', :term, '%'))
               ))
              AND (:clientId = '' OR d.clientId = :clientId)
              AND (:clientTerm = ''
               OR lower(c.name) LIKE lower(concat('%', :clientTerm, '%'))
               OR lower(c.lastName) LIKE lower(concat('%', :clientTerm, '%')))
            """)
    Page<DeviceListView> findPage(
            @Param("term") String term,
            @Param("clientId") String clientId,
            @Param("clientTerm") String clientTerm,
            @Param("sortBy") String sortBy,
            @Param("sortDir") String sortDir,
            Pageable pageable);

    @Query("""
            SELECT d.id AS id,
                   d.brand AS brand,
                   d.model AS model,
                   d.serialNumber AS serialNumber,
                   d.deviceTypeId AS deviceTypeId,
                   d.deviceType.name AS deviceTypeName,
                   d.clientId AS clientId,
                   c.name AS clientName,
                   c.lastName AS clientLastName,
                   (SELECT h.passwordValue FROM DevicePasswordHistory h WHERE h.deviceId = d.id AND h.isCurrent = true) AS currentPassword
            FROM Device d
            LEFT JOIN d.client c
            WHERE lower(d.brand) LIKE lower(concat('%', ?1, '%'))
               OR lower(d.model) LIKE lower(concat('%', ?1, '%'))
               OR lower(d.serialNumber) LIKE lower(concat('%', ?1, '%'))
               OR lower(d.clientId) LIKE lower(concat('%', ?1, '%'))
               OR lower(d.deviceType.name) LIKE lower(concat('%', ?1, '%'))
            ORDER BY d.creationDateTime DESC
            """)
    List<DeviceListView> searchListRows(String term);

    @Query("""
            SELECT d.id AS id,
                   d.brand AS brand,
                   d.model AS model,
                   d.serialNumber AS serialNumber,
                   d.deviceTypeId AS deviceTypeId,
                   d.deviceType.name AS deviceTypeName,
                   d.clientId AS clientId
            FROM Device d
            ORDER BY d.creationDateTime DESC
            """)
    List<DeviceBasicView> findBasicLatest(Pageable pageable);

    @Query("""
            SELECT d.id AS id,
                   d.brand AS brand,
                   d.model AS model,
                   d.serialNumber AS serialNumber,
                   d.deviceTypeId AS deviceTypeId,
                   d.deviceType.name AS deviceTypeName,
                   d.clientId AS clientId
            FROM Device d
            WHERE d.id IN ?1
            """)
    List<DeviceBasicView> findBasicByIdIn(Collection<String> ids);

    @Query("""
            SELECT d.deviceType.name AS deviceTypeName, COUNT(d) AS total
            FROM Device d
            GROUP BY d.deviceType.name
            """)
    List<com.taller.model.repository.projection.DeviceTypeCountView> countByDeviceType();
}
