package com.taller.model;

import com.taller.model.enums.CommitmentOutcomeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "follow_up_commitments")
@AttributeOverride(name = "id", column = @Column(name = "id_commitment"))
public class FollowUpCommitment extends BasicEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follow_up_id", nullable = false)
    private FollowUp followUp;

    @Column(name = "promised_date", nullable = false)
    private LocalDate promisedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false)
    private CommitmentOutcomeEnum outcome = CommitmentOutcomeEnum.PENDING;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
