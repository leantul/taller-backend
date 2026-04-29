package com.taller.model.repository;

import com.taller.model.Client;
import com.taller.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.domain.Pageable;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {

    @Query("SELECT d FROM Device d WHERE d.clientId = ?1")
    List<Device> findDevicesByClientId(String id);

    @Query("""
            SELECT c FROM Client c
            WHERE lower(c.name) LIKE lower(concat('%', ?1, '%'))
               OR lower(c.lastName) LIKE lower(concat('%', ?1, '%'))
               OR lower(c.dni) LIKE lower(concat('%', ?1, '%'))
               OR lower(c.phone) LIKE lower(concat('%', ?1, '%'))
               OR lower(c.email) LIKE lower(concat('%', ?1, '%'))
            """)
    List<Client> search(String term);

    @Query("SELECT c FROM Client c WHERE EXISTS (SELECT d FROM Device d WHERE d.clientId = c.id) ORDER BY c.creationDateTime DESC")
    List<Client> findTop5WithDevices(Pageable pageable);
}
