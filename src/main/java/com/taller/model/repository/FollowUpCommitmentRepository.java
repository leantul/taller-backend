package com.taller.model.repository;

import com.taller.model.FollowUpCommitment;
import com.taller.model.enums.CommitmentOutcomeEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowUpCommitmentRepository extends JpaRepository<FollowUpCommitment, String> {
    List<FollowUpCommitment> findByFollowUpIdOrderByCreationDateTimeDesc(String followUpId);
    List<FollowUpCommitment> findByFollowUpIdAndOutcome(String followUpId, CommitmentOutcomeEnum outcome);
}
