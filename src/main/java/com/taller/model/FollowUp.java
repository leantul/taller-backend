package com.taller.model;

import com.taller.model.enums.FollowUpStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "follow_ups")
@AttributeOverride(name = "id", column = @Column(name = "id_follow_up"))
public class FollowUp extends BasicEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_channel")
    private String contactChannel;

    @Column(name = "contact_value")
    private String contactValue;

    @Column(name = "device_description", nullable = false)
    private String deviceDescription;

    @Column(name = "reported_problem", columnDefinition = "TEXT")
    private String reportedProblem;

    @Column(name = "next_contact_date")
    private LocalDate nextContactDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FollowUpStatusEnum status = FollowUpStatusEnum.PENDING;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "followUp", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("creationDateTime DESC")
    private List<FollowUpCommitment> commitments = new ArrayList<>();
}
