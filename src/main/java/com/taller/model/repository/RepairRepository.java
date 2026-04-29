package com.taller.model.repository;

import com.taller.model.Repair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface RepairRepository extends JpaRepository<Repair, String> {
    @Query("""
            SELECT r FROM Repair r
            WHERE lower(r.orderNumber) LIKE lower(concat('%', ?1, '%'))
               OR lower(r.description) LIKE lower(concat('%', ?1, '%'))
            """)
    List<Repair> search(String term);

    List<Repair> findByReturnDateTimeBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT r FROM Repair r ORDER BY r.creationDateTime DESC")
    List<Repair> findLatest(Pageable pageable);
}
