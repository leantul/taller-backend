package com.taller.model.repository;

import com.taller.model.DeviceObservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DeviceObservationRepository extends JpaRepository<DeviceObservation, String> {
    List<DeviceObservation> findByDeviceIdOrderByObservedAtDesc(String deviceId);
    List<DeviceObservation> findByDeviceIdAndResolvedAtIsNullOrderByObservedAtDesc(String deviceId);
    List<DeviceObservation> findByDeviceIdInAndResolvedAtIsNullOrderByObservedAtDesc(Collection<String> deviceIds);
    List<DeviceObservation> findByRepairIdOrderByObservedAtDesc(String repairId);
    List<DeviceObservation> findByRepairId(String repairId);
    List<DeviceObservation> findByResolvedAtIsNullAndFollowUpAtLessThanEqualOrderByFollowUpAtAsc(LocalDateTime followUpAt);
    List<DeviceObservation> findByResolvedAtIsNullAndFollowUpAtBetweenOrderByFollowUpAtAsc(LocalDateTime from, LocalDateTime to);
    Optional<DeviceObservation> findByIdAndDeviceId(String id, String deviceId);
}
