package com.taller.model.repository;

import com.taller.model.FollowUp;
import com.taller.model.repository.projection.FollowUpListView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowUpRepository extends JpaRepository<FollowUp, String> {

    @Query(value = """
            SELECT f.id AS id,
                   c.id AS clientId,
                   c.name AS clientName,
                   c.lastName AS clientLastName,
                   f.contactName AS contactName,
                   f.contactChannel AS contactChannel,
                   f.contactValue AS contactValue,
                   f.deviceDescription AS deviceDescription,
                   f.nextContactDate AS nextContactDate,
                   (SELECT MAX(fc.promisedDate) FROM FollowUpCommitment fc WHERE fc.followUp = f AND fc.outcome = com.taller.model.enums.CommitmentOutcomeEnum.PENDING) AS currentPromisedDate,
                   f.status AS status,
                   (SELECT COUNT(fc.id) FROM FollowUpCommitment fc WHERE fc.followUp = f) AS commitmentCount,
                   (SELECT COUNT(fc.id) FROM FollowUpCommitment fc WHERE fc.followUp = f AND fc.outcome IN (com.taller.model.enums.CommitmentOutcomeEnum.RESCHEDULED, com.taller.model.enums.CommitmentOutcomeEnum.NOT_COMPLETED)) AS missedCommitmentCount
            FROM FollowUp f
            LEFT JOIN f.client c
            WHERE :term = ''
               OR lower(f.contactName) LIKE lower(concat('%', :term, '%'))
               OR lower(f.contactValue) LIKE lower(concat('%', :term, '%'))
               OR lower(f.deviceDescription) LIKE lower(concat('%', :term, '%'))
               OR lower(c.name) LIKE lower(concat('%', :term, '%'))
               OR lower(c.lastName) LIKE lower(concat('%', :term, '%'))
            ORDER BY
              CASE WHEN :sortBy = 'name' AND :sortDir = 'asc' THEN lower(coalesce(c.name, f.contactName)) END ASC,
              CASE WHEN :sortBy = 'name' AND :sortDir = 'desc' THEN lower(coalesce(c.name, f.contactName)) END DESC,
              CASE WHEN :sortBy = 'nextContactDate' AND :sortDir = 'asc' THEN f.nextContactDate END ASC,
              CASE WHEN :sortBy = 'nextContactDate' AND :sortDir = 'desc' THEN f.nextContactDate END DESC,
              CASE WHEN :sortBy = 'commitmentCount' AND :sortDir = 'asc' THEN (SELECT COUNT(fc.id) FROM FollowUpCommitment fc WHERE fc.followUp = f) END ASC,
              CASE WHEN :sortBy = 'commitmentCount' AND :sortDir = 'desc' THEN (SELECT COUNT(fc.id) FROM FollowUpCommitment fc WHERE fc.followUp = f) END DESC,
              f.creationDateTime DESC
            """,
            countQuery = """
            SELECT COUNT(f) FROM FollowUp f LEFT JOIN f.client c
            WHERE :term = ''
               OR lower(f.contactName) LIKE lower(concat('%', :term, '%'))
               OR lower(f.contactValue) LIKE lower(concat('%', :term, '%'))
               OR lower(f.deviceDescription) LIKE lower(concat('%', :term, '%'))
               OR lower(c.name) LIKE lower(concat('%', :term, '%'))
               OR lower(c.lastName) LIKE lower(concat('%', :term, '%'))
            """)
    Page<FollowUpListView> findPage(@Param("term") String term,
                                    @Param("sortBy") String sortBy,
                                    @Param("sortDir") String sortDir,
                                    Pageable pageable);
}
