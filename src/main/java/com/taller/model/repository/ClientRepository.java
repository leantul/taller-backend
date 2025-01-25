package com.taller.model.repository;

import com.taller.model.Client;
import com.taller.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {

    @Query("SELECT d FROM Device d WHERE d.clientId = ?1")
    List<Device> findDevicesByClientId(String id);
}
