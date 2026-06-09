package com.taller.model.repository;

import com.taller.model.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceTypeRepository extends JpaRepository<DeviceType, String> {
    List<DeviceType> findAllByOrderByNameAsc();
}
