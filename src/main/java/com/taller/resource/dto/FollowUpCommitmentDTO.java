package com.taller.resource.dto;

import com.taller.model.enums.CommitmentOutcomeEnum;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FollowUpCommitmentDTO(
        String id,
        @NotNull(message = "La fecha prometida es obligatoria") LocalDate promisedDate,
        CommitmentOutcomeEnum outcome,
        String notes,
        LocalDateTime createdAt
) { }
