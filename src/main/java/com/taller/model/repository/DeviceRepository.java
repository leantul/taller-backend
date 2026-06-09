package com.taller.model.repository;

import com.taller.model.Device;
import com.taller.model.repository.projection.DeviceBasicView;
import com.taller.model.repository.projection.DeviceListView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
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
                   (SELECT h.passwordValue FROM DevicePasswordHistory h WHERE h.deviceId = d.id AND h.isCurrent = true) AS currentPassword
            FROM Device d
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
                   (SELECT h.passwordValue FROM DevicePasswordHistory h WHERE h.deviceId = d.id AND h.isCurrent = true) AS currentPassword
            FROM Device d
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
