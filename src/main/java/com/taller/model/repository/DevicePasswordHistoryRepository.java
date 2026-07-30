package com.taller.model.repository;

import com.taller.model.DevicePasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DevicePasswordHistoryRepository extends JpaRepository<DevicePasswordHistory, String> {
    List<DevicePasswordHistory> findByDeviceIdOrderByCreationDateTimeDesc(String deviceId);
    Optional<DevicePasswordHistory> findByIdAndDeviceId(String id, String deviceId);
    Optional<DevicePasswordHistory> findFirstByDeviceIdAndIsCurrentTrue(String deviceId);
    long countByDeviceId(String deviceId);

    @Modifying
    @Query("UPDATE DevicePasswordHistory h SET h.isCurrent = false WHERE h.deviceId = ?1 AND h.isCurrent = true")
    int clearCurrentByDeviceId(String deviceId);

    @Modifying
    @Query(value = """
            INSERT INTO device_password_history (
                id_device_password_history, device_id, password_value, is_current,
                creation_date_time, modification_datetime
            )
            SELECT CAST(gen_random_uuid() AS varchar), d.id_device, d.password, true,
                   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
            FROM devices d
            WHERE d.password IS NOT NULL
              AND btrim(d.password) <> ''
              AND NOT EXISTS (
                  SELECT 1 FROM device_password_history h WHERE h.device_id = d.id_device
              )
            """, nativeQuery = true)
    int bootstrapLegacyPasswords();

    @Modifying
    @Query(value = """
            WITH ranked AS (
                SELECT id_device_password_history,
                       row_number() OVER (
                           PARTITION BY device_id
                           ORDER BY creation_date_time DESC, id_device_password_history DESC
                       ) AS position
                FROM device_password_history
                WHERE is_current = true
            )
            UPDATE device_password_history
               SET is_current = false,
                   modification_datetime = CURRENT_TIMESTAMP
             WHERE id_device_password_history IN (
                 SELECT id_device_password_history FROM ranked WHERE position > 1
             )
            """, nativeQuery = true)
    int normalizeCurrentPasswords();

    @Modifying
    @Query(value = """
            CREATE UNIQUE INDEX IF NOT EXISTS ux_device_password_history_current
                ON device_password_history(device_id)
                WHERE is_current = true
            """, nativeQuery = true)
    void ensureSingleCurrentPasswordIndex();
}
