package com.taller.model.repository;

import com.taller.model.Client;
import com.taller.model.repository.projection.ClientBasicView;
import com.taller.model.repository.projection.ClientDetailView;
import com.taller.model.repository.projection.ClientListView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {

    @Query(value = """
            SELECT c.id AS id,
                   c.name AS name,
                   c.lastName AS lastName,
                   c.phone AS phone,
                   (SELECT COUNT(d.id) FROM Device d WHERE d.clientId = c.id) AS deviceCount,
                   (SELECT COUNT(r.id) FROM Repair r WHERE r.idClient = c.id) AS repairCount
            FROM Client c
            WHERE :term = ''
               OR lower(c.name) LIKE lower(concat('%', :term, '%'))
               OR lower(c.lastName) LIKE lower(concat('%', :term, '%'))
               OR lower(c.dni) LIKE lower(concat('%', :term, '%'))
               OR lower(c.phone) LIKE lower(concat('%', :term, '%'))
               OR lower(c.email) LIKE lower(concat('%', :term, '%'))
            ORDER BY
              CASE WHEN :sortBy = 'name' AND :sortDir = 'asc' THEN lower(concat(c.name, ' ', c.lastName)) END ASC,
              CASE WHEN :sortBy = 'name' AND :sortDir = 'desc' THEN lower(concat(c.name, ' ', c.lastName)) END DESC,
              CASE WHEN :sortBy = 'deviceCount' AND :sortDir = 'asc' THEN (SELECT COUNT(d.id) FROM Device d WHERE d.clientId = c.id) END ASC,
              CASE WHEN :sortBy = 'deviceCount' AND :sortDir = 'desc' THEN (SELECT COUNT(d.id) FROM Device d WHERE d.clientId = c.id) END DESC,
              CASE WHEN :sortBy = 'repairCount' AND :sortDir = 'asc' THEN (SELECT COUNT(r.id) FROM Repair r WHERE r.idClient = c.id) END ASC,
              CASE WHEN :sortBy = 'repairCount' AND :sortDir = 'desc' THEN (SELECT COUNT(r.id) FROM Repair r WHERE r.idClient = c.id) END DESC,
              CASE WHEN :sortBy = 'phone' AND :sortDir = 'asc' THEN lower(c.phone) END ASC,
              CASE WHEN :sortBy = 'phone' AND :sortDir = 'desc' THEN lower(c.phone) END DESC,
              c.creationDateTime DESC
            """,
            countQuery = """
            SELECT COUNT(c)
            FROM Client c
            WHERE :term = ''
               OR lower(c.name) LIKE lower(concat('%', :term, '%'))
               OR lower(c.lastName) LIKE lower(concat('%', :term, '%'))
               OR lower(c.dni) LIKE lower(concat('%', :term, '%'))
               OR lower(c.phone) LIKE lower(concat('%', :term, '%'))
               OR lower(c.email) LIKE lower(concat('%', :term, '%'))
            """)
    Page<ClientListView> findPage(
            @Param("term") String term,
            @Param("sortBy") String sortBy,
            @Param("sortDir") String sortDir,
            Pageable pageable);

    @Query("""
            SELECT c.id AS id,
                   c.name AS name,
                   c.lastName AS lastName,
                   c.dni AS dni,
                   c.email AS email,
                   c.address AS address,
                   c.phone AS phone,
                   c.notes AS notes,
                   c.birthDate AS birthDate
            FROM Client c
            WHERE c.id = ?1
            """)
    Optional<ClientDetailView> findDetailById(String id);

    @Query("SELECT phone FROM Client c JOIN c.phones phone WHERE c.id = ?1")
    List<String> findAdditionalPhonesById(String id);

    @Query("SELECT email FROM Client c JOIN c.emails email WHERE c.id = ?1")
    List<String> findAdditionalEmailsById(String id);

    @Query("""
            SELECT c FROM Client c
            WHERE lower(c.name) LIKE lower(concat('%', ?1, '%'))
               OR lower(c.lastName) LIKE lower(concat('%', ?1, '%'))
               OR lower(c.dni) LIKE lower(concat('%', ?1, '%'))
               OR lower(c.phone) LIKE lower(concat('%', ?1, '%'))
               OR lower(c.email) LIKE lower(concat('%', ?1, '%'))
            """)
    List<Client> search(String term);

    @Query("""
            SELECT c.id AS id,
                   c.name AS name,
                   c.lastName AS lastName,
                   c.dni AS dni,
                   c.email AS email,
                   c.phone AS phone
            FROM Client c
            WHERE EXISTS (SELECT d FROM Device d WHERE d.clientId = c.id)
            ORDER BY c.creationDateTime DESC
            """)
    List<ClientBasicView> findTop5WithDevicesBasic(Pageable pageable);

    @Query("""
            SELECT c.id AS id,
                   c.name AS name,
                   c.lastName AS lastName,
                   c.dni AS dni,
                   c.email AS email,
                   c.phone AS phone
            FROM Client c
            WHERE c.id IN ?1
            """)
    List<ClientBasicView> findBasicByIdIn(Collection<String> ids);
}
