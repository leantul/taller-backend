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
              CASE WHEN :sortBy <> 'status' AND f.status = com.taller.model.enums.FollowUpStatusEnum.CONFIRMED THEN 0
                   WHEN :sortBy <> 'status' AND f.status = com.taller.model.enums.FollowUpStatusEnum.PENDING THEN 1
                   WHEN :sortBy <> 'status' THEN 2 END ASC,
              CASE WHEN :sortBy = 'status' AND :sortDir = 'asc' AND f.status = com.taller.model.enums.FollowUpStatusEnum.CONFIRMED THEN 0
                   WHEN :sortBy = 'status' AND :sortDir = 'asc' AND f.status = com.taller.model.enums.FollowUpStatusEnum.PENDING THEN 1
                   WHEN :sortBy = 'status' AND :sortDir = 'asc' THEN 2 END ASC,
              CASE WHEN :sortBy = 'status' AND :sortDir = 'desc' AND f.status = com.taller.model.enums.FollowUpStatusEnum.CONFIRMED THEN 0
                   WHEN :sortBy = 'status' AND :sortDir = 'desc' AND f.status = com.taller.model.enums.FollowUpStatusEnum.PENDING THEN 1
                   WHEN :sortBy = 'status' AND :sortDir = 'desc' THEN 2 END DESC,
              CASE WHEN :sortBy = 'name' AND :sortDir = 'asc' THEN lower(concat(coalesce(c.name, f.contactName), ' ', coalesce(c.lastName, ''))) END ASC,
              CASE WHEN :sortBy = 'name' AND :sortDir = 'desc' THEN lower(concat(coalesce(c.name, f.contactName), ' ', coalesce(c.lastName, ''))) END DESC,
              CASE WHEN :sortBy = 'deviceDescription' AND :sortDir = 'asc' THEN lower(f.deviceDescription) END ASC,
              CASE WHEN :sortBy = 'deviceDescription' AND :sortDir = 'desc' THEN lower(f.deviceDescription) END DESC,
              CASE WHEN :sortBy = 'promisedDate' AND :sortDir = 'asc' THEN (SELECT MAX(fc.promisedDate) FROM FollowUpCommitment fc WHERE fc.followUp = f AND fc.outcome = com.taller.model.enums.CommitmentOutcomeEnum.PENDING) END ASC,
              CASE WHEN :sortBy = 'promisedDate' AND :sortDir = 'desc' THEN (SELECT MAX(fc.promisedDate) FROM FollowUpCommitment fc WHERE fc.followUp = f AND fc.outcome = com.taller.model.enums.CommitmentOutcomeEnum.PENDING) END DESC,
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
