package com.taller.model.repository;

import com.taller.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, String> {
    @Query("""
            SELECT d FROM Device d
            WHERE lower(d.brand) LIKE lower(concat('%', ?1, '%'))
               OR lower(d.model) LIKE lower(concat('%', ?1, '%'))
               OR lower(d.serialNumber) LIKE lower(concat('%', ?1, '%'))
            """)
    List<Device> search(String term);
}
