package com.taller.model.repository;

import com.taller.model.DevicePasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DevicePasswordHistoryRepository extends JpaRepository<DevicePasswordHistory, String> {
    List<DevicePasswordHistory> findByDeviceIdOrderByCreationDateTimeDesc(String deviceId);
    List<DevicePasswordHistory> findByDeviceIdIn(Collection<String> deviceIds);
    Optional<DevicePasswordHistory> findByIdAndDeviceId(String id, String deviceId);
    Optional<DevicePasswordHistory> findFirstByDeviceIdAndIsCurrentTrue(String deviceId);
    long countByDeviceId(String deviceId);
}
